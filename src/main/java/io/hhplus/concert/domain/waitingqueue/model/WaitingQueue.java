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

    public static WaitingQueue createActiveWaitingQueue(String token, LocalDateTime expireAt) {
        return WaitingQueue.builder()
            .token(token)
            .status(WaitingQueueStatus.ACTIVE)
            .expireAt(expireAt)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static WaitingQueue createWaitingQueue(String token) {
        return WaitingQueue.builder()
            .token(token)
            .status(WaitingQueueStatus.WAITING)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public void checkNotWaiting() {
        if(!WaitingQueueStatus.WAITING.equals(this.status)) {
            throw WaitingQueueException.INVALID_STATE_NOT_WAITING;
        }
    }

    public void checkActivated(LocalDateTime currentTime) {
        boolean isActive = WaitingQueueStatus.ACTIVE.equals(this.status);
        if(!isActive) {
            throw WaitingQueueException.INVALID_WAITING_QUEUE;
        }

        boolean isExpired = this.expireAt.isBefore(currentTime);
        if(isExpired) {
            throw WaitingQueueException.INVALID_WAITING_QUEUE;
        }
    }

    public void expire() {
        this.status = WaitingQueueStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
}
