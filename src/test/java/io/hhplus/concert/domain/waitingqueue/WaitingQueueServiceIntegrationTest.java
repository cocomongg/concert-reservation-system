package io.hhplus.concert.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.ActivateWaitingTokens;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
import io.hhplus.concert.infra.redis.repository.RedisRepository;
import io.hhplus.concert.domain.waitingqueue.model.TokenMeta;
import io.hhplus.concert.support.RedisCleanUp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WaitingQueueServiceIntegrationTest {

    @Autowired
    private WaitingQueueService waitingQueueService;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    public void teardown() {
        redisCleanUp.execute();
    }

    @DisplayName("insertWaitingQueue 테스트")
    @Nested
    class InsertWaitingQueueTest {
        @DisplayName("주어진 값을 통해 대기열에 토큰을 추가한다.")
        @Test
        void should_insertTokenToWaitingQueue_When_Input () {
            // given
            String token = "token";
            LocalDateTime now = LocalDateTime.now();
            InsertWaitingQueue command = new InsertWaitingQueue(token, now);

            // when
            WaitingQueueTokenInfo result = waitingQueueService.insertWaitingQueue(command);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);
        }
    }

    @DisplayName("getWaitingQueueToken 테스트")
    @Nested
    class GetWaitingQueueTokenTest {
        @DisplayName("대기 상태인 토큰의 값을 통해 조회 하면 해당 토큰의 정보를 반환한다.")
        @Test
        void should_getWaitingStatusToken_When_InputWaitingToken() {
            // given
            String token = "token";
            redisRepository.addSortedSet("waiting_queue", token, System.currentTimeMillis());
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            // when
            WaitingQueueTokenInfo result = waitingQueueService.getWaitingQueueToken(query);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.WAITING);
        }

        @DisplayName("활성 상태인 토큰의 값을 통해 조회 하면 해당 토큰의 정보를 반환한다.")
        @Test
        void should_getActiveStatusToken_When_InputActiveToken() {
            // given
            String token = "token1";
            TokenMeta tokenMeta = new TokenMeta(LocalDateTime.now());
            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token, tokenMeta, Duration.ofMinutes(10));

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            // when
            WaitingQueueTokenInfo result = waitingQueueService.getWaitingQueueToken(query);

            // then
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getStatus()).isEqualTo(WaitingQueueTokenStatus.ACTIVE);
            assertThat(result.getExpireAt()).isEqualTo(tokenMeta.getExpireAt());
        }

        @DisplayName("대기 상태도 활성 상태도 아닌 토큰의 값을 통해 조회 하면 예외를 발생한다.")
        @Test
        void should_throwException_When_InputInvalidToken() {
            // given
            String token = "tokenValue";
            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);

            // when & then
            assertThatThrownBy(() -> waitingQueueService.getWaitingQueueToken(query))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }
    }

    @DisplayName("getWaitingTokenOrder 테스트")
    @Nested
    class GetWaitingTokenOrderTest {

        @DisplayName("대기 상태의 토큰을 통해 대기열의 토큰 순서를 조회한다.")
        @Test
        void should_getWaitingTokenOrder_When_InputToken() {
            // given
            String waitingToken = "tokenValue";
            int existsTokenCount = 5;
            for (int i = 0; i < existsTokenCount; i++) {
                redisRepository.addSortedSet("waiting_queue", "token" + i, System.currentTimeMillis());
            }

            redisRepository.addSortedSet("waiting_queue", waitingToken, System.currentTimeMillis());

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(waitingToken);

            // when
            Long result = waitingQueueService.getWaitingTokenOrder(query);

            // then
            assertThat(result).isEqualTo(existsTokenCount + 1);
        }

        @DisplayName("대기 상태가 아닌 토큰을 통해 대기열의 토큰 순서를 조회하면 0을 반환한다.")
        @Test
        void should_returnZero_When_InputActiveToken() {
            // given
            String activeToken = "tokenValue";
            redisRepository.addSet("active_queue", activeToken);
            redisRepository.setStringValue("active_queue:" + activeToken,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(activeToken);

            // when
            Long result = waitingQueueService.getWaitingTokenOrder(query);

            // then
            assertThat(result).isEqualTo(0);
        }
    }

    @DisplayName("checkTokenActivate 테스트")
    @Nested
    class CheckTokenActivateTest {
        @DisplayName("활성화 상태가 아닌 토큰이면 예외가 발생한다.")
        @Test
        void should_throwException_When_InputWaitingToken() {
            // given
            String waitingToken = "tokenValue";
            redisRepository.addSortedSet("waiting_queue", waitingToken, System.currentTimeMillis());

            CheckTokenActivate query = new CheckTokenActivate(waitingToken, LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }

        @DisplayName("활성 대기열에 있지만, TokenMeta가 없다면 예외가 발생한다.")
        @Test
        void should_throwException_When_InputActiveTokenWithoutTokenMeta() {
            // given
            String activeToken = "tokenValue";
            redisRepository.addSet("active_queue", activeToken);

            CheckTokenActivate query = new CheckTokenActivate(activeToken, LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> waitingQueueService.checkTokenActivate(query))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE.getMessage());
        }
    }

    @DisplayName("activateToken 테스트")
    @Nested
    class ActivateTokenTest {
        @DisplayName("대기열에서 가장 오래된 토큰들중에 limit 수만큼 활성화한다.")
        @Test
        void should_activateOldestToken_When_Input() {
            // given
            int waitingTokenCount = 5;
            int activateCount = 3;
            for (int i = 0; i < waitingTokenCount; i++) {
                redisRepository.addSortedSet("waiting_queue", "token" + i, System.currentTimeMillis());
            }

            ActivateWaitingTokens command = new ActivateWaitingTokens(activateCount, 10, TimeUnit.MINUTES);

            // when
            long result = waitingQueueService.activateToken(command);

            // then
            assertThat(result).isEqualTo(activateCount);

            for(int i = 0; i < activateCount; i++) {
                boolean inSet = redisRepository.isInSet("active_queue", "token" + i);
                assertThat(inSet).isTrue();
            }
        }
    }

    @DisplayName("expireToken 테스트")
    @Nested
    class ExpireTokenTest {
        @DisplayName("활성 대기열에 있는 토큰을 만료시킨다.")
        @Test
        void should_expireToken_When_Input() {
            // given
            String activeToken = "tokenValue";
            redisRepository.addSet("active_queue", activeToken);
            redisRepository.setStringValue("active_queue:" + activeToken,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

            GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(activeToken);

            // when
            waitingQueueService.expireToken(query);

            // then
            boolean inSet = redisRepository.isInSet("active_queue", activeToken);
            assertThat(inSet).isFalse();

            TokenMeta tokenMeta = redisRepository.getStringValue("active_queue:" + activeToken,
                TokenMeta.class);
            assertThat(tokenMeta).isNull();
        }
    }
}