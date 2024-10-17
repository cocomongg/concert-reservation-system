package io.hhplus.concert.domain.concert.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        if(Objects.isNull(this.tempReservedAt)) {
            return false;
        }

        LocalDateTime tempReserveExpiredAt =
            this.tempReservedAt.plusMinutes(tempReserveDurationMinutes);

        return tempReserveExpiredAt.isAfter(currentTime); // 임시 배정 상태 만료시간이 현재 시간보다 더 뒤라면
    }

    public void reserveSeatTemporarily(LocalDateTime currentTime) {
        this.tempReservedAt = currentTime;
        this.updatedAt = currentTime;
    }
}
