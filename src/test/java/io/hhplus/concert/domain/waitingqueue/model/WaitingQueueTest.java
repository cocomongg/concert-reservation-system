package io.hhplus.concert.domain.waitingqueue.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WaitingQueueTest {

    @DisplayName("isWaiting() 테스트")
    @Nested
    class IsWaitingTest {
        @DisplayName("대기상태가 아니라면 false를 반환한다.")
        @Test
        void should_ReturnFalse_When_StatusIsNotWaiting() {
            // given
            WaitingQueueStatus status = WaitingQueueStatus.ACTIVE;
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                LocalDateTime.now(), LocalDateTime.now(), null);

            // when
            boolean result = waitingQueue.isWaiting();

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("대기상태면 true를 반환한다.")
        @Test
        void should_ReturnTrue_When_StatusIsWaiting() {
            WaitingQueueStatus status = WaitingQueueStatus.WAITING;
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                LocalDateTime.now(), LocalDateTime.now(), null);

            // when
            boolean result = waitingQueue.isWaiting();

            // then
            assertThat(result).isTrue();
        }
    }

}