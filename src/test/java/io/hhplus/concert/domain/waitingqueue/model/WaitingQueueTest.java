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

    @DisplayName("isAvailable() 테스트")
    @Nested
    class IsAvailableTest {
        @DisplayName("활성화 상태가 아니면 false를 반환한다.")
        @Test
        void should_ReturnFalse_When_NotActive() {
            // given
            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token("token")
                .status(WaitingQueueStatus.WAITING)
                .expireAt(LocalDateTime.now())
                .build();

            // when
            boolean result = waitingQueue.isAvailable(LocalDateTime.now());

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("활성화 상태고, 만료시간이 지났다면 false를 반환한다.")
        @Test
        void should_ReturnFalse_When_ActiveAndExpired() {
            // given
            LocalDateTime expireAt = LocalDateTime.now();
            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token("token")
                .status(WaitingQueueStatus.WAITING)
                .expireAt(expireAt)
                .build();

            // when
            boolean result = waitingQueue.isAvailable(expireAt.plusSeconds(1));

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("활성화 상태고, 만료시간이 지나지 않았다면 true를 반환한다.")
        @Test
        void should_ReturnTrue_When_ActiveAndNotExpired() {
            // given
            LocalDateTime expireAt = LocalDateTime.now();
            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token("token")
                .status(WaitingQueueStatus.WAITING)
                .expireAt(expireAt)
                .build();

            // when
            boolean result = waitingQueue.isAvailable(expireAt.minusSeconds(1));

            // then
            assertThat(result).isFalse();
        }
    }

    @DisplayName("expire 함수를 호출하면 WaitingQueue가 만료상태가 된다.")
    @Test
    void should_SetExpiredStatus_When_CallExpire() {
        // given
        WaitingQueue waitingQueue = WaitingQueue.builder()
            .token("token")
            .status(WaitingQueueStatus.ACTIVE)
            .expireAt(LocalDateTime.now())
            .build();

        // when
        waitingQueue.expire();

        // then
        assertThat(waitingQueue.getStatus()).isEqualTo(WaitingQueueStatus.EXPIRED);
    }
}