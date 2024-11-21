package io.hhplus.concert.app.payment.domain.model;

import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePayment;
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
@Table(name = "payment")
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "paid_amount")
    private int paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment(CreatePayment command) {
        this.memberId = command.getMemberId();
        this.reservationId = command.getReservationId();
        this.paidAmount = command.getPaidAmount();
        this.status = command.getStatus();
        this.paidAt = command.getCurrentTime();
        this.createdAt = LocalDateTime.now();
    }
}

