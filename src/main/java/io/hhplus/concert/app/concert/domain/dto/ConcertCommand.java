package io.hhplus.concert.app.concert.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertCommand {

    @Getter
    @AllArgsConstructor
    public static class CreateConcertReservation {
        private final long memberId;
        private final long concertSeatId;
        private final int priceAmount;
        private final LocalDateTime dateTime;
    }

    @Getter
    @AllArgsConstructor
    public static class ReserveConcertSeat {
        private final long concertSeatId;
        private final LocalDateTime dateTime;
        private final int tempReserveDurationMinutes;
    }

    @Getter
    @AllArgsConstructor
    public static class ConfirmReservation {
        private final long concertSeatId;
        private final long concertReservationId;
        private final LocalDateTime dateTime;
    }
}
