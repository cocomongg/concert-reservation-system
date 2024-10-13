package io.hhplus.concert.domain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorInfo {
    private final Integer status;
    private final String code;
    private final String message;
}
