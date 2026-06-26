package com.ecommerce.common.security;

import java.util.Date;

public record JwtClaims(
        String jti,
        String subject,
        Long userId,
        String email,
        String role,
        Date expiration
) {
}
