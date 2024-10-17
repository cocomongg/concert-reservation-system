package io.hhplus.concert.application.member;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberFacadeTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberPointJpaRepository memberPointJpaRepository;

    @Autowired
    private MemberFacade memberFacade;

    @AfterEach
    public void tearDown() {
        memberPointJpaRepository.deleteAllInBatch();
        memberJpaRepository.deleteAllInBatch();
    }

    @DisplayName("getMemberPoint()")
    @Nested
    class GetMemberPoint {
        @DisplayName("memberId에 해당하는 Member가 없으면 MemberException이 발생한다.")
        @Test
        void should_ThrowMemberException_WhenMemberNotFound() {
            // given
            Long memberId = 0L;

            // when, then
            assertThatThrownBy(() -> memberFacade.getMemberPoint(memberId))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 MemberPoint를 생성한다.")
        @Test
        void should_CreateMemberPoint_WhenMemberPointNotFound() {
            // given
            Member savedMember = memberJpaRepository.save(Member.builder()
                .name("name")
                .email("email@email.com")
                .createdAt(LocalDateTime.now())
                .build());

            Long memberId = savedMember.getId();

            // when
            MemberPointDto.MemberPointInfo memberPointInfo = memberFacade.getMemberPoint(memberId);

            // then
            assertThat(memberPointInfo).isNotNull();
            assertThat(memberPointInfo.getMemberId()).isEqualTo(memberId);
            assertThat(memberPointInfo.getPointAmount()).isEqualTo(0);
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 있으면 해당 MemberPoint를 반환한다.")
        @Test
        void should_ReturnMemberPoint_WhenMemberPointFound() {
            // given
            Member savedMember = memberJpaRepository.save(Member.builder()
                .name("name")
                .email("email@email.com")
                .createdAt(LocalDateTime.now())
                .build());

            Long memberId = savedMember.getId();

            MemberPoint savedMemberPoint = memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(100)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            MemberPointDto.MemberPointInfo memberPointInfo = memberFacade.getMemberPoint(memberId);

            // then
            assertThat(memberPointInfo).isNotNull();
            assertThat(memberPointInfo.getMemberId()).isEqualTo(memberId);
            assertThat(memberPointInfo.getPointAmount()).isEqualTo(100);
        }
    }

    @DisplayName("chargeMemberPoint()")
    @Nested
    class ChargeMemberPoint {

        @DisplayName("memberId에 해당하는 Member가 없으면 MemberException이 발생한다.")
        @Test
        void should_ThrowMemberException_WhenMemberNotFound() {
            // given
            Long memberId = 0L;
            int amount = 100;

            // when, then
            assertThatThrownBy(() -> memberFacade.chargeMemberPoint(memberId, amount))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 없으면 MemberPoint를 생성하고 point를 충전한다.")
        @Test
        void should_CreateMemberPointAndChargePoint_WhenMemberPointNotFound() {
            // given
            Member savedMember = memberJpaRepository.save(Member.builder()
                .name("name")
                .email("email@email.com")
                .createdAt(LocalDateTime.now())
                .build());

            Long memberId = savedMember.getId();
            int amount = 100;

            // when
            MemberPointDto.MemberPointInfo memberPointInfo = memberFacade.chargeMemberPoint(
                memberId, amount);

            // then
            assertThat(memberPointInfo).isNotNull();
            assertThat(memberPointInfo.getMemberId()).isEqualTo(memberId);
            assertThat(memberPointInfo.getPointAmount()).isEqualTo(amount);
        }

        @DisplayName("memberId에 해당하는 MemberPoint가 있으면 point를 충전한다.")
        @Test
        void should_ChargePoint_WhenMemberPointFound() {
            // given
            Member savedMember = memberJpaRepository.save(Member.builder()
                .name("name")
                .email("email@email.com")
                .createdAt(LocalDateTime.now())
                .build());

            Long memberId = savedMember.getId();
            int amount = 100;

            MemberPoint savedMemberPoint = memberPointJpaRepository.save(MemberPoint.builder()
                .memberId(memberId)
                .pointAmount(100)
                .createdAt(LocalDateTime.now())
                .build());

            // when
            MemberPointDto.MemberPointInfo memberPointInfo = memberFacade.chargeMemberPoint(
                memberId, amount);

            // then
            assertThat(memberPointInfo).isNotNull();
            assertThat(memberPointInfo.getMemberId()).isEqualTo(memberId);
            assertThat(memberPointInfo.getPointAmount()).isEqualTo(200);
        }
    }
}