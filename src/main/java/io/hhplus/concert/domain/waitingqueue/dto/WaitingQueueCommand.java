package io.hhplus.concert.domain.waitingqueue.dto;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WaitingQueueCommand {

    @Getter
    @AllArgsConstructor
    public static class CreateWaitingQueue {
        private final String token;
        private final int maxActiveCount;
        private final LocalDateTime expireAt;
    }
}
