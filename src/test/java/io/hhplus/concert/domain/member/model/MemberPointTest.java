package io.hhplus.concert.domain.member.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.app.member.domain.model.MemberPoint;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberPointTest {
    
    private MemberPoint memberPoint;
    
    @BeforeEach
    public void setup() {
        memberPoint = new MemberPoint(1L, 1L, 100, LocalDateTime.now(), 
            LocalDateTime.now());
    }

    @DisplayName("validateAmount() 테스트")
    @Nested
    class ValidateAmountTest {
        @Test
        @DisplayName("amount가 0이면 CoreException이 발생한다.")
        void should_ThrowCoreException_When_AmountIsZero() {
            // given
            int amount = 0;
            
            // when, then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }

        @Test
        @DisplayName("amount가 음수이면 INVALID_POINT_AMOUNT 예외가 발생한다.")
        void should_ThrowException_When_AmountIsNegative() {
            // given
            int amount = -10;

            // when, then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }
    }

    @DisplayName("chargePoint() 테스트")
    @Nested
    class ChargePointTest {

        @Test
        @DisplayName("amount가 양수이면 pointAmount가 증가하고 updatedAt이 갱신된다.")
        void should_IncreasePointAmountAndUpdateUpdatedAt_When_AmountIsPositive() {
            // given
            int amount = 50;
            int originalPointAmount = memberPoint.getPointAmount();
            LocalDateTime originalUpdatedAt = memberPoint.getUpdatedAt();

            // when
            memberPoint.chargePoint(amount);

            // then
            assertThat(memberPoint.getPointAmount()).isEqualTo(originalPointAmount + amount);
            assertThat(memberPoint.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("amount가 0이면 CoreException이 발생한다.")
        void should_ThrowCoreException_When_AmountIsZero() {
            // given
            int amount = 0;

            // When & Then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }

        @Test
        @DisplayName("amount가 음수이면 CoreException이 발생한다.")
        void should_ThrowCoreException_When_AmountIsNegative() {
            // given
            int amount = -20;

            // when, then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }
    }

    @DisplayName("usePoint() 테스트")
    @Nested
    class UsePointTest {

        @Test
        @DisplayName("amount가 양수이고 보유 포인트가 충분하면 pointAmount가 감소하고, updatedAt이 갱신된다.")
        void should_UpdateUpdatedAt_When_PointAmountIsSufficient() {
            // given
            int amount = 50;
            int originalPointAmount = memberPoint.getPointAmount();
            LocalDateTime originalUpdatedAt = memberPoint.getUpdatedAt();

            // when
            memberPoint.usePoint(amount);

            // then
            assertThat(memberPoint.getPointAmount()).isEqualTo(originalPointAmount - amount);
            assertThat(memberPoint.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("보유 포인트가 부족하면 CoreException이 발생한다.")
        void should_ThrowCoreException_When_PointAmountIsInsufficient() {
            // given
            int originAmount = memberPoint.getPointAmount();
            int amount = originAmount + 1;

            // when, then
            assertThatThrownBy(() -> memberPoint.usePoint(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT.getMessage());
        }

        @Test
        @DisplayName("amount가 0이면 CoreException이 발생한다.")
        void should_ThrowMemberPException_When_AmountIsZero() {
            // given
            int amount = 0;

            // when, then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }

        @Test
        @DisplayName("amount가 음수이면 CoreException이 발생한다.")
        void should_ThrowException_When_AmountIsNegative() {
            // given
            int amount = -10;

            // when, then
            assertThatThrownBy(() -> memberPoint.validateAmount(amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INVALID_POINT_AMOUNT.getMessage());
        }
    }
}