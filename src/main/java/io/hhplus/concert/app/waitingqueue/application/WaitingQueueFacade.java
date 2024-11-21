package io.hhplus.concert.app.waitingqueue.application;

import static io.hhplus.concert.app.common.ServicePolicy.TOKEN_ACTIVATE_INTERVAL_TIMEUNIT;
import static io.hhplus.concert.app.common.ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;
import static io.hhplus.concert.app.common.ServicePolicy.WAITING_QUEUE_ACTIVATE_INTERVAL;
import static io.hhplus.concert.app.common.ServicePolicy.WAITING_QUEUE_EXPIRED_MINUTES;

import io.hhplus.concert.app.waitingqueue.domain.service.WaitingQueueService;
import io.hhplus.concert.app.waitingqueue.domain.service.WaitingQueueTokenGenerator;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueCommand.ActivateWaitingTokens;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.app.waitingqueue.domain.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenInfo;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingTokenWithOrderInfo;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WaitingQueueFacade {

    private final WaitingQueueService waitingQueueService;
    private final WaitingQueueTokenGenerator waitingQueueTokenGenerator;

    public WaitingQueueTokenInfo issueWaitingToken() {
        String token = waitingQueueTokenGenerator.generateWaitingQueueToken();
        InsertWaitingQueue command = new InsertWaitingQueue(token, LocalDateTime.now());

        return waitingQueueService.insertWaitingQueue(command);
    }

    public WaitingTokenWithOrderInfo getWaitingTokenWithOrderInfo(String token) {
        GetWaitingQueueCommonQuery tokenQuery = new GetWaitingQueueCommonQuery(token);
        WaitingQueueTokenInfo tokenInfo = waitingQueueService.getWaitingQueueToken(tokenQuery);
        Long waitingOrder = waitingQueueService.getWaitingTokenOrder(tokenQuery);

        GetRemainingWaitTimeSeconds remainingWaitTimeQuery = new GetRemainingWaitTimeSeconds(
            waitingOrder, WAITING_QUEUE_ACTIVATE_COUNT, WAITING_QUEUE_ACTIVATE_INTERVAL);
        Long remainingWaitTimeSeconds = waitingQueueService.getRemainingWaitTimeSeconds(
            remainingWaitTimeQuery);

        return new WaitingTokenWithOrderInfo(tokenInfo, waitingOrder, remainingWaitTimeSeconds);
    }

    public void checkTokenActivate(String token, LocalDateTime currentTime) {
        CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
        waitingQueueService.checkTokenActivate(query);
    }

    public Long activateWaitingToken() {
        ActivateWaitingTokens command = new ActivateWaitingTokens(
            WAITING_QUEUE_ACTIVATE_COUNT, WAITING_QUEUE_EXPIRED_MINUTES,
            TOKEN_ACTIVATE_INTERVAL_TIMEUNIT);

        return waitingQueueService.activateToken(command);
    }

//    public Long expireWaitingQueues(LocalDateTime currentTime) {
//        return waitingQueueService.expireToken(currentTime);
//    }
}
