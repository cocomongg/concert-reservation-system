package io.hhplus.concert.app.payment.domain.event;

import io.hhplus.concert.app.common.event.DomainEvent;
import io.hhplus.concert.app.payment.domain.model.Payment;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DonePaymentEvent extends DomainEvent {
    private final Payment payment;
    private final String token;

    public DonePaymentEvent(Payment payment, String token) {
        super(LocalDateTime.now());
        this.payment = payment;
        this.token = token;
    }
}
