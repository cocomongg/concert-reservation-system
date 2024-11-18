package io.hhplus.concert.domain.payment.event;

import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
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
