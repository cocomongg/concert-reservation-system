package io.hhplus.concert.app.concert.interfaces.controller;

import io.hhplus.concert.app.concert.application.ConcertDto.ConcertInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertReservationInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.app.concert.application.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.app.concert.application.ConcertFacade;
import io.hhplus.concert.app.common.api.response.ApiResult;
import io.hhplus.concert.app.concert.interfaces.dto.ConcertRequest.ReserveConcert;
import io.hhplus.concert.app.concert.interfaces.dto.ConcertResponse.ConcertItem;
import io.hhplus.concert.app.concert.interfaces.dto.ConcertResponse.ConcertScheduleItem;
import io.hhplus.concert.app.concert.interfaces.dto.ConcertResponse.ConcertSeatItem;
import io.hhplus.concert.app.concert.interfaces.dto.ConcertResponse.ReserveConcertResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
@RestController
public class ConcertController implements ConcertControllerDocs {

    private final ConcertFacade concertFacade;
    
    @GetMapping("")
    public ApiResult<List<ConcertItem>> getConcerts() {
        List<ConcertInfo> concerts = concertFacade.getConcerts();
        List<ConcertItem> response = concerts.stream()
            .map(ConcertItem::new)
            .collect(Collectors.toList());

        return ApiResult.OK(response);
    }

    @GetMapping("/{concertId}/schedules")
    public ApiResult<List<ConcertScheduleItem>> getConcertSchedules(@PathVariable Long concertId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        List<ConcertScheduleInfo> reservableConcertSchedules = concertFacade.getReservableConcertSchedules(
            concertId, LocalDateTime.now());

        List<ConcertScheduleItem> response = reservableConcertSchedules.stream()
            .map(ConcertScheduleItem::new)
            .collect(Collectors.toList());

        return ApiResult.OK(response);
    }

    @GetMapping("/{concertId}/schedules/{scheduleId}/seats")
    public ApiResult<List<ConcertSeatItem>> getConcertSeats(@PathVariable Long concertId,
        @PathVariable Long scheduleId, @RequestHeader("X-QUEUE-TOKEN") String token) {
        List<ConcertSeatInfo> reservableConcertSeats = concertFacade.getReservableConcertSeats(
            scheduleId, LocalDateTime.now());

        List<ConcertSeatItem> response = reservableConcertSeats.stream()
            .map(ConcertSeatItem::new)
            .collect(Collectors.toList());

        return ApiResult.OK(response);
    }

    @PostMapping("/{concertId}/schedules/{scheduleId}/reservation")
    public ApiResult<ReserveConcertResult> reserveConcert(@PathVariable Long concertId,
        @PathVariable Long scheduleId, @RequestBody ReserveConcert request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        ConcertReservationInfo concertReservationInfo = concertFacade.reserveConcertSeat(
            request.getSeatId(), request.getMemberId(), LocalDateTime.now());

        ReserveConcertResult response = new ReserveConcertResult(concertReservationInfo);
        return ApiResult.OK(response);
    }
}
