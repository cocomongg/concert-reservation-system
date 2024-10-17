package io.hhplus.concert.application.waitingqueue;

import io.hhplus.concert.application.waitingqueue.dto.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueTokenGenerator;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
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
}
