package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueErrorCode;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
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

@SpringBootTest
class WaitingQueueServiceIntegrationTest {

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private WaitingQueueService waitingQueueService;

    @AfterEach
    public void teardown() {
        waitingQueueJpaRepository.deleteAllInBatch();
    }

    @DisplayName("createWaitingQueue 테스트")
    @Nested
    class CreateWaitingQueueTest {
        @DisplayName("입력된 값들을 통해 waitingQueue객체를 반환한다.")
        @Test
        void should_ReturnWaitingQueue_When_CommandGiven() {
            // given
            CreateWaitingQueue command = new CreateWaitingQueue("token",
                WaitingQueueStatus.WAITING, LocalDateTime.now());

            // when
            WaitingQueue result = waitingQueueService.createWaitingQueue(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(command.getToken());
            assertThat(result.getStatus()).isEqualTo(command.getStatus());
            assertThat(result.getExpireAt()).isEqualTo(command.getExpireAt());
        }

        @DisplayName("입력된 값들을 통해 waitingQueue객체를 저장한다.")
        @Test
        void should_SaveWaitingQueue_When_CommandGiven() {
            // given
            CreateWaitingQueue command = new CreateWaitingQueue("token",
                WaitingQueueStatus.WAITING, LocalDateTime.now());

            // when
            WaitingQueue waitingQueue = waitingQueueService.createWaitingQueue(command);
            Optional<WaitingQueue> savedWaitingQueueOptional =
                waitingQueueJpaRepository.findById(waitingQueue.getId());

            // then
            assertThat(savedWaitingQueueOptional).isPresent();

            WaitingQueue savedWaitingQueue = savedWaitingQueueOptional.get();
            assertThat(savedWaitingQueue.getToken()).isEqualTo(command.getToken());
            assertThat(savedWaitingQueue.getStatus()).isEqualTo(command.getStatus());
            assertThat(savedWaitingQueue.getExpireAt()).isEqualTo(command.getExpireAt());
        }
    }

    @DisplayName("getWaitingQueue 테스트")
    @Nested
    class GetWaitingQueueTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_NotFound() {
            // given
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueue(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue를 반환한다.")
        @Test
        void should_ReturnWaitingQueueException_When_Found() {
            // given
            String token = "token";
            CreateWaitingQueue command = new CreateWaitingQueue(token,
                WaitingQueueStatus.WAITING, LocalDateTime.now());
            WaitingQueue givenWaitingQueue = new WaitingQueue(command);
            waitingQueueJpaRepository.save(givenWaitingQueue);

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            // when
            WaitingQueue result = waitingQueueService.getWaitingQueue(query);

            // then
            assertThat(result.getToken()).isEqualTo(givenWaitingQueue.getToken());
            assertThat(result.getStatus()).isEqualTo(givenWaitingQueue.getStatus());
            assertThat(result.getExpireAt()).isEqualTo(givenWaitingQueue.getExpireAt());
        }
    }

    @DisplayName("getWaitingQueueWithOrder 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_NotFound () {
            // given
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery("token");

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueWithOrder(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 waitingQueue가 대기 상태가 아니라면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_StatusIsNotWaiting() {
            // given
            String token = "tokenValue";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
            WaitingQueue waitingQueue = new WaitingQueue(1L, token, WaitingQueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueWithOrder(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_STATE_NOT_WAITING.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 있으면 순서와 함께 WaitingQueueWithOrder가 반환된다.")
        @Test
        void should_ReturnWaitingQueueWithOrder_When_Found () {
            // given
            List<WaitingQueue> waitingQueueList = new ArrayList<>();
            for(int i = 0; i < 10; ++i) {
                waitingQueueList.add(this.createWaitingQueue("token" + i, WaitingQueueStatus.ACTIVE));
            }
            for(int i = 0; i < 10; ++i) {
                waitingQueueList.add(this.createWaitingQueue("token" + i, WaitingQueueStatus.WAITING));
            }
            List<WaitingQueue> waitingQueueList1 = waitingQueueJpaRepository.saveAll(
                waitingQueueList);

            WaitingQueue givenWaitingQueue = waitingQueueJpaRepository.save(
                this.createWaitingQueue("token", WaitingQueueStatus.WAITING)
            );

            GetWaitingQueueCommonQuery query =
                new GetWaitingQueueCommonQuery(givenWaitingQueue.getToken());

            // when
            WaitingQueueWithOrder result = waitingQueueService.getWaitingQueueWithOrder(query);

            // then
            WaitingQueue waitingQueue = result.getWaitingQueue();
            assertThat(waitingQueue.getToken()).isEqualTo(givenWaitingQueue.getToken());
            assertThat(waitingQueue.getStatus()).isEqualTo(givenWaitingQueue.getStatus());

            Long waitingOrder = result.getWaitingOrder();
            assertThat(waitingOrder).isEqualTo(11);
        }

        private WaitingQueue createWaitingQueue(String token, WaitingQueueStatus status) {
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
        @DisplayName("입력된 값에 해당하는 WaitingQueue가 없으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_NotFound() {
            // given
            CheckTokenActivate query = new CheckTokenActivate("token", LocalDateTime.now());

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.WAITING_QUEUE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 활성화 상태가 아니라면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_StatusIsNotActive() {
            // given
            String token = "tokenValue";
            CheckTokenActivate query = new CheckTokenActivate(token, LocalDateTime.now());
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 만료되었으면 WaitingQueueException이 발생한다.")
        @Test
        void should_ThrowWaitingQueueException_When_Expired() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueStatus.ACTIVE,
                currentTime.minusDays(1), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(WaitingQueueException.class)
                .hasMessage(WaitingQueueErrorCode.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("입력된 값에 해당하는 WaitingQueue가 활성화 상태이고 만료되지 않았으면 WaitingQueueException이 발생하지 않는다.")
        @Test
        void should_NotThrowWaitingQueueException_When_ActiveAndNotExpired() {
            // given
            String token = "tokenValue";
            LocalDateTime currentTime = LocalDateTime.now();
            CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
            WaitingQueue waitingQueue = new WaitingQueue(null, token, WaitingQueueStatus.ACTIVE,
                currentTime.plusDays(1), LocalDateTime.now(), null);

            waitingQueueJpaRepository.save(waitingQueue);

            // when, then
            assertThatCode(() -> waitingQueueService.checkTokenActivate(query))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("getActiveCount 테스트")
    @Nested
    class GetActiveCountTest {
        @DisplayName("대기열의 활성화 상태 개수를 구한다.")
        @Test
        void should_ReturnActiveCount() {
            // given
            WaitingQueue waitingQueue1 = WaitingQueue.builder()
                .token("token1")
                .status(WaitingQueueStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitingQueue2 = WaitingQueue.builder()
                .token("token2")
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            WaitingQueue waitingQueue3 = WaitingQueue.builder()
                .token("token3")
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

            waitingQueueJpaRepository.saveAll(List.of(waitingQueue1, waitingQueue2, waitingQueue3));

            // when
            Long activeCount = waitingQueueService.getActiveCount();

            // then
            assertThat(activeCount).isEqualTo(2L);
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
                    .status(WaitingQueueStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            for (int i = 5; i < 10; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }
            
            waitingQueueJpaRepository.saveAll(waitingQueueList);
            int countToActivate = 0;
            
            // when
            waitingQueueService.activateToken(countToActivate + 5);
        
            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int waitingCount = 0;
            for(WaitingQueue queue: waitingQueues) {
                if(WaitingQueueStatus.WAITING.equals(queue.getStatus())) {
                    waitingCount++;
                } else {
                    activeCount++;
                }
            }
            
            assertThat(activeCount).isEqualTo(5);
            assertThat(waitingCount).isEqualTo(5);
        }
        
        @DisplayName("대기중인 waitingQueue가 없으면 activate업데이트가 일어나지 않는다.")
        @Test
        void should_NothingActivate_When_WaitingCountIsZero() {
            // given
            int givenActiveCount = 5;
            List<WaitingQueue> waitingQueueList = new ArrayList<>();

            for (int i = 0; i < givenActiveCount; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }
            
            waitingQueueJpaRepository.saveAll(waitingQueueList);
            
            int countToActivate = 5;
            
            // when
            waitingQueueService.activateToken(countToActivate + givenActiveCount);
        
            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            for(WaitingQueue queue: waitingQueues) {
                if(WaitingQueueStatus.ACTIVE.equals(queue.getStatus())) {
                    activeCount++;
                }
            }

            assertThat(activeCount).isEqualTo(givenActiveCount);
        }

        @DisplayName("활성화 대상 개수가 1 이상이고, 대기중인 WaitingQueue가 있을 때 가장 오래된 waiting의 상태가 active로 변경된다.")
        @Test
        void should_ActivateOldestWaiting_When_WaitingExist() {
            // given
            int givenWaitingCount = 5;
            int givenActiveCount = 5;
            List<WaitingQueue> waitingQueueList = new ArrayList<>();

            for (int i = 0; i < givenWaitingCount; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.WAITING)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            for (int i = 5; i < 5 + givenActiveCount; i++) {
                WaitingQueue waitingQueue = WaitingQueue.builder()
                    .token("token" + i)
                    .status(WaitingQueueStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

                waitingQueueList.add(waitingQueue);
            }

            waitingQueueJpaRepository.saveAll(waitingQueueList);
            int countToActivate = 3;

            // when
            waitingQueueService.activateToken(givenActiveCount + countToActivate);

            // then
            List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findAll();
            int activeCount = 0;
            int waitingCount = 0;
            for(WaitingQueue queue: waitingQueues) {
                if(WaitingQueueStatus.WAITING.equals(queue.getStatus())) {
                    waitingCount++;
                } else {
                    activeCount++;
                }
            }

            assertThat(activeCount).isEqualTo(givenActiveCount + countToActivate);
            assertThat(waitingCount).isEqualTo(givenWaitingCount - countToActivate);
        }
    }
}