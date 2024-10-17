package io.hhplus.concert.infra.db.member;

import io.hhplus.concert.domain.member.MemberRepository;
import io.hhplus.concert.domain.member.exception.MemberException;
import io.hhplus.concert.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member getMember(Long memberId) {
        return memberJpaRepository.findById(memberId)
            .orElseThrow(() -> MemberException.MEMBER_NOT_FOUND);
    }
}
