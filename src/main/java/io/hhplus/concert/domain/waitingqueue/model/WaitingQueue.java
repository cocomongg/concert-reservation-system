package io.hhplus.concert.domain.waitingqueue.model;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.exception.WaitingQueueException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WaitingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    WaitingQueueStatus status;

    @Column(name = "expired_at")
    private LocalDateTime expireAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WaitingQueue(CreateWaitingQueue command) {
        this.token = command.getToken();
        this.status = command.getStatus();
        this.expireAt = command.getExpireAt();
        this.createdAt = LocalDateTime.now();
    }

    public void checkNotWaiting() {
        if(!WaitingQueueStatus.WAITING.equals(this.status)) {
            throw WaitingQueueException.INVALID_STATE_NOT_WAITING;
        }
    }

    public void checkActivated(LocalDateTime currentTime) {
        boolean isActive = WaitingQueueStatus.ACTIVE.equals(this.status);
        boolean isExpired = this.expireAt.isBefore(currentTime);

        if(!isActive || isExpired) {
            throw WaitingQueueException.INVALID_WAITING_QUEUE;
        }
    }

    public void expire() {
        this.status = WaitingQueueStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
}
