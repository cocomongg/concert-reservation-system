package io.hhplus.concert.domain.member.model;

import io.hhplus.concert.domain.member.exception.MemberPointException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime createdAT;

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
            throw MemberPointException.INSUFFICIENT_POINT_AMOUNT;
        }

        this.pointAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void validateAmount(int amount) {
        if(amount <= 0) {
            throw MemberPointException.INVALID_POINT_AMOUNT;
        }
    }
}
