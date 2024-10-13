package io.hhplus.concert.interfaces.api.concert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ConcertRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveConcert {
        @Schema(description = "예약자 id")
        private Long memberId;

        @Schema(description = "예약하려는 좌석 id")
        private Long seatId;
    }
}
