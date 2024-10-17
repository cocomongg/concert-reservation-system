package io.hhplus.concert.domain.waitingqueue.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.ErrorInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WaitingQueueErrorCode implements DomainErrorCode {
    INVALID_CREATION_INPUT(400, "WQ_400_1", "Invalid input data for WaitingQueue creation."),
    INVALID_RETRIEVAL_INPUT(400, "WQ_400_2", "Invalid input data for WaitingQueue retrieval."),
    INVALID_STATE_NOT_WAITING(400, "WQ_400_3", "Invalid state, should be in waiting status."),
    INVALID_WAITING_QUEUE(400, "WQ_400_4", "Invalid WaitingQueue"),
    WAITING_QUEUE_NOT_FOUND(404, "WQ_404_1", "WaitingQueue not found");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorInfo getErrorInfo() {
        return new ErrorInfo(this.status, this.code, this.message);
    }
}
