package com.invest.relay.api.controller;

import com.invest.relay.domain.OutboxPollingRelay;
import com.invest.relay.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

import static com.invest.relay.domain.OutboxPollingRelay.EXECUTE_PLAN_EVENT_TYPE;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    private final OutboxRepository outboxRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "relay-service");
        health.put("unpublishedExecutePlanEvents", outboxRepository.countByPublishedFalseAndEventType(EXECUTE_PLAN_EVENT_TYPE));
        return ResponseEntity.ok(health);
    }
}
