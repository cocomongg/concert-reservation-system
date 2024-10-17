package io.hhplus.concert.application.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.exception.ConcertErrorCode;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.infra.db.concert.ConcertJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertScheduleJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcertFacadeIntegrationTest {

    @Autowired
    private ConcertJpaRepository concertJpaRepository;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private ConcertSeatJpaRepository concertSeatJpaRepository;
    
    @Autowired
    private ConcertFacade concertFacade;

    @AfterEach
    public void tearDown() {
        concertScheduleJpaRepository.deleteAllInBatch();
        concertScheduleJpaRepository.deleteAllInBatch();
    }

    @DisplayName("getReservableConcertSchedules() 테스트")
    @Nested
    class GetReservableConcertSchedulesTest {
        @DisplayName("concertId에 해당하는 Concert가 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertNotFound() {
            // given
            long concertId = 0L;

            // when, then
            assertThatThrownBy(() -> concertFacade.getReservableConcertSchedules(concertId, LocalDateTime.now()))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 날짜 이후로 예약 가능한 콘서트 일정이 없으면 빈 리스트가 반환된다.")
        @Test
        void should_ReturnEmptyList_When_NoReservableConcertSchedules() {
            // given
            Concert savedConcert = concertJpaRepository.save(Concert
                .builder()
                .title("title")
                .description("description")
                .createdAt(LocalDateTime.now())
                .build());

            long concertId = savedConcert.getId();
            LocalDateTime now = LocalDateTime.now();

            ConcertSchedule schedule1 = ConcertSchedule.builder()
                .concertId(concertId)
                .scheduledAt(now.minusDays(1))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

            ConcertSchedule schedule2 = ConcertSchedule.builder()
                .concertId(concertId)
                .scheduledAt(now.minusDays(2))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

            concertScheduleJpaRepository.saveAll(List.of(schedule1, schedule2));

            // when
            List<ConcertScheduleInfo> reservableConcertSchedules =
                concertFacade.getReservableConcertSchedules(concertId, now);

            // then
            assertThat(reservableConcertSchedules).hasSize(0);
        }

        @DisplayName("입력된 날짜 이후로 예약 가능한 콘서트 일정 목록을 반환한다.")
        @Test
        void should_ReturnConcertScheduleInfoList_When_ExistReservableConcertSchedules() {
            // given
            Concert savedConcert = concertJpaRepository.save(Concert
                .builder()
                .title("title")
                .description("description")
                .createdAt(LocalDateTime.now())
                .build());

            long concertId = savedConcert.getId();
            LocalDateTime now = LocalDateTime.now();

            ConcertSchedule schedule1 = ConcertSchedule.builder()
                .concertId(concertId)
                .scheduledAt(now.minusDays(1))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdAt(LocalDateTime.now())
                .build();

            ConcertSchedule schedule2 = ConcertSchedule.builder()
                .concertId(concertId)
                .scheduledAt(now.plusDays(1))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdAt(LocalDateTime.now())
                .build();

            ConcertSchedule schedule3 = ConcertSchedule.builder()
                .concertId(concertId)
                .scheduledAt(now.plusDays(2))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdAt(LocalDateTime.now())
                .build();

            concertScheduleJpaRepository.saveAll(List.of(schedule1, schedule2, schedule3));

            // when
            List<ConcertScheduleInfo> reservableConcertSchedules =
                concertFacade.getReservableConcertSchedules(concertId, now);
        
            // then
            assertThat(reservableConcertSchedules).hasSize(2);
        }
    }

    @DisplayName("getReservableConcertSeats() 테스트")
    @Nested
    class GetReservableConcertSeats {
        @DisplayName("concertScheduleId에 해당하는 ConcertSchedule이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertScheduleNotFound() {
            // given
            long concertScheduleId = 0L;

            // when, then
            assertThatThrownBy(() -> concertFacade.getReservableConcertSeats(concertScheduleId, LocalDateTime.now()))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_SCHEDULE_NOT_FOUND.getMessage());
        }

        @DisplayName("입력된 날짜 이후로 예약 가능한 콘서트 좌석이 없으면 빈 리스트가 반환된다.")
        @Test
        void should_ReturnEmptyList_When_NoReservableConcertSeats() {
            // given
            Concert savedConcert = concertJpaRepository.save(Concert
                .builder()
                .title("title")
                .description("description")
                .createdAt(LocalDateTime.now())
                .build());

            ConcertSchedule savedSchedule = concertScheduleJpaRepository.save(ConcertSchedule
                .builder()
                .concertId(savedConcert.getId())
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdAt(LocalDateTime.now())
                .build());

            LocalDateTime now = LocalDateTime.now();

            // when
            List<ConcertSeatInfo> reservableConcertSeats =
                concertFacade.getReservableConcertSeats(savedSchedule.getId(), now);

            // then
            assertThat(reservableConcertSeats).hasSize(0);
        }

        @DisplayName("입력된 날짜 이후로 예약 가능한 콘서트 좌석 목록을 반환한다.")
        @Test
        void should_ReturnConcertSeatInfoList_When_ExistReservableConcertSeats() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            Concert savedConcert = concertJpaRepository.save(Concert
                .builder()
                .title("title")
                .description("description")
                .createdAt(LocalDateTime.now())
                .build());

            ConcertSchedule savedSchedule = concertScheduleJpaRepository.save(ConcertSchedule
                .builder()
                .concertId(savedConcert.getId())
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .startAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdAt(LocalDateTime.now())
                .build());

            LocalDateTime now = LocalDateTime.now();

            concertSeatJpaRepository.saveAll(List.of(
                ConcertSeat.builder()
                    .concertScheduleId(savedSchedule.getId())
                    .seatNumber(1)
                    .status(ConcertSeatStatus.AVAILABLE)
                    .priceAmount(10000)
                    .createdAt(LocalDateTime.now())
                    .build(),
                ConcertSeat.builder()
                    .concertScheduleId(savedSchedule.getId())
                    .seatNumber(2)
                    .status(ConcertSeatStatus.AVAILABLE)
                    .priceAmount(10000)
                    .createdAt(LocalDateTime.now())
                    .build(),
                ConcertSeat.builder()
                    .concertScheduleId(savedSchedule.getId())
                    .seatNumber(3)
                    .status(ConcertSeatStatus.RESERVED_COMPLETE)
                    .priceAmount(10000)
                    .createdAt(LocalDateTime.now())
                    .build(),
                ConcertSeat.builder()
                    .concertScheduleId(savedSchedule.getId())
                    .seatNumber(4)
                    .priceAmount(10000)
                    .tempReservedAt(now.plusMinutes(tempReserveDurationMinutes + 1)) //임시예약
                    .createdAt(LocalDateTime.now())
                    .build()
            ));

            // when
            List<ConcertSeatInfo> reservableConcertSeats =
                concertFacade.getReservableConcertSeats(savedSchedule.getId(), now);

            // then
            assertThat(reservableConcertSeats).hasSize(2);
        }
    }
}