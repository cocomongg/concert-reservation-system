package io.hhplus.concert.app.payment.domain.model;

import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePaymentHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_history")
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
