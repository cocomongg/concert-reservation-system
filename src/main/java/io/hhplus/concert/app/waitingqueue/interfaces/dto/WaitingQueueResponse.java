package io.hhplus.concert.app.waitingqueue.interfaces.dto;

import io.hhplus.concert.app.waitingqueue.domain.model.WaitingQueueTokenInfo;
import io.hhplus.concert.app.waitingqueue.domain.model.WaitingTokenWithOrderInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class WaitingQueueResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GetQueue {
        @Schema(description = "토큰 값")
        private final String token;

        @Schema(description = "현재 토큰의 상태값")
        private final String tokenStatus;

        @Schema(description = "사용자의 대기 순서")
        private final Long order;

        @Schema(description = "사용자의 남은 예상 대기 시간")
        private final Long remainingWaitTimeSeconds;

        public static GetQueue from(WaitingTokenWithOrderInfo info) {
            WaitingQueueTokenInfo tokenInfo = info.getTokenInfo();

            return GetQueue.builder()
                .token(tokenInfo.getToken())
                .tokenStatus(tokenInfo.getStatus().toString())
                .order(info.getOrder())
                .remainingWaitTimeSeconds(info.getRemainingWaitTimeSeconds())
                .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateQueueToken {
        @Schema(description = "해당 토큰을 헤더에 담아서 다른 API를 호출할 수 있다.")
        private final String token;

        @Schema(description = "사용자의 대기열 순서")
        private final Long order;

        @Schema(description = "현재 사용자의 대기열에서의 상태값")
        private final String queueStatus;

        @Schema(description = "대기열 토큰 만료 일시")
        private final LocalDateTime expiredAt;

        public static CreateQueueToken from(WaitingQueueTokenInfo info) {
            return CreateQueueToken.builder()
                .token(info.getToken())
//                .order(info.getOrder())
                .queueStatus(info.getStatus().toString())
                .expiredAt(info.getExpireAt())
                .build();
        }
    }
}
