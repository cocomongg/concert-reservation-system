package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.concert.domain.member.exception.MemberErrorCode;
import io.hhplus.concert.domain.member.exception.MemberException;
import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.infra.db.member.MemberJpaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberServiceIntegrationTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberService memberService;

    @AfterEach
    public void teardown() {
        memberJpaRepository.deleteAllInBatch();
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
}