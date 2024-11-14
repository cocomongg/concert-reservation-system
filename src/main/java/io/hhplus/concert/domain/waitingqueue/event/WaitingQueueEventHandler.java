package io.hhplus.concert.domain.waitingqueue.event;

import static io.hhplus.concert.domain.waitingqueue.event.WaitingQueueEvent.ExpireTokenEvent;

import io.hhplus.concert.domain.waitingqueue.WaitingQueueService;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitingQueueEventHandler {

    private final WaitingQueueService waitingQueueService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentDoneEvent(ExpireTokenEvent event) {
        try {
            String token = event.getToken();
            waitingQueueService.expireToken(new GetWaitingQueueCommonQuery(token));
        } catch (Exception e) {
            log.error("ExpireTokenEvent 처리 중 오류 발생", e);
        }
    }
}
