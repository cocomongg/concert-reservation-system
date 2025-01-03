package io.hhplus.concert.domain.concert.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import io.hhplus.concert.app.concert.domain.model.ConcertSeatStatus;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConcertSeatTest {

    @DisplayName("isSeatReservable() 테스트")
    @Nested
    class IsSeatReservableTest {
        @DisplayName("좌석이 에약된 상태면, false를 반환한다.")
        @Test
        void should_ReturnFalse_When_StatusIsReserved() {
            // given
            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .build();

            // when
            boolean result = concertSeat.isReservable(LocalDateTime.now(), 5);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("좌석이 임시예약된 상태면, false를 반환한다.")
        @Test
        void should_ReturnFalse_When_StatusIsTempReservedAndStillValid() {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int tempReserveDurationMinutes = 5;

            ConcertSeat concertSeat = ConcertSeat.builder()
                .tempReservedAt(currentTime.minusMinutes(tempReserveDurationMinutes - 1))
                .build();

            // when
            boolean result = concertSeat.isReservable(currentTime, tempReserveDurationMinutes);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("좌석이 사용가능한 상태일 때, true를 반환한다.")
        @Test
        void should_ReturnTrue_When_StatusIsAvailable() {
            // given
            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.AVAILABLE)
                .build();

            // when
            boolean result = concertSeat.isReservable(LocalDateTime.now(), 5);

            // then
            assertThat(result).isTrue();
        }
    }

    @DisplayName("isTemporarilyReserved() 테스트")
    @Nested
    class IsTemporarilyReservedTest {
        @DisplayName("tempReservedAt이 없다면 false 반환")
        @Test
        void should_ReturnFalse_When_TempReservedAtIsNull () {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int tempReserveDurationMinutes = 5;

            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.AVAILABLE)
                .build();

            // when
            boolean result = concertSeat.isTemporarilyReserved(currentTime, tempReserveDurationMinutes);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("tempReservedAt이 존재하고, 임시예약 가능 시간이 지나지 않았다면 true를 반환한다.")
        @Test
        void should_ReturnTrue_When_TempReservedAtExistAndStillValid () {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int tempReserveDurationMinutes = 5;

            ConcertSeat concertSeat = ConcertSeat.builder()
                .tempReservedAt(currentTime.minusMinutes(tempReserveDurationMinutes - 1))
                .build();

            // when
            boolean result = concertSeat.isTemporarilyReserved(currentTime, tempReserveDurationMinutes);

            // then
            assertThat(result).isTrue();
        }

        @DisplayName("tempReservedAt이 존재하고, 임시예약 가능 시간이 지났다면 false를 반환한다.")
        @Test
        void should_ReturnFalse_When_TempReservedAtExistAtAndStillNotValid () {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int tempReserveDurationMinutes = 5;

            ConcertSeat concertSeat = ConcertSeat.builder()
                .tempReservedAt(currentTime.minusMinutes(tempReserveDurationMinutes + 1))
                .build();

            // when
            boolean result =
                concertSeat.isTemporarilyReserved(currentTime, tempReserveDurationMinutes);

            // then
            assertThat(result).isFalse();
        }
    }

    @DisplayName("reserve()를 호출하면 임시예약된다.")
    @Nested
    class ReserveTest {
        @DisplayName("임시예약이 불가능한 상태라면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_NotReservable () {
            // given
            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .build();

            // when, then
            assertThatThrownBy(() -> concertSeat.reserve(LocalDateTime.now(), 5))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.NOT_RESERVABLE_SEAT.getMessage());
        }

        @DisplayName("임시예약이 가능한 상태라면 임시예약된다.")
        @Test
        void should_ReserveTemporarily_When_Reservable () {
            // given
            LocalDateTime currentTime = LocalDateTime.now();
            int tempReserveDurationMinutes = 5;

            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.AVAILABLE)
                .build();

            // when
            concertSeat.reserve(currentTime, tempReserveDurationMinutes);

            // then
            boolean temporarilyReserved =
                concertSeat.isTemporarilyReserved(LocalDateTime.now(), tempReserveDurationMinutes);
            assertThat(temporarilyReserved).isTrue();
        }
    }

    @DisplayName("completeReservation()을 호출하면 예약이 완료된다.")
    @Test
    void should_ReservedComplete_When_CallCompleteReservation () {
        // given
        LocalDateTime now = LocalDateTime.now();
        ConcertSeat concertSeat = ConcertSeat.builder()
            .status(ConcertSeatStatus.AVAILABLE)
            .build();

        // when
        concertSeat.confirmReservation(now);

        // then
        assertThat(concertSeat.getStatus()).isEqualTo(ConcertSeatStatus.RESERVED_COMPLETE);
        assertThat(concertSeat.getReservedAt()).isEqualTo(now);
    }
}