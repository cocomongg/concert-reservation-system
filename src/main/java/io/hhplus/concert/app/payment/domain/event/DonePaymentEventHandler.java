package io.hhplus.concert.app.payment.domain.event;

import io.hhplus.concert.app.payment.domain.service.PaymentService;
import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class DonePaymentEventHandler {

    private final PaymentService paymentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDonePaymentEvent(DonePaymentEvent event) {
        try {
            Payment payment = event.getPayment();
            CreatePaymentHistory command = new CreatePaymentHistory(
                payment.getId(), PaymentStatus.PAID, payment.getPaidAmount());
            paymentService.createPaymentHistory(command);
        } catch (Exception e) {
            log.error("CreatePaymentHistoryEvent 처리 중 오류 발생", e);
        }
    }
}
