package io.hhplus.concert.domain.concert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.concert.dto.ConcertCommand.CreateConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.exception.ConcertErrorCode;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.infra.db.concert.ConcertReservationJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcertServiceIntegrationTest {

    @Autowired
    private ConcertSeatJpaRepository concertSeatJpaRepository;

    @Autowired
    private ConcertReservationJpaRepository concertReservationJpaRepository;

    @Autowired
    private ConcertService concertService;

    @AfterEach
    public void teardown() {
        concertSeatJpaRepository.deleteAllInBatch();
        concertReservationJpaRepository.deleteAllInBatch();
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

}