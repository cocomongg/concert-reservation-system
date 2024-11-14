package io.hhplus.concert.infra.client;

import io.hhplus.concert.domain.notification.NotificationClient;
import io.hhplus.concert.domain.notification.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FakeNotificationClient implements NotificationClient {
    @Override
    public void sendNotification(NotificationMessage message) {
        log.info("send notify: {}", message.toString());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("failed send notify: {}", e.getMessage());
        }
    }
}
