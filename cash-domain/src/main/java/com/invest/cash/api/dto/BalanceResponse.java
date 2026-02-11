package com.invest.cash.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private String userId;
    private BigDecimal balance;
    private LocalDateTime timestamp;
}