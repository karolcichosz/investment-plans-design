package com.invest.execution.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashBalanceEvent {

    private String userId;
    private BigDecimal balance;
    private LocalDateTime timestamp;
}