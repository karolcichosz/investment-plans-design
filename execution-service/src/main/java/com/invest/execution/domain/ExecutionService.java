package com.invest.execution.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.execution.domain.event.ExecutePlanEvent;
import com.invest.execution.kafka.producer.OrderCommandProducer;
import com.invest.execution.repository.CashBalanceRepository;
import com.invest.execution.repository.OrderCommandRepository;
import com.invest.execution.repository.OutboxRepository;
import com.invest.execution.repository.PlanExecutionRepository;
import com.invest.execution.repository.entity.CashBalance;
import com.invest.execution.repository.entity.OrderCommand;
import com.invest.execution.repository.entity.OutboxRecord;
import com.invest.execution.repository.entity.PlanExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

    private final PlanExecutionRepository executionRepository;
    private final OrderCommandRepository orderCommandRepository;
    private final CashBalanceRepository cashBalanceRepository;
    private final OrderCommandProducer orderCommandProducer;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Main handler for ExecutePlan events from Kafka
     *
     * Flow:
     * 1. Validate cash balance
     * 2. Create/update PlanExecution
     * 3. Create OrderCommands (save before publishing!)
     * 4. Publish to Kafka
     * 5. Mark execution as COMPLETED
     * 6. Schedule next execution in outbox
     */
    @Transactional
    public void handleExecutePlanEvent(ExecutePlanEvent event) {
        try {
            log.info("=== EXECUTION SERVICE ===");
            log.info("Processing ExecutePlanEvent | plan: {} | user: {}",
                    event.getPlanId(), event.getUserId());

            // ============= STEP 1: VALIDATION =============

            // Calculate total amount needed
            BigDecimal totalRequired = event.getInvestments().stream()
                    .map(ExecutePlanEvent.InvestmentDto::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.debug("Total investment required: {}", totalRequired);

            // Check user's cash balance
            Optional<CashBalance> cashOpt = cashBalanceRepository.findById(event.getUserId());
            BigDecimal availableBalance = cashOpt.map(CashBalance::getBalance).orElse(BigDecimal.ZERO);

            log.info("User {} balance check | available: {}, required: {}",
                    event.getUserId(), availableBalance, totalRequired);

            // Insufficient funds - exit early
            if (availableBalance.compareTo(totalRequired) < 0) {
                log.warn("❌ Insufficient funds | plan: {} | user: {} | balance: {}, needed: {}",
                        event.getPlanId(), event.getUserId(), availableBalance, totalRequired);
                return;
            }

            // ============= STEP 2: GET OR CREATE EXECUTION =============

            UUID planId = UUID.fromString(event.getPlanId());

            // Find existing execution for this plan (created by plan-service)
            List<PlanExecution> executions = executionRepository.findByPlanId(planId);
            if (executions.isEmpty()) {
                log.error("❌ No PlanExecution found for plan: {}", planId);
                throw new RuntimeException("PlanExecution not found for plan: " + planId);
            }

            PlanExecution execution = executions.getFirst();
            log.debug("Found execution: {} | current status: {}", execution.getExecutionId(), execution.getStatus());

            // Update status to IN_PROGRESS
            execution.setStatus("IN_PROGRESS");
            executionRepository.save(execution);
            executionRepository.flush();  // ✅ Ensure persisted
            log.info("✓ Updated execution status to IN_PROGRESS: {}", execution.getExecutionId());

            // ============= STEP 3: CREATE AND SAVE ORDER COMMANDS =============

            log.info("Creating {} order commands...", event.getInvestments().size());

            for (ExecutePlanEvent.InvestmentDto investment : event.getInvestments()) {
                // Create order with generated UUID
                OrderCommand order = OrderCommand.builder()
                        .orderId(UUID.randomUUID())
                        .planExecution(execution)
                        .planId(planId)
                        .userId(event.getUserId())
                        .assetId(investment.getAssetId())
                        .amount(investment.getAmount())
                        .status("PENDING")
                        .build();

                // ✅ CRITICAL: Save to database FIRST with flush
                orderCommandRepository.save(order);
                orderCommandRepository.flush();  // Ensures UUID is persisted
                log.debug("✓ Saved order {} to database", order.getOrderId());

                // ✅ THEN publish to Kafka with same UUID
                orderCommandProducer.publishOrderCommand(order);
                log.info("✓ Published order command {} | asset: {} | amount: {}",
                        order.getOrderId(), investment.getAssetId(), investment.getAmount());
            }

            // ============= STEP 4: MARK EXECUTION AS COMPLETED =============

            execution.setStatus("COMPLETED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setTotalExecuted(totalRequired);

            executionRepository.save(execution);
            executionRepository.flush();  // ✅ Ensure persisted
            log.info("✓ Execution {} completed | total executed: {}",
                    execution.getExecutionId(), totalRequired);

            // ============= STEP 5: DEDUCT CASH BALANCE =============

            if (cashOpt.isPresent()) {
                CashBalance balance = cashOpt.get();
                BigDecimal newBalance = balance.getBalance().subtract(totalRequired);
                balance.setBalance(newBalance);
                balance.setUpdatedAt(LocalDateTime.now());

                cashBalanceRepository.save(balance);
                cashBalanceRepository.flush();
                log.info("✓ Updated cash balance for user {} | new balance: {}",
                        event.getUserId(), newBalance);
            }

            // ============= STEP 6: SCHEDULE NEXT EXECUTION =============

            // Create next execution for next month
            PlanExecution nextExecution = PlanExecution.builder()
                    .executionId(UUID.randomUUID())
                    .planId(planId)
                    .userId(event.getUserId())
                    .triggeredAt(LocalDateTime.now().plusMonths(1))
                    .status("PENDING")
                    .orderCommands(new ArrayList<>())
                    .build();

            executionRepository.save(nextExecution);
            executionRepository.flush();
            log.info("✓ Created next execution: {} | scheduled for: {}",
                    nextExecution.getExecutionId(), nextExecution.getTriggeredAt());

            // ============= STEP 7: PUBLISH NEXT EXECUTION EVENT TO OUTBOX =============

            // Get the original plan event from outbox to reuse payload
            List<OutboxRecord> originalEvents = outboxRepository.findByAggregateIdOrderByCreatedAtDesc(planId);

            if (!originalEvents.isEmpty()) {
                OutboxRecord original = originalEvents.getFirst();

                OutboxRecord nextExecutePlanEvent = OutboxRecord.builder()
                        .aggregateId(planId)
                        .aggregateType("Plan")
                        .eventType("ExecutePlan")
                        .payload(original.getPayload())  // Reuse plan details
                        .published(false)
                        .scheduledFor(LocalDateTime.now().plusMonths(1))  // Schedule for next month
                        .build();

                outboxRepository.save(nextExecutePlanEvent);
                outboxRepository.flush();
                log.info("✓ Outbox event created | type: ExecutePlan | scheduled for: {}",
                        nextExecutePlanEvent.getScheduledFor());
            } else {
                log.warn("⚠️  No original event found in outbox for plan: {}", planId);
            }

            log.info("=== EXECUTION COMPLETE ===\n");

        } catch (Exception e) {
            log.error("❌ Failed to process ExecutePlanEvent | plan: {} | user: {}",
                    event.getPlanId(), event.getUserId(), e);
            throw e;  // Ensures transaction rollback
        }
    }

    /**
     * Health check - verify service is working
     */
    public void healthCheck() {
        long totalExecutions = executionRepository.count();
        long totalOrders = orderCommandRepository.count();
        log.info("Execution Service Health | Executions: {} | Orders: {}",
                totalExecutions, totalOrders);
    }
}
