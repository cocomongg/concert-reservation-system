package io.hhplus.concert.app.concert.infra.db;

import io.hhplus.concert.app.concert.domain.repository.ConcertRepository;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.app.concert.domain.model.Concert;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
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
    public List<Concert> getConcerts() {
        return concertJpaRepository.findAll();
    }

    @Override
    public ConcertSeat getConcertSeat(GetConcertSeat query) {
        return concertSeatJpaRepository.findById(query.getConcertSeatId())
            .orElseThrow(() -> new CoreException(CoreErrorType.Concert.CONCERT_SEAT_NOT_FOUND));
    }

    @Override
    public ConcertSeat getConcertSeatWithLock(GetConcertSeat query) {
        return concertSeatJpaRepository.findByIdWithLock(query.getConcertSeatId())
            .orElseThrow(() -> new CoreException(CoreErrorType.Concert.CONCERT_SEAT_NOT_FOUND));
    }

    @Override
    public ConcertReservation saveConcertReservation(ConcertReservation reservation) {
        return concertReservationJpaRepository.save(reservation);
    }

    @Override
    public Concert getConcert(GetConcert query) {
        return concertJpaRepository.findById(query.getConcertId())
            .orElseThrow(() -> new CoreException(CoreErrorType.Concert.CONCERT_NOT_FOUND));
    }

    @Override
    public ConcertSchedule getConcertSchedule(GetConcertSchedule query) {
        return concertScheduleJpaRepository.findById(query.getConcertScheduleId())
            .orElseThrow(() -> new CoreException(CoreErrorType.Concert.CONCERT_SCHEDULE_NOT_FOUND));
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
            .orElseThrow(() -> new CoreException(CoreErrorType.Concert.CONCERT_RESERVATION_NOT_FOUND));
    }
}
