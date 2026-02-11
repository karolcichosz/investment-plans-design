package com.invest.transactions.kafka.producer;

import com.invest.transactions.domain.event.OrderFilledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderFilledProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void publishOrderFilled(String orderId, String assetId,
                                   java.math.BigDecimal amount) {
        try {
            OrderFilledEvent event = OrderFilledEvent.builder()
                    .orderId(orderId)
                    .assetId(assetId)
                    .amount(amount)
                    .status("FILLED")
                    .filledAt(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-filled", orderId, json);

            log.info("Published order filled: {} for asset: {}", orderId, assetId);
        } catch (Exception e) {
            log.error("Failed to publish order filled event", e);
        }
    }
}