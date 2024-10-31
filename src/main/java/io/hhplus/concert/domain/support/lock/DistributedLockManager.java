package io.hhplus.concert.domain.support.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLockManager {

    boolean tryLock(String key, long waitTime, long expireTime, TimeUnit timeUnit);

    void unlock(String key);
}
