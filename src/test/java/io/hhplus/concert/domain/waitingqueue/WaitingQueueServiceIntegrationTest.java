package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WaitingQueueServiceIntegrationTest {

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private WaitingQueueService waitingQueueService;

    @AfterEach
    public void teardown() {
        waitingQueueJpaRepository.deleteAllInBatch();
    }

    @DisplayName("createWaitingQueue 테스트")
    @Nested
    class CreateWaitingQueueTest {
        @DisplayName("입력된 값들을 통해 waitingQueue객체를 반환한다.")
        @Test
        void should_ReturnWaitingQueue_When_CommandGiven() {
            // given
            CreateWaitingQueueCommand command = new CreateWaitingQueueCommand("token",
                WaitingQueueStatus.WAITED, LocalDateTime.now());

            // when
            WaitingQueue result = waitingQueueService.createWaitingQueue(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(command.getToken());
            assertThat(result.getStatus()).isEqualTo(command.getStatus());
            assertThat(result.getExpiredAt()).isEqualTo(command.getExpiredAt());
        }

        @DisplayName("입력된 값들을 통해 waitingQueue객체를 저장한다.")
        @Test
        void should_SaveWaitingQueue_When_CommandGiven() {
            // given
            CreateWaitingQueueCommand command = new CreateWaitingQueueCommand("token",
                WaitingQueueStatus.WAITED, LocalDateTime.now());

            // when
            WaitingQueue waitingQueue = waitingQueueService.createWaitingQueue(command);
            Optional<WaitingQueue> savedWaitingQueueOptional =
                waitingQueueJpaRepository.findById(waitingQueue.getId());

            // then
            assertThat(savedWaitingQueueOptional).isPresent();

            WaitingQueue savedWaitingQueue = savedWaitingQueueOptional.get();
            assertThat(savedWaitingQueue.getToken()).isEqualTo(command.getToken());
            assertThat(savedWaitingQueue.getStatus()).isEqualTo(command.getStatus());
            assertThat(savedWaitingQueue.getExpiredAt()).isEqualTo(command.getExpiredAt());
        }
    }
}