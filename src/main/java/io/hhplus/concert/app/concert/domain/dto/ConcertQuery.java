package io.hhplus.concert.app.concert.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertQuery {

    @Getter
    @AllArgsConstructor
    public static class GetConcertSeat {
        private final long concertSeatId;
    }

    @Getter
    @AllArgsConstructor
    public static class GetConcert {
        private final long concertId;
    }

    @Getter
    @AllArgsConstructor
    public static class GetConcertSchedule {
        private final long concertScheduleId;
    }

    @Getter
    @AllArgsConstructor
    public static class GetReservableConcertSchedules {
        private final long concertId;
        private final LocalDateTime currentTime;
    }

    @Getter
    @AllArgsConstructor
    public static class GetReservableConcertSeats {
        private final long concertScheduleId;
        private final LocalDateTime currentTime;
    }

    @Getter
    @AllArgsConstructor
    public static class GetConcertReservation {
        private final long concertReservationId;
    }

    @Getter
    @AllArgsConstructor
    public static class CheckConcertSeatExpired {
        private final long concertSeatId;
        private final LocalDateTime currentTime;
        private final int tempReserveDurationMinutes;
    }
}
