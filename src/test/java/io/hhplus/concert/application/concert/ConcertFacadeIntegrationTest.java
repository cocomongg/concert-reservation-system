package io.hhplus.concert.application.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.application.concert.ConcertDto.ConcertScheduleInfo;
import io.hhplus.concert.application.concert.ConcertDto.ConcertSeatInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.exception.ConcertErrorCode;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.Concert;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSchedule;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.domain.member.exception.MemberErrorCode;
import io.hhplus.concert.domain.member.exception.MemberException;
import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.infra.db.concert.ConcertJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertReservationJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertScheduleJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import io.hhplus.concert.infra.db.member.MemberJpaRepository;
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
    private ConcertReservationJpaRepository concertReservationJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;
    
    @Autowired
    private ConcertFacade concertFacade;

    @AfterEach
    public void tearDown() {
        concertJpaRepository.deleteAllInBatch();
        concertScheduleJpaRepository.deleteAllInBatch();
        concertSeatJpaRepository.deleteAllInBatch();
        concertReservationJpaRepository.deleteAllInBatch();
        memberJpaRepository.deleteAllInBatch();
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

    @DisplayName("reserveConcertSeat() 테스트")
    @Nested
    class ReserveConcertSeatTest {
        @DisplayName("memberId에 해당하는 Member가 없다면 MemberException이 발생한다.")
        @Test
        void should_ThrowMemberException_When_MemberNotFound() {
            // given
            long concertSeatId = 1L;
            long memberId = 0L;
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(
                () -> concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("concertSeatId에 해당하는 ConcertSeat이 없다면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertSeatNotFound() {
            // given
            Member defaultMember = this.createDefaultMember();

            long concertSeatId = 0L;
            long memberId = defaultMember.getId();
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(
                () -> concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("이미 예약이 완료된 좌석이라면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_NotReservableSeat() {
            // given
            Member defaultMember = this.createDefaultMember();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat
                .builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .status(ConcertSeatStatus.RESERVED_COMPLETE)
                .priceAmount(10000)
                .createdAt(LocalDateTime.now())
                .build());

            long concertSeatId = savedSeat.getId();
            long memberId = defaultMember.getId();
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(
                () -> concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.NOT_RESERVABLE_SEAT.getMessage());
        }

        @DisplayName("임시 예약된 좌석이라면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_TempReservedSeat() {
            // given
            Member defaultMember = this.createDefaultMember();

            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat
                .builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .priceAmount(10000)
                .tempReservedAt(LocalDateTime.now().plusMinutes(tempReserveDurationMinutes + 1))
                .createdAt(LocalDateTime.now())
                .build());

            long concertSeatId = savedSeat.getId();
            long memberId = defaultMember.getId();
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(
                () -> concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertErrorCode.NOT_RESERVABLE_SEAT.getMessage());
        }

        @DisplayName("예약이 완료되면 ConcertReservationInfo를 반환한다.")
        @Test
        void should_ReturnConcertReservationInfo_When_CompleteReservation() {
            // given
            Member defaultMember = this.createDefaultMember();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat
                .builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .status(ConcertSeatStatus.AVAILABLE)
                .priceAmount(10000)
                .createdAt(LocalDateTime.now())
                .build());

            long concertSeatId = savedSeat.getId();
            long memberId = defaultMember.getId();
            LocalDateTime dateTime = LocalDateTime.now();

            // when
            ConcertDto.ConcertReservationInfo reservationInfo =
                concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime);

            // then
            assertThat(reservationInfo.getMemberId()).isEqualTo(memberId);
            assertThat(reservationInfo.getConcertSeatId()).isEqualTo(concertSeatId);
            assertThat(reservationInfo.getStatus()).isEqualTo(ConcertReservationStatus.PENDING);
            assertThat(reservationInfo.getReservedAt()).isNotNull();
        }

        @DisplayName("예약이 완료되면 ConcertSeat의 tempReservedAt이 예약이 완료된 시간으로 업데이트된다.")
        @Test
        void should_UpdateTempReservedAt_When_CompleteReservation() {
            // given
            Member defaultMember = this.createDefaultMember();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat
                .builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .status(ConcertSeatStatus.AVAILABLE)
                .priceAmount(10000)
                .createdAt(LocalDateTime.now())
                .build());

            long concertSeatId = savedSeat.getId();
            long memberId = defaultMember.getId();
            LocalDateTime dateTime = LocalDateTime.now();

            // when
            concertFacade.reserveConcertSeat(concertSeatId, memberId, dateTime);

            // then
            ConcertSeat updatedSeat = concertSeatJpaRepository.findById(concertSeatId).orElse(null);
            assertThat(updatedSeat).isNotNull();
            assertThat(updatedSeat.getTempReservedAt()).isEqualTo(dateTime);
        }

        public Member createDefaultMember() {
            return memberJpaRepository.save(Member.builder()
                .name("name")
                .email("email")
                .createdAt(LocalDateTime.now())
                .build());
        }
    }
}