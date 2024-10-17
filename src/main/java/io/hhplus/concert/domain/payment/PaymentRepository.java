package io.hhplus.concert.domain.payment;

import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentHistory;

public interface PaymentRepository {

    Payment savePayment(Payment payment);

    PaymentHistory savePaymentHistory(PaymentHistory paymentHistory);
}
