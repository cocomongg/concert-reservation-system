package io.hhplus.concert.application.payment;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertSeat;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertSeat;
import io.hhplus.concert.domain.member.MemberService;
import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePaymentHistory;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentHistory;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
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
    private final WaitingQueueService waitingQueueService;

    @Transactional
    public PaymentInfo payment(Long reservationId, String token, LocalDateTime dateTime) {
        ConcertReservation concertReservation =
            concertService.getConcertReservation(new GetConcertReservation(reservationId));

        Long concertSeatId = concertReservation.getConcertSeatId();
        ConcertSeat concertSeat =
            concertService.getConcertSeatWithLock(new GetConcertSeat(concertSeatId));
        concertSeat.checkExpired(dateTime, ServicePolicy.TEMP_RESERVE_DURATION_MINUTES);

        int priceAmount = concertSeat.getPriceAmount();
        // 포인트 차감
        Long memberId = concertReservation.getMemberId();
        memberService.usePoint(memberId, priceAmount);

        // 좌석, 예약 정보 업데이트
        concertSeat.completeReservation(dateTime);
        concertReservation.completeReservation(dateTime);

        // 대기열 만료 처리
        WaitingQueue waitingQueue = waitingQueueService.getWaitingQueue(
            new GetWaitingQueueCommonQuery(token));
        waitingQueue.expire();

        // 결제 정보 저장
        Payment payment = paymentService.createPayment(new CreatePayment(memberId, reservationId,
            priceAmount, PaymentStatus.PAID, dateTime));

        // 결제 이력 저장
        PaymentHistory paymentHistory = paymentService.createPaymentHistory(
            new CreatePaymentHistory(payment.getId(),
                PaymentStatus.PAID, priceAmount));

        return new PaymentInfo(payment);
    }
}
