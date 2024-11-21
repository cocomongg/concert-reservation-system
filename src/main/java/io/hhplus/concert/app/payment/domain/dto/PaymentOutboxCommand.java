package io.hhplus.concert.app.payment.domain.dto;

import io.hhplus.concert.app.payment.domain.model.PaymentEventType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PaymentOutboxCommand {
    @Getter
    @AllArgsConstructor
    public static class CreateOutbox {
        private String eventId;
        private PaymentEventType eventType;
        private String topic;
        private Object payload;
        private LocalDateTime dateTime;
    }
}
