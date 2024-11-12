package io.hhplus.concert.domain.concert.model;

import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "concert_reservation")
@Entity
public class ConcertReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "concert_seat_id")
    private Long concertSeatId;

    @Column(name = "price_amount")
    private int priceAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ConcertReservationStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ConcertReservation(CreateConcertReservation command) {
        this.memberId = command.getMemberId();
        this.concertSeatId = command.getConcertSeatId();
        this.priceAmount = command.getPriceAmount();
        this.status = ConcertReservationStatus.PENDING;
        this.reservedAt = command.getDateTime();
        this.createdAt = LocalDateTime.now();
    }

    public void confirmReservation(LocalDateTime currentTime) {
        this.status = ConcertReservationStatus.COMPLETED;
        this.reservedAt = currentTime;
        this.updatedAt = LocalDateTime.now();
    }
}
