package io.hhplus.concert.domain.common.exception;

public class DomainException extends RuntimeException{
    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode) {
        super(errorCode.getErrorInfo().getMessage());
        this.errorCode = errorCode;
    }
}
