package io.hhplus.concert.domain.waitingqueue.event;

import io.hhplus.concert.domain.common.event.DomainEvent;
import java.time.LocalDateTime;
import lombok.Getter;

public class WaitingQueueEvent {
    @Getter
    public static class ExpireTokenEvent extends DomainEvent {
        private final String token;

        public ExpireTokenEvent(String token) {
            super(LocalDateTime.now());
            this.token = token;
        }
    }
}
