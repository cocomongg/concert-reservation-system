package io.hhplus.concert.domain.concert;

import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
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
        return concertRepository.getConcertSeat(query);
    }

    @Transactional(readOnly = true)
    public ConcertSeat getConcertSeatWithOptimisticLock(GetConcertSeat query) {
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

    @Transactional
    public ConcertSeat reserveConcertSeat(ReserveConcertSeat command) {
        ConcertSeat concertSeat =
            this.getConcertSeatWithOptimisticLock(new GetConcertSeat(command.getConcertSeatId()));

        concertSeat.reserve(command.getCurrentTime(), command.getTempReserveDurationMinutes());

        return concertSeat;
    }
}
