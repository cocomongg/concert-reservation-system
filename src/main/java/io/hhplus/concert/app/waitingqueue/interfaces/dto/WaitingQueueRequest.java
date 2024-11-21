package io.hhplus.concert.app.waitingqueue.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class WaitingQueueRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateQueue {
        @Schema(description = "member id")
        private Long memberId;
    }
}
