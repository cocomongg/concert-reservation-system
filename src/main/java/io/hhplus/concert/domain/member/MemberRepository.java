package io.hhplus.concert.domain.member;

import io.hhplus.concert.domain.member.model.Member;

public interface MemberRepository {

    Member getMember(Long memberId);
}
