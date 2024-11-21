package io.hhplus.concert.app.payment.domain.repository;

import io.hhplus.concert.app.payment.domain.model.OutboxStatus;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOutboxRepository {
    PaymentOutbox saveOutbox(PaymentOutbox outbox);

    PaymentOutbox getOutbox(String eventId);

    List<PaymentOutbox> getOutboxesForRepublish(OutboxStatus status, LocalDateTime thresholdTime);
}
