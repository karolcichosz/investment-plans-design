package com.invest.execution.repository.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashBalance {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
