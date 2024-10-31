package io.hhplus.concert.domain.support.aop;

import io.hhplus.concert.domain.support.utils.CustomSpringELParser;
import io.hhplus.concert.domain.support.error.CoreErrorType;
import io.hhplus.concert.domain.support.error.CoreException;
import io.hhplus.concert.domain.support.lock.DistributedLockManager;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Order(1)
@Aspect
@Component
public class DistributedLockAspect {
    private static final String DISTRIBUTE_LOCK_PREFIX = "LOCK:";

    private final DistributedLockManager distributedLockManager;

    @Around("@annotation(io.hhplus.concert.domain.support.aop.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = DISTRIBUTE_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
            signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        boolean locked = false;

        try {
            locked = distributedLockManager.tryLock(key, distributedLock.waitTime(),
                distributedLock.leaseTime(), distributedLock.timeUnit());
            if(!locked) {
                throw new CoreException(CoreErrorType.ACQUIRED_LOCK_FAILURE);
            }

            return joinPoint.proceed();
        } catch (Exception e) {
            if(e instanceof CoreException) {
                throw e;
            } else {
                throw new CoreException(CoreErrorType.INTERNAL_ERROR);
            }
        } finally {
            try {
                if(locked) {
                    distributedLockManager.unlock(key);
                }
            } catch (Exception e) {
                log.info("distributed lock already unlocked, key: {}", key);
            }
        }
    }
}
