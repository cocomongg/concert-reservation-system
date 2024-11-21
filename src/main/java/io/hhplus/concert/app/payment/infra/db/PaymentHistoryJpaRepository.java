package io.hhplus.concert.app.payment.infra.db;

import io.hhplus.concert.app.payment.domain.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory, Long> {

}
