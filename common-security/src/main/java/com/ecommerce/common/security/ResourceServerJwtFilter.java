package com.ecommerce.common.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ResourceServerJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ResourceServerJwtFilter.class);

    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PublicEndpointMatcher publicEndpointMatcher;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;
    private final Timer authValidationTimer;

    public ResourceServerJwtFilter(JwtTokenService jwtTokenService,
                                   TokenBlacklistService tokenBlacklistService,
                                   PublicEndpointMatcher publicEndpointMatcher,
                                   ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.publicEndpointMatcher = publicEndpointMatcher;
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable(io.micrometer.core.instrument.simple.SimpleMeterRegistry::new);
        this.authSuccessCounter = Counter.builder("auth.jwt.validation.total")
                .tag("result", "success")
                .register(meterRegistry);
        this.authFailureCounter = Counter.builder("auth.jwt.validation.total")
                .tag("result", "failure")
                .register(meterRegistry);
        this.authValidationTimer = Timer.builder("auth.jwt.validation.duration")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (publicEndpointMatcher.isPublic(request.getMethod(), path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            authFailureCounter.increment();
            filterChain.doFilter(request, response);
            return;
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
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
                return;
            }

            SecurityPrincipal principal = SecurityPrincipal.fromClaims(claims);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            authSuccessCounter.increment();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed for path {}: {}", path, ex.getMessage());
            authFailureCounter.increment();
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return jwtTokenService.stripBearerPrefix(authHeader);
        }
        return request.getHeader(SecurityConstants.AUTH_TOKEN_HEADER);
    }
}
