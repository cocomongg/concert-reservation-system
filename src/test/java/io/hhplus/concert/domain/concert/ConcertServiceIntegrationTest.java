package io.hhplus.concert.domain.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.ReserveConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcert;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSchedule;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSchedules;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetReservableConcertSeats;
import io.hhplus.concert.domain.concert.exception.ConcertErrorCode;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.infra.db.concert.ConcertJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertReservationJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertScheduleJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ConcertServiceIntegrationTest {

    @Autowired
    private ConcertJpaRepository concertJpaRepository;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private ConcertSeatJpaRepository concertSeatJpaRepository;

    @Autowired
    private ConcertReservationJpaRepository concertReservationJpaRepository;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("getConcertSeat() 테스트")
    @Nested
    class GetConcertSeatTest {
        @DisplayName("id에 해당하는 concertSeat이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_NotFound () {
            // given
            GetConcertSeat query = new GetConcertSeat(0L);

            // when, then
            assertThatThrownBy(() -> concertService.getConcertSeat(query))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("id에 해당하는 concertSeat을 반환한다.")
        @Test
        void should_ReturnConcertSeat_When_Found () {
            // given
            ConcertSeat savedConcertSeat = concertSeatJpaRepository.save(
                ConcertSeat.builder()
                    .concertScheduleId(1L)
                    .seatNumber(1)
                    .status(ConcertSeatStatus.AVAILABLE)
                    .priceAmount(1000)
                    .createdAt(LocalDateTime.now())
                    .build());

            GetConcertSeat query = new GetConcertSeat(savedConcertSeat.getId());

            // when
            ConcertSeat result = concertService.getConcertSeat(query);

            // then
            assertThat(result.getId()).isEqualTo(savedConcertSeat.getId());
            assertThat(result.getConcertScheduleId()).isEqualTo(savedConcertSeat.getConcertScheduleId());
            assertThat(result.getStatus()).isEqualTo(savedConcertSeat.getStatus());
            assertThat(result.getPriceAmount()).isEqualTo(savedConcertSeat.getPriceAmount());
            assertThat(result.getCreatedAt()).isEqualTo(savedConcertSeat.getCreatedAt());
        }
    }
    
    @DisplayName("createConcertReservation() 테스트")
    @Nested
    class CreateConcertReservationTest {
        @DisplayName("입력된 값을 통해 pending상태인 concertReservation을 생성 후 저장한다.")
        @Test
        void should_SaveConcertReservation_When_ByInput() {
            // given
            CreateConcertReservation command = new CreateConcertReservation(1L, 1L,
                LocalDateTime.now());

            // when
            ConcertReservation concertReservation = concertService.createConcertReservation(
                command);

            // then
            ConcertReservation result = concertReservationJpaRepository.findById(
                    concertReservation.getId())
                .orElse(null);

            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(command.getMemberId());
            assertThat(result.getConcertSeatId()).isEqualTo(command.getConcertSeatId());
            assertThat(result.getStatus()).isEqualTo(ConcertReservationStatus.PENDING);
            assertThat(result.getReservedAt()).isEqualTo(command.getDateTime());
        }

        @DisplayName("입력된 값을 통해 pending상태인 concertReservation을 생성 후 반환한다.")
        @Test
        void should_ReturnConcertReservation_When_ByInput() {
            // given
            CreateConcertReservation command = new CreateConcertReservation(1L, 1L,
                LocalDateTime.now());

            // when
            ConcertReservation result = concertService.createConcertReservation(
                command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(command.getMemberId());
            assertThat(result.getConcertSeatId()).isEqualTo(command.getConcertSeatId());
            assertThat(result.getStatus()).isEqualTo(ConcertReservationStatus.PENDING);
            assertThat(result.getReservedAt()).isEqualTo(command.getDateTime());
        }
    }

    @DisplayName("getConcert() 테스트")
    @Nested
    class GetConcertTest {
        @DisplayName("id에 해당하는 concert가 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertNotFound() {
            // given
            GetConcert query = new GetConcert(0L);

            // when, then
            assertThatThrownBy(() -> concertService.getConcert(query))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_NOT_FOUND.getMessage());
        }

        @DisplayName("id에 해당하는 concert을 반환한다.")
        @Test
        void should_ReturnConcert_When_ConcertFound () {
            // given
            Concert concert = new Concert(null, "title", "description",
                LocalDateTime.now(), LocalDateTime.now());

            Concert savedConcert = concertJpaRepository.save(concert);

            // when
            Concert result = concertService.getConcert(new GetConcert(savedConcert.getId()));

            // then
            assertThat(result.getId()).isEqualTo(savedConcert.getId());
            assertThat(result.getTitle()).isEqualTo(savedConcert.getTitle());
            assertThat(result.getDescription()).isEqualTo(savedConcert.getDescription());
            assertThat(result.getCreatedAt()).isEqualTo(savedConcert.getCreatedAt());
        }
    }

    @DisplayName("getConcertSchedule() 테스트")
    @Nested
    class GetConcertScheduleTest {
        @DisplayName("id에 해당하는 concertSchedule이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertScheduleNotFound() {
            // given
            GetConcertSchedule query = new GetConcertSchedule(0L);

            // when, then
            assertThatThrownBy(() -> concertService.getConcertSchedule(query))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_SCHEDULE_NOT_FOUND.getMessage());
        }

        @DisplayName("id에 해당하는 concertSchedule을 반환한다.")
        @Test
        void should_ReturnConcertSchedule_When_ConcertScheduleFound() {
            // given
            LocalDateTime now = LocalDateTime.now();
            ConcertSchedule concertSchedule = new ConcertSchedule(null, 1L, now, now,
                now, now, now);

            ConcertSchedule savedConcertSchedule =
                concertScheduleJpaRepository.save(concertSchedule);

            GetConcertSchedule query = new GetConcertSchedule(savedConcertSchedule.getId());

            // when
            ConcertSchedule result = concertService.getConcertSchedule(query);

            // then
            assertThat(result.getId()).isEqualTo(savedConcertSchedule.getId());
            assertThat(result.getConcertId()).isEqualTo(savedConcertSchedule.getConcertId());
            assertThat(result.getScheduledAt()).isEqualTo(savedConcertSchedule.getScheduledAt());
            assertThat(result.getStartAt()).isEqualTo(savedConcertSchedule.getStartAt());
            assertThat(result.getEndAt()).isEqualTo(savedConcertSchedule.getEndAt());
            assertThat(result.getCreatedAt()).isEqualTo(savedConcertSchedule.getCreatedAt());
        }
    }

    @DisplayName("getReservableConcertSchedules() 테스트")
    @Nested
    class GetReservableConcertSchedulesTest {
        @DisplayName("현재 시간 기준으로 예약 가능한 concertSchedule이 없다면 빈 리스트를 반환한다.")
        @Test
        void should_ReturnEmptyList_When_ReservableScheduleNotExist() {
            // given
            GetReservableConcertSchedules query = new GetReservableConcertSchedules(1L,
                LocalDateTime.now());

            // when, then
            assertThat(concertService.getReservableConcertSchedules(query)).isEmpty();
        }

        @DisplayName("현재 시간 기준으로 예약 가능한 concertSchedule 목록을 반환한다.")
        @Test
        void should_ReturnScheduleList_When_ReservableScheduleExist() {
            // given
            LocalDateTime now = LocalDateTime.now();
            ConcertSchedule concertSchedule1 = new ConcertSchedule(null, 1L, now.minusDays(1), now,
                now, now, null);
            ConcertSchedule concertSchedule2 = new ConcertSchedule(null, 1L, now.plusHours(1), now,
                now, now, null);
            ConcertSchedule concertSchedule3 = new ConcertSchedule(null, 1L, now.plusDays(1), now,
                now, now, null);

            List<ConcertSchedule> concertSchedules = List.of(concertSchedule1, concertSchedule2,
                concertSchedule3);
            concertScheduleJpaRepository.saveAll(concertSchedules);

            // when
            List<ConcertSchedule> result = concertService.getReservableConcertSchedules(
                new GetReservableConcertSchedules(1L, now));

            // then
            assertThat(result).hasSize(2);
        }
    }

    @DisplayName("getReservableConcertSeats() 테스트")
    @Nested
    class GetReservableConcertSeatsTest {
        @DisplayName("현재 시간 기준으로 예약 가능한 concertSeat이 없다면 빈 리스트를 반환한다.")
        @Test
        void should_ReturnEmptyList_When_ReservableConcertSeatNotExist() {
            // given
            GetReservableConcertSeats query = new GetReservableConcertSeats(1L,
                LocalDateTime.now());

            // when
            List<ConcertSeat> result = concertService.getReservableConcertSeats(
                query);

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("현재 시간 기준으로 예약 가능한 concertSeat 목록을 반환한다.")
        @Test
        void should_ReturnConcertSeatList_When_ReservableConcertSeatExist() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            LocalDateTime now = LocalDateTime.now();
            Long concertScheduleId = 1L;
            ConcertSeat reservableConcertSeat1 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(1)
                .status(ConcertSeatStatus.AVAILABLE)
                .priceAmount(1000)
                .createdAt(now)
                .build();

            ConcertSeat reservableConcertSeat2 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(2)
                .priceAmount(1000)
                .tempReservedAt(now.minusMinutes(tempReserveDurationMinutes + 1))
                .createdAt(now)
                .build();

            ConcertSeat reservedConcertSeat3 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(3)
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .priceAmount(1000)
                .createdAt(now)
                .build();

            ConcertSeat reservedConcertSeat4 = ConcertSeat.builder()
                .concertScheduleId(concertScheduleId)
                .seatNumber(4)
                .priceAmount(1000)
                .tempReservedAt(now.minusMinutes(tempReserveDurationMinutes - 1))
                .createdAt(now)
                .build();

            List<ConcertSeat> concertSeats = List.of(reservableConcertSeat1, reservableConcertSeat2,
                reservedConcertSeat3, reservedConcertSeat4);
            concertSeatJpaRepository.saveAll(concertSeats);

            GetReservableConcertSeats query = new GetReservableConcertSeats(concertScheduleId, now);

            // when
            List<ConcertSeat> result = concertService.getReservableConcertSeats(
                query);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(ConcertSeat::getSeatNumber)
                .containsExactlyInAnyOrder(
                    reservableConcertSeat1.getSeatNumber(),
                    reservableConcertSeat2.getSeatNumber()
                );
        }
    }

    @DisplayName("getConcertReservation() 테스트")
    @Nested
    class GetConcertReservationTest {
        @DisplayName("id에 해당하는 concertReservation이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertReservationNotFound() {
            // given
            long concertReservationId = 0L;
            GetConcertReservation query = new GetConcertReservation(concertReservationId);

            // when, then
            assertThatThrownBy(() -> concertService.getConcertReservation(query))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_RESERVATION_NOT_FOUND.getMessage());
        }

        @DisplayName("id에 해당하는 concertReservation을 반환한다.")
        @Test
        void should_ReturnConcertReservation_When_ConcertReservationFound() {
            // given
            ConcertReservation concertReservation = new ConcertReservation(null, 1L, 1L,
                ConcertReservationStatus.PENDING, null, LocalDateTime.now(), null);

            ConcertReservation savedConcertReservation =
                concertReservationJpaRepository.save(concertReservation);

            GetConcertReservation query =
                new GetConcertReservation(savedConcertReservation.getId());
            // when
            ConcertReservation result = concertService.getConcertReservation(query);

            // then
            assertThat(result.getMemberId()).isEqualTo(savedConcertReservation.getMemberId());
            assertThat(result.getConcertSeatId()).isEqualTo(savedConcertReservation.getConcertSeatId());
            assertThat(result.getStatus()).isEqualTo(savedConcertReservation.getStatus());
        }
    }

    @DisplayName("reserveConcertSeat() 테스트")
    @Nested
    class ReserveConcertSeatTest {

        @DisplayName("id에 해당하는 concertSeat이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertSeatNotFound() {
            // given
            long concertSeatId = 0L;
            ReserveConcertSeat command = new ReserveConcertSeat(concertSeatId, LocalDateTime.now(),
                ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

            // when, then
            assertThatThrownBy(() -> concertService.reserveConcertSeat(command))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("예약이 불가능한 상태면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_NotReservable() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            LocalDateTime now = LocalDateTime.now();
            ConcertSeat concertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .priceAmount(1000)
                .createdAt(now)
                .build();

            ConcertSeat savedConcertSeat = concertSeatJpaRepository.save(concertSeat);

            ReserveConcertSeat command = new ReserveConcertSeat(savedConcertSeat.getId(), now,
                tempReserveDurationMinutes);

            // when, then
            assertThatThrownBy(() -> concertService.reserveConcertSeat(command))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.NOT_RESERVABLE_SEAT.getMessage());
        }

        @DisplayName("예약이 가능한 상태면 concertSeat을 임시 예약한다.")
        @Test
        void should_ReserveConcertSeat_When_Reservable() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            LocalDateTime now = LocalDateTime.now();
            ConcertSeat concertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .status(ConcertSeatStatus.AVAILABLE)
                .priceAmount(1000)
                .createdAt(now)
                .build();

            ConcertSeat savedConcertSeat = concertSeatJpaRepository.save(concertSeat);

            ReserveConcertSeat command = new ReserveConcertSeat(savedConcertSeat.getId(), now,
                tempReserveDurationMinutes);

            // when
            ConcertSeat result = concertService.reserveConcertSeat(command);

            // then
            boolean temporarilyReserved = result.isTemporarilyReserved(LocalDateTime.now(),
                tempReserveDurationMinutes);

            assertThat(temporarilyReserved).isTrue();
            assertThat(result.getTempReservedAt()).isEqualTo(now);
        }
    }
}