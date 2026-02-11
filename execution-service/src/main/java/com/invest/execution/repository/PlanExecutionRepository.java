package com.invest.execution.repository;

import com.invest.execution.repository.entity.PlanExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanExecutionRepository extends JpaRepository<PlanExecution, UUID> {
    List<PlanExecution> findByPlanId(UUID planId);
}