package io.hhplus.concert.application.concert;

import io.hhplus.concert.application.concert.ConcertDto.ConcertReservationInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.member.MemberService;
import io.hhplus.concert.domain.member.model.Member;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ConcertFacade {

    private final ConcertService concertService;
    private final MemberService memberService;

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

    @Transactional
    public ConcertReservationInfo reserveConcertSeat(Long concertSeatId, Long memberId, LocalDateTime dateTime) {
        Member member = memberService.getMember(memberId);

        ReserveConcertSeat reserveCommand =
            new ReserveConcertSeat(concertSeatId, dateTime, ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

        ConcertSeat concertSeat = concertService.reserveConcertSeat(reserveCommand);
        ConcertReservation concertReservation = concertService.createConcertReservation(
            new CreateConcertReservation(member.getId(), concertSeat.getId(), dateTime));

        return new ConcertReservationInfo(concertReservation, concertSeat);
    }
}
