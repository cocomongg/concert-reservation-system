package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
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
class WaitingQueueServiceIntegrationTest {

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private WaitingQueueService waitingQueueService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("insertWaitingQueue 테스트")
    @Nested
    class InsertWaitingQueueTest {
        @DisplayName("대기열 토큰의 활성화 상태 개수가 최대 활성화 상태 개수보다 크거나 같으면 대기 상태인 대기열 토큰을 생성한다.")
        @Test
        void should_CreateWaitingQueue_When_ActiveCountIsGreaterThanOrEqualToMaxActiveCount() {
            // given
            String token = "token";
            int maxCount = 3;
            LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
            InsertWaitingQueue command = new InsertWaitingQueue(token, expireAt);

            WaitingQueue waitingQueue1 = WaitingQueue.builder()
                .token("token1")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitingQueue2 = WaitingQueue.builder()
                .token("token2")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitingQueue3 = WaitingQueue.builder()
                .token("token3")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.saveAll(List.of(waitingQueue1, waitingQueue2, waitingQueue3));

            // when
            WaitingQueueTokenInfo result = waitingQueueService.insertWaitingQueue(command);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);
            assertThat(result.getExpireAt()).isNull();
        }
    }

    @DisplayName("getWaitingToken 테스트")
    @Nested
    class GetWaitingTokenTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_NotFound() {
            // given
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueToken(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 대기 상태가 아니라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_NotWaitingStatus() {
            // given
            WaitingQueue givenWaitingQueue = waitingQueueJpaRepository.save(
                WaitingQueue.builder()
                    .token("token")
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .build()
            );

            GetWaitingQueueCommonQuery query =
                new GetWaitingQueueCommonQuery(givenWaitingQueue.getToken());

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueToken(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_STATE_NOT_WAITING.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 대기 상태라면 해당하는 WaitingQueueTokenInfo가 반환된다.")
        @Test
        void should_ReturnWaitingQueueTokenInfo_When_WaitingStatus() {
            // given
            WaitingQueue givenWaitingQueue = waitingQueueJpaRepository.save(
                WaitingQueue.builder()
                    .token("token")
                    .status(WaitingQueueTokenStatus.WAITING)
                    .build()
            );

            GetWaitingQueueCommonQuery query =
                new GetWaitingQueueCommonQuery(givenWaitingQueue.getToken());

            // when
            WaitingQueueTokenInfo result = waitingQueueService.getWaitingQueueToken(query);

            // then
            assertThat(result.getToken()).isEqualTo(givenWaitingQueue.getToken());
            assertThat(result.getStatus()).isEqualTo(givenWaitingQueue.getStatus());
        }
    }

    @DisplayName("getWaitingTokenOrder 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_NotFound () {
            // given
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingTokenOrder(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue의 대기 순서를 반환한다.")
        @Test
        void should_ReturnWaitingQueueWithOrder_When_Found () {
            // given
            List<WaitingQueue> waitingQueueList = new ArrayList<>();
            for(int i = 0; i < 10; ++i) {
                waitingQueueList.add(this.createWaitingQueue("token" + i, WaitingQueueTokenStatus.ACTIVE));
            }
            for(int i = 0; i < 10; ++i) {
                waitingQueueList.add(this.createWaitingQueue("token" + i, WaitingQueueTokenStatus.WAITING));
            }
            waitingQueueJpaRepository.saveAll(waitingQueueList);

            WaitingQueue givenWaitingQueue = waitingQueueJpaRepository.save(
                this.createWaitingQueue("token", WaitingQueueTokenStatus.WAITING)
            );

            GetWaitingQueueCommonQuery query =
                new GetWaitingQueueCommonQuery(givenWaitingQueue.getToken());

            // when
            Long result = waitingQueueService.getWaitingTokenOrder(query);

            // then
            assertThat(result).isEqualTo(11L);
        }

        private WaitingQueue createWaitingQueue(String token, WaitingQueueTokenStatus status) {
            return WaitingQueue.builder()
                .token(token)
                .status(status)
                .expireAt(LocalDateTime.now())
                .build();
        }
    }

    @DisplayName("checkTokenActivate 테스트")
    @Nested
    class CheckTokenActivateTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_NotFound() {
            // given
            CheckTokenActivate query = new CheckTokenActivate("token", LocalDateTime.now());

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 활성화 상태가 아니라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_StatusIsNotActive() {
            // given
            String token = "tokenValue";
            CheckTokenActivate query = new CheckTokenActivate(token, LocalDateTime.now());
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueTokenStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 만료되었으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_Expired() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueTokenStatus.ACTIVE,
                currentTime.minusDays(1), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 활성화 상태이고 만료되지 않았으면 CoreException이 발생하지 않는다.")
        @Test
        void should_NotThrowCoreException_When_ActiveAndNotExpired() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueTokenStatus.ACTIVE,
                currentTime.plusDays(1), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatCode(() -> waitingQueueService.checkTokenActivate(query))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("activateToken 테스트")
    @Nested
    class ActivateTokenTest {
        @DisplayName("활성화 대상 개수가 0이면 아무일도 일어나지 않는다.")
        @Test
        void should_Nothing_When_CountTOActivateIsZero() {
            // given
            List<WaitingQueue> waitingQueueList = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            for (int i = 5; i < 10; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }
            
            waitingQueueJpaRepository.saveAll(waitingQueueList);
            int countToActivate = 0;
            
            // when
            waitingQueueService.activateToken(countToActivate);
        
            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int waitingCount = 0;
            for(WaitingQueue queue: waitingQueues) {
                if(WaitingQueueTokenStatus.WAITING.equals(queue.getStatus())) {
                    waitingCount++;
                } else {
                    activeCount++;
                }
            }
            
            assertThat(activeCount).isEqualTo(5);
            assertThat(waitingCount).isEqualTo(5);
        }
        
        @DisplayName("대기중인 waitingQueue가 없으면 activate업데이트가 일어나지 않고 0이 반환된다.")
        @Test
        void should_NothingActivate_When_WaitingCountIsZero() {
            // given
            int limit = 100;
            int remainWaitingQueueCount = 10;
            for(int i = 0; i < remainWaitingQueueCount; ++i) {
                waitingQueueJpaRepository.save(WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build());
            }

            // when
            Long result = waitingQueueService.activateToken(limit);

            // then
            assertThat(result).isEqualTo(0L);
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();

            int activeCount = 0;
            for(WaitingQueue waitingQueue: waitingQueues) {
                if(waitingQueue.getStatus().equals(WaitingQueueTokenStatus.ACTIVE)) {
                    activeCount++;
                }
            }
            assertThat(activeCount).isEqualTo(remainWaitingQueueCount);
        }

        @DisplayName("대기중인 WaitingQueue가 있을 때 오래 기다린 waiting의 상태가 active로 변경된다.")
        @Test
        void should_ActivateToken_When_WaitingExist() {
            // given
            int givenWaitingCount = 5;
            int givenActiveCount = 5;
            List<WaitingQueue> waitingQueueList = new ArrayList<>();

            for (int i = 0; i < givenWaitingCount; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            for (int i = 5; i < 5 + givenActiveCount; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            waitingQueueJpaRepository.saveAll(waitingQueueList);
            int countToActivate = 3;

            // when
            waitingQueueService.activateToken(countToActivate);

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int waitingCount = 0;
            for(WaitingQueue queue: waitingQueues) {
                if(WaitingQueueTokenStatus.WAITING.equals(queue.getStatus())) {
                    waitingCount++;
                } else {
                    activeCount++;
                }
            }

            assertThat(activeCount).isEqualTo(givenActiveCount + countToActivate);
            assertThat(waitingCount).isEqualTo(givenWaitingCount - countToActivate);
        }
    }

    @DisplayName("expireTokens 테스트")
    @Nested
    class ExpireTokensTest {
        @DisplayName("만료 대상 토큰이 없으면 아무일도 일어나지 않는다.")
        @Test
        void should_Nothing_When_NothingToExpire() {
            // given
            List<WaitingQueue> waitingQueueList = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (int i = 0; i < 5; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .expireAt(now.plusDays(1))
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            for (int i = 5; i < 10; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            waitingQueueJpaRepository.saveAll(waitingQueueList);

            // when
            waitingQueueService.expireTokens(now);

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int waitingCount = 0;
            for (WaitingQueue queue : waitingQueues) {
                if (WaitingQueueTokenStatus.WAITING.equals(queue.getStatus())) {
                    waitingCount++;
                } else {
                    activeCount++;
                }
            }

            assertThat(activeCount).isEqualTo(5);
            assertThat(waitingCount).isEqualTo(5);
        }

        @DisplayName("만료 대상 토큰이 있으면 토큰의 상태가 expired로 변경된다.")
        @Test
        void should_ExpireTokens_When_Expired() {
            // given
            List<WaitingQueue> waitingQueueList = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueTokenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .expireAt(LocalDateTime.now().minusDays(1))
                    .build();

                waitingQueueList.add(waitingQueue);
            }
            waitingQueueJpaRepository.saveAll(waitingQueueList);

            // when
            waitingQueueService.expireTokens(LocalDateTime.now());

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int expiredCount = 0;
            for (WaitingQueue queue : waitingQueues) {
                if (WaitingQueueTokenStatus.ACTIVE.equals(queue.getStatus())) {
                    activeCount++;
                } else {
                    expiredCount++;
                }
            }

            assertThat(activeCount).isEqualTo(0);
            assertThat(expiredCount).isEqualTo(5);
        }
    }

    @DisplayName("expireToken 테스트")
    @Nested
    class ExpireTokenTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 아무일도 일어나지 않는다.")
        @Test
        void should_Nothing_When_NotFound() {
            // given
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when
            waitingQueueService.expireToken(query);

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int expireCount = 0;
            for (WaitingQueue queue : waitingQueues) {
                if (WaitingQueueTokenStatus.EXPIRED.equals(queue.getStatus())) {
                    activeCount++;
                } else {
                    activeCount++;
                }
            }

            assertThat(activeCount).isEqualTo(0);
            assertThat(expireCount).isEqualTo(0);
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 있으면 해당 토큰의 상태가 expired로 변경된다.")
        @Test
        void should_ExpireToken_When_Found() {
            // given
            WaitingQueue waitingQueue = WaitingQueue.builder()
                .token("token")
                .status(WaitingQueueTokenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.save(waitingQueue);

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when
            waitingQueueService.expireToken(query);

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int expiredCount = 0;
            for (WaitingQueue queue : waitingQueues) {
                if (WaitingQueueTokenStatus.ACTIVE.equals(queue.getStatus())) {
                    activeCount++;
                } else {
                    expiredCount++;
                }
            }

            assertThat(activeCount).isEqualTo(0);
            assertThat(expiredCount).isEqualTo(1);
        }
    }
}