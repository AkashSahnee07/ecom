package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gateway filter that validates Bearer tokens on protected endpoints.
 * Public endpoints (auth, swagger, actuator, product listing) bypass authentication.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/users/register",
        "/api/products",
        "/actuator"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isBlank()) {
                log.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization scheme for path: {}", path);
                return onError(exchange, "Invalid Authorization header format. Expected: Bearer <token>",
                    HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                log.warn("Empty Bearer token for path: {}", path);
                return onError(exchange, "Empty Bearer token", HttpStatus.UNAUTHORIZED);
            }

            // Forward the token to downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-Token", token)
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private boolean isPublicEndpoint(String path) {
        if (path.contains("/v3/api-docs")
            || path.contains("/swagger-ui")
            || path.contains("/webjars")
            || path.equals("/swagger-ui.html")) {
            return true;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
            status.value(),
            status.getReasonPhrase(),
            message,
            exchange.getRequest().getPath().toString()
        );

        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties if needed
    }
}

