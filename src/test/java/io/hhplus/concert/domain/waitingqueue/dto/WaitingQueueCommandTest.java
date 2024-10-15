package io.hhplus.concert.domain.waitingqueue.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueErrorCode;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WaitingQueueCommandTest {

    @DisplayName("CreateWaitingQueueCommand 생성자 테스트")
    @Nested
    class CreateWaitingQueueCommandConstructorTest {
        @DisplayName("token이 유효하지 않은 값이면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_InvalidTokenValue() {
            // given
            String invalidTokenValue = "";

            // when, then
            assertThatThrownBy(() -> new CreateWaitingQueueCommand(invalidTokenValue,
                WaitingQueueStatus.WAITED, LocalDateTime.now()))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_CREATION_INPUT.getMessage());
        }

        @DisplayName("status가 유효하지 않은 값이면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_InvalidStatusValue() {
            assertThatThrownBy(() -> new CreateWaitingQueueCommand("token", null,
                LocalDateTime.now()))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_CREATION_INPUT.getMessage());
        }

        @DisplayName("expiredAt이 유효하지 않은 값이면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_InvalidExpiredAtValue() {
            assertThatThrownBy(() -> new CreateWaitingQueueCommand("token",
                WaitingQueueStatus.WAITED, null))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_CREATION_INPUT.getMessage());
        }

        @DisplayName("모든 값이 유효하면 CreateWaitingQueueCommand 객체가 생성된다.")
        @Test
        void should_GenerateCreateWaitingQueueCommand_When_ValidValue() {
            // given
            String token = "token";
            WaitingQueueStatus status = WaitingQueueStatus.WAITED;
            LocalDateTime expiredAt = LocalDateTime.now();

            // when
            CreateWaitingQueueCommand command =
                new CreateWaitingQueueCommand(token, status, expiredAt);

            // then
            assertThat(command).isNotNull();
            assertThat(command.getToken()).isEqualTo(token);
            assertThat(command.getStatus()).isEqualTo(status);
            assertThat(command.getExpiredAt()).isEqualTo(expiredAt);
        }
    }
}