package io.hhplus.concert.application.waitingqueue;

import static io.hhplus.concert.domain.common.ServicePolicy.*;

import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueTokenGenerator;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetRemainingWaitTimeSeconds;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import io.hhplus.concert.domain.waitingqueue.model.WaitingTokenWithOrderInfo;
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
        return waitingQueueService.activateToken(WAITING_QUEUE_ACTIVATE_COUNT);
    }

    public Long expireWaitingQueues(LocalDateTime currentTime) {
        return waitingQueueService.expireTokens(currentTime);
    }
}
