package io.hhplus.concert.interfaces.api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final int status;
    private final T data;

    public static ApiResponse<?> OK() {
        return new ApiResponse<>(HttpStatus.OK.value(), null);
    }

    public static <T> ApiResponse<T> OK(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data);
    }
}
