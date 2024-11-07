package io.hhplus.concert.domain.common;

public class ServicePolicy {
    public static final int TEMP_RESERVE_DURATION_MINUTES = 5;
    public static final int WAITING_QUEUE_EXPIRED_MINUTES = 20;
    public static final int WAITING_QUEUE_ACTIVATE_COUNT = 1000;

    public static final int WAITING_QUEUE_ACTIVATE_INTERVAL = 3_000;
    public static final String CACHE_CONCERT_PREFIX = "concert";
}
