package com.invest.relay.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.relay.repository.OutboxRepository;
import com.invest.relay.repository.entity.OutboxRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publish(OutboxRecord outboxRecord) throws Exception {
        try {
            String topic = getTopic(outboxRecord.getEventType());

            OutboxPollingRelay.KafkaEvent event = OutboxPollingRelay.KafkaEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(outboxRecord.getEventType())
                    .aggregateId(String.valueOf(outboxRecord.getAggregateId()))
                    .payload(outboxRecord.getPayload())
                    .timestamp(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, String.valueOf(outboxRecord.getAggregateId()), json)
                    .get(5, TimeUnit.SECONDS);

            outboxRepository.markAsPublished(outboxRecord.getOutboxId(), LocalDateTime.now());

            log.info("Published outbox event {} → {}", outboxRecord.getOutboxId(), topic);

        } catch (Exception e) {
            log.warn("Publish failed for outbox #{}", outboxRecord.getOutboxId(), e);
            throw e;  // ← important: rollback on error
        }
    }

    private String getTopic(String eventType) {
        return EventType.fromValue(eventType).getTopic();
    }
}