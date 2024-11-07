package io.hhplus.concert.infra.redis.repository;

import io.hhplus.concert.domain.waitingqueue.WaitingQueueRedisRepository;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.TokenMeta;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Primary
@RequiredArgsConstructor
@Repository
public class WaitingQueueRedisRepositoryImpl implements WaitingQueueRedisRepository {
    private static final String WAITING_QUEUE_KEY = "waiting_queue";
    private static final String ACTIVE_QUEUE_KEY = "active_queue";

    private final RedisRepository redisRepository;

    @Override
    public boolean insertWaitingQueue(InsertWaitingQueue command) {
        LocalDateTime now = command.getNow();
        long epochMilli = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return redisRepository.addSortedSet(WAITING_QUEUE_KEY, command.getToken(), epochMilli);
    }

    @Override
    public boolean isWaitingStatus(String token) {
        Double score = redisRepository.getSortedSetScore(WAITING_QUEUE_KEY, token);
        return score != null;
    }

    @Override
    public boolean isActiveStatus(String token) {
        boolean inSet = redisRepository.isInSet(ACTIVE_QUEUE_KEY, token);
        TokenMeta tokenMeta = this.getTokenMeta(token);

        return inSet && tokenMeta != null;
    }

    @Override
    public TokenMeta getTokenMeta(String token) {
        return redisRepository.getStringValue(
            String.format("%s:%s", ACTIVE_QUEUE_KEY, token), TokenMeta.class);
    }

    @Override
    public Long getWaitingTokenOrder(String token) {
        Long rank = redisRepository.getSortedSetRank(WAITING_QUEUE_KEY, token);
        return rank == null ? 0 : rank + 1;
    }

    @Override
    public boolean isTokenActive(String token) {
        boolean inSet = redisRepository.isInSet(ACTIVE_QUEUE_KEY, token);
        if(!inSet) {
            return false;
        }

        TokenMeta tokenMeta = redisRepository.getStringValue(
            String.format("%s:%s", ACTIVE_QUEUE_KEY, token), TokenMeta.class);

        return tokenMeta != null;
    }

    @Override
    public List<String> popOldestWaitingTokens(long limit) {
        Set<TypedTuple<Object>> waitingTokens =
            redisRepository.popMinSortedSet(WAITING_QUEUE_KEY, limit);

        return waitingTokens.stream()
            .map(token -> (String) token.getValue())
            .toList();
    }

    @Override
    public Long activateWaitingTokens(String token, TokenMeta tokenMeta, Duration ttl) {

        redisRepository.setStringValue(String.format("%s:%s", ACTIVE_QUEUE_KEY, token), tokenMeta, ttl);
        return redisRepository.addSet(ACTIVE_QUEUE_KEY, token);
    }

    @Override
    public void expireToken(String token) {
        redisRepository.removeSet(ACTIVE_QUEUE_KEY, token);
        redisRepository.delete(String.format("%s:%s", ACTIVE_QUEUE_KEY, token));
    }
}
