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
}
