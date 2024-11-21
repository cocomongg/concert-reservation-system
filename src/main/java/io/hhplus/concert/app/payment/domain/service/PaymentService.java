package io.hhplus.concert.app.payment.domain.service;

import io.hhplus.concert.app.payment.domain.repository.PaymentRepository;
import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentHistory;
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
