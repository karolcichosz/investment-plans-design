package com.invest.relay.domain;

import com.invest.relay.repository.OutboxRepository;
import com.invest.relay.repository.entity.OutboxRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxPollingRelay {

    private final OutboxRepository outboxRepository;
    private final OutboxPublisher outboxPublisher;

    public static final String EXECUTE_PLAN_EVENT_TYPE = "ExecutePlan";

    /**
     * Polls the outbox table for ready, unpublished events and delegates publishing
     * to the transactional OutboxPublisher.
     */
    @Scheduled(fixedDelay = 3000, initialDelay = 5000)
    public void publishPendingEvents() {
        log.debug("Relay: Polling outbox for unpublished events...");

        LocalDateTime now = LocalDateTime.now();

        List<OutboxRecord> pendingEvents = outboxRepository
                .findByPublishedFalseAndEventTypeAndScheduledForLessThanEqualOrderByCreatedAtAsc(
                        EXECUTE_PLAN_EVENT_TYPE,
                        now
                );

        if (pendingEvents.isEmpty()) {
            log.debug("No pending events in outbox");
            return;
        }

        log.info("Relay: Found {} pending events, attempting to publish...", pendingEvents.size());

        for (OutboxRecord outboxRecord : pendingEvents) {
            try {
                outboxPublisher.publish(outboxRecord);
            } catch (Exception e) {
                log.error("Failed to publish outbox event #{} - will retry next cycle",
                        outboxRecord.getOutboxId(), e);
                // Note: Because publish() is transactional, partial failures are rolled back.
                // The event stays unpublished and will be picked up again next poll.
            }
        }
    }

    /**
     * Periodic health check / monitoring of the relay.
     */
    @Scheduled(fixedDelay = 60000)
    public void checkRelayHealth() {
        long unpublishedCount = outboxRepository
                .countByPublishedFalseAndEventType(EXECUTE_PLAN_EVENT_TYPE);
        log.info("Relay Health: {} unpublished '{}' events in outbox",
                unpublishedCount, EXECUTE_PLAN_EVENT_TYPE);
    }

    // Inner class moved here so it can be used by both OutboxPublisher and this class
    // if you prefer to keep it in one place.
    // You can also move it to its own file if it grows.
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class KafkaEvent {
        private String eventId;
        private String eventType;
        private String aggregateId;
        private String payload;
        private LocalDateTime timestamp;
    }
}