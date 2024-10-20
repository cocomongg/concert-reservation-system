package io.hhplus.concert.infra.db.payment;

import io.hhplus.concert.domain.payment.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory, Long> {

}
