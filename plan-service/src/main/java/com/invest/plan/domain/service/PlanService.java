package com.invest.plan.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.plan.api.dto.CreatePlanRequest;
import com.invest.plan.api.dto.PlanResponse;
import com.invest.plan.domain.event.ExecutePlanEvent;
import com.invest.plan.domain.event.PlanCreatedEvent;
import com.invest.plan.repository.OrderCommandRepository;
import com.invest.plan.repository.OutboxRepository;
import com.invest.plan.repository.PlanExecutionRepository;
import com.invest.plan.repository.PlanRepository;
import com.invest.plan.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    private final PlanExecutionRepository executionRepository;

    private final OrderCommandRepository orderCommandRepository;

    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;

    /**
     * Create a plan with transactional outbox pattern.
     * CRITICAL: Plan + Event written in same database transaction.
     */
    @Transactional
    public Plan createPlan(String userId, CreatePlanRequest request) {
        log.info("Creating plan for user: {}", userId);

        // 1. Calculate total amount
        BigDecimal totalAmount = request.getInvestments().stream()
                .map(CreatePlanRequest.InvestmentDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Create Plan FIRST (without investments)
        var planId = UUID.randomUUID();
        Plan plan = Plan.builder()
                .planId(planId)
                .userId(userId)
                .name(request.getName())
                .executionDay(request.getExecutionDay())
                .status("ACTIVE")
                .totalAmount(totalAmount)
                .version(1L)
                .investments(new HashSet<>())  // Empty set initially
                .build();

        // 3. Create investments and SET THE PLAN REFERENCE
        Set<PlanInvestment> investments = request.getInvestments().stream()
                .map(inv -> PlanInvestment.builder()
                        .plan(plan)  // ✅ SET THE PLAN OBJECT REFERENCE!
                        .assetId(inv.getAssetId())
                        .amount(inv.getAmount())
                        .build())
                .collect(Collectors.toSet());

        // 4. Set investments on plan
        plan.setInvestments(investments);

        // 5. Save Plan (now with proper relationships)
        log.info("Plan before save: {}", plan);
        planRepository.save(plan);
        log.info("Plan saved to database: {}", plan.getPlanId());

        // 6. Create PlanExecution with status=PENDING
        PlanExecution execution = PlanExecution.builder()
                .executionId(UUID.randomUUID())
                .planId(plan.getPlanId())
                .userId(userId)
                .triggeredAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        executionRepository.save(execution);
        log.info("Execution created: {}", execution.getExecutionId());

        var investmentsDTO = request.getInvestments().stream()
                .map(inv -> PlanCreatedEvent.InvestmentDto.builder()
                        .assetId(inv.getAssetId())
                        .amount(inv.getAmount())
                        .build())
                .collect(Collectors.toList());

        var timestamp = LocalDateTime.now();
        // 4. Create events
        PlanCreatedEvent planCreatedEvent = PlanCreatedEvent.builder()
                .planId(plan.getPlanId().toString())
                .userId(userId)
                .name(request.getName())
                .executionDay(request.getExecutionDay())
                .investments(investmentsDTO)
                .timestamp(timestamp)
                .build();


        ExecutePlanEvent executePlanEvent = ExecutePlanEvent.builder()
                .planId(plan.getPlanId().toString())
                .userId(userId)
                .name(request.getName())
                .executionDay(timestamp)        // should be related to executionDay
                .investments(investmentsDTO)
                .timestamp(timestamp)
                .build();

        // 5. Write events to outbox (SAME TRANSACTION)
        try {
            var outboxPlanCreated = OutboxRecord.builder()
                    .aggregateId(plan.getPlanId())
                    .aggregateType("Plan")
                    .eventType("PlanCreated")
                    .payload(objectMapper.writeValueAsString(planCreatedEvent))
                    .published(false)
                    .build();

            outboxRepository.save(outboxPlanCreated);
            log.info("{} Event written to outbox: {}", outboxPlanCreated.getEventType(), outboxPlanCreated.getOutboxId());

            var outboxExecutePlan = OutboxRecord.builder()
                    .aggregateId(plan.getPlanId())
                    .aggregateType("Plan")
                    .eventType("ExecutePlan")
                    .payload(objectMapper.writeValueAsString(executePlanEvent))
                    .published(false)
                    .scheduledFor(timestamp)
                    .build();

            outboxRepository.save(outboxExecutePlan);
            log.info("{} Event written to outbox: {}", outboxPlanCreated.getEventType(), outboxPlanCreated.getOutboxId());

        } catch (Exception e) {
            log.error("Failed to serialize event", e);
            throw new RuntimeException("Failed to create plan event", e);
        }

        // 6. Commit happens automatically (Spring handles it)
        return plan;
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(String userId, UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to plan");
        }

        return toPlanResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getUserPlans(String userId) {
        return planRepository.findByUserId(userId).stream()
                .map(this::toPlanResponse)
                .collect(Collectors.toList());
    }

    private PlanResponse toPlanResponse(Plan plan) {
        List<PlanExecution> executions = executionRepository.findByPlanId(plan.getPlanId());

        List<PlanResponse.ExecutionDto> executionDtos = executions.stream()
                .map(exec -> {
                    List<OrderCommand> orders = orderCommandRepository.findByExecutionId(exec.getExecutionId());

                    List<PlanResponse.OrderDto> orderDtos = orders.stream()
                            .map(order -> PlanResponse.OrderDto.builder()
                                    .orderId(String.valueOf(order.getOrderId()))
                                    .assetId(order.getAssetId())
                                    .amount(order.getAmount())
                                    .status(order.getStatus())
                                    .build())
                            .collect(Collectors.toList());

                    return PlanResponse.ExecutionDto.builder()
                            .executionId(String.valueOf(exec.getExecutionId()))
                            .status(exec.getStatus())
                            .triggeredAt(exec.getTriggeredAt())
                            .completedAt(exec.getCompletedAt())
                            .totalExecuted(exec.getTotalExecuted())
                            .orders(orderDtos)
                            .build();
                })
                .collect(Collectors.toList());

        Set<PlanResponse.InvestmentDto> investmentDtos = plan.getInvestments().stream()
                .map(inv -> PlanResponse.InvestmentDto.builder()
                        .assetId(inv.getAssetId())
                        .amount(inv.getAmount())
                        .build())
                .collect(Collectors.toSet());

        return PlanResponse.builder()
                .planId(String.valueOf(plan.getPlanId()))
                .userId(plan.getUserId())
                .name(plan.getName())
                .executionDay(plan.getExecutionDay())
                .status(plan.getStatus())
                .totalAmount(plan.getTotalAmount())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .investments(investmentDtos)
                .executions(executionDtos)
                .build();
    }
}
