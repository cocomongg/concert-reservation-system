package io.hhplus.concert.application.concert;

import io.hhplus.concert.domain.concert.model.Concert;
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
    public static class ConcertInfo {
        private final Long id;
        private final String title;
        private final String description;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public ConcertInfo(Concert concert) {
            this.id = concert.getId();
            this.title = concert.getTitle();
            this.description = concert.getDescription();
            this.createdAt = concert.getCreatedAt();
            this.updatedAt = concert.getUpdatedAt();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ConcertScheduleInfo {
        private final Long id;
        private final Long concertId;
        private final LocalDateTime scheduledAt;
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;
        private final LocalDateTime createdAt;

        public ConcertScheduleInfo(ConcertSchedule concertSchedule) {
            this.id = concertSchedule.getId();
            this.concertId = concertSchedule.getConcertId();
            this.scheduledAt = concertSchedule.getScheduledAt();
            this.startAt = concertSchedule.getStartAt();
            this.endAt = concertSchedule.getEndAt();
            this.createdAt = concertSchedule.getCreatedAt();
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
        private final int paidAmount;
        private final ConcertReservationStatus status;
        private final LocalDateTime reservedAt;
        private final LocalDateTime createdAt;

        public ConcertReservationInfo(ConcertReservation concertReservation, ConcertSeat concertSeat) {
            this.id = concertReservation.getId();
            this.memberId = concertReservation.getMemberId();
            this.concertSeatId = concertReservation.getConcertSeatId();
            this.paidAmount = concertSeat.getPriceAmount();
            this.status = concertReservation.getStatus();
            this.reservedAt = concertReservation.getReservedAt();
            this.createdAt = concertReservation.getCreatedAt();
        }
    }

}
