package com.invest.plan.repository.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_commands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommand {
    
    @Id
    @UuidGenerator
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "plan_id", nullable = false, columnDefinition = "UUID")
    private UUID planId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "asset_id", nullable = false)
    private String assetId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name= "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name= "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
