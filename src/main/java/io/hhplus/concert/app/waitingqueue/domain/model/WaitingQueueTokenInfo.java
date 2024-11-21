package io.hhplus.concert.app.waitingqueue.domain.model;

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
