package io.hhplus.concert.interfaces.api.queue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QueueRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateQueue {
        @Schema(description = "user id")
        private Long userId;
    }
}
