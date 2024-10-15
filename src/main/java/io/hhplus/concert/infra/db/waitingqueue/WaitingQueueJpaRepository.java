package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingQueueJpaRepository extends JpaRepository<WaitingQueue, Long> {

}
