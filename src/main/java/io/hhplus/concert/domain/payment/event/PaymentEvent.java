package io.hhplus.concert.domain.payment.event;

import io.hhplus.concert.domain.common.event.DomainEvent;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PaymentEvent {
    @Getter
    public static class CreatePaymentHistoryEvent extends DomainEvent {
        private final long paymentId;
        private final int amount;

        public CreatePaymentHistoryEvent(long paymentId, int amount) {
            super(LocalDateTime.now());
            this.paymentId = paymentId;
            this.amount = amount;
        }
    }
}
