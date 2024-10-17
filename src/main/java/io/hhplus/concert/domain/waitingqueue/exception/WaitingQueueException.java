package io.hhplus.concert.domain.waitingqueue.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.DomainException;

public class WaitingQueueException extends DomainException {

    public static final WaitingQueueException INVALID_CREATION_INPUT =
        new WaitingQueueException(WaitingQueueErrorCode.INVALID_CREATION_INPUT);

    public static final WaitingQueueException INVALID_RETRIEVAL_INPUT =
        new WaitingQueueException(WaitingQueueErrorCode.INVALID_RETRIEVAL_INPUT);

    public static final WaitingQueueException INVALID_STATE_NOT_WAITING =
        new WaitingQueueException(WaitingQueueErrorCode.INVALID_STATE_NOT_WAITING);

    public static final WaitingQueueException INVALID_WAITING_QUEUE =
        new WaitingQueueException(WaitingQueueErrorCode.INVALID_WAITING_QUEUE);

    public static final WaitingQueueException WAITING_QUEUE_NOT_FOUND =
        new WaitingQueueException(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND);

    public WaitingQueueException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
