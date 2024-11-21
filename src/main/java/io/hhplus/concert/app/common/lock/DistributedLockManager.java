package io.hhplus.concert.app.common.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLockManager {

    boolean tryLock(String key, long waitTime, long expireTime, TimeUnit timeUnit);

    void unlock(String key);
}
