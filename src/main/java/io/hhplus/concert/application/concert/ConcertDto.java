package io.hhplus.concert.application.concert;

import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertDto {

    @Getter
    @AllArgsConstructor
    public static class ConcertScheduleInfo {
        private final Long id;
        private final Long concertId;
        private final String scheduledAt;
        private final String startAt;
        private final String endAt;
        private final String createdAt;

        public ConcertScheduleInfo(ConcertSchedule concertSchedule) {
            this.id = concertSchedule.getId();
            this.concertId = concertSchedule.getConcertId();
            this.scheduledAt = concertSchedule.getScheduledAt().toString();
            this.startAt = concertSchedule.getStartAt().toString();
            this.endAt = concertSchedule.getEndAt().toString();
            this.createdAt = concertSchedule.getCreatedAt().toString();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ConcertSeatInfo {
        private final Long id;
        private final Long concertScheduleId;
        private final int seatNumber;
        private final ConcertSeatStatus status;
        private final int priceAmount;
        private final String createdAt;

        public ConcertSeatInfo(ConcertSeat concertSeat) {
            this.id = concertSeat.getId();
            this.concertScheduleId = concertSeat.getConcertScheduleId();
            this.seatNumber = concertSeat.getSeatNumber();
            this.status = concertSeat.getStatus();
            this.priceAmount = concertSeat.getPriceAmount();
            this.createdAt = concertSeat.getCreatedAt().toString();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ConcertReservationInfo {
        private final Long id;
        private final Long memberId;
        private final Long concertSeatId;
        private final ConcertReservationStatus status;
        private final LocalDateTime reservedAt;
        private final LocalDateTime createdAt;

        public ConcertReservationInfo(ConcertReservation concertReservation) {
            this.id = concertReservation.getId();
            this.memberId = concertReservation.getMemberId();
            this.concertSeatId = concertReservation.getConcertSeatId();
            this.status = concertReservation.getStatus();
            this.reservedAt = concertReservation.getReservedAt();
            this.createdAt = concertReservation.getCreatedAt();
        }
    }

}
