package io.hhplus.concert.application.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.concert.application.waitingqueue.dto.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
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
}