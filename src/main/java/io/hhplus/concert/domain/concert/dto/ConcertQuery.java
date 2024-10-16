package io.hhplus.concert.domain.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertQuery {

    @Getter
    @AllArgsConstructor
    public static class GetConcertSeat {
        private final long concertSeatId;
    }
}
