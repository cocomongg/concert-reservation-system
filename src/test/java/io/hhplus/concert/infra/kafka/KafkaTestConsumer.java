package io.hhplus.concert.infra.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class KafkaTestConsumer {

    private List<KafkaTestMessage> messages = new ArrayList<>();

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.test}", groupId = "test_group")
    public void consume(ConsumerRecord<String, Object> message, Acknowledgment ack) {
        try {
            Object value = message.value();
            KafkaTestMessage deserializedMessage = objectMapper.convertValue(value, KafkaTestMessage.class);
            log.info("Consumed message: {}", deserializedMessage.toString());
            messages.add(deserializedMessage);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to consume message", e);
        }
    }
}
