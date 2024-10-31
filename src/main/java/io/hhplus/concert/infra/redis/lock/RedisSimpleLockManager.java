package io.hhplus.concert.infra.redis.lock;

import io.hhplus.concert.domain.support.lock.DistributedLockManager;
import io.hhplus.concert.infra.redis.RedisRepository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisSimpleLockManager implements DistributedLockManager {

    private final RedisRepository redisRepository;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        LocalDateTime expiredDateTime = LocalDateTime.now().plus(leaseTime, timeUnit.toChronoUnit());
        return redisRepository.setNx(key, expiredDateTime.toString(), leaseTime, timeUnit);
    }

    @Override
    public void unlock(String key) {
        redisRepository.delete(key);
    }
}
