package com.invest.plan.repository.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "plan_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanExecution {

    @Id
    @UuidGenerator
    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "total_executed")
    private BigDecimal totalExecuted;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "execution_id")
    private Set<OrderCommand> orders = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
