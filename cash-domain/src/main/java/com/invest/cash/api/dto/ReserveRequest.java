package com.invest.cash.api.dto;

import lombok.*;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveRequest {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}