package com.ecommerce.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip authentication for public endpoints
            if (isPublicEndpoint(request.getPath().toString())) {
                return chain.filter(exchange);
            }
            
            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            // Extract token and validate (simplified for demo)
            String token = authHeader.substring(7);
            if (!isValidToken(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            return chain.filter(exchange);
        };
    }
    
    private boolean isPublicEndpoint(String path) {
        List<String> publicPaths = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/products",
            "/actuator"
        );
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    private boolean isValidToken(String token) {
        // Simplified token validation - in real implementation,
        // you would validate JWT token with proper signature verification
        return token != null && !token.isEmpty();
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}