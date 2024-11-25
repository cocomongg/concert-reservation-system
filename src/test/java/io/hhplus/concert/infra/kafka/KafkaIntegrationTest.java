package io.hhplus.concert.infra.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092"},
    ports = {9092})
@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaTestConsumer kafkaTestConsumer;

    @Value("${kafka.topics.test}")
    private String topic;

    @DisplayName("Kafka의 특정 토픽에 여러 번 메시지를 전송하고, 해당 토픽을 구독하는 컨슈머가 메시지를 소비한다.")
    @Test
    public void should_ConsumeMessages_When_ProducingMultipleMessages() {
        // given
        int sendCount = 5;
        List<KafkaTestMessage> messages = new ArrayList<>();

        // when
        for(int i = 0; i < sendCount; ++i) {
            String content = "test message " + i;
            KafkaTestMessage message = new KafkaTestMessage(content, LocalDateTime.now());
            messages.add(message);
            kafkaTemplate.send(topic, message);
        }

        // then
        await().pollInterval(500, TimeUnit.MILLISECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<KafkaTestMessage> consumedMessages = kafkaTestConsumer.getMessages();
                assertThat(consumedMessages.size()).isEqualTo(sendCount);

                for(int i = 0; i < sendCount; ++i) {
                    KafkaTestMessage consumedMessage = consumedMessages.get(i);
                    assertThat(consumedMessage.getMessage()).isEqualTo(messages.get(i).getMessage());
                }
            });
    }
}
