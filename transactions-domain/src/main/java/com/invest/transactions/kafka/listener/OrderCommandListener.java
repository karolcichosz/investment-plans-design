package com.invest.transactions.kafka.listener;

import com.invest.transactions.domain.TransactionsService;
import com.invest.transactions.domain.entity.OrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCommandListener {

    private final TransactionsService transactionsService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-commands", groupId = "transactions-domain-group")
    public void onOrderCommand(String message) {
        try {
            log.info("Transactions Domain: Received order-command message");

            OrderCommand command = objectMapper.readValue(message, OrderCommand.class);
            transactionsService.processOrder(command);

        } catch (Exception e) {
            log.error("Failed to process order command", e);
        }
    }
}