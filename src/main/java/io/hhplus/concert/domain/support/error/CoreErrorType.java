package io.hhplus.concert.domain.support.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;

@AllArgsConstructor
@Getter
public enum CoreErrorType implements ErrorType {
    INTERNAL_ERROR(ErrorCode.INTERNAL_ERROR, "Internal Server Error", LogLevel.ERROR);

    @Getter
    @AllArgsConstructor
    public enum Concert implements ErrorType{
        NOT_RESERVABLE_SEAT(ErrorCode.BUSINESS_ERROR, "ConcertSeat is not reservable", LogLevel.WARN),
        TEMPORARY_RESERVATION_EXPIRED(ErrorCode.BUSINESS_ERROR, "Temporary reservation expired", LogLevel.WARN),
        CONCERT_NOT_FOUND(ErrorCode.NOT_FOUND, "Concert not found", LogLevel.WARN),
        CONCERT_SCHEDULE_NOT_FOUND(ErrorCode.NOT_FOUND, "ConcertSchedule not found", LogLevel.WARN),
        CONCERT_SEAT_NOT_FOUND(ErrorCode.NOT_FOUND, "ConcertSeat not found", LogLevel.WARN),
        CONCERT_RESERVATION_NOT_FOUND(ErrorCode.NOT_FOUND, "ConcertReservation not found", LogLevel.WARN);

        private final ErrorCode errorCode;
        private final String message;
        private final LogLevel logLevel;
    }

    @Getter
    @AllArgsConstructor
    public enum Member implements ErrorType {
        MEMBER_NOT_FOUND(ErrorCode.NOT_FOUND, "Member not found", LogLevel.WARN),
        INVALID_POINT_AMOUNT(ErrorCode.BUSINESS_ERROR, "Invalid point amount", LogLevel.WARN),
        INSUFFICIENT_POINT_AMOUNT(ErrorCode.BUSINESS_ERROR, "Insufficient point amount", LogLevel.WARN);

        private final ErrorCode errorCode;
        private final String message;
        private final LogLevel logLevel;
    }

    @Getter
    @AllArgsConstructor
    public enum WaitingQueue implements ErrorType {
        INVALID_STATE_NOT_WAITING(ErrorCode.BUSINESS_ERROR, "Invalid state, should be in waiting status", LogLevel.WARN),
        INVALID_WAITING_QUEUE(ErrorCode.BUSINESS_ERROR, "Invalid WaitingQueue", LogLevel.WARN),
        WAITING_QUEUE_NOT_FOUND(ErrorCode.NOT_FOUND, "WaitingQueue not found", LogLevel.WARN);

        private final ErrorCode errorCode;
        private final String message;
        private final LogLevel logLevel;
    }

//    @Getter
//    @AllArgsConstructor
//    public enum Payment implements ErrorType {
//
//        private final ErrorCode errorCode;
//        private final String message;
//        private final LogLevel logLevel;
//    }

    private final ErrorCode errorCode;
    private final String message;
    private final LogLevel logLevel;
}
