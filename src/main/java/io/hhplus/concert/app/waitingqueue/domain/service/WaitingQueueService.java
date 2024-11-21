package io.hhplus.concert.app.waitingqueue.domain.service;

import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueCommand.ActivateWaitingTokens;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenInfo;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenStatus;
import io.hhplus.concert.app.waitingqueue.domain.model.TokenMeta;
import io.hhplus.concert.app.waitingqueue.domain.repository.WaitingQueueRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {
    private final WaitingQueueRepository waitingQueueRepository;

    public WaitingQueueTokenInfo insertWaitingQueue(InsertWaitingQueue command) {
        waitingQueueRepository.insertWaitingQueue(command);
        return new WaitingQueueTokenInfo(command.getToken(), WaitingQueueTokenStatus.WAITING, null);
    }

    public WaitingQueueTokenInfo getWaitingQueueToken(GetWaitingQueueCommonQuery query) {
        String token = query.getToken();
        boolean waitingStatus = waitingQueueRepository.isWaitingStatus(token);
        if(waitingStatus) {
            return new WaitingQueueTokenInfo(token, WaitingQueueTokenStatus.WAITING, null);
        }

        boolean activeStatus = waitingQueueRepository.isActiveStatus(token);
        if(activeStatus) {
            TokenMeta tokenMeta = waitingQueueRepository.getTokenMeta(token);
            return new WaitingQueueTokenInfo(token, WaitingQueueTokenStatus.ACTIVE, tokenMeta.getExpireAt());
        }

        throw new CoreException(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE);
    }

    public Long getWaitingTokenOrder(GetWaitingQueueCommonQuery query) {
        return waitingQueueRepository.getWaitingTokenOrder(query.getToken());
    }

    public Long getRemainingWaitTimeSeconds(GetRemainingWaitTimeSeconds query) {
        long activateStep = query.getWaitingOrder() / query.getActivationBatchSize();
        int activationIntervalSeconds = query.getActivationIntervalSeconds();

        return activateStep == 0 ? activationIntervalSeconds : activateStep * activationIntervalSeconds;
    }

    public void checkTokenActivate(CheckTokenActivate query) {
        boolean activeStatus = waitingQueueRepository.isActiveStatus(query.getToken());
        if(!activeStatus) {
            throw new CoreException(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE);
        }
    }

    public long activateToken(ActivateWaitingTokens command) {
        List<String> tokens = waitingQueueRepository.popOldestWaitingTokens(command.getLimit());

        long activateCount = 0;
        Duration duration = Duration.of(command.getExpireTime(), command.getTimeUnit().toChronoUnit());
        for(String token : tokens) {
            TokenMeta tokenMeta = new TokenMeta(LocalDateTime.now().plus(duration));
            activateCount += waitingQueueRepository.activateWaitingTokens(token, tokenMeta, duration);
        }

        return activateCount;
    }

    public void expireToken(GetWaitingQueueCommonQuery query) {
        waitingQueueRepository.expireToken(query.getToken());
    }
}
