package io.hhplus.concert.app.payment.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.payment.domain.service.PaymentOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DonePaymentMessageConsumer {
    private final PaymentOutboxService outboxService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment}", groupId = "${kafka.groups.payment}")
    public void consume(ConsumerRecord<String, Object> message, Acknowledgment ack) {
        try {
            DonePaymentEvent donePaymentEvent = objectMapper.convertValue(message.value(),
                DonePaymentEvent.class);

            outboxService.publishSuccess(donePaymentEvent.getEventId());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("DonePaymentMessageConsumer consume error", e);
        }
    }
}
