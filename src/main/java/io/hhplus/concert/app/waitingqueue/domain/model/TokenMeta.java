package io.hhplus.concert.app.waitingqueue.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenMeta {
    private LocalDateTime expireAt;
}
