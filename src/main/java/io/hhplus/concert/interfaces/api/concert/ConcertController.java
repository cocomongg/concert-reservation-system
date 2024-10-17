package io.hhplus.concert.interfaces.api.concert;

import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertScheduleItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertSeatItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ReserveConcertResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/concerts")
@RestController
public class ConcertController implements ConcertControllerDocs{

    @GetMapping("")
    public ApiResult<List<ConcertItem>> getConcerts() {
        return ApiResult.OK(
            List.of(ConcertItem.builder()
                .concertId(1L)
                .concertTitle("콘서트 제목")
                .concertDescription("콘서트 설명")
                .createdAt(LocalDateTime.now())
                .build())
        );
    }

    @GetMapping("/{concertId}/schedules")
    public ApiResult<List<ConcertScheduleItem>> getConcertSchedules(@PathVariable Long concertId,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(List.of(
            ConcertScheduleItem.builder()
                .concertScheduleId(1L)
                .concertScheduledDate(LocalDate.now())
                .concertStartAt(LocalDateTime.now())
                .concertEndAt(LocalDateTime.now().plusHours(1))
                .build(),
            ConcertScheduleItem.builder()
                .concertScheduleId(2L)
                .concertScheduledDate(LocalDate.now().plusDays(2))
                .concertStartAt(LocalDateTime.now().plusDays(2))
                .concertEndAt(LocalDateTime.now().plusDays(2))
                .build()
        ));
    }

    @GetMapping("/{concertId}/schedules/{scheduleId}/seats")
    public ApiResult<List<ConcertSeatItem>> getConcertSeats(@PathVariable Long concertId,
        @PathVariable Long scheduleId, @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(List.of(
            ConcertSeatItem.builder()
                .concertSeatId(1L)
                .seatNumber(17)
                .priceAmount(10_000)
                .build(),
            ConcertSeatItem.builder()
                .concertSeatId(2L)
                .seatNumber(7)
                .priceAmount(15_000)
                .build()
        ));
    }

    @PostMapping("/{concertId}/schedules/{scheduleId}/reservation")
    public ApiResult<ReserveConcertResult> reserveConcert(@PathVariable Long concertId,
        @PathVariable Long scheduleId, @RequestBody ConcertRequest.ReserveConcert request,
        @RequestHeader("X-QUEUE-TOKEN") String token) {
        return ApiResult.OK(new ConcertResponse.ReserveConcertResult(1L, 10_000));
    }
}
