package io.hhplus.concert.domain.waitingqueue.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.springframework.util.StringUtils;

public class WaitingQueueQuery {

    @Getter
    public static class GetWaitingQueueCommonQuery {
        private final String token;

        public GetWaitingQueueCommonQuery(String token) {
            if(!StringUtils.hasText(token)) {
                throw new IllegalArgumentException("Invalid input data for GetWaitingQueueCommonQuery.");
            }

            this.token = token;
        }
    }

    @Getter
    public static class CheckTokenActivate {
        private final String token;
        private final LocalDateTime currentTime;

        public CheckTokenActivate(String token, LocalDateTime currentTime) {
            if(!StringUtils.hasText(token) || Objects.isNull(currentTime)) {
                throw new IllegalArgumentException("Invalid input data for CheckTokenActivate.");
            }

            this.token = token;
            this.currentTime = currentTime;
        }
    }
}
