package io.hhplus.concert.domain.waitingqueue;

import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.CreateWaitingQueueCommand;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public WaitingQueue createWaitingQueue(CreateWaitingQueueCommand command) {
        return waitingQueueRepository.createWaitingQueue(command);
    }
}
