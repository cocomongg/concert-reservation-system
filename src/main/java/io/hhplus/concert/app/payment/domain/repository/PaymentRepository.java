package io.hhplus.concert.app.payment.domain.repository;

import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentHistory;

public interface PaymentRepository {

    Payment savePayment(Payment payment);

    PaymentHistory savePaymentHistory(PaymentHistory paymentHistory);
}
