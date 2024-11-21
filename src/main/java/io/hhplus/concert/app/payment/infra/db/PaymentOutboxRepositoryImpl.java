package io.hhplus.concert.app.payment.infra.db;

import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.payment.domain.model.OutboxStatus;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import io.hhplus.concert.app.payment.domain.repository.PaymentOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

    private final PaymentOutboxJpaRepository outboxJpaRepository;

    @Override
    public PaymentOutbox saveOutbox(PaymentOutbox outbox) {
        return outboxJpaRepository.save(outbox);
    }

    @Override
    public PaymentOutbox getOutbox(String eventId) {
        return outboxJpaRepository.findByEventId(eventId)
            .orElseThrow(() -> new CoreException(CoreErrorType.PaymentOutbox.OUTBOX_NOT_FOUND));
    }

    @Override
    public List<PaymentOutbox> getOutboxesForRepublish(OutboxStatus status, LocalDateTime thresholdTime) {
        return outboxJpaRepository.findAllByStatusTargetDatetime(status, thresholdTime);
    }
}
