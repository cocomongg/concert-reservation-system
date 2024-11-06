package io.hhplus.concert.domain.waitingqueue.model;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingQueueTokenInfo {
    private final String token;
    private final WaitingQueueTokenStatus status;
    private final LocalDateTime expireAt;

    public void checkNotWaiting() {
        if(!WaitingQueueTokenStatus.WAITING.equals(this.status)) {
            throw new CoreException(CoreErrorType.WaitingQueue.INVALID_STATE_NOT_WAITING);
        }
    }

    public void checkActivated(LocalDateTime currentTime) {
        boolean isActive = WaitingQueueTokenStatus.ACTIVE.equals(this.status);
        if(!isActive) {
            throw new CoreException(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE);
        }

        boolean isExpired = this.expireAt.isBefore(currentTime);
        if(isExpired) {
            throw new CoreException(CoreErrorType.WaitingQueue.INVALID_WAITING_QUEUE);
        }
    }
}
