package io.hhplus.concert.domain.waitingqueue.dto;

import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import lombok.Getter;
import org.springframework.util.StringUtils;

public class WaitingQueueQuery {

    @Getter
    public static class GetWaitingQueueCommonQuery {
        private final String token;

        public GetWaitingQueueCommonQuery(String token) {
            if(!StringUtils.hasText(token)) {
                throw WaitingQueueException.INVALID_RETRIEVAL_INPUT;
            }

            this.token = token;
        }
    }
}
