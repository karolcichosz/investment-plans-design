package com.invest.plan.repository;
import com.invest.plan.repository.entity.OutboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {
    @Query("SELECT o FROM OutboxRecord o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<OutboxRecord> findByPublishedFalseOrderByCreatedAtAsc();
}
