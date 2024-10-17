package io.hhplus.concert.interfaces.api.payment;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
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
        @Schema(description = "콘서트 예약 Id")
        private Long reservationId;

        @Schema(description = "결제 id")
        private Long paymentId;

        @Schema(description = "결제 금액")
        private int paidAmount;

        @Schema(description = "결제 시각")
        private LocalDateTime paidAt;

        public static PaymentResult from(PaymentInfo paymentInfo) {
            return PaymentResult.builder()
                .reservationId(paymentInfo.getReservationId())
                .paymentId(paymentInfo.getId())
                .paidAmount(paymentInfo.getPaidAmount())
                .paidAt(paymentInfo.getPaidAt())
                .build();
        }
    }
}
