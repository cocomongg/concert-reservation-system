package io.hhplus.concert.domain.support.error;

import org.springframework.boot.logging.LogLevel;

public interface ErrorType {
    ErrorCode getErrorCode();
    String getMessage();
    LogLevel getLogLevel();
}
