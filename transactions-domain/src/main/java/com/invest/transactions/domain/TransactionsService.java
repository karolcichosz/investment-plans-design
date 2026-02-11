package com.invest.transactions.domain;

import com.invest.transactions.domain.entity.OrderCommand;
import com.invest.transactions.kafka.producer.OrderFilledProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionsService {

    private final OrderFilledProducer orderFilledProducer;

    /**
     * Process order command received from Kafka.
     * Simulate trading engine processing.
     */
    public void processOrder(OrderCommand command) {
        try {
            log.info("Transactions Domain: Processing order {} for {} {} @ {}",
                    command.getOrderId(), command.getAssetId(),
                    command.getAmount(), command.getUserId());

            // Simulate trading engine processing (latency)
            Thread.sleep(100);

            // Publish OrderFilledEvent to Kafka
            orderFilledProducer.publishOrderFilled(
                    command.getOrderId(),
                    command.getAssetId(),
                    command.getAmount()
            );

            log.info("Order {} filled successfully", command.getOrderId());

        } catch (InterruptedException e) {
            log.error("Order processing interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to process order", e);
        }
    }
}