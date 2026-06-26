package com.ecommerce.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "mySecretKeybXlTdXBlclNlY3JldEtleUZvckpXVEhTQTI1Ng==";
    private String issuer = "ecommerce-platform";
    private long accessTokenTtl = 900_000L;
    private long refreshTokenTtl = 604_800_000L;
    private String blacklistPrefix = "jwt:bl:";
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/users/register",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    ));

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(long accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(long refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String getBlacklistPrefix() {
        return blacklistPrefix;
    }

    public void setBlacklistPrefix(String blacklistPrefix) {
        this.blacklistPrefix = blacklistPrefix;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
