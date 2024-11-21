package io.hhplus.concert.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import io.hhplus.concert.app.member.domain.service.MemberService;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
import io.hhplus.concert.app.member.infra.db.MemberJpaRepository;
import io.hhplus.concert.app.member.infra.db.MemberPointJpaRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    @DisplayName("포인트 사용 동시성 테스트")
    @Nested
    class UsePointConcurrencyTest {
        @DisplayName("한명의 유저 포인트를 동시에 사용할 경우, 사용한 만큼 차감된다.")
        @Test
        void should_reduceUsingPoint_When_usePointConcurrency() throws InterruptedException {
            // given
            Long memberId = 1L;
            int balanceAmount = 1000;
            int useAmount = 10;
            MemberPoint memberPoint = memberPointJpaRepository.save(new MemberPoint(null, memberId, balanceAmount,
                LocalDateTime.now(), null));

            // when
            int attemptCount = 20;
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < attemptCount; i++) {
                executorService.submit(() -> {
                    try {
                        memberService.usePoint(memberId, useAmount);
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

            log.info("successCount: {}", successCount.get());
            log.info("failCount: {}", failCount.get());

            // then
            MemberPoint result = memberPointJpaRepository.findById(memberPoint.getId())
                .orElse(null);
            assertThat(result).isNotNull();
            assertThat(result.getPointAmount()).isEqualTo(balanceAmount - attemptCount * useAmount);
        }

        @DisplayName("한명의 유저 포인트를 동시에 사용할 경우, 잔액보다 많이 사용하면 음수가 되지 않고, CoreException이 발생한다.")
        @Test
        void should_throwCoreException_When_usePointConcurrencyAndAmountInsufficient() throws InterruptedException {
            // given
            Long memberId = 1L;
            int balanceAmount = 1000;
            int useAmount = 100;
            MemberPoint memberPoint = memberPointJpaRepository.save(new MemberPoint(null, memberId, balanceAmount,
                LocalDateTime.now(), null));

            // when
            int attemptCount = 20;
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < attemptCount; i++) {
                executorService.submit(() -> {
                    try {
                        memberService.usePoint(memberId, useAmount);
                        successCount.incrementAndGet();
                    } catch (CoreException e) {
                        if(e.getErrorType().equals(CoreErrorType.Member.INSUFFICIENT_POINT_AMOUNT)) {
                            failCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();


            // then
            assertThat(failCount.get()).isEqualTo(attemptCount - balanceAmount / useAmount);

            MemberPoint result = memberPointJpaRepository.findById(memberPoint.getId()).orElse(null);
            assertThat(result).isNotNull();
            assertThat(result.getPointAmount()).isEqualTo(0);
        }
    }

    @DisplayName("포인트 충전 동시성 테스트")
    @Nested
    class ChargePointConcurrencyTest {

        @DisplayName("한명의 유저 포인트를 동시에 충전할 경우, 충전한 만큼 증가한다.")
        @Test
        void should_increaseChargePoint_When_chargePointConcurrency() throws InterruptedException {
            // given
            Long memberId = 1L;
            int balanceAmount = 1000;
            int chargeAmount = 10;
            MemberPoint memberPoint = memberPointJpaRepository.save(
                new MemberPoint(null, memberId, balanceAmount,
                    LocalDateTime.now(), null));

            // when
            int attemptCount = 20;
            ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
            CountDownLatch latch = new CountDownLatch(attemptCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < attemptCount; i++) {
                executorService.submit(() -> {
                    try {
                        memberService.chargePoint(memberId, chargeAmount);
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

            // then
            assertThat(successCount.get()).isEqualTo(attemptCount);

            MemberPoint result = memberPointJpaRepository.findById(memberPoint.getId())
                .orElse(null);
            assertThat(result).isNotNull();
            assertThat(result.getPointAmount()).isEqualTo(
                balanceAmount + attemptCount * chargeAmount);
        }
    }
}