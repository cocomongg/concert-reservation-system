package io.hhplus.concert.domain.waitingqueue.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WaitingQueueCommand {

    @Getter
    @AllArgsConstructor
    public static class InsertWaitingQueue {
        private final String token;
        private final LocalDateTime now;
    }
}
