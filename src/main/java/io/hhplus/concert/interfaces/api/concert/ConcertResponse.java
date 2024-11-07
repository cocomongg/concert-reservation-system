package io.hhplus.concert.interfaces.api.concert;

import io.hhplus.concert.application.concert.ConcertDto.ConcertInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertReservationInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertSeatInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class ConcertResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ConcertItem {
        @Schema(description = "콘서트 Id")
        private Long concertId;

        @Schema(description = "콘서트 제목")
        private String concertTitle;

        @Schema(description = "콘서트 설명")
        private String concertDescription;

        @Schema(description = "콘서트 생성일시")
        private LocalDateTime createdAt;

        public ConcertItem(ConcertInfo concertInfo) {
            this.concertId = concertInfo.getId();
            this.concertTitle = concertInfo.getTitle();
            this.concertDescription = concertInfo.getDescription();
            this.createdAt = concertInfo.getCreatedAt();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ConcertScheduleItem {
        @Schema(description = "콘서트 스케줄 Id")
        private Long concertScheduleId;

        @Schema(description = "콘서트 스케줄 날짜")
        private LocalDateTime scheduledAt;

        @Schema(description = "콘서트 시작 일시")
        private LocalDateTime concertStartAt;

        @Schema(description = "콘서트 종료 일시")
        private LocalDateTime concertEndAt;

        public ConcertScheduleItem(ConcertScheduleInfo ConcertScheduleInfo) {
            this.concertScheduleId = ConcertScheduleInfo.getId();
            this.scheduledAt = ConcertScheduleInfo.getScheduledAt();
            this.concertStartAt = ConcertScheduleInfo.getStartAt();
            this.concertEndAt = ConcertScheduleInfo.getEndAt();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ConcertSeatItem {
        @Schema(description = "콘서트 좌석 Id")
        private Long concertSeatId;

        @Schema(description = "콘서트 좌석 번호")
        private int seatNumber;

        @Schema(description = "콘서트 좌석 가격")
        private int priceAmount;

        public ConcertSeatItem(ConcertSeatInfo concertSeatInfo) {
            this.concertSeatId = concertSeatInfo.getId();
            this.seatNumber = concertSeatInfo.getSeatNumber();
            this.priceAmount = concertSeatInfo.getPriceAmount();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ReserveConcertResult {
        @Schema(description = "콘서트 예약 Id")
        private Long reservationId;

        @Schema(description = "결제 할 가격")
        private int priceAmount;

        public ReserveConcertResult(ConcertReservationInfo concertReservationInfo) {
            this.reservationId = concertReservationInfo.getId();
            this.priceAmount = concertReservationInfo.getPaidAmount();
        }
    }
}
