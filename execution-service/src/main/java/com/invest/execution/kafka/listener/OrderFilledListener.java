package com.invest.execution.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.execution.domain.event.OrderFilledEvent;
import com.invest.execution.repository.OrderCommandRepository;
import com.invest.execution.repository.entity.OrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderFilledListener {

    private final OrderCommandRepository orderCommandRepository;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-filled", groupId = "execution-service-group")
    @Transactional
    public void onOrderFilledEvent(String message) {
        try {
            log.info("Execution Service: Received order-filled message");

            OrderFilledEvent event = objectMapper.readValue(message, OrderFilledEvent.class);

            // Update order status
            OrderCommand order = orderCommandRepository.findById(UUID.fromString(event.getOrderId()))
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            order.setStatus("FILLED");
            orderCommandRepository.save(order);

            log.info("Order {} marked as FILLED", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process order filled event", e);
        }
    }
}