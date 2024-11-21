package io.hhplus.concert.domain.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.hhplus.concert.app.concert.domain.repository.ConcertRepository;
import io.hhplus.concert.app.concert.domain.service.ConcertService;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import io.hhplus.concert.app.concert.domain.model.ConcertSeatStatus;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ConcertService concertService;

    @DisplayName("getReservableConcertSeats() 테스트")
    @Nested
    class GetReservableConcertSeatsTest {
        @DisplayName("현재 시간 기준으로 예약 가능한 concertSeat이 없다면 빈 리스트를 반환한다.")
        @Test
        void should_ReturnEmptyList_When_ReservableSeatNotExist() {
            // given
            long concertScheduleId = 1L;
            GetReservableConcertSeats query =
                new GetReservableConcertSeats(concertScheduleId, LocalDateTime.now());

            when(concertRepository.getConcertSeats(concertScheduleId))
                .thenReturn(List.of());

            // when
            List<ConcertSeat> result = concertService.getReservableConcertSeats(query);

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("현재 시간 기준으로 예약 가능한 concertSeat이 있다면 해당 concertSeat 리스트를 반환한다.")
        @Test
        void should_ReturnConcertSeatList_When_ReservableConcertSeatExist() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            long concertScheduleId = 1L;
            LocalDateTime now = LocalDateTime.now();

            ConcertSeat reservableSeat1 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(1)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();

            ConcertSeat reservableSeat2 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(2)
                .tempReservedAt(now.minusMinutes(tempReserveDurationMinutes + 1))
                .build();

            ConcertSeat reservedSeat1 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(3)
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .build();

            ConcertSeat reservedSeat2 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(4)
                .tempReservedAt(now.minusMinutes(tempReserveDurationMinutes - 1))
                .build();

            List<ConcertSeat> concertSeats = List.of(reservableSeat1, reservableSeat2,
                reservedSeat1, reservedSeat2);

            when(concertRepository.getConcertSeats(concertScheduleId))
                .thenReturn(concertSeats);

            GetReservableConcertSeats query =
                new GetReservableConcertSeats(concertScheduleId, now);

            // when
            List<ConcertSeat> result = concertService.getReservableConcertSeats(query);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ConcertSeat::getSeatNumber)
                .containsExactly(
                    reservableSeat1.getSeatNumber(),
                    reservableSeat2.getSeatNumber()
                );
        }
    }

    @DisplayName("reserveConcertSeat() 테스트")
    @Nested
    class ReserveConcertSeatTest {
        @DisplayName("concertSeat이 존재하지 않는다면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_ConcertSeatNotExist() {
            // given
            long concertSeatId = 1L;
            LocalDateTime now = LocalDateTime.now();

            when(concertRepository.getConcertSeatWithLock(any(GetConcertSeat.class)))
                .thenThrow(new CoreException(CoreErrorType.Concert.CONCERT_SEAT_NOT_FOUND));

            ReserveConcertSeat command =
                new ReserveConcertSeat(concertSeatId, now, ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

            // when, then
            assertThatThrownBy(() -> concertService.reserveConcertSeat(command))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("concertSeat이 예약 불가능한 상태라면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_NotReservable() {
            // given
            long concertSeatId = 1L;
            LocalDateTime now = LocalDateTime.now();

            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .build();

            when(concertRepository.getConcertSeatWithLock(any(GetConcertSeat.class)))
                .thenReturn(concertSeat);

            ReserveConcertSeat command =
                new ReserveConcertSeat(concertSeatId, now, ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

            // when, then
            assertThatThrownBy(() -> concertService.reserveConcertSeat(command))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.NOT_RESERVABLE_SEAT.getMessage());
        }

        @DisplayName("concertSeat이 예약 가능한 상태라면 예약한다.")
        @Test
        void should_ReserveConcertSeat_When_IsReservable() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            long concertSeatId = 1L;
            LocalDateTime now = LocalDateTime.now();

            ConcertSeat concertSeat = ConcertSeat.builder()
                .status(ConcertSeatStatus.AVAILABLE)
                .build();

            when(concertRepository.getConcertSeatWithLock(any(GetConcertSeat.class)))
                .thenReturn(concertSeat);

            ReserveConcertSeat command =
                new ReserveConcertSeat(concertSeatId, now, tempReserveDurationMinutes);

            // when
            ConcertSeat result = concertService.reserveConcertSeat(command);

            // then
            boolean temporarilyReserved =
                result.isTemporarilyReserved(LocalDateTime.now(), tempReserveDurationMinutes);
            assertThat(temporarilyReserved).isTrue();
            assertThat(result.getTempReservedAt()).isNotNull();
        }
    }
}