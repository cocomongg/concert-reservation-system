package io.hhplus.concert.infra.db.concert;

import io.hhplus.concert.domain.concert.model.ConcertSeat;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);

    @Lock(LockModeType.OPTIMISTIC) // 명시적으로 낙관적 락을 사용하도록 설정
    @Query("SELECT cs FROM ConcertSeat cs WHERE cs.id = :concertSeatId")
    Optional<ConcertSeat> findByIdWithLock(Long concertSeatId);
}
