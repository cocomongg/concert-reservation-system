package io.hhplus.concert.app.payment.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hhplus.concert.app.payment.domain.dto.PaymentOutboxCommand.CreateOutbox;
import io.hhplus.concert.app.payment.domain.model.OutboxStatus;
import io.hhplus.concert.app.payment.domain.model.PaymentOutbox;
import io.hhplus.concert.app.payment.domain.repository.PaymentOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentOutboxService {
    private final PaymentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PaymentOutbox getOutbox(String eventId) {
        return outboxRepository.getOutbox(eventId);
    }

    @Transactional(readOnly = true)
    public List<PaymentOutbox> getOutboxesForRepublish(LocalDateTime now) {
        LocalDateTime retryThresholdTime = now.minusMinutes(5);
        return outboxRepository.getOutboxesForRepublish(OutboxStatus.INIT, retryThresholdTime);
    }

    @Transactional
    public PaymentOutbox createOutbox(CreateOutbox command) {
        ObjectNode payload = objectMapper.valueToTree(command.getPayload());
        PaymentOutbox outbox = PaymentOutbox.createInitOutbox(command, payload);

        return outboxRepository.saveOutbox(outbox);
    }

    @Transactional
    public void publishSuccess(String eventId) {
        PaymentOutbox outbox = this.getOutbox(eventId);
        outbox.publishSuccess();
    }

    @Transactional
    public void publishFail(String eventId) {
        PaymentOutbox outbox = this.getOutbox(eventId);
        outbox.publishFail();
    }
}