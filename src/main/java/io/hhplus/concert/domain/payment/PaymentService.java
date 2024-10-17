package io.hhplus.concert.domain.payment;

import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(CreatePayment command) {
        Payment payment = new Payment(command);
        return paymentRepository.savePayment(payment);
    }

    @Transactional
    public PaymentHistory createPaymentHistory(CreatePaymentHistory command) {
        PaymentHistory paymentHistory = new PaymentHistory(command);
        return paymentRepository.savePaymentHistory(paymentHistory);
    }
}
