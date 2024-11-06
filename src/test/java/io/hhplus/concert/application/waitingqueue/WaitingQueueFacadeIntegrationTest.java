package io.hhplus.concert.application.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class WaitingQueueFacadeIntegrationTest {

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private WaitingQueueFacade waitingQueueFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("generateWaitingQueueToken() 테스트")
    @Nested
    class GenerateWaitingQueueToken {
        @DisplayName("입력된 토큰값에 대해 대기상태인 WaitingQueue를 생성한다.")
        @Test
        void should_CreateWaitingStatusWaitingQueue_When_InputToken () {
            // when
            WaitingQueueInfo result = waitingQueueFacade.issueWaitingToken();

            // then
            assertThat(result.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);

            WaitingQueue waitingQueue =
                waitingQueueJpaRepository.findByToken(result.getToken()).orElse(null);
            assertThat(waitingQueue).isNotNull();
            assertThat(result.getToken()).isEqualTo(waitingQueue.getToken());
        }
    }

    @DisplayName("getWaitingQueueWithOrder() 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("토큰에 해당하는 waitingQueue가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueNotFound () {
            // given
            String token = "InvalidToken";

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingQueueWithOrder(token))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 대기상태가 아니라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotWaiting () {
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
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_STATE_NOT_WAITING.getMessage());
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

    @DisplayName("checkTokenActivate() 테스트")
    @Nested
    class CheckTokenActivateTest {
        @DisplayName("토큰에 해당하는 waitingQueue가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueNotFound () {
            // given
            String token = "InvalidToken";
            LocalDateTime now = LocalDateTime.now();

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.checkTokenActivate(token, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 만료되었으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_ActiveTokenIsExpired() {
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
            assertThatThrownBy(() -> waitingQueueFacade.checkTokenActivate(token, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰의 상태가 Active가 아니면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotActive() {
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
            assertThatThrownBy(() -> waitingQueueFacade.checkTokenActivate(token, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰의 상태가 Active이고 만료되지 않았으면 CoreException이 발생하지 않는다.")
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
            waitingQueueFacade.checkTokenActivate(token, now);
        }
    }

    @DisplayName("activateWaitingToken() 테스트")
    @Nested
    class ActivateWaitingTokenTest {
        @DisplayName("제한된 수만큼 오래기다린 순서대로 대기열을 활성화한다.")
        @Test
        void should_ActivateWaitingToken_When_InputLimit () {
            // given
            int maxActivateCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;
            List<WaitingQueue> list = new ArrayList<>();
            for(int i = 0; i < maxActivateCount; ++i) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                list.add(waitingQueue);
            }
            waitingQueueJpaRepository.saveAll(list);

            String notActivatedToken = "notActivatedToken";
            WaitingQueue waitingQueueNotToActive = waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(notActivatedToken)
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            waitingQueueFacade.activateWaitingToken();

            // then
            WaitingQueue activatedQueue = waitingQueueJpaRepository.findById(waitingQueueNotToActive.getId())
                .orElse(null);
            assertThat(activatedQueue).isNotNull();
            assertThat(activatedQueue.getStatus()).isEqualTo(WaitingQueueStatus.WAITING);
        }
    }
}