package io.hhplus.concert.app.notification.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.app.notification.domain.NotificationService;
import io.hhplus.concert.app.notification.domain.model.NotificationMessage;
import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationDonePaymentConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment}", groupId = "${kafka.groups.notification}")
    public void consumeDonePaymentEvent(ConsumerRecord<Object, Object> message, Acknowledgment ack) {
        try {
            DonePaymentEvent donePaymentEvent = objectMapper.convertValue(message.value(),
                DonePaymentEvent.class);

            NotificationMessage notificationMessage = new NotificationMessage("결제 완료",
                "결제가 완료되었습니다.", donePaymentEvent.getPayment().getMemberId());
            notificationService.sendNotification(notificationMessage);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("NotificationConsumer consume error", e);
        }
    }
}
