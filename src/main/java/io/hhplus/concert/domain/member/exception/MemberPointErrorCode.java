package io.hhplus.concert.domain.member.exception;

import io.hhplus.concert.domain.common.exception.DomainErrorCode;
import io.hhplus.concert.domain.common.exception.ErrorInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberPointErrorCode implements DomainErrorCode {
    INVALID_POINT_AMOUNT(400, "mp_400_1", "Invalid point amount"),
    INSUFFICIENT_POINT_AMOUNT(400, "mp_400_2", "Insufficient point amount");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorInfo getErrorInfo() {
        return new ErrorInfo(status, code, message);
    }
}
