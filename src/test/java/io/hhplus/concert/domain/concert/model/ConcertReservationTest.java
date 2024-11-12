package io.hhplus.concert.domain.concert.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConcertReservationTest {

    @DisplayName("completeReservation을 호출하면 예약이 완료된다.")
    @Test
    void should_ReservationCompleted_When_CallCompleteReservation () {
        // given
        LocalDateTime now = LocalDateTime.now();
        ConcertReservation concertReservation = new ConcertReservation(1L, 1L, 1L, 1000,
            ConcertReservationStatus.PENDING, null, LocalDateTime.now(), null);

        // when
        concertReservation.confirmReservation(now);

        // then
        assertThat(concertReservation.getStatus()).isEqualTo(ConcertReservationStatus.COMPLETED);
        assertThat(concertReservation.getReservedAt()).isEqualTo(now);
    }
}