package com.ecommerce.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String email, String role) {
        return generateToken(userId, username, email, role, jwtProperties.getAccessTokenTtl());
    }

    public String generateRefreshToken(Long userId, String username, String email, String role) {
        return generateToken(userId, username, email, role, jwtProperties.getRefreshTokenTtl());
    }

    public String generateToken(Long userId, String username, String email, String role, long ttlMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public JwtClaims parseAndValidate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseClaimsJws(stripBearerPrefix(token))
                .getBody();

        Long userId = claims.get("userId", Number.class) != null
                ? claims.get("userId", Number.class).longValue()
                : null;

        return new JwtClaims(
                claims.get("jti", String.class),
                claims.getSubject(),
                userId,
                claims.get("email", String.class),
                claims.get("role", String.class),
                claims.getExpiration()
        );
    }

    public boolean isTokenValid(String token) {
        try {
            parseAndValidate(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long remainingTtlMillis(JwtClaims claims) {
        if (claims.expiration() == null) {
            return jwtProperties.getAccessTokenTtl();
        }
        return Math.max(claims.expiration().getTime() - System.currentTimeMillis(), 1L);
    }

    public String stripBearerPrefix(String token) {
        if (token == null) {
            return null;
        }
        if (token.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return token.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        return token.trim();
    }
}
