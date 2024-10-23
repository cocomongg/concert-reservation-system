package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.infra.db.member.MemberJpaRepository;
import io.hhplus.concert.infra.db.member.MemberPointJpaRepository;
import io.hhplus.concert.support.DatabaseCleanUp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
class MemberServiceIntegrationTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberPointJpaRepository memberPointJpaRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("getMember() 테스트")
    @Nested
    class GetMemberTest {
        @DisplayName("memberId에 해당하는 member가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_When_MemberNotFound() {
            // given
            Long memberId = 0L;

            // when, then
            assertThatThrownBy(() -> memberService.getMember(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.MEMBER_NOT_FOUND.getMessage());
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
    
    @DisplayName("getOrDefaultMemberPoint() 테스트")
    @Nested
    class GetOrDefaultMemberPointTest {
        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 MemberPoint를 생성하고 반환한다.")
        @Test
        void should_CreateAndReturnMemberPoint_When_NotFound() {
            // given
            Long memberId = 0L;
            
            // when
            MemberPoint result = memberService.getOrDefaultMemberPoint(memberId);

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
            MemberPoint result = memberService.getOrDefaultMemberPoint(memberId);

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
            MemberPoint result = memberService.getOrDefaultMemberPoint(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(savedMemberPoint.getMemberId());
            assertThat(result.getPointAmount()).isEqualTo(savedMemberPoint.getPointAmount());
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
            memberPointJpaRepository.save(new MemberPoint(null, memberId, 50,
                LocalDateTime.now(), null));

            // when, then
            assertThatThrownBy(() -> memberService.usePoint(memberId, amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT.getMessage());
        }



        @DisplayName("포인트가 충분하면 포인트를 차감 후 MemberPoint를 반환한다.")
        @Test
        void should_ReturnMemberPoint_When_EnoughPoint() {
            // given
            Long memberId = 1L;
            int amount = 100;
            memberPointJpaRepository.save(new MemberPoint(null, memberId, 150,
                LocalDateTime.now(), null));

            // when
            MemberPoint result = memberService.usePoint(memberId, amount);

            // then
            assertThat(result.getPointAmount()).isEqualTo(50);
        }
    }

    @DisplayName("chargePoint() 테스트")
    @Nested
    class ChargePointTest {
        @DisplayName("포인트를 충전하고 MemberPoint를 반환한다.")
        @Test
        void should_ReturnMemberPoint_When_ChargePoint() {
            // given
            Long memberId = 1L;
            int amount = 100;
            memberPointJpaRepository.save(new MemberPoint(null, memberId, 50,
                LocalDateTime.now(), null));

            // when
            MemberPoint result = memberService.chargePoint(memberId, amount);

            // then
            assertThat(result.getPointAmount()).isEqualTo(150);
        }
    }
}