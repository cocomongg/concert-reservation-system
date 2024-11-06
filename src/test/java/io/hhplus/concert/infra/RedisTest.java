package io.hhplus.concert.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void test() {
        // given
        String key = "testKey";
        String value = "testValue";
        redisTemplate.opsForValue().set(key, value);

        // when
        String result = String.valueOf(redisTemplate.opsForValue().get(key));

        // then
        assertThat(result).isEqualTo(value);
    }

}
