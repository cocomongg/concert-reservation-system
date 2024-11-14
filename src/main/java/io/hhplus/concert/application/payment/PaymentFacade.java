package io.hhplus.concert.application.payment;

import static io.hhplus.concert.domain.payment.event.PaymentEvent.CreatePaymentHistoryEvent;

import io.hhplus.concert.application.payment.PaymentDto.PaymentInfo;
import io.hhplus.concert.domain.common.ServicePolicy;
import io.hhplus.concert.domain.common.event.DomainEventPublisher;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.concert.dto.ConcertCommand.ConfirmReservation;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.CheckConcertSeatExpired;
import io.hhplus.concert.domain.concert.dto.ConcertQuery.GetConcertReservation;
import io.hhplus.concert.domain.concert.model.ConcertReservation;
import io.hhplus.concert.domain.member.MemberService;
import io.hhplus.concert.domain.notification.NotificationService;
import io.hhplus.concert.domain.notification.model.NotificationMessage;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.dto.PaymentCommand.CreatePayment;
import io.hhplus.concert.domain.payment.model.Payment;
import io.hhplus.concert.domain.payment.model.PaymentStatus;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
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
    private final DomainEventPublisher domainEventPublisher;
    private final NotificationService notificationService;

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

        // 결제 이력 저장
        domainEventPublisher.publish(new CreatePaymentHistoryEvent(payment.getId(), priceAmount));

        // 대기열 만료 처리
        waitingQueueService.expireToken(new GetWaitingQueueCommonQuery(token));

        // 결제 완료 내역 전송
        NotificationMessage message = new NotificationMessage("결제 완료", "결제가 완료되었습니다.", memberId);
        notificationService.sendNotification(message);

        return new PaymentInfo(payment);
    }
}
