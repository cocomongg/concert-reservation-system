package io.hhplus.concert.domain.member;

import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMember(Long memberId) {
        return memberRepository.getMember(memberId);
    }

    @Transactional
    public MemberPoint getOrDefaultMemberPoint(Long memberId) {
        Optional<MemberPoint> optionalMemberPoint =
            memberRepository.getOptionalMemberPoint(memberId);

        return optionalMemberPoint.orElseGet(() ->
            memberRepository.saveMemberPoint(MemberPoint.createDefault(memberId)));
    }

    @Transactional
    public MemberPoint usePoint(Long memberId, int amount) {
        MemberPoint memberPoint = this.getOrDefaultMemberPoint(memberId);
        memberPoint.usePoint(amount);

        return memberPoint;
    }

    @Transactional
    public MemberPoint chargePoint(Long memberId, int amount) {
        MemberPoint memberPoint = this.getOrDefaultMemberPoint(memberId);
        memberPoint.chargePoint(amount);

        return memberPoint;
    }
}
