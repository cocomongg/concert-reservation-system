package io.hhplus.concert.domain.concert.model;

import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
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
public class ConcertReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "concert_seat_id")
    private Long concertSeatId;

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
        this.status = ConcertReservationStatus.PENDING;
        this.reservedAt = command.getDateTime();
        this.createdAt = LocalDateTime.now();
    }
}
