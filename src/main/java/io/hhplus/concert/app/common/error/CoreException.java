package io.hhplus.concert.app.common.error;

import lombok.Getter;


@Getter
public class CoreException extends RuntimeException {
    private final ErrorType errorType;
    private final Object payload;

    public CoreException(ErrorType errorType, Object payload) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.payload = payload;
    }

    public CoreException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.payload = null;
    }
}
