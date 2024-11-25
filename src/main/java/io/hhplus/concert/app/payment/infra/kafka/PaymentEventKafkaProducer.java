package io.hhplus.concert.app.payment.infra.kafka;

import io.hhplus.concert.app.payment.domain.event.PaymentEvent;
import io.hhplus.concert.app.payment.domain.event.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentEventKafkaProducer implements PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void produce(String topic, PaymentEvent message) {
        kafkaTemplate.send(topic, message);
    }
}

