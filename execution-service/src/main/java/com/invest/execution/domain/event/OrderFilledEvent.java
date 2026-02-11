package com.invest.execution.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFilledEvent {

    private String orderId;
    private String assetId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime filledAt;
}