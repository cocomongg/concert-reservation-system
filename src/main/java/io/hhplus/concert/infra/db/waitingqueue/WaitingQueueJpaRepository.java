package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
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

    Long countByIdLessThanEqualAndStatus(Long id, WaitingQueueTokenStatus status);

    @Query("SELECT wq FROM WaitingQueue wq WHERE wq.status = :status ORDER BY wq.id ASC")
    List<WaitingQueue> findOldestWaitedIds(WaitingQueueTokenStatus status, Pageable pageable);

    @Query("SELECT wq FROM WaitingQueue wq WHERE wq.status = 'ACTIVE' and wq.expireAt < :now")
    List<WaitingQueue> findExpireTargetIds(LocalDateTime now);

    @Modifying
    @Query("UPDATE WaitingQueue wq SET wq.status = :status, wq.updatedAt = :now WHERE wq.token IN :tokens")
    int updateStatusByTokens(List<String> tokens, WaitingQueueTokenStatus status, LocalDateTime now);
}
