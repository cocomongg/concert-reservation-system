package io.hhplus.concert.app.concert.domain.service;

import io.hhplus.concert.app.concert.domain.repository.ConcertRepository;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.ConfirmReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.CheckConcertSeatExpired;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.app.concert.domain.model.Concert;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    @Cacheable(value = ServicePolicy.CACHE_CONCERT_PREFIX, key = "'all'")
    @Transactional(readOnly = true)
    public List<Concert> getConcerts() {
        return concertRepository.getConcerts();
    }

    @Transactional(readOnly = true)
    public ConcertSeat getConcertSeat(GetConcertSeat query) {
        return concertRepository.getConcertSeatWithLock(query);
    }

    @Transactional
    public ConcertReservation createConcertReservation(CreateConcertReservation command) {
        ConcertReservation concertReservation = new ConcertReservation(command);
        return concertRepository.saveConcertReservation(concertReservation);
    }

    @Transactional(readOnly = true)
    public Concert getConcert(GetConcert query) {
        return concertRepository.getConcert(query);
    }

    @Transactional(readOnly = true)
    public ConcertSchedule getConcertSchedule(GetConcertSchedule query) {
        return concertRepository.getConcertSchedule(query);
    }

    @Transactional(readOnly = true)
    public List<ConcertSchedule> getReservableConcertSchedules(GetReservableConcertSchedules query) {
        return concertRepository.getReservableConcertSchedules(query);
    }

    @Transactional(readOnly = true)
    public List<ConcertSeat> getReservableConcertSeats(GetReservableConcertSeats query) {
        List<ConcertSeat> reservableConcertSeats = concertRepository.getConcertSeats(
            query.getConcertScheduleId());

        return reservableConcertSeats.stream()
            .filter(concertSeat -> concertSeat.isReservable(query.getCurrentTime(), ServicePolicy.TEMP_RESERVE_DURATION_MINUTES))
            .toList();
    }

    @Transactional(readOnly = true)
    public ConcertReservation getConcertReservation(GetConcertReservation query) {
        return concertRepository.getConcertReservation(query);
    }

    @Transactional(readOnly = true)
    public void checkConcertSeatExpired(CheckConcertSeatExpired query) {
        ConcertSeat concertSeat = this.getConcertSeat(new GetConcertSeat(query.getConcertSeatId()));
        concertSeat.checkExpired(query.getCurrentTime(), query.getTempReserveDurationMinutes());
    }

    @Transactional
    public ConcertSeat reserveConcertSeat(ReserveConcertSeat command) {
        ConcertSeat concertSeat = this.getConcertSeat(new GetConcertSeat(command.getConcertSeatId()));
        concertSeat.reserve(command.getDateTime(), command.getTempReserveDurationMinutes());

        return concertSeat;
    }

    @Transactional
    public void confirmReservation(ConfirmReservation command) {
        long concertReservationId = command.getConcertReservationId();
        long concertSeatId = command.getConcertSeatId();

        ConcertSeat concertSeat = this.getConcertSeat(new GetConcertSeat(concertSeatId));
        concertSeat.confirmReservation(command.getDateTime());

        ConcertReservation concertReservation =
            this.getConcertReservation(new GetConcertReservation(concertReservationId));
        concertReservation.confirmReservation(command.getDateTime());
    }
}
