package com.invest.cash.config;

import com.invest.cash.domain.CashService;
import com.invest.cash.kafka.producer.CashEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CashDomainStartup {

    private final CashService cashService;

    private final CashEventProducer cashEventProducer;

    /**
     * When Cash Domain starts, publish initial cash balances to Kafka.
     * This shows that external services can proactively publish events.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void publishInitialBalances() {
        log.info("Cash Domain: Publishing initial cash balances to Kafka...");

        // Publish all user balances
        for (var entry : cashService.getAllBalances().entrySet()) {
            cashEventProducer.publishCashBalance(entry.getKey(), entry.getValue());
            try {
                Thread.sleep(100); // Small delay between messages
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("Cash Domain: Initial balances published");
    }
}