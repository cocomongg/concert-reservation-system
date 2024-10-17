package io.hhplus.concert.domain.waitingqueue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingQueueWithOrder {
    private final WaitingQueue waitingQueue;
    private final Long waitingOrder;
}
