package io.hhplus.concert.infra.redis.lock;

import io.hhplus.concert.domain.support.lock.DistributedLockManager;
import io.hhplus.concert.infra.redis.repository.RedisRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisSpinLockManager implements DistributedLockManager {
    private final static long DEFAULT_RETRY_INTERVAL_MILLIS = 10;

    private final RedisRepository redisRepository;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long endTime = System.currentTimeMillis() + waitTimeMillis;

        while (System.currentTimeMillis() < endTime) {
            boolean acquired = redisRepository.setNx(key, "", leaseTime, timeUnit);
            if (acquired) {
                return true;
            }

            try {
                Thread.sleep(DEFAULT_RETRY_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        redisRepository.delete(key);
    }
}