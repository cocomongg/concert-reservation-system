package io.hhplus.concert.domain.concert.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertCommand {

    @Getter
    @AllArgsConstructor
    public static class CreateConcertReservation {
        private final long memberId;
        private final long concertSeatId;
        private final LocalDateTime dateTime;
    }
}
