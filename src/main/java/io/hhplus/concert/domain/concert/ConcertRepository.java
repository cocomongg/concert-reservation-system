package io.hhplus.concert.domain.concert;

import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSeat;

public interface ConcertRepository {

    ConcertSeat getConcertSeat(GetConcertSeat query);

    ConcertReservation saveConcertReservation(ConcertReservation reservation);
}
