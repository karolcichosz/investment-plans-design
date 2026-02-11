package com.invest.relay.repository;

import com.invest.relay.repository.entity.OutboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {

    /**
     * Finds all unpublished "ExecutePlan" events that are ready to be processed
     * (scheduledFor ≤ current time), ordered by creation time (oldest first).
     */
    List<OutboxRecord> findByPublishedFalseAndEventTypeAndScheduledForLessThanEqualOrderByCreatedAtAsc(
            String eventType,
            LocalDateTime scheduledFor
    );

    /**
     * Counts unpublished "ExecutePlan" events (no scheduledFor filter here -
     * usually you want total pending count regardless of schedule time).
     */
    long countByPublishedFalseAndEventType(String eventType);

    /**
     * Marks a single outbox record as published.
     * Uses @Query because we need to set two fields atomically.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE OutboxRecord o
           SET o.published    = true,
               o.publishedAt  = :publishedAt
         WHERE o.outboxId     = :outboxId
        """)
    void markAsPublished(
            @Param("outboxId") Long outboxId,
            @Param("publishedAt") LocalDateTime publishedAt
    );

    // Optional: batch version (useful when you want to mark many at once)
    @Modifying
    @Transactional
    @Query("""
        UPDATE OutboxRecord o
           SET o.published    = true,
               o.publishedAt  = :publishedAt
         WHERE o.outboxId IN :outboxIds
        """)
    int markAsPublishedInBatch(
            @Param("outboxIds") List<Long> outboxIds,
            @Param("publishedAt") LocalDateTime publishedAt
    );
}