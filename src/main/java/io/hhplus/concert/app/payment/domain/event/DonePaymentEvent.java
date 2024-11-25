package io.hhplus.concert.app.payment.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hhplus.concert.app.payment.domain.model.Payment;
import lombok.Getter;

@Getter
public class DonePaymentEvent extends PaymentEvent {
    private final Payment payment;
    private final String token;

    public DonePaymentEvent(@JsonProperty("payment") Payment payment, @JsonProperty("token") String token) {
        this.payment = payment;
        this.token = token;
    }
}
