package io.hhplus.concert.app.payment.domain.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hhplus.concert.app.payment.domain.dto.PaymentOutboxCommand.CreateOutbox;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "outbox")
@Entity
public class PaymentOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_id")
    private String eventId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "event_type")
    private PaymentEventType eventType;

    @Column(name = "topic")
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json")
    private ObjectNode payload;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PaymentOutbox(String eventId, PaymentEventType eventType, String topic, ObjectNode payload, LocalDateTime createdAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.status = OutboxStatus.INIT;
        this.createdAt = createdAt;
    }

    public static PaymentOutbox createInitOutbox(CreateOutbox command, ObjectNode payload) {
        return PaymentOutbox.builder()
            .eventId(command.getEventId())
            .eventType(command.getEventType())
            .topic(command.getTopic())
            .payload(payload)
            .status(OutboxStatus.INIT)
            .createdAt(command.getDateTime())
            .build();
    }

    public void publishSuccess() {
        this.status = OutboxStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void publishFail() {
        this.status = OutboxStatus.FAIL;
        this.updatedAt = LocalDateTime.now();
    }
}
