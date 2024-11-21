package io.hhplus.concert.config.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableScheduling
@Configuration
public class SchedulerConfig {

    private static final String THREAD_NAME_PREFIX = "schedule-th-";

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler schedulerTask = new ThreadPoolTaskScheduler();

        schedulerTask.setPoolSize(10);
        schedulerTask.setThreadNamePrefix(THREAD_NAME_PREFIX);
        schedulerTask.initialize();

        return schedulerTask;
    }
}
