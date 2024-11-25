package io.hhplus.concert.app.payment.domain.event.listener;

import static io.hhplus.concert.app.payment.domain.dto.PaymentOutboxCommand.CreateOutbox;

import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.payment.domain.event.producer.PaymentEventProducer;
import io.hhplus.concert.app.payment.domain.model.PaymentEventType;
import io.hhplus.concert.app.payment.domain.service.PaymentOutboxService;
import java.time.LocalDateTime;
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
    private final PaymentOutboxService outboxService;

    @Value("${kafka.topics.payment}")
    private String paymentTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createDonePaymentOutbox(DonePaymentEvent event) {
        CreateOutbox command = new CreateOutbox(event.getEventId(),
            PaymentEventType.DONE_PAYMENT, paymentTopic, event, LocalDateTime.now());

        outboxService.createOutbox(command);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void produceMessage(DonePaymentEvent event) {
        paymentEventProducer.produce(paymentTopic, event);
    }
}
