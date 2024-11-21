package io.hhplus.concert.app.notification.domain;

import io.hhplus.concert.app.notification.domain.model.NotificationMessage;

public interface NotificationClient {
    void sendNotification(NotificationMessage message);
}
