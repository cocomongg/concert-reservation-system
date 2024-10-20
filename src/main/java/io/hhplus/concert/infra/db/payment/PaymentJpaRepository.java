package io.hhplus.concert.infra.db.payment;

import io.hhplus.concert.domain.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

}
