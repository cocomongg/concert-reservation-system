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

    @Transactional(readOnly = true)
    public boolean existsMember(Long memberId) {
        return memberRepository.existsMember(memberId);
    }

    @Transactional(readOnly = true)
    public Optional<MemberPoint> getOptionalMemberPoint(Long memberId) {
        return memberRepository.getOptionalMemberPoint(memberId);
    }

    @Transactional
    public MemberPoint getOrCreateMemberPoint(Long memberId) {
        Optional<MemberPoint> optionalMemberPoint = this.getOptionalMemberPoint(memberId);

        return optionalMemberPoint.orElseGet(() ->
            memberRepository.saveMemberPoint(MemberPoint.createDefault(memberId)));
    }
}
