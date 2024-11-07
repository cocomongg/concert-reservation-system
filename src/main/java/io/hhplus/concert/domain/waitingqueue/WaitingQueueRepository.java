package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import java.time.LocalDateTime;
import java.util.List;

public interface WaitingQueueRepository {

    WaitingQueueTokenInfo insertWaitingQueue(InsertWaitingQueue command);

    WaitingQueueTokenInfo getWaitingQueueToken(GetWaitingQueueCommonQuery query);

    Long getWaitingTokenOrder(GetWaitingQueueCommonQuery query);

    List<WaitingQueueTokenInfo> getOldestWaitingTokens(int limit);

    Long activateWaitingTokens(List<String> tokens);

    List<WaitingQueueTokenInfo> getActiveTokensToExpire(LocalDateTime now);

    Long expireActiveTokens(List<String> tokens);
}
