package com.invest.cash.api.controller;

import com.invest.cash.api.dto.BalanceResponse;
import com.invest.cash.api.dto.ReserveRequest;
import com.invest.cash.domain.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cash")
@Slf4j
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String userId) {
        log.info("GET /api/cash/balance/{}", userId);

        BigDecimal balance = cashService.getBalance(userId);

        BalanceResponse response = BalanceResponse.builder()
                .userId(userId)
                .balance(balance)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Map<String, String>> reserve(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReserveRequest request) {

        log.info("POST /api/cash/reserve - User: {}, Amount: {}", userId, request.getAmount());

        cashService.reserve(userId, request.getAmount());

        Map<String, String> response = new HashMap<>();
        response.put("status", "RESERVED");
        response.put("userId", userId);
        response.put("amount", request.getAmount().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balances")
    public ResponseEntity<Map<String, BigDecimal>> getAllBalances() {
        log.info("GET /api/cash/balances");
        return ResponseEntity.ok(cashService.getAllBalances());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "cash-domain");
        return ResponseEntity.ok(health);
    }
}