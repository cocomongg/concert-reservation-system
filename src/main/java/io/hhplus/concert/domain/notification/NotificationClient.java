package io.hhplus.concert.domain.notification;

import io.hhplus.concert.domain.notification.model.NotificationMessage;

public interface NotificationClient {
    void sendNotification(NotificationMessage message);
}
