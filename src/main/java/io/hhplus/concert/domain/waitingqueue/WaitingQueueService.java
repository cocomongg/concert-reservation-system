package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public WaitingQueueTokenInfo insertWaitingQueue(InsertWaitingQueue command) {
        return waitingQueueRepository.insertWaitingQueue(command);
    }

    @Transactional(readOnly = true)
    public WaitingQueueTokenInfo getWaitingQueueToken(GetWaitingQueueCommonQuery query) {
        return waitingQueueRepository.getWaitingQueueToken(query);
    }

    @Transactional(readOnly = true)
    public Long getWaitingTokenOrder(GetWaitingQueueCommonQuery query) {
        return waitingQueueRepository.getWaitingTokenOrder(query);
    }

    public Long getRemainingWaitTimeSeconds(GetRemainingWaitTimeSeconds query) {
        long activateStep = query.getWaitingOrder() / query.getActivationBatchSize();
        int activationIntervalSeconds = query.getActivationIntervalSeconds();

        return activateStep == 0 ? activationIntervalSeconds : activateStep * activationIntervalSeconds;
    }

    @Transactional(readOnly = true)
    public void checkTokenActivate(CheckTokenActivate query) {
        WaitingQueueTokenInfo tokenInfo =
            waitingQueueRepository.getWaitingQueueToken(new GetWaitingQueueCommonQuery(query.getToken()));

        tokenInfo.checkActivated(query.getCurrentTime());
    }

    @Transactional
    public Long activateToken(int limit) {
        List<WaitingQueueTokenInfo> waitingTokens =
            waitingQueueRepository.getOldestWaitingTokens(limit);

        if(waitingTokens.isEmpty()) {
            return 0L;
        }

        List<String> tokensToActive = waitingTokens.stream()
            .map(WaitingQueueTokenInfo::getToken)
            .collect(Collectors.toList());

        return waitingQueueRepository.activateWaitingTokens(tokensToActive);
    }

    @Transactional
    public Long expireTokens(LocalDateTime currentTime) {
        List<WaitingQueueTokenInfo> activeTokens = waitingQueueRepository.getActiveTokensToExpire(currentTime);

        if(activeTokens.isEmpty()) {
            return 0L;
        }

        List<String> tokensToExpire = activeTokens.stream()
            .map(WaitingQueueTokenInfo::getToken)
            .collect(Collectors.toList());

        return waitingQueueRepository.expireActiveTokens(tokensToExpire);
    }

    @Transactional
    public void expireToken(GetWaitingQueueCommonQuery query) {
        waitingQueueRepository.expireActiveTokens(List.of(query.getToken()));
    }
}
