package io.hhplus.concert.application.waitingqueue.dto;

import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WaitingQueueDto {

    @Getter
    @AllArgsConstructor
    public static class WaitingQueueInfo {
        private final Long id;
        private final String token;
        private final WaitingQueueStatus status;
        private final LocalDateTime expireAt;
        private final LocalDateTime createdAt;

        public WaitingQueueInfo(WaitingQueue waitingQueue) {
            this.id = waitingQueue.getId();
            this.token = waitingQueue.getToken();
            this.status = waitingQueue.getStatus();
            this.expireAt = waitingQueue.getExpireAt();
            this.createdAt = waitingQueue.getCreatedAt();
        }
    }
}
