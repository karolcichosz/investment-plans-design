package com.invest.cash.domain;

import com.invest.cash.kafka.producer.CashEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Slf4j
public class CashService {

    private final CashEventProducer cashEventProducer;

    // In-memory store for cash balances
    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

    public CashService(CashEventProducer cashEventProducer) {
        this.cashEventProducer = cashEventProducer;
        // Initialize with default balances
        balances.put("user_123", BigDecimal.valueOf(10000.00));
        balances.put("user_456", BigDecimal.valueOf(15000.00));
        balances.put("user_789", BigDecimal.valueOf(20000.00));
        balances.put("demo_user", BigDecimal.valueOf(50000.00));
    }

    public BigDecimal getBalance(String userId) {
        log.info("Cash Domain: Getting balance for user: {}", userId);
        return balances.getOrDefault(userId, BigDecimal.ZERO);
    }

    public void setBalance(String userId, BigDecimal balance) {
        log.info("Cash Domain: Setting balance for user: {} = {}", userId, balance);
        balances.put(userId, balance);

        // Publish event to Kafka
        cashEventProducer.publishCashBalance(userId, balance);
    }

    public void reserve(String userId, BigDecimal amount) {
        BigDecimal current = balances.getOrDefault(userId, BigDecimal.ZERO);
        if (current.compareTo(amount) >= 0) {
            BigDecimal newBalance = current.subtract(amount);
            balances.put(userId, newBalance);
            log.info("Reserved {} for user: {}, new balance: {}", amount, userId, newBalance);
        } else {
            log.warn("Insufficient balance for user: {}", userId);
        }
    }

    public Map<String, BigDecimal> getAllBalances() {
        return new ConcurrentHashMap<>(balances);
    }
}