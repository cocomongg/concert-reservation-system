package io.hhplus.concert.infra.db.member;

import io.hhplus.concert.domain.member.model.MemberPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPointJpaRepository extends JpaRepository<MemberPoint, Long> {
    Optional<MemberPoint> findByMemberId(Long memberId);
}
