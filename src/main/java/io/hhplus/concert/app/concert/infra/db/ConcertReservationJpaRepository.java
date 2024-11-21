package io.hhplus.concert.app.concert.infra.db;

import io.hhplus.concert.app.concert.domain.model.ConcertReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertReservationJpaRepository extends JpaRepository<ConcertReservation, Long> {

}
