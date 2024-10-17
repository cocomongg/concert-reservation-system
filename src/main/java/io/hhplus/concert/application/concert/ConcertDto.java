package io.hhplus.concert.application.concert;

import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConcertDto {

    @Getter
    @AllArgsConstructor
    public static class ConcertScheduleInfo {
        private final Long id;
        private final Long concertId;
        private final String scheduledAt;
        private final String startAt;
        private final String endAt;
        private final String createdAt;

        public ConcertScheduleInfo(ConcertSchedule concertSchedule) {
            this.id = concertSchedule.getId();
            this.concertId = concertSchedule.getConcertId();
            this.scheduledAt = concertSchedule.getScheduledAt().toString();
            this.startAt = concertSchedule.getStartAt().toString();
            this.endAt = concertSchedule.getEndAt().toString();
            this.createdAt = concertSchedule.getCreatedAt().toString();
        }
    }

}
