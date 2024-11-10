package io.hhplus.concert.infra.redis.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public boolean setNx(String key, String value, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public Boolean addSortedSet(String key, String member, long score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    public Double getSortedSetScore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    public Long getSortedSetRank(String key, String member) {
        return redisTemplate.opsForZSet().rank(key, member);
    }

    public Set<TypedTuple<Object>> popMinSortedSet(String key, long count) {
        return redisTemplate.opsForZSet().popMin(key, count);
    }

    public Long addSet(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long removeSet(String key, String value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    public boolean isInSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public void setStringValue(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize " + value + " to JSON", e);
        }
    }

    public <T> T getStringValue(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getName(), e);
        }
    }
}
