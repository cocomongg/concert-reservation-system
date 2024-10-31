package io.hhplus.concert.domain.member.model;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_point")
@Entity
public class MemberPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "point_amount")
    private int pointAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public MemberPoint(Long id, Long memberId, int pointAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.pointAmount = pointAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberPoint createDefault(Long memberId) {
        return new MemberPoint(null, memberId, 0, LocalDateTime.now(), null);
    }

    public void chargePoint(int amount) {
        this.validateAmount(amount);
        this.pointAmount += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void usePoint(int amount) {
        this.validateAmount(amount);

        if(this.pointAmount < amount) {
            throw new CoreException(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT);
        }

        this.pointAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void validateAmount(int amount) {
        if(amount <= 0) {
            throw new CoreException(CoreErrorType.Member.INVALID_POINT_AMOUNT);
        }
    }
}
