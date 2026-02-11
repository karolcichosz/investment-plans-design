package com.invest.execution.repository;

import com.invest.execution.repository.entity.OrderCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderCommandRepository extends JpaRepository<OrderCommand, UUID> {

    // Recommended: clear and follows the actual field name
    List<OrderCommand> findByPlanExecutionExecutionId(UUID executionId);
}