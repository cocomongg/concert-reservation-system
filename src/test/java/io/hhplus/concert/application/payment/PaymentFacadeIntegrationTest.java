package io.hhplus.concert.application.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.exception.ConcertException;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.domain.member.exception.MemberPointErrorCode;
import io.hhplus.concert.domain.member.exception.MemberPointException;
import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.infra.db.concert.ConcertReservationJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import io.hhplus.concert.infra.db.member.MemberPointJpaRepository;
import io.hhplus.concert.infra.db.payment.PaymentJpaRepository;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class PaymentFacadeIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private MemberPointJpaRepository memberPointJpaRepository;

    @Autowired
    private WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Autowired
    private ConcertSeatJpaRepository concertSeatJpaRepository;

    @Autowired
    private ConcertReservationJpaRepository concertReservationJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("payment() 테스트")
    @Nested
    class PaymentTest {
        @DisplayName("reservationId에 해당하는 ConcertReservation이 존재하지 않으면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertReservationNotFound() {
            // given
            Long reservationId = 1L;
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(() -> paymentFacade.payment(reservationId, token, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertException.CONCERT_RESERVATION_NOT_FOUND.getMessage());
        }

        @DisplayName("reservationId에 해당하는 ConcertReservation의 ConcertSeat이 존재하지 않으면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_ConcertSeatNotFound() {
            // given
            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(1L)
                    .concertSeatId(0L)
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(() -> paymentFacade.payment(reservationId, token, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertException.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("ConcertSeat이 임시 예약이 만료되면 ConcertException이 발생한다.")
        @Test
        void should_ThrowConcertException_When_TemporaryReservationExpired() {
            // given
            int tempReserveDurationMinutes = ServicePolicy.TEMP_RESERVE_DURATION_MINUTES;
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime.minusMinutes(tempReserveDurationMinutes + 1))
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(1L)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();

            // when, then
            assertThatThrownBy(() -> paymentFacade.payment(reservationId, token, dateTime))
                .isInstanceOf(ConcertException.class)
                .hasMessage(ConcertException.TEMPORARY_RESERVATION_EXPIRED.getMessage());
        }

        @DisplayName("포인트 잔액이 부족하면 MemberException이 발생한다.")
        @Test
        void should_ThrowMemberPointException_When_PointIsNotEnough() {
            // given
            Long memberId = 1L;
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(0)
                .build());

            // when, then
            assertThatThrownBy(() -> paymentFacade.payment(reservationId, token, dateTime))
                .isInstanceOf(MemberPointException.class)
                .hasMessage(MemberPointErrorCode.INSUFFICIENT_POINT_AMOUNT.getMessage());
        }

        @DisplayName("결제가 정상적으로 이뤄지면 포인트가 ConcertSeat의 가격만큼 차감된다.")
        @Test
        void should_DecreasePoint_When_PaymentIsSuccessful() {
            // given
            Long memberId = 1L;
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            MemberPoint updatedMemberPoint = memberPointJpaRepository.findByMemberId(memberId).orElseThrow();
            assertThat(updatedMemberPoint.getPointAmount()).isEqualTo(10000);
        }

        @DisplayName("결제가 정상적으로 이뤄지면 Concert 좌석과 예약의 상태가 바뀐다.")
        @Test
        void should_CompleteConcertSeatReservation_When_PaymentIsSuccessful() {
            // given
            Long memberId = 1L;
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            ConcertReservation updatedReservation = concertReservationJpaRepository.findById(reservationId).orElseThrow();

            assertThat(updatedReservation.getStatus()).isEqualTo(ConcertReservationStatus.COMPLETED);
            assertThat(updatedReservation.getReservedAt()).isEqualTo(dateTime);

            ConcertSeat updatedSeat = concertSeatJpaRepository.findById(savedSeat.getId())
                .orElse(null);
            assertThat(updatedSeat).isNotNull();
            assertThat(updatedSeat.getStatus()).isEqualTo(ConcertSeatStatus.RESERVED_COMPLETE);
            assertThat(updatedSeat.getReservedAt()).isEqualTo(dateTime);
        }

        @DisplayName("결제가 정상적으로 이뤄지면 대기열이 만료된다.")
        @Test
        void should_ExpireWaitingQueue_When_PaymentIsSuccessful() {
            // given
            Long memberId = 1L;
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            WaitingQueue updatedWaitingQueue = waitingQueueJpaRepository.findByToken(token).orElseThrow();
            assertThat(updatedWaitingQueue.getStatus()).isEqualTo(WaitingQueueStatus.EXPIRED);
        }

        @DisplayName("결제가 정상적으로 이뤄지면 Payment과 PaymentHistory가 생성된다.")
        @Test
        void should_CreatePaymentAndPaymentHistory_When_PaymentIsSuccessful() {
            // given
            Long memberId = 1L;
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(10000)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            PaymentInfo paymentInfo = paymentFacade.payment(reservationId, token, dateTime);

            // then
            Payment payment = paymentJpaRepository.findById(paymentInfo.getId())
                .orElse(null);
            assertThat(payment).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(payment.getPaidAmount()).isEqualTo(10000);
        }
    }

}