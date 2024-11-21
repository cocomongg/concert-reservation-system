package io.hhplus.concert.app.payment.domain.event;

import io.hhplus.concert.app.payment.domain.model.Payment;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DonePaymentEvent extends PaymentEvent {
    private final Payment payment;
    private final String token;

    public DonePaymentEvent(Payment payment, String token) {
        this.payment = payment;
        this.token = token;
    }
}
