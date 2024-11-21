package io.hhplus.concert.app.notification.domain;

import io.hhplus.concert.app.notification.domain.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    public void sendNotification(NotificationMessage message) {
        notificationClient.sendNotification(message);
    }
}
