package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.TokenMeta;
import java.time.Duration;
import java.util.List;

public interface WaitingQueueRedisRepository {
    boolean insertWaitingQueue(InsertWaitingQueue command);

    Long getWaitingTokenOrder(String token);

    boolean isWaitingStatus(String token);

    boolean isActiveStatus(String token);

    TokenMeta getTokenMeta(String token);

    boolean isTokenActive(String token);

    List<String> popOldestWaitingTokens(long limit);

    Long activateWaitingTokens(String token, TokenMeta tokenMeta, Duration ttl);

    void expireToken(String token);
}
