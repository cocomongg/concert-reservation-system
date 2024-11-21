package io.hhplus.concert.app.payment.infra.db;

import io.hhplus.concert.app.payment.domain.model.OutboxStatus;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutbox, Long> {
    Optional<PaymentOutbox> findByEventId(String eventId);

    @Query("select o from PaymentOutbox o where o.status = :status and o.createdAt < :thresholdTime")
    List<PaymentOutbox> findAllByStatusTargetDatetime(OutboxStatus status, LocalDateTime thresholdTime);
}
