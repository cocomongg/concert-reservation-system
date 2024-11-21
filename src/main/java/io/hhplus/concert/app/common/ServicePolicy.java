package io.hhplus.concert.app.common;

import java.util.concurrent.TimeUnit;

public class ServicePolicy {
    public static final int TEMP_RESERVE_DURATION_MINUTES = 5;
    public static final int WAITING_QUEUE_EXPIRED_MINUTES = 20;
    public static final int WAITING_QUEUE_ACTIVATE_COUNT = 1000;
    public static final int WAITING_QUEUE_ACTIVATE_INTERVAL = 30_000;
    public static final TimeUnit TOKEN_ACTIVATE_INTERVAL_TIMEUNIT = TimeUnit.MINUTES;
    public static final String CACHE_CONCERT_PREFIX = "concert";
}
