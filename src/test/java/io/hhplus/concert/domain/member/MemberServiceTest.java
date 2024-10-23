package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;
    
    @DisplayName("getOrDefaultMemberPoint() 테스트")
    @Nested
    class GetOrDefaultMemberPointTest {
        @DisplayName("memberId에 해당하는 memberPoint가 존재하면 새로 생성 및 저장하지 않고 기존 memberPoint를 반환한다.")
        @Test
        void should_ReturnExistingMemberPoint_When_Found() {
            // Given
            Long memberId = 1L;
            MemberPoint existingMemberPoint = new MemberPoint(1L, memberId, 100,
                LocalDateTime.now(), null);

            when(memberRepository.getOptionalMemberPoint(memberId))
                .thenReturn(Optional.of(existingMemberPoint));

            // When
            MemberPoint result = memberService.getOrDefaultMemberPoint(memberId);

            // Then
            assertThat(result.getId()).isEqualTo(existingMemberPoint.getId());
            assertThat(result.getMemberId()).isEqualTo(existingMemberPoint.getMemberId());
            assertThat(result.getPointAmount()).isEqualTo(existingMemberPoint.getPointAmount());
            verify(memberRepository, never()).saveMemberPoint(any(MemberPoint.class));
        }

        @DisplayName("memberId에 해당하는 memberPoint가 존재하지 않으면 새로 생성, 저장해서 반환한다.")
        @Test
        void should_SaveAndReturnExistingMemberPoint_When_Found() {
            // Given
            Long memberId = 1L;

            when(memberRepository.getOptionalMemberPoint(memberId))
                .thenReturn(Optional.empty());

            MemberPoint savedMemberPoint = MemberPoint.createDefault(memberId);
            when(memberRepository.saveMemberPoint(any()))
                .thenReturn(savedMemberPoint);

            // When
            MemberPoint result = memberService.getOrDefaultMemberPoint(memberId);

            // Then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getPointAmount()).isEqualTo(0);
            verify(memberRepository, times(1)).saveMemberPoint(any(MemberPoint.class));
        }
    }

    @DisplayName("usePoint() 테스트")
    @Nested
    class UsePointTest {
        @DisplayName("포인트가 부족하면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_InsufficientPoint() {
            // given
            Long memberId = 1L;
            int amount = 100;
            MemberPoint existingMemberPoint = new MemberPoint(1L, memberId, amount,
                LocalDateTime.now(), null);

            when(memberRepository.getOptionalMemberPoint(memberId))
                .thenReturn(Optional.of(existingMemberPoint));

            // when, then
            assertThatThrownBy(() -> memberService.usePoint(memberId, amount + 10))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT.getMessage());
        }


        @DisplayName("포인트가 충분하면 포인트를 차감 후 MemberPoint를 반환한다.")
        @Test
        void should_UsePointAndReturn_When_Found() {
            // given
            Long memberId = 1L;
            MemberPoint existingMemberPoint = new MemberPoint(1L, memberId, 100,
                LocalDateTime.now(), null);

            when(memberRepository.getOptionalMemberPoint(memberId))
                .thenReturn(Optional.of(existingMemberPoint));

            // when
            MemberPoint result = memberService.usePoint(memberId, 50);

            // then
            assertThat(result.getId()).isEqualTo(existingMemberPoint.getId());
            assertThat(result.getMemberId()).isEqualTo(existingMemberPoint.getMemberId());
            assertThat(result.getPointAmount()).isEqualTo(50);
        }
    }

    @DisplayName("chargePoint() 테스트")
    @Nested
    class ChargePointTest {
        @DisplayName("memberId에 해당하는 memberPoint가 존재하면 point를 증가 후, 반환한다.")
        @Test
        void should_ChargePointAndReturn_When_Found() {
            // given
            Long memberId = 1L;
            MemberPoint existingMemberPoint = new MemberPoint(1L, memberId, 100,
                LocalDateTime.now(), null);

            when(memberRepository.getOptionalMemberPoint(memberId))
                .thenReturn(Optional.of(existingMemberPoint));

            // when
            MemberPoint result = memberService.chargePoint(memberId, 50);

            // then
            assertThat(result.getId()).isEqualTo(existingMemberPoint.getId());
            assertThat(result.getMemberId()).isEqualTo(existingMemberPoint.getMemberId());
            assertThat(result.getPointAmount()).isEqualTo(150);
            verify(memberRepository, never()).saveMemberPoint(any(MemberPoint.class));
        }
    }
}