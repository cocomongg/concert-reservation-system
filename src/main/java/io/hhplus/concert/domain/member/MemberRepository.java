package io.hhplus.concert.domain.member;

import io.hhplus.concert.domain.member.model.Member;
import io.hhplus.concert.domain.member.model.MemberPoint;
import java.util.Optional;

public interface MemberRepository {

    Member getMember(Long memberId);

    Optional<MemberPoint> getOptionalMemberPoint(Long memberId);

    Optional<MemberPoint> getOptionalMemberPointWithLock(Long memberId);

    MemberPoint saveMemberPoint(MemberPoint memberPoint);
}
