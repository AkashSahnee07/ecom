package com.ecommerce.gateway.filter;

import com.ecommerce.common.security.JwtClaims;
import com.ecommerce.common.security.JwtTokenService;
import com.ecommerce.common.security.PublicEndpointMatcher;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.common.security.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PublicEndpointMatcher publicEndpointMatcher;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;
    private final Timer authValidationTimer;

    public JwtAuthGlobalFilter(JwtTokenService jwtTokenService,
                               TokenBlacklistService tokenBlacklistService,
                               PublicEndpointMatcher publicEndpointMatcher,
                               ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.publicEndpointMatcher = publicEndpointMatcher;
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable(
                io.micrometer.core.instrument.simple.SimpleMeterRegistry::new);
        this.authSuccessCounter = Counter.builder("auth.jwt.validation.total")
                .tag("result", "success")
                .tag("layer", "gateway")
                .register(meterRegistry);
        this.authFailureCounter = Counter.builder("auth.jwt.validation.total")
                .tag("result", "failure")
                .tag("layer", "gateway")
                .register(meterRegistry);
        this.authValidationTimer = Timer.builder("auth.jwt.validation.duration")
                .tag("layer", "gateway")
                .register(meterRegistry);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";

        if (publicEndpointMatcher.isPublic(method, path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            authFailureCounter.increment();
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = jwtTokenService.stripBearerPrefix(authHeader);
        if (!StringUtils.hasText(token)) {
            authFailureCounter.increment();
            return unauthorized(exchange, "Empty Bearer token");
        }

        try {
            JwtClaims claims;
            try {
                claims = authValidationTimer.recordCallable(() -> jwtTokenService.parseAndValidate(token));
            } catch (Exception ex) {
                throw new JwtException("Invalid token", ex);
            }
            if (claims.jti() != null && tokenBlacklistService.isBlacklisted(claims.jti())) {
                authFailureCounter.increment();
                return unauthorized(exchange, "Token revoked");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(SecurityConstants.AUTH_TOKEN_HEADER, token)
                    .header(SecurityConstants.USER_ID_HEADER, claims.userId() != null ? String.valueOf(claims.userId()) : "")
                    .header(SecurityConstants.USER_ROLE_HEADER, claims.role() != null ? claims.role() : "")
                    .header(SecurityConstants.USERNAME_HEADER, claims.subject() != null ? claims.subject() : "")
                    .build();

            authSuccessCounter.increment();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed for path {}: {}", path, ex.getMessage());
            authFailureCounter.increment();
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                message,
                exchange.getRequest().getPath().value()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
