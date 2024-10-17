package io.hhplus.concert.infra.db.concert;

import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ConcertRepositoryImpl implements ConcertRepository {

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
}
