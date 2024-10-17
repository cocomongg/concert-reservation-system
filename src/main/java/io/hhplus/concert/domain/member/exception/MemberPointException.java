package io.hhplus.concert.domain.member.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.DomainException;

public class MemberPointException extends DomainException {

    public static MemberPointException INVALID_POINT_AMOUNT =
        new MemberPointException(MemberPointErrorCode.INVALID_POINT_AMOUNT);

    public static MemberPointException INSUFFICIENT_POINT_AMOUNT =
        new MemberPointException(MemberPointErrorCode.INSUFFICIENT_POINT_AMOUNT);

    public MemberPointException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
