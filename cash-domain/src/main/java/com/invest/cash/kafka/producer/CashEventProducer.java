package com.invest.cash.kafka.producer;

import com.invest.cash.domain.event.CashBalanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CashEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void publishCashBalance(String userId, java.math.BigDecimal balance) {
        try {
            CashBalanceEvent event = CashBalanceEvent.builder()
                    .userId(userId)
                    .balance(balance)
                    .timestamp(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("cash-events", userId, json);

            log.info("Published cash balance for user: {} = {}", userId, balance);
        } catch (Exception e) {
            log.error("Failed to publish cash balance event", e);
        }
    }
}