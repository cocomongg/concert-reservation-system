package io.hhplus.concert.app.common.error;

import org.springframework.boot.logging.LogLevel;

public interface ErrorType {
    ErrorCode getErrorCode();
    String getMessage();
    LogLevel getLogLevel();
}
