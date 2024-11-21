package io.hhplus.concert.app.waitingqueue.domain.dto;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WaitingQueueCommand {

    @Getter
    @AllArgsConstructor
    public static class InsertWaitingQueue {
        private final String token;
        private final LocalDateTime now;
    }

    @Getter
    @AllArgsConstructor
    public static class ActivateWaitingTokens {
        private final int limit;
        private final long expireTime;
        private final TimeUnit timeUnit;
    }
}
