package io.hhplus.concert.app.member.domain.model;

import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_point", indexes = {
    @Index(name = "member_point_member_id_uindex", columnList = "memberId", unique = true)
})
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
