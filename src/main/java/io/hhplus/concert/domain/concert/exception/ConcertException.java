package io.hhplus.concert.domain.concert.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.DomainException;

public class ConcertException extends DomainException {

    public static ConcertException NOT_RESERVABLE_SEAT =
        new ConcertException(ConcertErrorCode.NOT_RESERVABLE_SEAT);

    public static ConcertException CONCERT_SEAT_NOT_FOUND =
        new ConcertException(ConcertErrorCode.CONCERT_SEAT_NOT_FOUND);

    public static ConcertException CONCERT_NOT_FOUND =
        new ConcertException(ConcertErrorCode.CONCERT_NOT_FOUND);

    public static ConcertException CONCERT_SCHEDULE_NOT_FOUND =
        new ConcertException(ConcertErrorCode.CONCERT_SCHEDULE_NOT_FOUND);

    public static ConcertException CONCERT_RESERVATION_NOT_FOUND =
        new ConcertException(ConcertErrorCode.CONCERT_RESERVATION_NOT_FOUND);

    public ConcertException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
