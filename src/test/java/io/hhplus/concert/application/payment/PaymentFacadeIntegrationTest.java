package io.hhplus.concert.application.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservationStatus;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertSeatStatus;
import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueStatus;
import io.hhplus.concert.infra.db.concert.ConcertReservationJpaRepository;
import io.hhplus.concert.infra.db.concert.ConcertSeatJpaRepository;
import io.hhplus.concert.infra.db.member.MemberPointJpaRepository;
import io.hhplus.concert.infra.db.payment.PaymentJpaRepository;
import io.hhplus.concert.infra.db.waitingqueue.WaitingQueueJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
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
        @DisplayName("reservationId에 해당하는 ConcertReservation이 존재하지 않으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_ConcertReservationNotFound() {
            // given
            Long reservationId = 1L;
            String token = "token";
            LocalDateTime dateTime = LocalDateTime.now();

            // when, then
            assertThatThrownBy(() -> paymentFacade.payment(reservationId, token, dateTime))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.CONCERT_RESERVATION_NOT_FOUND.getMessage());
        }

        @DisplayName("reservationId에 해당하는 ConcertReservation의 ConcertSeat이 존재하지 않으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_ConcertSeatNotFound() {
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
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.CONCERT_SEAT_NOT_FOUND.getMessage());
        }

        @DisplayName("ConcertSeat이 임시 예약이 만료되면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_TemporaryReservationExpired() {
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
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Concert.TEMPORARY_RESERVATION_EXPIRED.getMessage());
        }

        @DisplayName("포인트 잔액이 부족하면 CoreException이 발생한다.")
        @Test
        void should_ThrowCorePointException_When_PointIsNotEnough() {
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
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT.getMessage());
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

    @DisplayName("결제 동시성 테스트")
    @Nested
    class PaymentConcurrencyTest {
        @DisplayName("같은 예약에 대해서 동시에 결제 요청이 들어오면 하나의 결제만 처리된다.")
        @Test
        void should_ExecuteOnlyOnePayment_When_PaymentRequestIsConcurrent()
            throws InterruptedException {
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
            int attemptCount = 30;
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < attemptCount; i++) {
                executorService.submit(() -> {
                    try {
                        paymentFacade.payment(reservationId, token, dateTime);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            List<Payment> payments = paymentJpaRepository.findAll();
            assertThat(payments).hasSize(1);

            Payment payment = payments.get(0);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(payment.getPaidAmount()).isEqualTo(savedSeat.getPriceAmount());
            assertThat(payment.getReservationId()).isEqualTo(savedReservation.getId());

            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(attemptCount - 1);
        }

        @DisplayName("한 명의 유저가 여러 예약에 대해 동시에 결제 요청을 할 경우, 각 예약에 대한 결제가 이뤄지고, 총 결제 금액만큼 포인트가 차감된다.")
        @Test
        void should_ExecutePaymentForEachReservation_When_PaymentRequestIsConcurrent()
            throws InterruptedException {
            // given
            int attemptCount = 10;
            int seatPrice = 1000;
            Long memberId = 1L;
            LocalDateTime dateTime = LocalDateTime.now();

            List<ConcertSeat> savedSeatList = new ArrayList<>();
            List<ConcertReservation> savedReservationList = new ArrayList<>();
            for(int i = 0; i < attemptCount; i++) {
                ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                    .concertScheduleId(1L)
                    .seatNumber(i + 1)
                    .priceAmount(1000)
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

                savedSeatList.add(savedSeat);
                savedReservationList.add(savedReservation);
            }

            String token = "token";
            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(100000)
                .build());

            waitingQueueJpaRepository.save(WaitingQueue.builder()
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < savedReservationList.size(); i++) {
                Long reservationId = savedReservationList.get(i).getId();
                executorService.submit(() -> {
                    try {
                        paymentFacade.payment(reservationId, token, dateTime);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(attemptCount);
            assertThat(failCount.get()).isEqualTo(0);

            memberPointJpaRepository.findByMemberId(memberId).ifPresent(memberPoint -> {
                assertThat(memberPoint.getPointAmount()).isEqualTo(100000 - seatPrice * attemptCount);
            });

            List<Payment> payments = paymentJpaRepository.findAll();
            assertThat(payments).hasSize(attemptCount);

            for(Payment payment : payments) {
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
                assertThat(payment.getPaidAmount()).isEqualTo(seatPrice);
                assertThat(payment.getReservationId()).isIn(savedReservationList.stream().map(ConcertReservation::getId).toArray());
            }
        }
    }

}