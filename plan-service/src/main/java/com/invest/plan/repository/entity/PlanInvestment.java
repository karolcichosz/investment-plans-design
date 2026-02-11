package com.invest.plan.repository.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plan_investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // ✅ Add LAZY to avoid circular loads
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}