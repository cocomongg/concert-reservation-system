package io.hhplus.concert.app.waitingqueue.domain.repository;

import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.app.waitingqueue.domain.model.TokenMeta;
import java.time.Duration;
import java.util.List;

public interface WaitingQueueRepository {
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
