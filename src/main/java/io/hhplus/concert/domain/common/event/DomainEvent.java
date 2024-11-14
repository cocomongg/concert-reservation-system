package io.hhplus.concert.domain.common.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DomainEvent {
    private final LocalDateTime publishAt;
}
