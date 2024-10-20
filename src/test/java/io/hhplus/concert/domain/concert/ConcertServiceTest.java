package io.hhplus.concert.domain.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
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
}