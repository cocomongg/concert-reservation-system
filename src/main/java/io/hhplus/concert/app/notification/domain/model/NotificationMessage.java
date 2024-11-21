package io.hhplus.concert.app.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class NotificationMessage {
    private final String title;
    private final String content;
    private final Long targetMemberId;
}
