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
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import java.time.LocalDateTime;
import java.util.List;
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

    @DisplayName("createWaitingQueue() 테스트")
    @Nested
    class CreateWaitingQueueTest {
        @DisplayName("활성화 대기열 토큰이 최대 활성화 수 보다 적다면 활성화 토큰을 생성한다.")
        @Test
        void should_CreateActiveWaitingQueue_When_ActiveCountLessThanMaxActiveCount() {
            // given
            String token = "tokenValue";
            LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);
            int maxActiveCount = 10;
            long activeCount = 5L;
            CreateWaitingQueue command = new CreateWaitingQueue(token, maxActiveCount, expireAt);

            when(waitingQueueRepository.getActiveCount())
                .thenReturn(activeCount);

            // when
            WaitingQueue result = waitingQueueService.createWaitingQueue(command);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getExpireAt()).isEqualTo(expireAt);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueStatus.ACTIVE);
        }

        @DisplayName("활성화 대기열이 최대 인원보다 많다면 일반 대기열을 생성한다.")
        @Test
        void should_CreateWaitingQueue_When_ActiveCountGreaterThanMaxActiveCount() {
            // given
            String token = "tokenValue";
            LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);
            int maxActiveCount = 10;
            long activeCount = 15L;
            CreateWaitingQueue command = new CreateWaitingQueue(token, maxActiveCount, expireAt);

            when(waitingQueueRepository.getActiveCount())
                .thenReturn(activeCount);

            // when
            WaitingQueue result = waitingQueueService.createWaitingQueue(command);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getExpireAt()).isNull();
            assertThat(result.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);
        }
    }

    @DisplayName("getWaitingQueueWithOrder() 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("조회 조건에 해당하는 waitingQueue가 대기 상태가 아니라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotWaiting() {
            // given
            String token = "tokenValue";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(query))
                .thenReturn(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueWithOrder(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_STATE_NOT_WAITING.getMessage());
        }
        
        @DisplayName("조회 조건에 해당하는 waitingQueue와 대기순번을 조회하여 반환한다.")
        @Test
        void should_ReturnWaitingQueueWithOrder_When_Found() {
            // given
            String token = "tokenValue";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(query))
                .thenReturn(waitingQueue);

            when(waitingQueueRepository.countWaitingOrder(waitingQueue.getId()))
                .thenReturn(10L);

            // when
            WaitingQueueWithOrder result = waitingQueueService.getWaitingQueueWithOrder(query);

            // then
            assertThat(result.getWaitingQueue()).isEqualTo(waitingQueue);
            assertThat(result.getWaitingOrder()).isEqualTo(10L);
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

            when(waitingQueueRepository.getWaitingQueue(any(GetWaitingQueueCommonQuery.class)))
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
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.WAITING,
                currentTime.plusMinutes(10), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(waitingQueue);

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
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.ACTIVE,
                currentTime.minusMinutes(10), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(waitingQueue);

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
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.ACTIVE,
                currentTime.plusMinutes(10), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(any(GetWaitingQueueCommonQuery.class)))
                .thenReturn(waitingQueue);

            // when, then
            assertThatCode(() -> waitingQueueService.checkTokenActivate(query))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("activateToken() 테스트")
    @Nested
    class ActivateTokenTest {
        @DisplayName("활성화할 인원이 0이라면 아무일도 일어나지 않고, return된다.")
        @Test
        void should_Nothing_When_countToActivateIsZero() {
            // given
            int maxActiveCount = 10;

            when(waitingQueueRepository.getActiveCount())
                .thenReturn(10L);

            // when
            waitingQueueService.activateToken(maxActiveCount);

            // then
            verify(waitingQueueRepository, never()).getOldestWaitedQueueIds(anyInt());
            verify(waitingQueueRepository, never()).activateWaitingQueues(anyList());
        }

        @DisplayName("대기중인 사용자가 없다면 activateWaitingQueues함수가 호출되지 않는다.")
        @Test
        void should_NotCallActivateWaitingQueues_When_WaitingQueueCountEmpty() {
            // given
            int maxActiveCount = 10;
            long activeCount = 5L;

            when(waitingQueueRepository.getActiveCount())
                .thenReturn(activeCount);

            when(waitingQueueRepository.getOldestWaitedQueueIds(anyInt()))
                .thenReturn(List.of());

            // when
            waitingQueueService.activateToken(maxActiveCount);

            // then
            verify(waitingQueueRepository, times(1)).getOldestWaitedQueueIds(anyInt());
            verify(waitingQueueRepository, never()).activateWaitingQueues(anyList());
        }

        @DisplayName("활성화 대상 개수가 1 이상이고, 대기중인 사용자가 있다면 activateWaitingQueues함수가 호출된다.")
        @Test
        void should_CallActivateWaitingQueues_When_WaitedQueueExist() {
            // given
            int maxActiveCount = 6;
            long activeCount = 3L;
            int countToActivate = maxActiveCount - (int) activeCount;

            when(waitingQueueRepository.getActiveCount())
                .thenReturn(activeCount);

            List<Long> waitingQueueIds = List.of(1L, 2L, 3L);
            when(waitingQueueRepository.getOldestWaitedQueueIds(countToActivate))
                .thenReturn(waitingQueueIds);

            // when
            waitingQueueService.activateToken(maxActiveCount);

            // then
            verify(waitingQueueRepository, times(1))
                .getOldestWaitedQueueIds(countToActivate);
            verify(waitingQueueRepository, times(1))
                .activateWaitingQueues(waitingQueueIds);
        }
    }
}