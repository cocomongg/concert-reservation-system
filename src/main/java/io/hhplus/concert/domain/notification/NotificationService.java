package io.hhplus.concert.domain.notification;

import io.hhplus.concert.domain.notification.model.NotificationMessage;
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
