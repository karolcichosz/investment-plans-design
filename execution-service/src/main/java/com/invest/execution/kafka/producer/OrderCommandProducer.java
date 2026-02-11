package com.invest.execution.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.execution.kafka.dto.OrderCommandDTO;
import com.invest.execution.repository.entity.OrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCommandProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void publishOrderCommand(OrderCommand order) {
        try {
            String json = objectMapper.writeValueAsString(OrderCommandDTO.builder()
                                                                            .orderId(String.valueOf(order.getOrderId()))
                                                                            .planId(String.valueOf(order.getPlanId()))
                                                                            .userId(order.getUserId())
                                                                            .assetId(order.getAssetId())
                                                                            .amount(order.getAmount())
                                                                            .build());
            kafkaTemplate.send("order-commands", String.valueOf(order.getOrderId()), json);
            log.info("Published order command: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order command", e);
        }
    }
}