package io.hhplus.concert.app.member.domain.service;

import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import io.hhplus.concert.app.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
            memberRepository.getOptionalMemberPointWithLock(memberId);

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
