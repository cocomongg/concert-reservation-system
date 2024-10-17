package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueErrorCode;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
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

    @DisplayName("getWaitingQueueWithOrder() 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("조회 조건에 해당하는 waitingQueue가 대기 상태가 아니라면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_StatusIsNotWaiting() {
            // given
            String token = "tokenValue";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), null);

            when(waitingQueueRepository.getWaitingQueue(query))
                .thenReturn(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueWithOrder(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_STATE_NOT_WAITING.getMessage());
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

    @DisplayName("activateOldestWaitedQueues() 테스트")
    @Nested
    class ActivateOldestWaitedQueuesTest {
        @DisplayName("활성화할 인원이 0이라면 아무일도 일어나지 않고, return된다.")
        @Test
        void should_Nothing_When_countToActivateIsZero() {
            // given
            int countToActivate = 0;

            // when
            waitingQueueService.activateOldestWaitedQueues(countToActivate);

            // then
            verify(waitingQueueRepository, never()).getOldestWaitedQueueIds(anyInt());
            verify(waitingQueueRepository, never()).activateWaitingQueues(anyList());
        }

        @DisplayName("대기중인 사용자가 없다면 activateWaitingQueues함수가 호출되지 않는다.")
        @Test
        void should_NotCallActivateWaitingQueues_When_WaitingQueueCountEmpty() {
            // given
            int countToActivate = 10;

            // when
            waitingQueueService.activateOldestWaitedQueues(countToActivate);

            // then
            verify(waitingQueueRepository, times(1)).getOldestWaitedQueueIds(anyInt());
            verify(waitingQueueRepository, never()).activateWaitingQueues(anyList());
        }

        @DisplayName("활성화 대상 개수가 1 이상이고, 대기중인 사용자가 있다면 activateWaitingQueues함수가 호출된다.")
        @Test
        void should_CallActivateWaitingQueues_When_WaitedQueueExist() {
            // given
            int countToActivate = 3;
            List<Long> waitingQueueIds = List.of(1L, 2L, 3L);
            when(waitingQueueRepository.getOldestWaitedQueueIds(countToActivate))
                .thenReturn(waitingQueueIds);

            // when
            waitingQueueService.activateOldestWaitedQueues(countToActivate);

            // then
            verify(waitingQueueRepository, times(1)).getOldestWaitedQueueIds(countToActivate);
            verify(waitingQueueRepository, times(1)).activateWaitingQueues(waitingQueueIds);
        }
    }
}