package io.hhplus.concert.domain.member;

import io.hhplus.concert.domain.member.model.Member;
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
}
