package io.hhplus.concert.app.concert.infra.db;

import io.hhplus.concert.app.concert.domain.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {

}
