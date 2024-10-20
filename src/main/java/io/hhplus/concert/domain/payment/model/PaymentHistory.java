package io.hhplus.concert.domain.payment.model;

import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "amount")
    private int amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PaymentHistory(CreatePaymentHistory command) {
        this.paymentId = command.getPaymentId();
        this.status = command.getStatus();
        this.amount = command.getAmount();
        this.createdAt = LocalDateTime.now();
    }
}
