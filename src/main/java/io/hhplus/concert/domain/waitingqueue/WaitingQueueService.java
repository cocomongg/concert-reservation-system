package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public WaitingQueue insertWaitingQueue(CreateWaitingQueue command) {
        WaitingQueue waitingQueue = WaitingQueue.createWaitingQueue(command.getToken());
        return waitingQueueRepository.insertWaitingQueue(waitingQueue);
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

    @Transactional
    public int activateToken(int limit) {
        List<Long> waitingQueueIds =
            waitingQueueRepository.getOldestWaitedQueueIds(limit);

        if(waitingQueueIds.isEmpty()) {
            return 0;
        }

        return waitingQueueRepository.activateWaitingQueues(waitingQueueIds);
    }

    @Transactional
    public int expireTokens(LocalDateTime currentTime) {
        List<Long> expireTargetIds = waitingQueueRepository.getExpireTargetIds(currentTime);

        if(expireTargetIds.isEmpty()) {
            return 0;
        }

        return waitingQueueRepository.expireWaitingQueues(expireTargetIds);
    }
}
