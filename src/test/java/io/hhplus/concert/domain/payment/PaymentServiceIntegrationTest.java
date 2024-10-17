package io.hhplus.concert.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentHistory;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import io.hhplus.concert.infra.db.payment.PaymentHistoryJpaRepository;
import io.hhplus.concert.infra.db.payment.PaymentJpaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private PaymentHistoryJpaRepository paymentHistoryJpaRepository;

    @Autowired
    private PaymentService paymentService;

    @AfterEach
    public void teardown() {
        paymentJpaRepository.deleteAllInBatch();
        paymentHistoryJpaRepository.deleteAllInBatch();
    }

    @DisplayName("createPayment() 테스트")
    @Nested
    class CreatePaymentTest {
        @DisplayName("입력된 값으로 Payment를 생성하고 반환한다.")
        @Test
        void should_ReturnPayment_When_inputCommand () {
            // given
            CreatePayment command = new CreatePayment(1L, 1L, 1000, PaymentStatus.PAID,
                LocalDateTime.now());

            // when
            Payment result = paymentService.createPayment(command);

            // then
            assertThat(result.getMemberId()).isEqualTo(command.getMemberId());
            assertThat(result.getReservationId()).isEqualTo(command.getReservationId());
            assertThat(result.getPaidAmount()).isEqualTo(command.getPaidAmount());
            assertThat(result.getStatus()).isEqualTo(command.getStatus());
        }

        @DisplayName("입력된 값으로 Payment를 생성하고 저장한다.")
        @Test
        void should_SavePayment_When_inputCommand () {
            // given
            CreatePayment command = new CreatePayment(1L, 1L, 1000, PaymentStatus.PAID,
                LocalDateTime.now());

            // when
            Payment result = paymentService.createPayment(command);

            // then
            Payment savedPayment = paymentJpaRepository.findById(result.getId()).orElse(null);
            assertThat(savedPayment).isNotNull();
            assertThat(savedPayment.getMemberId()).isEqualTo(command.getMemberId());
            assertThat(savedPayment.getReservationId()).isEqualTo(command.getReservationId());
            assertThat(savedPayment.getPaidAmount()).isEqualTo(command.getPaidAmount());
            assertThat(savedPayment.getStatus()).isEqualTo(command.getStatus());
        }
    }

    @DisplayName("createPaymentHistory() 테스트")
    @Nested
    class CreatePaymentHistoryTest {
        @DisplayName("입력된 값으로 PaymentHistory를 생성하고 반환한다.")
        @Test
        void should_ReturnPaymentHistory_When_inputCommand () {
            // given
            CreatePaymentHistory command = new CreatePaymentHistory(1L,
                PaymentStatus.PAID, 1000);

            // when
            PaymentHistory result = paymentService.createPaymentHistory(command);

            // then
            assertThat(result.getPaymentId()).isEqualTo(command.getPaymentId());
            assertThat(result.getStatus()).isEqualTo(command.getStatus());
            assertThat(result.getAmount()).isEqualTo(command.getAmount());
        }

        @DisplayName("입력된 값으로 PaymentHistory를 생성하고 저장한다.")
        @Test
        void should_SavePaymentHistory_When_inputCommand () {
            // given
            CreatePaymentHistory command = new CreatePaymentHistory(1L,
                PaymentStatus.PAID, 1000);

            // when
            PaymentHistory result = paymentService.createPaymentHistory(command);

            // then
            PaymentHistory savedPaymentHistory =
                paymentHistoryJpaRepository.findById(result.getId()).orElse(null);
            assertThat(savedPaymentHistory).isNotNull();
            assertThat(savedPaymentHistory.getPaymentId()).isEqualTo(command.getPaymentId());
            assertThat(savedPaymentHistory.getStatus()).isEqualTo(command.getStatus());
            assertThat(savedPaymentHistory.getAmount()).isEqualTo(command.getAmount());
        }
    }
}