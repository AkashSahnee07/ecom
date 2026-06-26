package com.ecommerce.common.security;

public interface TokenBlacklistService {

    boolean isBlacklisted(String jti);

    void blacklist(String jti, long ttlMillis);
}
