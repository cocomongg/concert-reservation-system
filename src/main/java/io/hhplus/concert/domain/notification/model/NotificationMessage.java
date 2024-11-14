package io.hhplus.concert.domain.notification.model;

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
