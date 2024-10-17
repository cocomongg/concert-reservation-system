package io.hhplus.concert.interfaces.api.concert;

import io.hhplus.concert.interfaces.api.common.response.ApiResult;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertScheduleItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ConcertSeatItem;
import io.hhplus.concert.interfaces.api.concert.ConcertResponse.ReserveConcertResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "콘서트 API", description = "콘서트 관련 API")
public interface ConcertControllerDocs {

    @Operation(summary = "콘서트 목록 조회", description = "콘서트 목록 반환")
    ApiResult<List<ConcertItem>> getConcerts();

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "예약 가능 날짜 조회", description = "예약 가능 날짜 목록 반환")
    ApiResult<List<ConcertScheduleItem>> getConcertSchedules(Long concertId,
        @Parameter(hidden = true) String token);

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "예약 가능한 좌석 정보 조회", description = "예약 가능한 좌석 목록 반환")
    ApiResult<List<ConcertSeatItem>> getConcertSeats(Long concertId, Long scheduleId,
        @Parameter(hidden = true) String token);

    @SecurityRequirement(name = "queueToken")
    @Operation(summary = "좌석 예약 요청", description = "날짜와 좌석 정보를 입력받아 좌석을 예약 처리")
    ApiResult<ReserveConcertResult> reserveConcert(Long concertId, Long scheduleId,
        ConcertRequest.ReserveConcert request, @Parameter(hidden = true) String token);
}
