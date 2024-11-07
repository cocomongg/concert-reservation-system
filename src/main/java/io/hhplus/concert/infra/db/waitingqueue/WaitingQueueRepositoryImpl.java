package io.hhplus.concert.infra.db.waitingqueue;

import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.waitingqueue.WaitingQueueRepository;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueCommand.InsertWaitingQueue;
import io.hhplus.concert.domain.waitingqueue.dto.WaitingQueueQuery.GetWaitingQueueCommonQuery;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueue;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenStatus;
import io.hhplus.concert.domain.waitingqueue.model.WaitingQueueTokenInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class WaitingQueueRepositoryImpl implements WaitingQueueRepository {

    private final WaitingQueueJpaRepository waitingQueueJpaRepository;

    @Override
    public WaitingQueueTokenInfo insertWaitingQueue(InsertWaitingQueue command) {
        WaitingQueue waitingQueue = WaitingQueue.builder()
            .token(command.getToken())
            .status(WaitingQueueTokenStatus.WAITING)
            .createdAt(command.getNow())
            .build();

        WaitingQueue savedWaitingQueue = waitingQueueJpaRepository.save(waitingQueue);

        return savedWaitingQueue.toTokenInfo();
    }

    @Override
    public WaitingQueueTokenInfo getWaitingQueueToken(GetWaitingQueueCommonQuery query) {
        WaitingQueue waitingQueue = waitingQueueJpaRepository.findByToken(query.getToken())
            .orElseThrow(() -> new CoreException(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND));
        return waitingQueue.toTokenInfo();
    }

    @Override
    public Long getWaitingTokenOrder(GetWaitingQueueCommonQuery query) {
        WaitingQueue waitingQueue = waitingQueueJpaRepository.findByToken(query.getToken())
            .orElseThrow(() -> new CoreException(CoreErrorType.WaitingQueue.WAITING_QUEUE_NOT_FOUND));

        return waitingQueueJpaRepository.countByIdLessThanEqualAndStatus(waitingQueue.getId(),
            WaitingQueueTokenStatus.WAITING);
    }

    @Override
    public List<WaitingQueueTokenInfo> getOldestWaitingTokens(int limit) {
        if(limit <= 0) {
            return new ArrayList<>();
        }

        List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findOldestWaitedIds(
            WaitingQueueTokenStatus.WAITING, PageRequest.of(0, limit));

        return waitingQueues.stream()
            .map(WaitingQueue::toTokenInfo)
            .collect(Collectors.toList());
    }

    @Override
    public Long activateWaitingTokens(List<String> tokens) {
        return (long) waitingQueueJpaRepository.updateStatusByTokens(tokens, WaitingQueueTokenStatus.ACTIVE,
            LocalDateTime.now());
    }

    @Override
    public List<WaitingQueueTokenInfo> getActiveTokensToExpire(LocalDateTime currentTime) {
        List<WaitingQueue> waitingQueues = waitingQueueJpaRepository.findExpireTargetIds(currentTime);

        return waitingQueues.stream()
            .map(WaitingQueue::toTokenInfo)
            .collect(Collectors.toList());
    }

    @Override
    public Long expireActiveTokens(List<String> tokens) {
        return (long) waitingQueueJpaRepository.updateStatusByTokens(tokens, WaitingQueueTokenStatus.EXPIRED,
            LocalDateTime.now());
    }
}
