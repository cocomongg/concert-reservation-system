package io.hhplus.concert.application.member;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class MemberFacadeTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberPointJpaRepository memberPointJpaRepository;

    @Autowired
    private MemberFacade memberFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void teardown() {
        databaseCleanUp.execute();
    }

    @DisplayName("getMemberPoint()")
    @Nested
    class GetMemberPoint {
        @DisplayName("memberId에 해당하는 Member가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_WhenMemberNotFound() {
            // given
            Long memberId = 0L;

            // when, then
            assertThatThrownBy(() -> memberFacade.getMemberPoint(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.MEMBER_NOT_FOUND.getMessage());
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

        @DisplayName("memberId에 해당하는 Member가 없으면 CoreException이 발생한다.")
        @Test
        void should_ThrowCoreException_WhenMemberNotFound() {
            // given
            Long memberId = 0L;
            int amount = 100;

            // when, then
            assertThatThrownBy(() -> memberFacade.chargeMemberPoint(memberId, amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(CoreErrorType.Member.MEMBER_NOT_FOUND.getMessage());
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

    @DisplayName("포인트 충전 동시성 테스트")
    @Nested
    class ChargeMemberPointConcurrencyTest {
        @DisplayName("한명의 유저 포인트에 대해 동시에 충전할 경우, 충전한 만큼 증가한다")
        @Test
        void should_CalculateCorrectly_WhenChargePointConcurrently() throws InterruptedException {
            // given
            Long memberId = 1L;
            int balanceAmount = 1000;
            int chargeAmount = 10;

            Member member = memberJpaRepository.save(new Member(null, "name", "email",
                LocalDateTime.now(), null));

            MemberPoint memberPoint = memberPointJpaRepository.save(
                new MemberPoint(null, member.getId(), balanceAmount,
                    LocalDateTime.now(), null));

            // when
            int attemptCount = 1000;
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            StopWatch stopWatch = new StopWatch("시나리오: 포인트 충전");
            stopWatch.start("[재시도 100ms마다 5번, 낙관적 락 적용]" + "Task count: " + attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < attemptCount; i++) {
                executorService.submit(() -> {
                    try {
                        memberFacade.chargeMemberPoint(memberId, chargeAmount);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            stopWatch.stop();

            // then
            MemberPoint result = memberPointJpaRepository.findById(memberPoint.getId())
                .orElse(null);
            assertThat(result).isNotNull();

            assertThat(result.getPointAmount())
                .isEqualTo(balanceAmount + chargeAmount * attemptCount);
            assertThat(successCount.get()).isEqualTo(attemptCount);
        }
    }
}