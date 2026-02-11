package com.invest.transactions.api.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Asset ID is required")
    private String assetId;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String planId;
    private String executionId;
}