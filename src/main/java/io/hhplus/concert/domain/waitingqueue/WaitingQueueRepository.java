package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import java.util.List;

public interface WaitingQueueRepository {

    WaitingQueue createWaitingQueue(CreateWaitingQueueCommand command);

    WaitingQueue getWaitingQueue(GetWaitingQueueCommonQuery query);

    Long countWaitingOrder(Long queueId);

    Long getActiveCount();

    List<Long> getOldestWaitedQueueIds(int limit);

    int activateWaitingQueues(List<Long> ids);
}
