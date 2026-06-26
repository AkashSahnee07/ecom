package com.ecommerce.common.security;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Boolean exists = redisTemplate.hasKey(key(jti));
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void blacklist(String jti, long ttlMillis) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(key(jti), "1", Duration.ofMillis(Math.max(ttlMillis, 1L)));
    }

    private String key(String jti) {
        return jwtProperties.getBlacklistPrefix() + jti;
    }
}
