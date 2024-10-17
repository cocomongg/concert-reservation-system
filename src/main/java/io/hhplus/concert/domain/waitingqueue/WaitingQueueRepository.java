package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import java.util.List;

public interface WaitingQueueRepository {

    WaitingQueue saveWaitingQueue(WaitingQueue waitingQueue);

    WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query);

    Long countWaitingOrder(Long queueId);

    Long getActiveCount();

    List<Long> getOldestWaitedQueueIds(int limit);

    int activateWaitingQueues(List<Long> ids);
}
