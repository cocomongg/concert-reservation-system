package io.hhplus.concert.app.concert.domain.repository;

import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.app.concert.domain.model.Concert;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import java.util.List;

public interface ConcertRepository {

    List<Concert> getConcerts();

    ConcertSeat getConcertSeat(GetConcertSeat query);

    ConcertSeat getConcertSeatWithLock(GetConcertSeat query);

    ConcertReservation saveConcertReservation(ConcertReservation reservation);

    Concert getConcert(GetConcert query);

    ConcertSchedule getConcertSchedule(GetConcertSchedule query);

    List<ConcertSchedule> getReservableConcertSchedules(GetReservableConcertSchedules query);

    List<ConcertSeat> getConcertSeats(long concertScheduleId);

    ConcertReservation getConcertReservation(GetConcertReservation query);
}
