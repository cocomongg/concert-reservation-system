package io.hhplus.concert.domain.waitingqueue.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.DomainException;

public class WaitingQueueException extends DomainException {

    public static final WaitingQueueException INVALID_CREATION_INPUT =
        new WaitingQueueException(WaitingQueueErrorCode.INVALID_CREATION_INPUT);

    public WaitingQueueException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
