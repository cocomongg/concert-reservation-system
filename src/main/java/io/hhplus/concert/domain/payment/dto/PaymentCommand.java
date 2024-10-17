package io.hhplus.concert.domain.payment.dto;

import io.hhplus.concert.domain.payment.model.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PaymentCommand {

    @Getter
    @AllArgsConstructor
    public static class CreatePayment {
        private final long memberId;
        private final long reservationId;
        private final int paidAmount;
        private final PaymentStatus status;
        private final LocalDateTime currentTime;
    }

    @Getter
    @AllArgsConstructor
    public static class CreatePaymentHistory {
        private final long paymentId;
        private final PaymentStatus status;
        private final int amount;
    }
}