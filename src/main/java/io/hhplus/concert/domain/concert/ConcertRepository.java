package io.hhplus.concert.domain.concert;

import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import java.util.List;

public interface ConcertRepository {

    ConcertSeat getConcertSeat(GetConcertSeat query);

    ConcertReservation saveConcertReservation(ConcertReservation reservation);

    Concert getConcert(GetConcert query);

    ConcertSchedule getConcertSchedule(GetConcertSchedule query);

    List<ConcertSchedule> getReservableConcertSchedules(GetReservableConcertSchedules query);

    List<ConcertSeat> getConcertSeats(long concertScheduleId);

    ConcertReservation getConcertReservation(GetConcertReservation query);
}
