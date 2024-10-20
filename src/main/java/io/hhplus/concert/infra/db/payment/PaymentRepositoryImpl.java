package io.hhplus.concert.infra.db.payment;

import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;

    @Override
    public Payment savePayment(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public PaymentHistory savePaymentHistory(PaymentHistory paymentHistory) {
        return paymentHistoryJpaRepository.save(paymentHistory);
    }
}
