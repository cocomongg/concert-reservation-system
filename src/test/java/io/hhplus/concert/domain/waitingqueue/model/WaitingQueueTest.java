package io.hhplus.concert.domain.waitingqueue.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WaitingQueueTest {

    @DisplayName("checkNotWaiting() 테스트")
    @Nested
    class CheckNotWaitingTest {
        @DisplayName("대기상태가 아니면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotWaiting() {
            // given
            WaitingQueueStatus status = WaitingQueueStatus.ACTIVE;
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                LocalDateTime.now(), LocalDateTime.now(), null);

            // when, then
            assertThatThrownBy(() -> waitingQueue.checkNotWaiting())
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_STATE_NOT_WAITING.getMessage());
        }

        @DisplayName("대기상태면 예외를 발생시키지 않는다.")
        @Test
        void should_NotThrowException_When_StatusIsWaiting() {
            WaitingQueueStatus status = WaitingQueueStatus.WAITING;
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                LocalDateTime.now(), LocalDateTime.now(), null);

            // when, then
            assertThatCode(() -> waitingQueue.checkNotWaiting())
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("checkActivated() 테스트")
    @Nested
    class CheckActivatedTest {
        @DisplayName("활성화 상태가 아니면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotActive() {
            // given
            WaitingQueueStatus status = WaitingQueueStatus.WAITING;
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                LocalDateTime.now(), LocalDateTime.now(), null);

            // when, then
            assertThatThrownBy(() -> waitingQueue.checkActivated(LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("만료시간이 지났으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_Expired() {
            // given
            WaitingQueueStatus status = WaitingQueueStatus.ACTIVE;
            LocalDateTime currentTime = LocalDateTime.now();
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                currentTime.minusMinutes(1), LocalDateTime.now(), null);

            // when, then
            assertThatThrownBy(() -> waitingQueue.checkActivated(currentTime))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("활성화 상태면 예외를 발생시키지 않는다.")
        @Test
        void should_NotThrowException_When_StatusIsActive() {
            WaitingQueueStatus status = WaitingQueueStatus.ACTIVE;
            LocalDateTime currentTime = LocalDateTime.now();
            WaitingQueue waitingQueue = new WaitingQueue(1L, "token", status,
                currentTime.plusMinutes(1), LocalDateTime.now(), null);

            // when, then
            assertThatCode(() -> waitingQueue.checkActivated(currentTime))
                .doesNotThrowAnyException();
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