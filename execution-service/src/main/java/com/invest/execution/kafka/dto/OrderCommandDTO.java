package com.invest.execution.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for publishing OrderCommand events to Kafka
 * Only includes the fields needed by downstream services
 * Excludes internal fields like planExecution, status, timestamps
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommandDTO {
    private String orderId;
    private String planId;
    private String userId;
    private String assetId;
    private BigDecimal amount;
}
