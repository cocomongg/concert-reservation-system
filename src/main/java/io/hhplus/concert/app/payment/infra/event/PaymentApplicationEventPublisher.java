package io.hhplus.concert.app.payment.infra.event;

import io.hhplus.concert.app.payment.domain.event.PaymentEvent;
import io.hhplus.concert.app.payment.domain.event.publisher.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentApplicationEventPublisher implements PaymentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
