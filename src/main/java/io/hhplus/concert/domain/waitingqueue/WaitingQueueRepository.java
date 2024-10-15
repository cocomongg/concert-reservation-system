package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;

public interface WaitingQueueRepository {

    WaitingQueue createWaitingQueue(CreateWaitingQueueCommand command);
}
