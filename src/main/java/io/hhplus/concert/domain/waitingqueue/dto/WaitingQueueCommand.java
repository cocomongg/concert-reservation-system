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
        private final WaitingQueueStatus status;
        private final LocalDateTime expireAt;

        public static CreateWaitingQueue createActiveQueue(String token, LocalDateTime expireAt) {
            return new CreateWaitingQueue(token, WaitingQueueStatus.ACTIVE, expireAt);
        }

        public static CreateWaitingQueue createWaitingQueue(String token) {
            return new CreateWaitingQueue(token, WaitingQueueStatus.WAITING, null);
        }
    }
}
