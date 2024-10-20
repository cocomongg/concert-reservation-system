package io.hhplus.concert.interfaces.api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResult<T> {
    private final int status;
    private final T data;

    public static ApiResult<?> OK() {
        return new ApiResult<>(HttpStatus.OK.value(), null);
    }

    public static <T> ApiResult<T> OK(T data) {
        return new ApiResult<>(HttpStatus.OK.value(), data);
    }
}
