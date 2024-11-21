package io.hhplus.concert.app.member.domain.repository;

import io.hhplus.concert.app.member.domain.model.Member;
import io.hhplus.concert.app.member.domain.model.MemberPoint;
import java.util.Optional;

public interface MemberRepository {

    Member getMember(Long memberId);

    Optional<MemberPoint> getOptionalMemberPoint(Long memberId);

    Optional<MemberPoint> getOptionalMemberPointWithLock(Long memberId);

    MemberPoint saveMemberPoint(MemberPoint memberPoint);
}
