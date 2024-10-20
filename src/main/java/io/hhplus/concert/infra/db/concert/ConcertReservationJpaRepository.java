package io.hhplus.concert.infra.db.concert;

import io.hhplus.concert.domain.concert.model.ConcertReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertReservationJpaRepository extends JpaRepository<ConcertReservation, Long> {

}
