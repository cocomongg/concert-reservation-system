package io.hhplus.concert.application.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.app.waitingqueue.application.WaitingQueueFacade;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreErrorType.WaitingQueue;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.waitingqueue.domain.model.TokenMeta;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenInfo;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenStatus;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingTokenWithOrderInfo;
import io.hhplus.concert.app.waitingqueue.infra.redis.RedisRepository;
import io.hhplus.concert.support.RedisCleanUp;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private RedisRepository redisRepository;

    @Autowired
    private WaitingQueueFacade waitingQueueFacade;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    public void teardown() {
        redisCleanUp.execute();
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

            Long waitingQueue = redisRepository.getSortedSetRank("waiting_queue", result.getToken());
            assertThat(waitingQueue).isNotNull();
            assertThat(waitingQueue).isGreaterThanOrEqualTo(0L);
        }
    }

    @DisplayName("getWaitingQueueWithOrder() 테스트")
    @Nested
    class GetWaitingQueueWithOrderTest {
        @DisplayName("토큰 값에 해당하는 토큰이 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_WaitingQueueNotFound () {
            // given
            String token = "InvalidToken";

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.getWaitingTokenWithOrderInfo(token))
                .isInstanceOf(CoreException.class)
                .hasMessage(WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰에 해당하는 waitingQueue가 대기상태라면 대기 시간에 따른 순서와 대기시간을 반환한다.")
        @Test
        void should_ReturnWaitingQueueWithOrderInfo_When_StatusIsWaiting() {
            // given
            String token = "tokenValue";
            int existsTokenCount = 5;
            for (int i = 0; i < existsTokenCount; i++) {
                redisRepository.addSortedSet("waiting_queue", "token" + i, System.currentTimeMillis());
            }

            redisRepository.addSortedSet("waiting_queue", token, System.currentTimeMillis());

            // when
            WaitingTokenWithOrderInfo waitingQueueWithOrderInfo =
                waitingQueueFacade.getWaitingTokenWithOrderInfo(token);

            // then
            WaitingQueueTokenInfo tokenInfo = waitingQueueWithOrderInfo.getTokenInfo();
            assertThat(tokenInfo.getToken()).isEqualTo(token);
            assertThat(tokenInfo.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);

            Long order = waitingQueueWithOrderInfo.getOrder();
            assertThat(order).isEqualTo(existsTokenCount + 1);

            Long remainingWaitTime = waitingQueueWithOrderInfo.getRemainingWaitTimeSeconds();
            assertThat(remainingWaitTime).isEqualTo(ServicePolicy.WAITING_QUEUE_ACTIVATE_INTERVAL);
        }
    }

    @DisplayName("checkTokenActivate() 테스트")
    @Nested
    class CheckTokenActivateTest {
        @DisplayName("대기 상태인 토큰이면 예외가 발생한다.")
        @Test
        void should_throwException_When_InputWaitingToken () {
            // given
            String waitingToken = "tokenValue";
            redisRepository.addSortedSet("waiting_queue", waitingToken, System.currentTimeMillis());

            // when, then
            assertThatThrownBy(() -> waitingQueueFacade.checkTokenActivate(waitingToken, LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("토큰의 상태가 Active이고 만료되지 않았으면 CoreException이 발생하지 않는다.")
        @Test
        void should_NotThrowException_When_TokenIsValid () {
            // given
            String activeToken = "tokenValue";
            redisRepository.addSet("active_queue", activeToken);
            redisRepository.setStringValue("active_queue:" + activeToken,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

            // when, then
            assertThatCode(() -> waitingQueueFacade.checkTokenActivate(activeToken, LocalDateTime.now()))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("activateWaitingToken() 테스트")
    @Nested
    class ActivateWaitingTokenTest {
        @DisplayName("정해진 수만큼 오래 기다린 순서대로 대기 토큰을 활성화한다.")
        @Test
        void should_ActivateWaitingToken_When_InputLimit () {
            // given
            int waitingTokenCount = 10;
            for(int i = 0; i < waitingTokenCount; ++i) {
                redisRepository.addSortedSet("waiting_queue", "token"+i, System.currentTimeMillis());
            }

            // when
            Long result = waitingQueueFacade.activateWaitingToken();

            // then
            assertThat(result).isEqualTo(waitingTokenCount);

            for(int i = 0; i < waitingTokenCount; ++i) {
                boolean inSet = redisRepository.isInSet("active_queue", "token" + i);
                assertThat(inSet).isTrue();

                TokenMeta stringValue = redisRepository.getStringValue("active_queue:token" + i,
                    TokenMeta.class);
                assertThat(stringValue).isNotNull();
            }
        }
    }
}