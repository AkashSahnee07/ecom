package com.ecommerce.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-with-enough-length-for-hmac-sha256");
        properties.setIssuer("ecommerce-platform");
        properties.setAccessTokenTtl(60_000L);
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void generatesAndValidatesToken() {
        String token = jwtTokenService.generateAccessToken(42L, "john", "john@example.com", "CUSTOMER");

        assertTrue(jwtTokenService.isTokenValid(token));

        JwtClaims claims = jwtTokenService.parseAndValidate(token);
        assertEquals("john", claims.subject());
        assertEquals(42L, claims.userId());
        assertEquals("CUSTOMER", claims.role());
        assertFalse(claims.jti().isBlank());
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtTokenService.generateAccessToken(1L, "user", "user@example.com", "CUSTOMER");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtTokenService.isTokenValid(tampered));
    }
}
