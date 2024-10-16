package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.WaitingQueueRepository;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public Long getActiveCount() {
        return waitingQueueJpaRepository.countByStatus(WaitingQueueStatus.ACTIVE);
    }

    @Override
    public List<Long> getOldestWaitedQueueIds(int limit) {
        return waitingQueueJpaRepository.findOldestWaitedIds(WaitingQueueStatus.WAITING,
            PageRequest.of(0, limit));
    }

    @Override
    public int activateWaitingQueues(List<Long> ids) {
        return waitingQueueJpaRepository.updateStatusByIds(ids, WaitingQueueStatus.ACTIVE,
            LocalDateTime.now());
    }
}
