package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import java.time.LocalDateTime;
import java.util.List;

public interface WaitingQueueRepository {

    WaitingQueue insertWaitingQueue(WaitingQueue waitingQueue);

    WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query);

    Long countWaitingOrder(Long queueId);

    Long getActiveCount();

    List<Long> getOldestWaitedQueueIds(int limit);

    List<Long> getExpireTargetIds(LocalDateTime now);

    int activateWaitingQueues(List<Long> ids);

    int expireWaitingQueues(List<Long> ids);
}
