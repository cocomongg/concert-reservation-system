package io.hhplus.concert.app.common.api.support;

import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.common.error.ErrorCode;
import io.hhplus.concert.app.common.error.ErrorType;
import io.hhplus.concert.app.common.api.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CoreException.class)
    public ResponseEntity<ApiErrorResponse> handleCoreException(CoreException e) {
        ErrorType errorType = e.getErrorType();
        ErrorCode errorCode = errorType.getErrorCode();

        String logMessage = String.format("Error Type: %s, Message: %s, Payload: %s",
            errorType, e.getMessage(), e.getPayload());

        switch(errorType.getLogLevel()) {
            case ERROR -> log.error(logMessage, e);
            case WARN -> log.warn(logMessage);
            case INFO -> log.info(logMessage);
        }

        HttpStatus httpStatus;
        switch(errorCode) {
            case INTERNAL_ERROR -> httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            case NOT_FOUND -> httpStatus = HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> httpStatus = HttpStatus.UNAUTHORIZED;
            default -> httpStatus = HttpStatus.OK;
        }

        ApiErrorResponse errorResponse =
            new ApiErrorResponse(errorType.toString(), errorType.getMessage());

        return ResponseEntity.status(httpStatus)
            .body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);

        ApiErrorResponse errorResponse =
            new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getLocalizedMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}
