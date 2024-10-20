package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.concert.domain.member.model.MemberPoint;
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
    
    @DisplayName("getOrCreateMemberPoint() 테스트")
    @Nested
    class GetOrCreateMemberPointTest {
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
            MemberPoint result = memberService.getOrCreateMemberPoint(memberId);

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
            MemberPoint result = memberService.getOrCreateMemberPoint(memberId);

            // Then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getPointAmount()).isEqualTo(0);
            verify(memberRepository, times(1)).saveMemberPoint(any(MemberPoint.class));
        }
    }
}