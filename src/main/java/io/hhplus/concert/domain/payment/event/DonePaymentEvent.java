package io.hhplus.concert.domain.payment.event;

import io.hhplus.concert.domain.common.event.DomainEvent;
import io.hhplus.concert.domain.payment.model.Payment;
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
