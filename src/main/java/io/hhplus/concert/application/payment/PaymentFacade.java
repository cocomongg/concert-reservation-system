package io.hhplus.concert.application.payment;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.common.event.DomainEventPublisher;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.ConfirmReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.CheckConcertSeatExpired;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.member.MemberService;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.domain.payment.event.DonePaymentEvent;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
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
    private final DomainEventPublisher domainEventPublisher;

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

        domainEventPublisher.publish(new DonePaymentEvent(payment, token));

        return new PaymentInfo(payment);
    }
}
