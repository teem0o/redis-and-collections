package com.ecwid.redisAndCollections.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

public class RedisConfig {

    public LettuceConnectionFactory redisConnectionFactory(String redisMode, String redisHost, int redisPort, String redisPassword, List<String> redisClusterNodes) {
        if ("CLUSTER".equalsIgnoreCase(redisMode)) {
            if (redisClusterNodes == null || redisClusterNodes.isEmpty()) {
                throw new IllegalArgumentException("REDIS_CLUSTER environment variable not set for cluster mode.");
            }
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration((redisClusterNodes));
            if (!redisPassword.isEmpty()) {
                clusterConfig.setPassword(RedisPassword.of(redisPassword));
            }
            return new LettuceConnectionFactory(clusterConfig);
        }else {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (!redisPassword.isEmpty()) {
                standaloneConfig.setPassword(RedisPassword.of(redisPassword));
            }
            return new LettuceConnectionFactory(standaloneConfig);
        }

    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        return template;
    }
}
