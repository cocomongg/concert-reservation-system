package io.hhplus.concert.app.payment.domain.event.listener;

import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.payment.domain.event.producer.PaymentEventProducer;
import io.hhplus.concert.app.payment.domain.service.PaymentService;
import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class DonePaymentEventListener {
    private final PaymentEventProducer paymentEventProducer;

    @Value("${kafka.topics.payment}")
    private String paymentTopic;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void produceMessage(DonePaymentEvent event) {
        paymentEventProducer.produce(paymentTopic, event);
    }
}
