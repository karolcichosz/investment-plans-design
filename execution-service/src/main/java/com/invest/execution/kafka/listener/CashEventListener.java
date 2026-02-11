package com.invest.execution.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invest.execution.domain.event.CashBalanceEvent;
import com.invest.execution.repository.CashBalanceRepository;
import com.invest.execution.repository.entity.CashBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CashEventListener {

    private final CashBalanceRepository cashBalanceRepository;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "cash-events", groupId = "execution-service-group")
    @Transactional
    public void onCashBalanceEvent(String message) {
        try {
            log.info("Execution Service: Received cash-event message");

            CashBalanceEvent event = objectMapper.readValue(message, CashBalanceEvent.class);

            // Cache/store the balance
            CashBalance cashBalance = CashBalance.builder()
                    .userId(event.getUserId())
                    .balance(event.getBalance())
                    .build();

            cashBalanceRepository.save(cashBalance);

            log.info("Updated cash balance for user: {} = {}",
                    event.getUserId(), event.getBalance());

        } catch (Exception e) {
            log.error("Failed to process cash event", e);
        }
    }
}