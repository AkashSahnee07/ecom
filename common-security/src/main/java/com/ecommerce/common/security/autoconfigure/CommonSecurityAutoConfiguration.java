package com.ecommerce.common.security.autoconfigure;

import com.ecommerce.common.security.InMemoryTokenBlacklistService;
import com.ecommerce.common.security.JwtProperties;
import com.ecommerce.common.security.JwtTokenService;
import com.ecommerce.common.security.PublicEndpointMatcher;
import com.ecommerce.common.security.RedisTokenBlacklistService;
import com.ecommerce.common.security.TokenBlacklistService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(JwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PublicEndpointMatcher publicEndpointMatcher(JwtProperties jwtProperties) {
        return new PublicEndpointMatcher(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean(TokenBlacklistService.class)
    public TokenBlacklistService tokenBlacklistService(
            org.springframework.beans.factory.ObjectProvider<org.springframework.data.redis.core.StringRedisTemplate> redisTemplateProvider,
            JwtProperties jwtProperties) {
        org.springframework.data.redis.core.StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return new RedisTokenBlacklistService(redisTemplate, jwtProperties);
        }
        return new InMemoryTokenBlacklistService();
    }
}
