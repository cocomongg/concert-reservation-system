package io.hhplus.concert.app.concert.domain.model;

import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "concert_seat", indexes = {
    @Index(name = "concert_seat_concert_schedule_id_index", columnList = "concertScheduleId"),
})
@Entity
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId;

    @Column(name = "seat_number")
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ConcertSeatStatus status;

    @Column(name = "price_amount")
    private int priceAmount;

    @Column(name = "temp_reserved_at")
    private LocalDateTime tempReservedAt;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isReservable(LocalDateTime currentTime, int tempReserveDurationMinutes) {
        if (this.isTemporarilyReserved(currentTime, tempReserveDurationMinutes)) {
            return false;
        }

        if (ConcertSeatStatus.RESERVED_COMPLETE.equals(this.status)) {
            return false;
        }

        return true;
    }

    public boolean isTemporarilyReserved(LocalDateTime currentTime, int tempReserveDurationMinutes) {
        if(Objects.isNull(this.tempReservedAt) || ConcertSeatStatus.RESERVED_COMPLETE.equals(this.status)) {
            return false;
        }

        LocalDateTime tempReserveExpiredAt =
            this.tempReservedAt.plusMinutes(tempReserveDurationMinutes);

        return tempReserveExpiredAt.isAfter(currentTime); // 임시 배정 상태 만료시간이 현재 시간보다 더 뒤라면
    }

    public void reserve(LocalDateTime currentTime, int tempReserveDurationMinutes) {
        if (!this.isReservable(currentTime, tempReserveDurationMinutes)) {
            throw new CoreException(CoreErrorType.Concert.NOT_RESERVABLE_SEAT);
        }

        this.tempReservedAt = currentTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void checkExpired(LocalDateTime currentTime, int tempReserveDurationMinutes) {
        if(!this.isTemporarilyReserved(currentTime, tempReserveDurationMinutes)) {
            throw new CoreException(CoreErrorType.Concert.TEMPORARY_RESERVATION_EXPIRED);
        }
    }

    public void confirmReservation(LocalDateTime currentTime) {
        this.status = ConcertSeatStatus.RESERVED_COMPLETE;
        this.reservedAt = currentTime;
        this.updatedAt = LocalDateTime.now();
    }
}
