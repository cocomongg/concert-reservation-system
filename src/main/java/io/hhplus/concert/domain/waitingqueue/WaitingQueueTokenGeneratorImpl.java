package io.hhplus.concert.domain.waitingqueue;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WaitingQueueTokenGeneratorImpl implements WaitingQueueTokenGenerator {
    @Override
    public String generateWaitingQueueToken() {
        return UUID.randomUUID().toString();
    }
}
