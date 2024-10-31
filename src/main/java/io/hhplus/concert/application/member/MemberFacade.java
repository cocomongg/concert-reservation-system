package io.hhplus.concert.application.member;

import io.hhplus.concert.application.member.MemberPointDto.MemberPointInfo;
import io.hhplus.concert.domain.member.MemberService;
import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MemberFacade {
    private final MemberService memberService;

    public MemberPointInfo getMemberPoint(Long memberId) {
        Member member = memberService.getMember(memberId);
        MemberPoint memberPoint = memberService.getOrDefaultMemberPoint(member.getId());

        return new MemberPointInfo(memberPoint);
    }

    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 5,
        backoff = @Backoff(delay = 100)
    )
    @Transactional
    public MemberPointInfo chargeMemberPoint(Long memberId, int amount) {
        Member member = memberService.getMember(memberId);
        MemberPoint memberPoint = memberService.chargePoint(member.getId(), amount);

        return new MemberPointInfo(memberPoint);
    }
}
