package io.hhplus.concert.interfaces.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class PaymentResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PaymentResult {
        @Schema(description = "결제 성공한 콘서트 Id")
        private Long concertId;

        @Schema(description = "결제 성공한 콘서트 이름")
        private String concertTitle;

        @Schema(description = "결제 성공한 콘서트 좌석")
        private int seatNumber;

        @Schema(description = "결제 id")
        private Long paymentId;

        @Schema(description = "결제 금액")
        private int paidAmount;
    }
}
