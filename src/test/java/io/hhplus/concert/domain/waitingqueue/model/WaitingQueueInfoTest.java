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

class WaitingQueueInfoTest {

    @DisplayName("checkActivated() 테스트")
    @Nested
    class CheckActivatedTest {
        @DisplayName("활성화 상태가 아니면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotActive() {
            // given
            WaitingQueueTokenStatus status = WaitingQueueTokenStatus.WAITING;
            WaitingQueueTokenInfo tokenInfo = new WaitingQueueTokenInfo("token", status,
                LocalDateTime.now());

            // when, then
            assertThatThrownBy(() -> tokenInfo.checkActivated(LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("만료시간이 지났으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_Expired() {
            // given
            WaitingQueueTokenStatus status = WaitingQueueTokenStatus.ACTIVE;
            LocalDateTime currentTime = LocalDateTime.now();
            WaitingQueueTokenInfo tokenInfo = new WaitingQueueTokenInfo("token", status,
                currentTime.minusMinutes(1));

            // when, then
            assertThatThrownBy(() -> tokenInfo.checkActivated(currentTime))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("활성화 상태면 예외를 발생시키지 않는다.")
        @Test
        void should_NotThrowException_When_StatusIsActive() {
            WaitingQueueTokenStatus status = WaitingQueueTokenStatus.ACTIVE;
            LocalDateTime currentTime = LocalDateTime.now();
            WaitingQueueTokenInfo tokenInfo = new WaitingQueueTokenInfo("token", status,
                currentTime.plusMinutes(1));

            // when, then
            assertThatCode(() -> tokenInfo.checkActivated(currentTime))
                .doesNotThrowAnyException();
        }
    }
}