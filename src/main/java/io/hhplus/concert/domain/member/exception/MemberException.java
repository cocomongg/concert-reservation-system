package io.hhplus.concert.domain.member.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.DomainException;

public class MemberException extends DomainException {

    public static MemberException MEMBER_NOT_FOUND =
        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);

    public MemberException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
