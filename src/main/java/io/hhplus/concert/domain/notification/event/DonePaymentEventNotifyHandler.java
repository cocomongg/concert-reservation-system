package io.hhplus.concert.domain.notification.event;

import io.hhplus.concert.domain.notification.NotificationService;
import io.hhplus.concert.domain.notification.model.NotificationMessage;
import io.hhplus.concert.domain.payment.event.DonePaymentEvent;
import io.hhplus.concert.domain.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class DonePaymentEventNotifyHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDonePaymentEvent(DonePaymentEvent event) {
        Payment payment = event.getPayment();
        NotificationMessage message = new NotificationMessage("결제 완료", "결제가 완료되었습니다.",
            payment.getMemberId());

        try {
            notificationService.sendNotification(message);
        } catch (Exception e) {
            log.error("SendNotificationEvent 처리 중 오류 발생", e);
        }
    }
}
