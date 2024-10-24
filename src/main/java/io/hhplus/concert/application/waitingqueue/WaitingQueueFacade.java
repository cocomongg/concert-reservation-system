package io.hhplus.concert.application.waitingqueue;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueTokenGenerator;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.CheckTokenActivate;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueWithOrder;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WaitingQueueFacade {

    private final WaitingQueueService waitingQueueService;
    private final WaitingQueueTokenGenerator waitingQueueTokenGenerator;

    public WaitingQueueInfo generateWaitingQueueToken() {
        String token = waitingQueueTokenGenerator.generateWaitingQueueToken();
        LocalDateTime expireAt = LocalDateTime.now()
            .plusMinutes(ServicePolicy.WAITING_QUEUE_EXPIRED_MINUTES);

        CreateWaitingQueue command =
            new CreateWaitingQueue(token, ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT, expireAt);

        WaitingQueue waitingQueue = waitingQueueService.createWaitingQueue(command);
        return new WaitingQueueInfo(waitingQueue);
    }

    public WaitingQueueWithOrderInfo getWaitingQueueWithOrder(String token) {
        GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
        WaitingQueueWithOrder waitingQueueWithOrder =
            waitingQueueService.getWaitingQueueWithOrder(query);

        return new WaitingQueueWithOrderInfo(waitingQueueWithOrder);
    }

    // todo: call by interceptor
    public void checkTokenActivate(String token, LocalDateTime currentTime) {
        CheckTokenActivate query = new CheckTokenActivate(token, currentTime);
        waitingQueueService.checkTokenActivate(query);
    }

    //todo: call by activate scheduler
    public void activateOldestWaitedQueues() {
        waitingQueueService.activateToken(ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT);
    }
}
