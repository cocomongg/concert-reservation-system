package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.WaitingQueueRepository;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class WaitingQueueRepositoryImpl implements WaitingQueueRepository {

    private final WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Override
    public WaitingQueue createWaitingQueue(CreateWaitingQueueCommand command) {
        WaitingQueue waitingQueue = new WaitingQueue(command);
        return waitingQueueJpaRepository.save(waitingQueue);
    }

    @Override
    public WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query) {
        return waitingQueueJpaRepository.findByToken(query.getToken())
            .orElseThrow(() -> WaitingQueueException.WAITING_QUEUE_NOT_FOUND);
    }

    @Override
    public Long countWaitingOrder(Long queueId) {
        return waitingQueueJpaRepository.countByIdLessThanEqualAndStatus(queueId, WaitingQueueStatus.WAITING);
    }
}
