package io.hhplus.concert.app.member.application;

import io.hhplus.concert.app.member.application.MemberPointDto.MemberPointInfo;
import io.hhplus.concert.app.member.domain.service.MemberService;
import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public MemberPointInfo chargeMemberPoint(Long memberId, int amount) {
        Member member = memberService.getMember(memberId);
        MemberPoint memberPoint = memberService.chargePoint(member.getId(), amount);

        return new MemberPointInfo(memberPoint);
    }
}
