package io.hhplus.concert.domain.waitingqueue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingTokenWithOrderInfo {
    private final WaitingQueueTokenInfo tokenInfo;
    private final Long order;
    private final Long remainingWaitTimeSeconds;
}
