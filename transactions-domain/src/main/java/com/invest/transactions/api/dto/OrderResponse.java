package com.invest.transactions.api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String orderId;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}