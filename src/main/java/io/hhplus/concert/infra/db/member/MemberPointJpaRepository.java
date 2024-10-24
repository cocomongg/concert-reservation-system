package io.hhplus.concert.infra.db.member;

import io.hhplus.concert.domain.member.model.MemberPoint;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MemberPointJpaRepository extends JpaRepository<MemberPoint, Long> {
    Optional<MemberPoint> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mp from MemberPoint mp where mp.memberId = :memberId")
    Optional<MemberPoint> findByMemberIdWithLock(Long memberId);
}
