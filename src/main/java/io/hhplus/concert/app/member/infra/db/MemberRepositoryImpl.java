package io.hhplus.concert.app.member.infra.db;

import io.hhplus.concert.app.member.domain.repository.MemberRepository;
import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import io.hhplus.concert.app.common.error.CoreErrorType;
import io.hhplus.concert.app.common.error.CoreException;
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
            .orElseThrow(() -> new CoreException(CoreErrorType.Member.MEMBER_NOT_FOUND));
    }

    @Override
    public Optional<MemberPoint> getOptionalMemberPoint(Long memberId) {
        return memberPointJpaRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<MemberPoint> getOptionalMemberPointWithLock(Long memberId) {
        return memberPointJpaRepository.findByMemberIdWithLock(memberId);
    }

    @Override
    public MemberPoint saveMemberPoint(MemberPoint memberPoint) {
        return memberPointJpaRepository.save(memberPoint);
    }
}
