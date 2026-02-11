package com.invest.plan.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private LocalDateTime executionDay;
    private List<PlanCreatedEvent.InvestmentDto> investments;
    private LocalDateTime timestamp;
}
