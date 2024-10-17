package io.hhplus.concert.domain.concert.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.ErrorInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConcertErrorCode implements DomainErrorCode {
    CONCERT_SEAT_NOT_FOUND(404, "C_404_1", "ConcertSeat not found"),
    CONCERT_NOT_FOUND(404, "C_404_2", "Concert not found"),
    CONCERT_SCHEDULE_NOT_FOUND(404, "C_404_3", "ConcertSchedule not found");


    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorInfo getErrorInfo() {
        return new ErrorInfo(this.status, this.code, this.message);
    }
}
