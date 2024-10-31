package io.hhplus.concert.infra.redis.lock;

import io.hhplus.concert.domain.support.lock.DistributedLockManager;
import io.hhplus.concert.infra.redis.RedisRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@RequiredArgsConstructor
@Component
public class RedisSpinLockManager implements DistributedLockManager {
    private final static long DEFAULT_RETRY_INTERVAL_MILLIS = 10;

    private final RedisRepository redisRepository;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        while (true) {
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
    }

    @Override
    public void unlock(String key) {
        redisRepository.delete(key);
    }
}