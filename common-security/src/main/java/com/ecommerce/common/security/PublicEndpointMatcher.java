package com.ecommerce.common.security;

public class PublicEndpointMatcher {

    private final JwtProperties jwtProperties;

    public PublicEndpointMatcher(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public boolean isPublic(String method, String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (isDocumentationPath(path) || isActuatorPath(path)) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && isPublicProductRead(path)) {
            return true;
        }

        return jwtProperties.getPublicPaths().stream().anyMatch(path::startsWith);
    }

    private boolean isDocumentationPath(String path) {
        return path.contains("/v3/api-docs")
                || path.contains("/swagger-ui")
                || path.contains("/webjars")
                || path.equals("/swagger-ui.html");
    }

    private boolean isActuatorPath(String path) {
        return path.startsWith("/actuator");
    }

    private boolean isPublicProductRead(String path) {
        return path.startsWith("/products")
                || path.startsWith("/categories")
                || path.startsWith("/api/products")
                || path.startsWith("/api/categories");
    }
}
