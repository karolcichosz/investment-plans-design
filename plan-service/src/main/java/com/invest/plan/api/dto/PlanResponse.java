package com.invest.plan.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    private String planId;
    private String userId;
    private String name;
    private Integer executionDay;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<InvestmentDto> investments;
    private List<ExecutionDto> executions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentDto {
        private String assetId;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExecutionDto {
        private String executionId;
        private String status;
        private LocalDateTime triggeredAt;
        private LocalDateTime completedAt;
        private BigDecimal totalExecuted;
        private List<OrderDto> orders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDto {
        private String orderId;
        private String assetId;
        private BigDecimal amount;
        private String status;
    }
}
