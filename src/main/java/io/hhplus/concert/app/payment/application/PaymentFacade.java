package io.hhplus.concert.app.payment.application;

import io.hhplus.concert.app.payment.application.PaymentDto.PaymentInfo;
import io.hhplus.concert.app.common.ServicePolicy;
import io.hhplus.concert.app.payment.domain.event.publisher.PaymentEventPublisher;
import io.hhplus.concert.app.concert.domain.service.ConcertService;
import io.hhplus.concert.app.concert.domain.dto.ConcertCommand.ConfirmReservation;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.CheckConcertSeatExpired;
import io.hhplus.concert.app.concert.domain.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import io.hhplus.concert.app.member.domain.service.MemberService;
import io.hhplus.concert.app.payment.domain.service.PaymentService;
import io.hhplus.concert.app.payment.domain.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.app.payment.domain.event.DonePaymentEvent;
import io.hhplus.concert.app.payment.domain.model.Payment;
import io.hhplus.concert.app.payment.domain.model.PaymentStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final ConcertService concertService;
    private final MemberService memberService;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public PaymentInfo payment(Long reservationId, String token, LocalDateTime dateTime) {
        ConcertReservation concertReservation =
            concertService.getConcertReservation(new GetConcertReservation(reservationId));

        Long concertSeatId = concertReservation.getConcertSeatId();
        Long memberId = concertReservation.getMemberId();
        int priceAmount = concertReservation.getPriceAmount();

        // 좌석 임시 배정 상태 검증
        concertService.checkConcertSeatExpired(new CheckConcertSeatExpired(concertSeatId, dateTime,
            ServicePolicy.TEMP_RESERVE_DURATION_MINUTES));

        // 포인트 차감
        memberService.usePoint(memberId, priceAmount);

        // 좌석, 예약 정보 업데이트
        concertService.confirmReservation(new ConfirmReservation(concertSeatId, reservationId, dateTime));

        // 결제 정보 저장
        Payment payment = paymentService.createPayment(new CreatePayment(memberId, reservationId,
            priceAmount, PaymentStatus.PAID, dateTime));

        paymentEventPublisher.publish(new DonePaymentEvent(payment, token));

        return new PaymentInfo(payment);
    }
}
