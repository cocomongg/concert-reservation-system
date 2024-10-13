package io.hhplus.concert.interfaces.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payment {
        @Schema(description = "콘서트 예약 Id")
        private Long reservationId;
    }
}
