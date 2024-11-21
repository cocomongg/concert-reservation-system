package io.hhplus.concert.app.concert.application;

import io.hhplus.concert.app.concert.application.ConcertDto.ConcertInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertReservationInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.concert.domain.service.ConcertService;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.app.concert.domain.model.Concert;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertSchedule;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import io.hhplus.concert.app.member.domain.service.MemberService;
import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.common.lock.DistributedLock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConcertFacade {

    private final ConcertService concertService;
    private final MemberService memberService;

    public List<ConcertInfo> getConcerts() {
        List<Concert> concerts = concertService.getConcerts();

        return concerts.stream()
            .map(ConcertDto.ConcertInfo::new)
            .toList();
    }

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

    @DistributedLock(key = "'concertSeat:' + #concertSeatId")
    @Transactional
    public ConcertReservationInfo reserveConcertSeat(Long concertSeatId, Long memberId, LocalDateTime dateTime) {
        Member member = memberService.getMember(memberId);

        ReserveConcertSeat reserveCommand =
            new ReserveConcertSeat(concertSeatId, dateTime, ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

        ConcertSeat concertSeat = concertService.reserveConcertSeat(reserveCommand);
        ConcertReservation concertReservation = concertService.createConcertReservation(
            new CreateConcertReservation(member.getId(), concertSeat.getId(), concertSeat.getPriceAmount(), dateTime));

        return new ConcertReservationInfo(concertReservation, concertSeat);
    }
}
