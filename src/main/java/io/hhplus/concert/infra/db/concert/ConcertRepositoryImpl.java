package io.hhplus.concert.infra.db.concert;

import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ConcertRepositoryImpl implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final ConcertSeatJpaRepository concertSeatJpaRepository;
    private final ConcertReservationJpaRepository concertReservationJpaRepository;

    @Override
    public ConcertSeat getConcertSeat(GetConcertSeat query) {
        return concertSeatJpaRepository.findById(query.getConcertSeatId())
            .orElseThrow(() -> ConcertException.CONCERT_SEAT_NOT_FOUND);
    }

    @Override
    public ConcertReservation saveConcertReservation(ConcertReservation reservation) {
        return concertReservationJpaRepository.save(reservation);
    }

    @Override
    public Concert getConcert(GetConcert query) {
        return concertJpaRepository.findById(query.getConcertId())
            .orElseThrow(() -> ConcertException.CONCERT_NOT_FOUND);
    }

    @Override
    public ConcertSchedule getConcertSchedule(GetConcertSchedule query) {
        return concertScheduleJpaRepository.findById(query.getConcertScheduleId())
            .orElseThrow(() -> ConcertException.CONCERT_SCHEDULE_NOT_FOUND);
    }

    @Override
    public List<ConcertSchedule> getReservableConcertSchedules(
        GetReservableConcertSchedules query) {
        return concertScheduleJpaRepository.findAllByConcertIdAndScheduledAtAfter(
            query.getConcertId(), query.getCurrentTime());
    }

    @Override
    public List<ConcertSeat> getConcertSeats(long concertScheduleId) {
        return concertSeatJpaRepository.findAllByConcertScheduleId(concertScheduleId);
    }

    @Override
    public ConcertReservation getConcertReservation(GetConcertReservation query) {
        return concertReservationJpaRepository.findById(query.getConcertReservationId())
            .orElseThrow(() -> ConcertException.CONCERT_RESERVATION_NOT_FOUND);
    }
}
