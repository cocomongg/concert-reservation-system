package io.hhplus.concert.app.payment.domain.event.producer;

import io.hhplus.concert.app.payment.domain.event.PaymentEvent;

public interface PaymentEventProducer {
    void produce(String topic, PaymentEvent message);
}