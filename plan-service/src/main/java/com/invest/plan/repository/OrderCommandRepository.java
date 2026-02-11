package com.invest.plan.repository;

import com.invest.plan.repository.entity.OrderCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderCommandRepository extends JpaRepository<OrderCommand, UUID> {
    List<OrderCommand> findByExecutionId(UUID executionId);
}
