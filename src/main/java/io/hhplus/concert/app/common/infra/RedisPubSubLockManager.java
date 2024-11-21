package io.hhplus.concert.app.common.infra;

import io.hhplus.concert.app.common.lock.DistributedLockManager;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@RequiredArgsConstructor
@Component
public class RedisPubSubLockManager implements DistributedLockManager {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long expireTime, TimeUnit timeUnit) {
        RLock rlock = redissonClient.getLock(key);

        boolean locked;
        try {
            locked = rlock.tryLock(waitTime, expireTime, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return locked;
    }

    @Override
    public void unlock(String key) {
        RLock rlock = redissonClient.getLock(key);
        rlock.unlock();
    }
}