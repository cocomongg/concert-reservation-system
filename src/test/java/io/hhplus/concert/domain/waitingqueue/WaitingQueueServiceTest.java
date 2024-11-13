package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.ActivateWaitingTokens;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
import io.hhplus.concert.domain.waitingqueue.model.TokenMeta;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WaitingQueueServiceTest {

    @Mock
    private WaitingQueueRepository waitingQueueRepository;

    @InjectMocks
    private WaitingQueueService waitingQueueService;

    @DisplayName("getWaitingQueueToken 테스트")
    @Nested
    class GetWaitingQueueTokenTest {
        @DisplayName("대기 상태인 토큰의 값을 통해 해당 토큰의 정보를 반환한다.")
        @Test
        void should_ReturnWaitingTokenInfo_When_InputWaitingStatusToken () {
            // given
            String token = "token";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            when(waitingQueueRepository.isWaitingStatus(token)).thenReturn(true);

            // when
            WaitingQueueTokenInfo result = waitingQueueService.getWaitingQueueToken(query);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);
        }

        @DisplayName("활성 상태인 토큰의 값을 통해 해당 토큰의 정보를 반환한다.")
        @Test
        void should_ReturnActiveTokenInfo_When_InputActiveStatusToken () {
            // given
            String token = "token";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            when(waitingQueueRepository.isWaitingStatus(token)).thenReturn(false);
            when(waitingQueueRepository.isActiveStatus(token)).thenReturn(true);

            TokenMeta tokenMeta = new TokenMeta(LocalDateTime.now());
            when(waitingQueueRepository.getTokenMeta(token)).thenReturn(tokenMeta);

            // when
            WaitingQueueTokenInfo result = waitingQueueService.getWaitingQueueToken(query);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.ACTIVE);
            assertThat(result.getExpireAt()).isEqualTo(tokenMeta.getExpireAt());
        }

        @DisplayName("대기 상태도 활성 상태도 아닌 토큰의 값은 예외가 발생한다.")
        @Test
        void should_ThrowException_When_InputInvalidToken () {
            // given
            String token = "token";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            when(waitingQueueRepository.isWaitingStatus(token)).thenReturn(false);
            when(waitingQueueRepository.isActiveStatus(token)).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueToken(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }
    }

    @DisplayName("getRemainingWaitTimeSeconds() 테스트")
    @Nested
    class GetRemainingWaitTimeSecondsTest {
        @DisplayName("대기 순서가 0이라면 남은 대기 시간은 활성화 간격이다.")
        @Test
        void should_ReturnActivationIntervalSeconds_When_WaitingOrderIsZero() {
            // given
            Long waitingOrder = 0L;
            int activationBatchSize = 10;
            int activationIntervalSeconds = 60;
            GetRemainingWaitTimeSeconds query =
                new GetRemainingWaitTimeSeconds(waitingOrder, activationBatchSize, activationIntervalSeconds);

            // when
            Long remainingWaitTimeSeconds = waitingQueueService.getRemainingWaitTimeSeconds(query);

            // then
            assertThat(remainingWaitTimeSeconds).isEqualTo(activationIntervalSeconds);
        }

        @DisplayName("대기 순서가 0이 아니고, 활성화 배치 사이즈보다 작다면 남은 대기 시간은 활성화 간격이다.")
        @Test
        void should_ReturnRemainingWaitTimeSeconds_When_WaitingOrderIsNotZero() {
            // given
            Long waitingOrder = 10L;
            int activationBatchSize = 10;
            int activationIntervalSeconds = 60;
            GetRemainingWaitTimeSeconds query =
                new GetRemainingWaitTimeSeconds(waitingOrder, activationBatchSize, activationIntervalSeconds);

            // when
            Long remainingWaitTimeSeconds = waitingQueueService.getRemainingWaitTimeSeconds(query);

            // then
            assertThat(remainingWaitTimeSeconds).isEqualTo(activationIntervalSeconds);
        }

        @DisplayName("대기 순서가 0이 아니고, 활성화 배치 사이즈보다 크다면 남은 대기 시간은 (현재 순서 / 활성화 배치 사이즈 * 활성화 간격) 이다.")
        @Test
        void should_ReturnRemainingWaitTimeSeconds_When_WaitingOrderIsNotZeroAndGreaterThanActivationBatchSize() {
            // given
            Long waitingOrder = 20L;
            int activationBatchSize = 10;
            int activationIntervalSeconds = 60;
            GetRemainingWaitTimeSeconds query =
                new GetRemainingWaitTimeSeconds(waitingOrder, activationBatchSize, activationIntervalSeconds);

            // when
            Long remainingWaitTimeSeconds = waitingQueueService.getRemainingWaitTimeSeconds(query);

            // then
            assertThat(remainingWaitTimeSeconds)
                .isEqualTo(waitingOrder / activationBatchSize * activationIntervalSeconds);
        }
    }

    @DisplayName("checkTokenActivate() 테스트")
    @Nested
    class CheckTokenActivateTest {
        @DisplayName("활성 상태인 토큰의 값을 통해 예외가 발생하지 않는다.")
        @Test
        void should_NotThrowException_When_InputActiveStatusToken() {
            // given
            String token = "token";
            CheckTokenActivate query = new CheckTokenActivate(token, LocalDateTime.now());

            when(waitingQueueRepository.isActiveStatus(token)).thenReturn(true);

            // when, then
            assertThatCode(() -> waitingQueueService.checkTokenActivate(query))
                .doesNotThrowAnyException();
        }

        @DisplayName("활성 상태가 아닌 토큰의 값을 통해 예외가 발생한다.")
        @Test
        void should_ThrowException_When_InputInvalidToken() {
            // given
            String token = "token";
            CheckTokenActivate query = new CheckTokenActivate(token, LocalDateTime.now());

            when(waitingQueueRepository.isActiveStatus(token)).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }
    }

    @DisplayName("activateToken() 테스트")
    @Nested
    class ActivateTokenTest {
        @DisplayName("대기 토큰을 활성화하고, 활성화된 토큰의 수를 반환한다.")
        @Test
        void should_ReturnActivatedTokenCount_When_ActivateTokens() {
            // given
            int limit = 10;
            ActivateWaitingTokens command = new ActivateWaitingTokens(limit, 10, TimeUnit.SECONDS);

            List<String> waitingTokens = List.of("token1", "token2", "token3");
            when(waitingQueueRepository.popOldestWaitingTokens(limit))
                .thenReturn(waitingTokens);

            when(waitingQueueRepository.activateWaitingTokens(any(String.class), any(TokenMeta.class), any(Duration.class)))
                .thenReturn(1L);

            // when
            long activatedTokenCount = waitingQueueService.activateToken(command);

            // then
            assertThat(activatedTokenCount).isEqualTo(waitingTokens.size());
        }
    }
}