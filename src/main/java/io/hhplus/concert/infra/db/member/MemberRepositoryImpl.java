package io.hhplus.concert.infra.db.member;

import io.hhplus.concert.domain.member.MemberRepository;
import io.hhplus.concert.domain.member.exception.MemberException;
import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberPointJpaRepository memberPointJpaRepository;

    @Override
    public Member getMember(Long memberId) {
        return memberJpaRepository.findById(memberId)
            .orElseThrow(() -> MemberException.MEMBER_NOT_FOUND);
    }

    @Override
    public Optional<MemberPoint> getOptionalMemberPoint(Long memberId) {
        return memberPointJpaRepository.findByMemberId(memberId);
    }

    @Override
    public MemberPoint saveMemberPoint(MemberPoint memberPoint) {
        return memberPointJpaRepository.save(memberPoint);
    }

    @Override
    public boolean existsMember(Long memberId) {
        return memberJpaRepository.existsById(memberId);
    }
}
