package io.hhplus.concert.domain.concert;

import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    @Transactional(readOnly = true)
    public ConcertSeat getConcertSeat(GetConcertSeat query) {
        return concertRepository.getConcertSeat(query);
    }

    @Transactional
    public ConcertReservation createConcertReservation(CreateConcertReservation command) {
        ConcertReservation concertReservation = new ConcertReservation(command);
        return concertRepository.saveConcertReservation(concertReservation);
    }
}
