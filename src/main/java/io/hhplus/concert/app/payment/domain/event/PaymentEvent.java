package io.hhplus.concert.app.payment.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public abstract class PaymentEvent {
    private final String eventId;
    private final LocalDateTime publishAt;

    public PaymentEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.publishAt = LocalDateTime.now();
    }
}