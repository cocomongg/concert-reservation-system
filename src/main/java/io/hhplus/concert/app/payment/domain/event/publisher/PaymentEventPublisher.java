package io.hhplus.concert.app.payment.domain.event.publisher;

import io.hhplus.concert.app.payment.domain.event.PaymentEvent;

public interface PaymentEventPublisher {
    void publish(PaymentEvent event);
}
