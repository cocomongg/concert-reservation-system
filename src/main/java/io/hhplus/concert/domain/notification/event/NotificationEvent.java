package io.hhplus.concert.domain.notification.event;

import io.hhplus.concert.domain.common.event.DomainEvent;
import java.time.LocalDateTime;
import lombok.Getter;

public class NotificationEvent {

    @Getter
    public static class SendNotificationEvent extends DomainEvent {
        private final String title;
        private final String content;
        private final Long memberId;

        public SendNotificationEvent(String title, String content, Long memberId) {
            super(LocalDateTime.now());
            this.title = title;
            this.content = content;
            this.memberId = memberId;
        }
    }
}
