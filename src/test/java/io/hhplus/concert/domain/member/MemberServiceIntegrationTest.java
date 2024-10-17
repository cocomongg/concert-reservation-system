package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.concert.domain.member.exception.MemberErrorCode;
import io.hhplus.concert.domain.member.exception.MemberException;
import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.infra.db.member.MemberJpaRepository;
import io.hhplus.concert.infra.db.member.MemberPointJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberServiceIntegrationTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberPointJpaRepository memberPointJpaRepository;

    @Autowired
    private MemberService memberService;

    @AfterEach
    public void teardown() {
        memberJpaRepository.deleteAllInBatch();
        memberPointJpaRepository.deleteAllInBatch();
    }
    @DisplayName("getMember() 테스트")
    @Nested
    class GetMemberTest {
        @DisplayName("memberId에 해당하는 member가 없으면 MemberException이 발생한다.")
        @Test
        void should_ThrowMemberException_When_MemberNotFound() {
            // given
            Long memberId = 0L;

            // when, then
            assertThatThrownBy(() -> memberService.getMember(memberId))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("memberId에 해당하는 member가 있으면 member를 반환한다.")
        @Test
        void should_ReturnMember_When_Found () {
            // given
            Member member =
                new Member(null, "email@email.com", "name", LocalDateTime.now(), null);

            Member savedMember = memberJpaRepository.save(member);

            // when
            Member result = memberService.getMember(savedMember.getId());

            // then
            assertThat(result.getEmail()).isEqualTo(member.getEmail());
            assertThat(result.getName()).isEqualTo(member.getName());
            assertThat(result.getCreatedAt()).isEqualTo(member.getCreatedAt());
        }
    }

    @DisplayName("getOptionalMemberPoint() 테스트")
    @Nested
    class GetOptionalMemberPointTest {
        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 Optional.empty를 반환한다.")
        @Test
        void should_ReturnOptionalEmpty_When_MemberPointNotFound () {
            // given
            Long memberId = 0L;

            // when
            Optional<MemberPoint> optionalMemberPoint = memberService.getOptionalMemberPoint(
                memberId);

            // then
            assertThat(optionalMemberPoint).isEmpty();
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 있으면 Optional<MemberPoint>를 반환한다.")
        @Test
        void should_ReturnPre() {
            // given
            Long memberId = 1L;
            MemberPoint memberPoint = new MemberPoint(null, memberId, 100,
                LocalDateTime.now(), null);

            MemberPoint savedMemberPoint = memberPointJpaRepository.save(memberPoint);

            // when
            Optional<MemberPoint> optionalMemberPoint = memberService.getOptionalMemberPoint(
                memberId);

            // then
            assertThat(optionalMemberPoint).isPresent();
        }
    }
    
    @DisplayName("getOrCreateMemberPoint() 테스트")
    @Nested
    class GetOrCreateMemberPointTest {
        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 MemberPoint를 생성하고 반환한다.")
        @Test
        void should_CreateAndReturnMemberPoint_When_NotFound() {
            // given
            Long memberId = 0L;
            
            // when
            MemberPoint result = memberService.getOrCreateMemberPoint(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getPointAmount()).isEqualTo(0);
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 MemberPoint를 생성하고 저장한다.")
        @Test
        void should_CreateAndSaveMemberPoint_When_NotFound() {
            // given
            Long memberId = 0L;

            // when
            MemberPoint result = memberService.getOrCreateMemberPoint(memberId);

            // then
            MemberPoint memberPoint = memberPointJpaRepository.findById(result.getId()).orElse(null);
            assertThat(memberPoint).isNotNull();
            assertThat(memberPoint.getMemberId()).isEqualTo(memberId);
            assertThat(memberPoint.getPointAmount()).isEqualTo(0);
        }

        @Transactional
        @DisplayName("memberId에 해당하는 MemberPoint가 있으면 MemberPoint를 반환한다.")
        @Test
        void should_ReturnExistMemberPoint_When_Found () {
            // given
            Long memberId = 1L;
            MemberPoint savedMemberPoint = memberPointJpaRepository.save(new MemberPoint(null,
                memberId, 100, LocalDateTime.now(), null));

            // when
            MemberPoint result = memberService.getOrCreateMemberPoint(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(savedMemberPoint.getMemberId());
            assertThat(result.getPointAmount()).isEqualTo(savedMemberPoint.getPointAmount());
        }
    }
    
    @DisplayName("existsMember() 테스트")
    @Nested
    class ExistsMemberTest {
        @DisplayName("memberId에 해당하는 member가 없으면 false를 반환한다.")
        @Test
        void should_ReturnFalse_When_MemberNotExists () {
            // given
            Long memberId = 0L;
            
            // when
            boolean result = memberService.existsMember(memberId);
        
            // then
            assertThat(result).isFalse();
        }

        @DisplayName("memberId에 해당하는 member가 있으면 true를 반환한다.")
        @Test
        void should_ReturnTrue_When_MemberExists () {
            // given
            Member member =
                new Member(null, "email", "name", LocalDateTime.now(), null);
            Member savedMember = memberJpaRepository.save(member);

            // when
            boolean result = memberService.existsMember(savedMember.getId());

            // then
            assertThat(result).isTrue();
        }
    }
}