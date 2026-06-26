package com.ecommerce.common.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isBlacklisted(String jti) {
        return jti != null && blacklist.contains(jti);
    }

    @Override
    public void blacklist(String jti, long ttlMillis) {
        if (jti != null) {
            blacklist.add(jti);
        }
    }
}
