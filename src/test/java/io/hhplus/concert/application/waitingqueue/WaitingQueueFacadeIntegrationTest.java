package io.hhplus.concert.application.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
import io.hhplus.concert.domain.waitingqueue.model.WaitingTokenWithOrderInfo;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @DisplayName("issueWaitingToken() 테스트")
    @Nested
    class IssueWaitingTokenTest {
        @DisplayName("대기상태인 WaitingToken을 생성한다.")
        @Test
        void should_IssueWaitingToken () {
            // when
            WaitingQueueTokenInfo result = waitingQueueFacade.issueWaitingToken();

            // then
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);

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
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingTokenWithOrderInfo(token))
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
                .status(WaitingQueueTokenStatus.ACTIVE)
                .expireAt(LocalDateTime.now().plusMinutes(1))
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingTokenWithOrderInfo(token))
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
                .status(WaitingQueueTokenStatus.ACTIVE)
                .expireAt(LocalDateTime.now().plusMinutes(1))
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitedWaitingQueue1 = WaitingQueue.builder()
                .token("token2")
                .status(WaitingQueueTokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitedWaitingQueue2 = WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueTokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.saveAll(List.of(activeWaitingQueue, waitedWaitingQueue1,
                waitedWaitingQueue2));

            // when
            WaitingTokenWithOrderInfo waitingQueueWithOrderInfo =
                waitingQueueFacade.getWaitingTokenWithOrderInfo(token);

            // then
            WaitingQueueTokenInfo tokenInfo = waitingQueueWithOrderInfo.getTokenInfo();
            assertThat(tokenInfo.getToken()).isEqualTo(token);
            assertThat(tokenInfo.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);

            Long order = waitingQueueWithOrderInfo.getOrder();
            assertThat(order).isEqualTo(2L);

            Long remainingWaitTime = waitingQueueWithOrderInfo.getRemainingWaitTimeSeconds();
            assertThat(remainingWaitTime).isEqualTo(ServicePolicy.WAITING_QUEUE_ACTIVATE_INTERVAL);
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
                .status(WaitingQueueTokenStatus.ACTIVE)
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
                .status(WaitingQueueTokenStatus.WAITING)
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
                .status(WaitingQueueTokenStatus.ACTIVE)
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
        @DisplayName("정해진 수만큼 오래 기다린 순서대로 대기 토큰을 활성화한다.")
        @Test
        void should_ActivateWaitingToken_When_InputLimit () {
            // given
            int maxActivateCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;
            List<WaitingQueue> list = new ArrayList<>();
            for(int i = 0; i < maxActivateCount; ++i) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                list.add(waitingQueue);
            }
            waitingQueueJpaRepository.saveAll(list);

            String notActivatedToken = "notActivatedToken";
            WaitingQueue waitingQueueNotToActive = waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(notActivatedToken)
                .status(WaitingQueueTokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            waitingQueueFacade.activateWaitingToken();

            // then
            WaitingQueue activatedQueue = waitingQueueJpaRepository.findById(waitingQueueNotToActive.getId())
                .orElse(null);
            assertThat(activatedQueue).isNotNull();
            assertThat(activatedQueue.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);
        }
    }

    @DisplayName("expireWaitingQueues() 테스트")
    @Nested
    class ExpireWaitingQueuesTest {
        @DisplayName("만료시간이 지난 활성 상태인 토큰을 만료한다.")
        @Test
        void should_ExpireWaitingQueues_When_ExpireTimePassed() {
            // given
            LocalDateTime now = LocalDateTime.now();
            WaitingQueue waitingQueue1 = WaitingQueue.builder()
                .token("token1")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .expireAt(now.minusMinutes(1))
                .createdAt(now)
                .build();

            WaitingQueue waitingQueue2 = WaitingQueue.builder()
                .token("token2")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .expireAt(now.plusMinutes(1))
                .createdAt(now)
                .build();

            waitingQueueJpaRepository.saveAll(List.of(waitingQueue1, waitingQueue2));

            // when
            waitingQueueFacade.expireWaitingQueues(now);

            // then
            WaitingQueue expiredQueue1 = waitingQueueJpaRepository.findById(waitingQueue1.getId())
                .orElse(null);
            assertThat(expiredQueue1).isNotNull();
            assertThat(expiredQueue1.getStatus()).isEqualTo(WaitingQueueTokenStatus.EXPIRED);

            WaitingQueue expiredQueue2 = waitingQueueJpaRepository.findById(waitingQueue2.getId())
                .orElse(null);
            assertThat(expiredQueue2).isNotNull();
            assertThat(expiredQueue2.getStatus()).isEqualTo(WaitingQueueTokenStatus.ACTIVE);
        }
    }
}