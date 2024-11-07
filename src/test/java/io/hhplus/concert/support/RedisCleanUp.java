package io.hhplus.concert.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Component
public class RedisCleanUp {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void execute() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}
