package io.hhplus.concert.application.waitingqueue;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueTokenGenerator;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
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

        CreateWaitingQueue command = CreateWaitingQueue.createWaitingQueue(token);

        Long activeCount = waitingQueueService.getActiveCount();
        if(activeCount < ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT) {
            LocalDateTime expireAt = LocalDateTime.now()
                .plusMinutes(ServicePolicy.WAITING_QUEUE_EXPIRED_MINUTES);

            command = CreateWaitingQueue.createActiveQueue(token, expireAt);
        }

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
    public void validateWaitingQueueToken(String token, LocalDateTime currentTime) {
        GetWaitingQueueCommonQuery query = new GetWaitingQueueCommonQuery(token);
        WaitingQueue waitingQueue = waitingQueueService.getWaitingQueue(query);

        boolean available = waitingQueue.isAvailable(currentTime);
        if(!available) {
            throw WaitingQueueException.INVALID_WAITING_QUEUE;
        }
    }

    //todo: call by activate scheduler
    public void activateOldestWaitedQueues() {
        Long activeCount = waitingQueueService.getActiveCount();
        int maxActiveCount = ServicePolicy.WAITING_QUEUE_ACTIVATE_COUNT;

        int countToActivate = maxActiveCount - activeCount.intValue();
        waitingQueueService.activateOldestWaitedQueues(countToActivate);
    }
}
