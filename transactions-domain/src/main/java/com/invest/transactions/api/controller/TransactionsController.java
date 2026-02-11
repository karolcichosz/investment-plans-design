package com.invest.transactions.api.controller;

import com.invest.transactions.api.dto.CreateOrderRequest;
import com.invest.transactions.api.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionsController {

    /**
     * Direct order creation endpoint (for testing).
     * In production, orders come primarily via Kafka from Execution Service.
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("POST /api/transactions/orders - User: {}, Asset: {}, Amount: {}",
                request.getUserId(), request.getAssetId(), request.getAmount());

        String orderId = UUID.randomUUID().toString();

        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .message("Order created successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "transactions-domain");
        return ResponseEntity.ok(health);
    }
}