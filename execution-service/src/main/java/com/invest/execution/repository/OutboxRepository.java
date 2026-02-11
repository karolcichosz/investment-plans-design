package com.invest.execution.repository;

import com.invest.execution.repository.entity.OutboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {
    List<OutboxRecord> findByAggregateIdOrderByCreatedAtDesc(UUID aggregateId);
}