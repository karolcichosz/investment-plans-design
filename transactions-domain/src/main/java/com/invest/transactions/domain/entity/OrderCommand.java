package com.invest.transactions.domain.entity;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommand {

    private String orderId;
    private String planId;
    private String userId;
    private String assetId;
    private BigDecimal amount;
    private String status;
}

