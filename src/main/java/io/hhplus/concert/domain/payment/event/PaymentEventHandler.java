package io.hhplus.concert.domain.payment.event;

import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.domain.payment.event.PaymentEvent.CreatePaymentHistoryEvent;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventHandler {

    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreatePaymentHistoryEvent(CreatePaymentHistoryEvent event) {
        try {
            CreatePaymentHistory command = new CreatePaymentHistory(
                event.getPaymentId(), PaymentStatus.PAID, event.getAmount());
            paymentService.createPaymentHistory(command);
        } catch (Exception e) {
            log.error("CreatePaymentHistoryEvent 처리 중 오류 발생", e);
        }
    }
}
