package io.hhplus.concert.application.concert;

import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
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

    public List<ConcertSeatInfo> getReservableConcertSeats(Long concertScheduleId, LocalDateTime currentTime) {
        GetConcertSchedule scheduleQuery = new GetConcertSchedule(concertScheduleId);
        ConcertSchedule concertSchedule = concertService.getConcertSchedule(scheduleQuery);

        GetReservableConcertSeats seatsQuery =
            new GetReservableConcertSeats(concertSchedule.getId(), currentTime);
        List<ConcertSeat> reservableConcertSeats = concertService.getReservableConcertSeats(seatsQuery);

        return reservableConcertSeats.stream()
            .map(ConcertDto.ConcertSeatInfo::new)
            .toList();
    }
}
