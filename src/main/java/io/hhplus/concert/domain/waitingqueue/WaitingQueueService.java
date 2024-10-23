package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public WaitingQueue createWaitingQueue(CreateWaitingQueue command) {
        Long activeCount = this.getActiveCount();
        if(activeCount < command.getMaxActiveCount()) {
            return WaitingQueue.createActiveWaitingQueue(command.getToken(), command.getExpireAt());
        }

        return WaitingQueue.createWaitingQueue(command.getToken());
    }

    @Transactional(readOnly = true)
    public WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query) {
        return waitingQueueRepository.getWaitingQueue(query);
    }

    @Transactional(readOnly = true)
    public WaitingQueueWithOrder getWaitingQueueWithOrder(GetWaitingQueueCommonQuery query) {
        WaitingQueue waitingQueue = this.getWaitingQueue(query);
        waitingQueue.checkNotWaiting();

        Long order = waitingQueueRepository.countWaitingOrder(waitingQueue.getId());

        return new WaitingQueueWithOrder(waitingQueue, order);
    }

    @Transactional(readOnly = true)
    public void checkTokenActivate(CheckTokenActivate query) {
        WaitingQueue waitingQueue =
            this.getWaitingQueue(new GetWaitingQueueCommonQuery(query.getToken()));

        waitingQueue.checkActivated(query.getCurrentTime());
    }

    @Transactional(readOnly = true)
    public Long getActiveCount() {
        return waitingQueueRepository.getActiveCount();
    }

    @Transactional
    public void activateToken(int maxActiveCount) {
        Long activeCount = this.getActiveCount();
        int countToActivate = maxActiveCount - activeCount.intValue();

        if(countToActivate <= 0) {
            return;
        }

        List<Long> waitingQueueIds =
            waitingQueueRepository.getOldestWaitedQueueIds(countToActivate);

        if(waitingQueueIds.isEmpty()) {
            return;
        }

        waitingQueueRepository.activateWaitingQueues(waitingQueueIds);
    }
}
