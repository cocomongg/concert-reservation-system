package io.hhplus.concert.app.payment.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class PaymentEvent {
    private final LocalDateTime publishAt;
}