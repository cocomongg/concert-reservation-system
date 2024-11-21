package io.hhplus.concert.app.concert.infra.db;

import io.hhplus.concert.app.concert.domain.model.ConcertSchedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {

    List<ConcertSchedule> findAllByConcertIdAndScheduledAtAfter(Long concertId, LocalDateTime currentTime);
}
