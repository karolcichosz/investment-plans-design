package com.invest.plan.api.controller;

import com.invest.plan.api.dto.CreatePlanRequest;
import com.invest.plan.api.dto.PlanResponse;
import com.invest.plan.domain.service.PlanService;
import com.invest.plan.repository.entity.Plan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@Slf4j
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreatePlanRequest request) {

        log.info("POST /api/v1/plans - User: {}", userId);

        Plan plan = planService.createPlan(userId, request);
        PlanResponse response = planService.getPlan(userId, plan.getPlanId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> getPlan(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String planId) {

        log.info("GET /api/v1/plans/{} - User: {}", planId, userId);

        PlanResponse response = planService.getPlan(userId, UUID.fromString(planId));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> listPlans(
            @RequestHeader("X-User-Id") String userId) {

        log.info("GET /api/v1/plans - User: {}", userId);

        List<PlanResponse> responses = planService.getUserPlans(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Plan Service is running");
    }
}
