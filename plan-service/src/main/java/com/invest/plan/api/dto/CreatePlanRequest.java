package com.invest.plan.api.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @Positive(message = "Execution day must be between 1 and 31")
    private Integer executionDay;

    @NotEmpty(message = "At least one investment is required")
    private List<InvestmentDto> investments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentDto {
        @NotBlank(message = "Asset ID is required")
        private String assetId;

        @Positive(message = "Amount must be positive")
        private BigDecimal amount;
    }
}
