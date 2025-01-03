package io.hhplus.concert.application.payment;

import static io.hhplus.concert.app.payment.domain.model.OutboxStatus.SUCCESS;
import static io.hhplus.concert.app.payment.domain.model.PaymentEventType.DONE_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertReservationStatus;
import io.hhplus.concert.app.concert.domain.model.ConcertSeat;
import io.hhplus.concert.app.concert.domain.model.ConcertSeatStatus;
import io.hhplus.concert.app.concert.infra.db.ConcertReservationJpaRepository;
import io.hhplus.concert.app.concert.infra.db.ConcertSeatJpaRepository;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import io.hhplus.concert.app.member.infra.db.MemberPointJpaRepository;
import io.hhplus.concert.app.notification.domain.NotificationService;
import io.hhplus.concert.app.notification.domain.model.NotificationMessage;
import io.hhplus.concert.app.payment.application.PaymentDto.PaymentInfo;
import io.hhplus.concert.app.payment.application.PaymentFacade;
import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import io.hhplus.concert.app.payment.domain.model.PaymentStatus;
import io.hhplus.concert.app.payment.infra.db.PaymentJpaRepository;
import io.hhplus.concert.app.payment.infra.db.PaymentOutboxJpaRepository;
import io.hhplus.concert.app.waitingqueue.domain.model.TokenMeta;
import io.hhplus.concert.app.waitingqueue.infra.redis.RedisRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import io.hhplus.concert.support.RedisCleanUp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    private RedisRepository redisRepository;

    @Autowired
    private ConcertSeatJpaRepository concertSeatJpaRepository;

    @Autowired
    private ConcertReservationJpaRepository concertReservationJpaRepository;

    @Autowired
    private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Value("${kafka.topics.payment}")
    private String paymentTopic;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
        redisCleanUp.execute();
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
            int seatPrice = 10000;

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(seatPrice)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .priceAmount(savedSeat.getPriceAmount())
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
            int seatPrice = 10000;

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(seatPrice)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .priceAmount(savedSeat.getPriceAmount())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

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
            int seatPrice = 10000;

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(seatPrice)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .priceAmount(savedSeat.getPriceAmount())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

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
        void should_ExpireWaitingQueue_When_PaymentIsSuccessful() throws InterruptedException {
            // given
            Long memberId = 1L;
            LocalDateTime dateTime = LocalDateTime.now();
            int seatPrice = 10000;

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(seatPrice)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .priceAmount(savedSeat.getPriceAmount())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(seatPrice + 1000)
                .build());

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            await().pollInterval(500, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    boolean inSet = redisRepository.isInSet("active_queue", token);
                    assertThat(inSet).isFalse();

                    TokenMeta tokenMeta = redisRepository.getStringValue("active_queue:" + token,
                        TokenMeta.class);
                    assertThat(tokenMeta).isNull();
                });
        }

        @DisplayName("결제가 정상적으로 이뤄지면 Payment과 PaymentHistory가 생성된다.")
        @Test
        void should_CreatePaymentAndPaymentHistory_When_PaymentIsSuccessful() {
            // given
            Long memberId = 1L;
            String token = "token";
            int seatPrice = 10000;
            LocalDateTime dateTime = LocalDateTime.now();

            ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(10)
                .priceAmount(seatPrice)
                .tempReservedAt(dateTime)
                .createdAt(LocalDateTime.now())
                .build());

            ConcertReservation savedReservation = concertReservationJpaRepository.save(
                ConcertReservation.builder()
                    .memberId(memberId)
                    .concertSeatId(savedSeat.getId())
                    .priceAmount(savedSeat.getPriceAmount())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

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
            int seatPrice = 10000;

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
                    .priceAmount(savedSeat.getPriceAmount())
                    .status(ConcertReservationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            Long reservationId = savedReservation.getId();
            String token = "token";

            memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(20000)
                .build());

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

            // when
            int attemptCount = 1000;
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
            throws InterruptedException                                       {
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
                        .priceAmount(savedSeat.getPriceAmount())
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

            redisRepository.addSet("active_queue", token);
            redisRepository.setStringValue("active_queue:" + token,
                new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

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

    @Nested
    @DisplayName("listen DonePaymentEvent 테스트")
    class DonePaymentEventListener {
        @DisplayName("결제가 완료되면 DonePaymentEvent가 발행되어 PaymentOutbox가 생성된다.")
        @Test
        void should_CreatePaymentOutbox_When_PublishedDonePaymentEvent() {
            // given
            Long memberId = 1L;
            String token = "token";
            int seatPrice = 10000;
            LocalDateTime dateTime = LocalDateTime.now();

            Long reservationId = createDataForSuccessPayment(memberId, token, seatPrice,
                dateTime);

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentOutbox> paymentOutboxes = paymentOutboxJpaRepository.findAll();
                    PaymentOutbox paymentOutbox = paymentOutboxes.get(0);

                    assertThat(paymentOutbox).isNotNull();
                    assertThat(paymentOutbox.getEventType()).isEqualTo(DONE_PAYMENT);
                    assertThat(paymentOutbox.getTopic()).isEqualTo(paymentTopic);
                });
        }
    }

    @Nested
    @DisplayName("consume DonePaymentEventMessage 테스트")
    class ProduceMessageTest {
        @DisplayName("결제가 완료되고 produce된 DonePaymentEvent message를 consume하면 NotificationService의 sendNotification()이 호출된다.")
        @Test
        void should_ProduceMessage_When_DonePaymentEventIsPublished() {
            // given
            Long memberId = 1L;
            String token = "token";
            int seatPrice = 10000;
            LocalDateTime dateTime = LocalDateTime.now();

            Long reservationId = createDataForSuccessPayment(memberId, token, seatPrice,
                dateTime);

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(notificationService, times(1))
                        .sendNotification(any(NotificationMessage.class));
                });
        }

        @DisplayName("결제가 완료되고 produce된 DonePaymentEvent message를 consume하면 대기열 토큰이 만료된다.")
        @Test
        void should_ExpireToken_When_ConsumeDonePaymentEvent() {
            // given
            Long memberId = 1L;
            String token = "token";
            int seatPrice = 10000;
            LocalDateTime dateTime = LocalDateTime.now();

            Long reservationId = createDataForSuccessPayment(memberId, token, seatPrice,
                dateTime);

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    boolean inSet = redisRepository.isInSet("active_queue", token);
                    assertThat(inSet).isFalse();

                    TokenMeta tokenMeta = redisRepository.getStringValue("active_queue:" + token,
                        TokenMeta.class);
                    assertThat(tokenMeta).isNull();
                });
        }

        @DisplayName("결제가 완료되고 produce된 DonePaymentEvent message를 consume하면 outbox 상태가 SUCCESS로 변경된다.")
        @Test
        void should_UpdateOutboxStatus_When_ConsumeDonePaymentEvent() {
            // given
            Long memberId = 1L;
            String token = "token";
            int seatPrice = 10000;
            LocalDateTime dateTime = LocalDateTime.now();

            Long reservationId = createDataForSuccessPayment(memberId, token, seatPrice,
                dateTime);

            // when
            paymentFacade.payment(reservationId, token, dateTime);

            // then
            await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentOutbox> paymentOutboxes = paymentOutboxJpaRepository.findAll();
                    PaymentOutbox paymentOutbox = paymentOutboxes.get(0);

                    assertThat(paymentOutbox).isNotNull();
                    assertThat(paymentOutbox.getStatus()).isEqualTo(SUCCESS);
                    assertThat(paymentOutbox.getTopic()).isEqualTo(paymentTopic);
                });
        }
    }

    private Long createDataForSuccessPayment(Long memberId, String token, int seatPrice, LocalDateTime dateTime) {
        ConcertSeat savedSeat = concertSeatJpaRepository.save(ConcertSeat.builder()
            .concertScheduleId(1L)
            .seatNumber(10)
            .priceAmount(seatPrice)
            .tempReservedAt(dateTime)
            .createdAt(LocalDateTime.now())
            .build());

        ConcertReservation savedReservation = concertReservationJpaRepository.save(
            ConcertReservation.builder()
                .memberId(memberId)
                .concertSeatId(savedSeat.getId())
                .priceAmount(savedSeat.getPriceAmount())
                .status(ConcertReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        Long reservationId = savedReservation.getId();

        memberPointJpaRepository.save(MemberPoint.builder()
            .memberId(memberId)
            .pointAmount(20000)
            .build());

        redisRepository.addSet("active_queue", token);
        redisRepository.setStringValue("active_queue:" + token,
            new TokenMeta(LocalDateTime.now()), Duration.ofMinutes(10));

        return reservationId;
    }
}