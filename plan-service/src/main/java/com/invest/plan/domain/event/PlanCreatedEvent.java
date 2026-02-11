package com.invest.plan.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanCreatedEvent {
    
    private String planId;
    private String userId;
    private String name;
    private Integer executionDay;
    private List<InvestmentDto> investments;
    private LocalDateTime timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentDto {
        private String assetId;
        private java.math.BigDecimal amount;
    }
}
