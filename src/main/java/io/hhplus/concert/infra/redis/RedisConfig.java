package io.hhplus.concert.infra.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private static final String REDISSON_HOST_PREFIX = "redis://";

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate( LettuceConnectionFactory lettuceConnectionFactory ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory( lettuceConnectionFactory );

        // set key serializer
        // template.setKeySerializer( new StringRedisSerializer(StandardCharsets.UTF_8) ); 와 동일함
        template.setKeySerializer( RedisSerializer.string() );
        template.setHashKeySerializer( RedisSerializer.string() );

        // set value serializer
        // template.setDefaultSerializer( new GenericJackson2JsonRedisSerializer() ); 와 동일함
        template.setDefaultSerializer( RedisSerializer.json() );
        template.setValueSerializer( RedisSerializer.json() );
        template.setHashValueSerializer( RedisSerializer.json() );

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redisson;
        Config config = new Config();
        config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
        redisson = Redisson.create(config);
        return redisson;
    }
}
