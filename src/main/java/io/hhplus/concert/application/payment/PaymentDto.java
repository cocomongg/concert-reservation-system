package io.hhplus.concert.application.payment;

import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PaymentDto {

    @Getter
    @AllArgsConstructor
    public static class PaymentInfo {
        private final Long id;
        private final Long memberId;
        private final Long reservationId;
        private final int paidAmount;
        private final PaymentStatus status;
        private final LocalDateTime paidAt;

        public PaymentInfo(Payment payment) {
            this.id = payment.getId();
            this.memberId = payment.getMemberId();
            this.reservationId = payment.getReservationId();
            this.paidAmount = payment.getPaidAmount();
            this.status = payment.getStatus();
            this.paidAt = payment.getPaidAt();
        }
    }
}
