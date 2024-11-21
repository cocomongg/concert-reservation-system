package io.hhplus.concert.app.payment.domain.service;

import static io.hhplus.concert.app.payment.domain.model.PaymentEventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.payment.domain.dto.PaymentOutboxCommand.CreateOutbox;
import io.hhplus.concert.app.payment.domain.model.OutboxStatus;
import io.hhplus.concert.app.payment.domain.model.PaymentEventType;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import io.hhplus.concert.app.payment.infra.db.PaymentOutboxJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class PaymentOutboxServiceIntegrationTest {

    @Autowired
    private PaymentOutboxService outboxService;

    @Autowired
    private PaymentOutboxJpaRepository outboxJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("getOutbox() test")
    @Nested
    class GetOutboxTest {
        @DisplayName("eventId에 해당하는 Outbox를 반환한다.")
        @Test
        void should_ReturnOutbox_When_GivenEventId() {
            // given
            String eventId = "testEventId";
            PaymentOutbox savedOutbox = outboxJpaRepository.save(
                new PaymentOutbox(eventId, DONE_PAYMENT, "testTopic", null, null));

            // when
            PaymentOutbox result = outboxService.getOutbox(eventId);

            // then
            assertThat(result.getId()).isEqualTo(savedOutbox.getId());
            assertThat(result.getEventId()).isEqualTo(eventId);
        }

        @DisplayName("eventId에 해당하는 Outbox가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_OutboxNotFound() {
            // given
            String eventId = "testEventId";

            // when, then
            assertThatThrownBy(() -> outboxService.getOutbox(eventId))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.PaymentOutbox.OUTBOX_NOT_FOUND.getMessage());
        }
    }

    @DisplayName("getOutboxesForRepublish() test")
    @Nested
    class GetOutboxesForRepublishTest {
        @DisplayName("주어진 일시 보다 5분 전에 생성된 INIT상태의 Outbox 목록을 반환한다.")
        @Test
        void should_ReturnOutboxes_whenCreatedBeforeGivenTimeAndStatusIsInit() {
            // given
            LocalDateTime now = LocalDateTime.now();
            int outboxForRepublishCount = 5;
            List<PaymentOutbox> outboxList = new ArrayList<>();
            for(int i = 0; i < outboxForRepublishCount; ++i) {
                PaymentOutbox outboxForRepublish = PaymentOutbox.builder()
                    .eventId("eventId" + i)
                    .eventType(DONE_PAYMENT)
                    .topic("topic" + i)
                    .status(OutboxStatus.INIT)
                    .createdAt(now.minusMinutes(10))
                    .build();

                outboxList.add(outboxJpaRepository.save(outboxForRepublish));
            }

            PaymentOutbox outboxNotForRepublish1 = PaymentOutbox.builder()
                .eventId("eventIdNotForRepublish1")
                .eventType(DONE_PAYMENT)
                .topic("topicNotForRepublish1")
                .status(OutboxStatus.INIT)
                .createdAt(now.minusMinutes(4))
                .build();

            PaymentOutbox outboxNotForRepublish2 = PaymentOutbox.builder()
                .eventId("eventIdNotForRepublish1")
                .eventType(DONE_PAYMENT)
                .topic("topicNotForRepublish1")
                .status(OutboxStatus.SUCCESS)
                .createdAt(now.minusMinutes(10))
                .build();

            outboxList.addAll(List.of(outboxNotForRepublish1, outboxNotForRepublish2));
            outboxJpaRepository.saveAll(outboxList);

            // when
            List<PaymentOutbox> result = outboxService.getOutboxesForRepublish(now);

            // then
            assertThat(result.size()).isEqualTo(outboxForRepublishCount);
            for(PaymentOutbox outbox : result) {
                assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.INIT);
                assertThat(outbox.getCreatedAt()).isBefore(now.minusMinutes(5));
            }
        }
    }

    @DisplayName("createOutbox() test")
    @Nested
    class CreateOutboxTest {
        @DisplayName("입력된 값을 통해 INIT상태의 Outbox를 반환한다.")
        @Test
        void should_ReturnOutbox_When_GivenCreateOutboxCommand() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("key1", "value1");
            CreateOutbox command = new CreateOutbox("testEventId", DONE_PAYMENT,
                "testTopic", payload, LocalDateTime.now());

            // when
            PaymentOutbox result = outboxService.createOutbox(command);

            // then
            assertThat(result.getEventId()).isEqualTo(command.getEventId());
            assertThat(result.getEventType()).isEqualTo(command.getEventType());
            assertThat(result.getTopic()).isEqualTo(command.getTopic());
            assertThat(result.getPayload()).isEqualTo(objectMapper.valueToTree(command.getPayload()));
            assertThat(result.getStatus()).isEqualTo(OutboxStatus.INIT);
        }

        @DisplayName("입력된 값을 통해 INIT상태의 Outbox를 저장한다.")
        @Test
        void should_SaveOutbox_When_GivenCreateOutboxCommand() {
            // given
            Map<String, Object> payload = new HashMap<>();
            payload.put("key1", "value1");
            CreateOutbox command = new CreateOutbox("testEventId", DONE_PAYMENT,
                "testTopic", payload, LocalDateTime.now());

            // when
            PaymentOutbox result = outboxService.createOutbox(command);

            // then
            PaymentOutbox savedOutbox = outboxJpaRepository.findById(result.getId()).orElse(null);
            assertThat(savedOutbox).isNotNull();
            assertThat(savedOutbox.getEventId()).isEqualTo(command.getEventId());
            assertThat(savedOutbox.getEventType()).isEqualTo(command.getEventType());
            assertThat(savedOutbox.getTopic()).isEqualTo(command.getTopic());
            assertThat(result.getPayload()).isEqualTo(objectMapper.valueToTree(command.getPayload()));
            assertThat(savedOutbox.getStatus()).isEqualTo(OutboxStatus.INIT);
        }
    }

    @DisplayName("publishSuccess() test")
    @Nested
    class PublishSuccessTest {
        @DisplayName("eventId에 해당하는 Outbox의 상태를 SUCCESS로 변경한다.")
        @Test
        void should_ChangeOutboxStatusToSuccess_When_GivenEventId() {
            // given
            String eventId = "testEventId";
            PaymentOutbox outbox = outboxJpaRepository.save(
                new PaymentOutbox(eventId, DONE_PAYMENT, "testTopic", null, null));

            // when
            outboxService.publishSuccess(eventId);

            // then
            PaymentOutbox updatedOutbox = outboxJpaRepository.findById(outbox.getId()).orElse(null);
            assertThat(updatedOutbox).isNotNull();
            assertThat(updatedOutbox.getStatus()).isEqualTo(OutboxStatus.SUCCESS);
        }
    }

    @DisplayName("publishFail() test")
    @Nested
    class PublishFailTest {
        @DisplayName("eventId에 해당하는 Outbox의 상태를 FAIL로 변경한다.")
        @Test
        void should_ChangeOutboxStatusToFail_When_GivenEventId() {
            // given
            String eventId = "testEventId";
            PaymentOutbox outbox = outboxJpaRepository.save(
                new PaymentOutbox(eventId, DONE_PAYMENT, "testTopic", null, null));

            // when
            outboxService.publishFail(eventId);

            // then
            PaymentOutbox updatedOutbox = outboxJpaRepository.findById(outbox.getId()).orElse(null);
            assertThat(updatedOutbox).isNotNull();
            assertThat(updatedOutbox.getStatus()).isEqualTo(OutboxStatus.FAIL);
        }
    }

}