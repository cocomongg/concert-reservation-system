package io.hhplus.concert.application.waitingqueue;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.support.error.ErrorType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitingQueueScheduler {

    private final WaitingQueueFacade waitingQueueFacade;

    @Scheduled(fixedRate = 60_000) // 1min
    public void activateOldestWaitingQueues() {
        log.info("activate token - st");
        int activatedCount = 0;
        try {
            activatedCount = waitingQueueFacade.activateOldestWaitedQueues();
        } catch (Exception e) {
            throw new CoreException(CoreErrorType.INTERNAL_ERROR, e);
        }
        log.info("activate token - ed [total activated: {}]", activatedCount);
    }

    @Scheduled(fixedRate = 600_000) // 10min
    public void expireWaitingQueues() {
        log.info("expire token - st");
        int expiredCount = 0;
        try {
            expiredCount = waitingQueueFacade.expireWaitingQueues(LocalDateTime.now());
        } catch (Exception e) {
            throw new CoreException(CoreErrorType.INTERNAL_ERROR, e);
        }

        log.info("expire token - ed [total expired: {}]", expiredCount);
    }
}
