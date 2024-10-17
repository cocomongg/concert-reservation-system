package io.hhplus.concert.interfaces.api.waitingqueue;

import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueInfo;
import io.hhplus.concert.application.waitingqueue.WaitingQueueDto.WaitingQueueWithOrderInfo;
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
        @Schema(description = "사용자의 대기열 순서")
        private final Long order;

        @Schema(description = "사용자 앞에 남은 대기자 수")
        private final Long remainingWaitingCount;

        @Schema(description = "현재 사용자의 대기열에서의 상태값")
        private final String queueStatus;

        @Schema(description = "대기열 토큰 만료 일시")
        private final LocalDateTime expiredAt;

        public static GetQueue from(WaitingQueueWithOrderInfo info) {
            WaitingQueueInfo waitingQueueInfo = info.getWaitingQueueInfo();
            Long order = info.getOrder();

            return GetQueue.builder()
                .order(waitingQueueInfo.getId())
                .remainingWaitingCount(order)
                .queueStatus(waitingQueueInfo.getStatus().toString())
                .expiredAt(waitingQueueInfo.getExpireAt())
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

        public static CreateQueueToken from(WaitingQueueInfo info) {
            return CreateQueueToken.builder()
                .token(info.getToken())
                .order(info.getId())
                .queueStatus(info.getStatus().toString())
                .expiredAt(info.getExpireAt())
                .build();
        }
    }
}
