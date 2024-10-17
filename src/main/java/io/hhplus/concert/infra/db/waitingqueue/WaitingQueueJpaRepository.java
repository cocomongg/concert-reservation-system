package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingQueueJpaRepository extends JpaRepository<WaitingQueue, Long> {

    Optional<WaitingQueue> findByToken(String token);

    Long countByIdLessThanEqualAndStatus(Long id, WaitingQueueStatus status);

    Long countByStatus(WaitingQueueStatus status);

    @Query("SELECT wq.id FROM WaitingQueue wq WHERE wq.status = :status ORDER BY wq.id ASC")
    List<Long> findOldestWaitedIds(WaitingQueueStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE WaitingQueue wq SET wq.status = :status, wq.updatedAt = :now WHERE wq.id IN :ids")
    int updateStatusByIds(List<Long> ids, WaitingQueueStatus status, LocalDateTime now);
}
