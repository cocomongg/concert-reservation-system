package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public WaitingQueue createWaitingQueue(CreateWaitingQueueCommand command) {
        return waitingQueueRepository.createWaitingQueue(command);
    }

    @Transactional(readOnly = true)
    public WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query) {
        return waitingQueueRepository.getWaitingQueue(query);
    }

    @Transactional(readOnly = true)
    public WaitingQueueWithOrder getWaitingQueueWithOrder(GetWaitingQueueCommonQuery query) {
        WaitingQueue waitingQueue = this.getWaitingQueue(query);

        if(!waitingQueue.isWaiting()) {
            throw WaitingQueueException.INVALID_STATE_NOT_WAITING;
        }
        Long order = waitingQueueRepository.countWaitingOrder(waitingQueue.getId());

        return new WaitingQueueWithOrder(waitingQueue, order);
    }

    @Transactional(readOnly = true)
    public Long getActiveCount() {
        return waitingQueueRepository.getActiveCount();
    }

    @Transactional
    public void activateOldestWaitedQueues(int countToActivate) {
        if(countToActivate <= 0) {
            return;
        }

        List<Long> waitingQueueIds = waitingQueueRepository.getOldestWaitedQueueIds(
            countToActivate);

        if(waitingQueueIds.isEmpty()) {
            return;
        }

        waitingQueueRepository.activateWaitingQueues(waitingQueueIds);
    }
}
