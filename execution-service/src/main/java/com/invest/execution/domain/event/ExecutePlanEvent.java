package com.invest.execution.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutePlanEvent {
    private String planId;
    private String userId;
    private String name;
    private LocalDateTime executionDay;  // ✅ Accept as LocalDateTime (what plan-service sends)
    private List<InvestmentDto> investments;
    private LocalDateTime timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentDto {
        private String assetId;
        private BigDecimal amount;
    }
}