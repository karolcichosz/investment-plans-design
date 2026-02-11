package com.invest.execution.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.execution.domain.ExecutionService;
import com.invest.execution.domain.event.ExecutePlanEvent;
import com.invest.execution.repository.PlanExecutionRepository;
import com.invest.execution.repository.entity.PlanExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExecutePlanListener {

    private final ExecutionService executionService;
    private final ObjectMapper objectMapper;
    private final PlanExecutionRepository executionRepository;

    @KafkaListener(topics = "plan-commands", groupId = "execution-service-group")
    public void onPlanCommandEvent(String message) {
        try {
            log.info("Execution Service: Received plan-commands message");

            // Step 1: Deserialize the outer KafkaEvent wrapper
            KafkaEvent kafkaEvent = objectMapper.readValue(message, KafkaEvent.class);
            log.debug("Received Kafka event: {}", kafkaEvent.getEventType());

            // Step 2: Extract the nested payload (which is a JSON string)
            String payloadJson = kafkaEvent.getPayload();
            log.debug("Payload: {}", payloadJson);

            // Step 3: Deserialize the payload string to ExecutePlanEvent
            ExecutePlanEvent event = objectMapper.readValue(payloadJson, ExecutePlanEvent.class);
            log.info("Deserialized ExecutePlanEvent: planId={}, userId={}",
                    event.getPlanId(), event.getUserId());

            // Idempotency check
            if (executionRepository.findByPlanId(UUID.fromString(event.getPlanId())).stream().map(PlanExecution::getStatus).anyMatch(status -> status.equals("COMPLETED")) ) {
                log.info("Skipping duplicate completed ExecutePlanEvent for plan {}", event.getPlanId());
                return;
            }

            // Step 4: Handle the event
            executionService.handleExecutePlanEvent(event);

        } catch (Exception e) {
            log.error("Failed to process plan event", e);
        }
    }

    /**
     * DTO for the outer Kafka event wrapper
     * Matches the structure from relay-service OutboxPollingRelay.KafkaEvent
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class KafkaEvent {
        private String eventId;
        private String eventType;
        private String aggregateId;
        private String payload;  // ← This is the nested JSON string!
        private java.time.LocalDateTime timestamp;
    }
}
