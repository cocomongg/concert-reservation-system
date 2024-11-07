package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(value = MockitoExtension.class)
class WaitingQueueServiceTest {

    @Mock
    private WaitingQueueRepository waitingQueueRepository;

    @InjectMocks
    private WaitingQueueService waitingQueueService;

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
        @DisplayName("토큰에 해당하는 대기열이 존재하지 않는다면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueNotFound() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);

            when(waitingQueueRepository.getWaitingQueueToken(any(GetWaitingQueueCommonQuery.class)))
                .thenThrow(new CoreException(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND));

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("대기열이 활성화 상태가 아니라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueIsNotActive() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueueTokenInfo tokenInfo =
                new WaitingQueueTokenInfo(token, WaitingQueueTokenStatus.WAITING, currentTime.plusMinutes(10));

            when(waitingQueueRepository.getWaitingQueueToken(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(tokenInfo);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("대기열이 만료되었다면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueIsExpired() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueueTokenInfo tokenInfo =
                new WaitingQueueTokenInfo(token, WaitingQueueTokenStatus.WAITING, currentTime.plusMinutes(10));

            when(waitingQueueRepository.getWaitingQueueToken(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(tokenInfo);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("대기열이 활성화 상태이고, 만료되지 않았다면 CoreException이 발생하지 않는다.")
        @Test
        void should_NotThrowCoreException_When_WaitingQueueIsActive() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueueTokenInfo tokenInfo =
                new WaitingQueueTokenInfo(token, WaitingQueueTokenStatus.ACTIVE, currentTime.plusMinutes(10));

            when(waitingQueueRepository.getWaitingQueueToken(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(tokenInfo);

            // when, then
            assertThatCode(() -> waitingQueueService.checkTokenActivate(query))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("activateToken() 테스트")
    @Nested
    class ActivateTokenTest {

        @DisplayName("대기중인 사용자가 없다면 activateWaitingQueues함수가 호출되지 않는다.")
        @Test
        void should_NotCallActivateWaitingQueues_When_WaitingQueueCountEmpty() {
            // given
            int maxActiveCount = 10;

            when(waitingQueueRepository.getOldestWaitingTokens(anyInt()))
                .thenReturn(List.of());

            // when
            waitingQueueService.activateToken(maxActiveCount);

            // then
            verify(waitingQueueRepository, times(1)).getOldestWaitingTokens(anyInt());
            verify(waitingQueueRepository, never()).activateWaitingTokens(anyList());
        }

        @DisplayName("대기중인 사용자가 있다면 activateWaitingQueues함수가 호출된다.")
        @Test
        void should_CallActivateWaitingQueues_When_WaitedQueueExist() {
            // given
            int countToActivate = 3;
            List<WaitingQueueTokenInfo> waitingQueueTokenInfoList = new ArrayList<>();

            for(int i = 0; i < countToActivate; i++) {
                WaitingQueueTokenInfo tokenInfo = new WaitingQueueTokenInfo("token" + i,
                    WaitingQueueTokenStatus.WAITING, LocalDateTime.now());

                waitingQueueTokenInfoList.add(tokenInfo);
            }
            when(waitingQueueRepository.getOldestWaitingTokens(countToActivate))
                .thenReturn(waitingQueueTokenInfoList);

            List<String> tokensToActive = waitingQueueTokenInfoList.stream()
                .map(WaitingQueueTokenInfo::getToken)
                .collect(Collectors.toList());

            // when
            waitingQueueService.activateToken(countToActivate);

            // then
            verify(waitingQueueRepository, times(1))
                .activateWaitingTokens(tokensToActive);
        }
    }

    @DisplayName("expireTokens() 테스트")
    @Nested
    class ExpireTokensTest {
        @DisplayName("만료될 대기열이 없다면 expireActiveTokens함수가 호출되지 않는다.")
        @Test
        void should_NotCallExpireActiveTokens_When_NoWaitingQueueToExpire() {
            // given
            LocalDateTime currentTime = LocalDateTime.now();

            when(waitingQueueRepository.getActiveTokensToExpire(currentTime))
                .thenReturn(List.of());

            // when
            waitingQueueService.expireTokens(currentTime);

            // then
            verify(waitingQueueRepository, never()).expireActiveTokens(anyList());
        }

        @DisplayName("만료될 대기열이 있다면 expireActiveTokens함수가 호출된다.")
        @Test
        void should_CallExpireActiveTokens_When_WaitingQueueToExpireExist() {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int countToExpire = 3;
            List<WaitingQueueTokenInfo> waitingQueueTokenInfoList = new ArrayList<>();

            for(int i = 0; i < countToExpire; i++) {
                WaitingQueueTokenInfo tokenInfo = new WaitingQueueTokenInfo("token" + i,
                    WaitingQueueTokenStatus.ACTIVE, LocalDateTime.now());

                waitingQueueTokenInfoList.add(tokenInfo);
            }
            when(waitingQueueRepository.getActiveTokensToExpire(currentTime))
                .thenReturn(waitingQueueTokenInfoList);

            List<String> tokensToExpire = waitingQueueTokenInfoList.stream()
                .map(WaitingQueueTokenInfo::getToken)
                .collect(Collectors.toList());

            // when
            waitingQueueService.expireTokens(currentTime);

            // then
            verify(waitingQueueRepository, times(1))
                .expireActiveTokens(tokensToExpire);
        }
    }
}