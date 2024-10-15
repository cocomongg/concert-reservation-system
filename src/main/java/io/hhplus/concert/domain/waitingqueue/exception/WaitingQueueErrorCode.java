package io.hhplus.concert.domain.waitingqueue.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.ErrorInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WaitingQueueErrorCode implements DomainErrorCode {
    INVALID_CREATION_INPUT(400, "WQ_400_1", "Invalid input data for WaitingQueue creation.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorInfo getErrorInfo() {
        return new ErrorInfo(this.status, this.code, this.message);
    }
}
