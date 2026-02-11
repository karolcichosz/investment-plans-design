package com.invest.execution.repository.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plan_executions")
@Getter
@Setter
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

    @JsonManagedReference  // ← Add this to allow serialization of the collection
    @OneToMany(mappedBy = "planExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderCommand> orderCommands = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to maintain both sides of the relationship
    public void addOrderCommand(OrderCommand orderCommand) {
        orderCommands.add(orderCommand);
        orderCommand.setPlanExecution(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}