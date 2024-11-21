package io.hhplus.concert.app.waitingqueue.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.waitingqueue.application.WaitingQueueFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitingQueueDonePaymentConsumer {

    private final WaitingQueueFacade waitingQueueFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment}", groupId = "${kafka.groups.waitingQueue}")
    public void consumeDonePaymentEvent(ConsumerRecord<String, Object> message, Acknowledgment ack) {
        try {
            DonePaymentEvent donePaymentEvent = objectMapper.convertValue(message.value(),
                DonePaymentEvent.class);

            waitingQueueFacade.expireToken(donePaymentEvent.getToken());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("WaitingQueueConsumer consume error", e);
        }
    }
}

