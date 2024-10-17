package io.hhplus.concert.application.concert;

import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConcertFacade {

    private final ConcertService concertService;

    public List<ConcertScheduleInfo> getReservableConcertSchedules(Long concertId, LocalDateTime currentTime) {
        Concert concert = concertService.getConcert(new GetConcert(concertId));

        List<ConcertSchedule> reservableConcertSchedules = concertService.getReservableConcertSchedules(
            new GetReservableConcertSchedules(concert.getId(), currentTime));

        return reservableConcertSchedules.stream()
            .map(ConcertDto.ConcertScheduleInfo::new)
            .toList();
    }
}
