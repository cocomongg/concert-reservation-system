package io.hhplus.concert.app.payment.application;

import static io.hhplus.concert.app.payment.domain.model.PaymentEventType.DONE_PAYMENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.payment.domain.event.producer.PaymentEventProducer;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import io.hhplus.concert.app.payment.domain.service.PaymentOutboxService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentOutboxScheduler {

    private final PaymentOutboxService outboxService;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = ServicePolicy.REPUBLISH_MESSAGE_INTERVAL)
    public void republishMessage() {
        List<PaymentOutbox> outboxesForRepublish = outboxService.getOutboxesForRepublish(
            LocalDateTime.now());

        for(PaymentOutbox outbox : outboxesForRepublish) {
            try {
                if(outbox.getEventType().equals(DONE_PAYMENT)) {
                    DonePaymentEvent donePaymentEvent = objectMapper.convertValue(outbox.getPayload(), DonePaymentEvent.class);
                    paymentEventProducer.produce(outbox.getTopic(), donePaymentEvent);
                    outboxService.publishSuccess(outbox.getEventId());
                }
            } catch (Exception e) {
                outboxService.publishFail(outbox.getEventId());
            }
        }
    }
}
