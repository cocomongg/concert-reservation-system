package io.hhplus.concert.application.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueErrorCode;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WaitingQueueFacadeIntegrationTest {

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private WaitingQueueFacade waitingQueueFacade;

    @AfterEach
    public void tearDown() {
        waitingQueueJpaRepository.deleteAllInBatch();
    }

    @DisplayName("generateWaitingQueueToken() 테스트")
    @Nested
    class GenerateWaitingQueueToken {
        @DisplayName("대기열에 활성화된 사용자가 가용인원보다 적을 때 활성화된 WaitingQueue를 생성한다.")
        @Test
        void should_CreateActiveWaitingQueue_When_LessThenMaxActivateCount () {
            // given
            waitingQueueJpaRepository.deleteAll();

            // when
            WaitingQueueInfo waitingQueueInfo = waitingQueueFacade.generateWaitingQueueToken();

            // then
            assertThat(waitingQueueInfo.getStatus()).isEqualTo(WaitingQueueStatus.ACTIVE);
            assertThat(waitingQueueInfo.getExpireAt()).isAfter(LocalDateTime.now());
        }

        @DisplayName("대기열에 활성화된 사용자가 가용인원만큼 있을 때 대기상태인 WaitingQueue를 생성한다.")
        @Test
        void should_CreateWaitingStatusWaitingQueue_When_NotLessThenMaxActivateCount () {
            // given
            int maxActivateCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;

            List<WaitingQueue> waitingQueueList = new ArrayList<>();
            for (int i = 0; i < maxActivateCount; ++i) {
                WaitingQueue waitingQueue = new WaitingQueue(null, "token" + i,
                    WaitingQueueStatus.ACTIVE, LocalDateTime.now().plusMinutes(1),
                    LocalDateTime.now(), null);

                waitingQueueList.add(waitingQueue);
            }

            waitingQueueJpaRepository.saveAll(waitingQueueList);

            // when
            WaitingQueueInfo waitingQueueInfo = waitingQueueFacade.generateWaitingQueueToken();

            // then
            assertThat(waitingQueueInfo.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);
            assertThat(waitingQueueInfo.getExpireAt()).isNull();
        }
    }

    @DisplayName("getWaitingQueueWithOrder() 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("토큰에 해당하는 waitingQueue가 없으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_WaitingQueueNotFound () {
            // given
            String token = "InvalidToken";

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingQueueWithOrder(token))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 대기상태가 아니라면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_StatusIsNotWaiting () {
            // given
            String token = "token";
            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .expireAt(LocalDateTime.now().plusMinutes(1))
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingQueueWithOrder(token))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_STATE_NOT_WAITING.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 대기상태라면 WaitingQueueWithOrder를 반환한다.")
        @Test
        void should_ReturnWaitingQueueWithOrderInfo_When_StatusIsWaiting() {
            // given
            String token = "token3";
            WaitingQueue activeWaitingQueue = WaitingQueue.builder()
                .token("token1")
                .status(WaitingQueueStatus.ACTIVE)
                .expireAt(LocalDateTime.now().plusMinutes(1))
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitedWaitingQueue1 = WaitingQueue.builder()
                .token("token2")
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitedWaitingQueue2 = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.saveAll(List.of(activeWaitingQueue, waitedWaitingQueue1,
                waitedWaitingQueue2));

            // when
            WaitingQueueWithOrderInfo waitingQueueWithOrderInfo =
                waitingQueueFacade.getWaitingQueueWithOrder(token);

            // then
            WaitingQueueInfo waitingQueueInfo = waitingQueueWithOrderInfo.getWaitingQueueInfo();
            assertThat(waitingQueueInfo.getToken()).isEqualTo(token);
            assertThat(waitingQueueInfo.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);

            Long order = waitingQueueWithOrderInfo.getOrder();
            assertThat(order).isEqualTo(2L);
        }
    }

    @DisplayName("validateWaitingQueueToken() 테스트")
    @Nested
    class ValidateWaitingQueueTokenTest {
        @DisplayName("토큰에 해당하는 waitingQueue가 없으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_WaitingQueueNotFound () {
            // given
            String token = "InvalidToken";
            LocalDateTime now = LocalDateTime.now();

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.validateWaitingQueueToken(token, now))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 만료되었으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_ActiveTokenIsExpired() {
            // given
            String token = "token";
            LocalDateTime now = LocalDateTime.now();

            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .expireAt(now.minusDays(1))
                .createdAt(now)
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.validateWaitingQueueToken(token, now))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰의 상태가 Active가 아니면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_StatusIsNotActive() {
            // given
            String token = "token";
            LocalDateTime now = LocalDateTime.now();

            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.WAITING)
                .createdAt(now)
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.validateWaitingQueueToken(token, now))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰의 상태가 Active이고 만료되지 않았으면 WaitingQueueException이 발생하지 않는다.")
        @Test
        void should_NotThrowException_When_TokenIsValid () {
            // given
            String token = "token";
            LocalDateTime now = LocalDateTime.now();

            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .expireAt(now.plusMinutes(1))
                .createdAt(now)
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            waitingQueueFacade.validateWaitingQueueToken(token, now);
        }
    }

    @DisplayName("activateOldestWaitedQueues() 테스트")
    @Nested
    class ActivateOldestWaitedQueuesTest {
        @DisplayName("최대 가용인원인 상태이면 아무일도 발생하지 않는다.")
        @Test
        void should_NothingHappen_When_ActiveCountIsMax() {
            // given
            int maxActivateCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;
            for(int i = 0; i < maxActivateCount; ++i) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.ACTIVE)
                    .expireAt(LocalDateTime.now().plusMinutes(1))
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueJpaRepository.save(waitingQueue);
            }

            WaitingQueue savedWaitedQueue = waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token("token")
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            waitingQueueFacade.activateOldestWaitedQueues();

            // then
            WaitingQueue waitedQueue = waitingQueueJpaRepository.findById(savedWaitedQueue.getId())
                .orElse(null);
            assertThat(waitedQueue).isNotNull();
            assertThat(waitedQueue.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);
        }

        @DisplayName("최대 가용인원보다 활성화된 사용자가 적은 상태이면 오래기다린 순서대로 대기열을 활성화한다.")
        @Test
        void should_ActivateOldestWaitedQueues_When_ActiveCountIsNotMax () {
            // given
            int maxActivateCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;
            for(int i = 0; i < maxActivateCount - 1; ++i) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.ACTIVE)
                    .expireAt(LocalDateTime.now().plusMinutes(1))
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueJpaRepository.save(waitingQueue);
            }

            WaitingQueue waitingQueueToActive = waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token("token50")
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

            WaitingQueue waitingQueueNotToActive = waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token("token51")
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            waitingQueueFacade.activateOldestWaitedQueues();

            // then
            WaitingQueue activatedQueue = waitingQueueJpaRepository.findById(waitingQueueToActive.getId())
                .orElse(null);
            assertThat(activatedQueue).isNotNull();
            assertThat(activatedQueue.getStatus()).isEqualTo(WaitingQueueStatus.ACTIVE);

            WaitingQueue notActivatedQueue = waitingQueueJpaRepository.findById(waitingQueueNotToActive.getId())
                .orElse(null);
            assertThat(notActivatedQueue).isNotNull();
            assertThat(notActivatedQueue.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);
        }
    }
}