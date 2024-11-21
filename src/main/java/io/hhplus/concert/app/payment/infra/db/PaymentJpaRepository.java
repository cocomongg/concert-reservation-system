package io.hhplus.concert.app.payment.infra.db;

import io.hhplus.concert.app.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

}
