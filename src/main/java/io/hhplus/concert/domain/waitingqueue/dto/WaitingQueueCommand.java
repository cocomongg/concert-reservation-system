package io.hhplus.concert.domain.waitingqueue.dto;

import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.springframework.util.StringUtils;

public class WaitingQueueCommand {

    @Getter
    public static class CreateWaitingQueueCommand {
        private final String token;
        private final WaitingQueueStatus status;
        private final LocalDateTime expiredAt;

        public CreateWaitingQueueCommand(String token, WaitingQueueStatus status,
            LocalDateTime expiredAt) {
            if(!StringUtils.hasText(token) || Objects.isNull(status) || Objects.isNull(expiredAt)) {
                throw WaitingQueueException.INVALID_CREATION_INPUT;
            }

            this.token = token;
            this.status = status;
            this.expiredAt = expiredAt;
        }
    }
}
