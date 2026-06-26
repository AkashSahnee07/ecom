package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserOwnershipFilter extends OncePerRequestFilter {

    private static final Pattern USER_PATH_PATTERN = Pattern.compile(".*/user[s]?/([^/]+).*");
    private static final Pattern CART_PATH_PATTERN = Pattern.compile(".*/cart/([^/]+).*");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAdmin(principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        String pathUserId = extractPathUserId(request.getRequestURI());
        if (pathUserId != null && principal.getUserId() != null
                && !pathUserId.equals(String.valueOf(principal.getUserId()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for requested user resource");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdmin(SecurityPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String extractPathUserId(String path) {
        Matcher cartMatcher = CART_PATH_PATTERN.matcher(path);
        if (cartMatcher.matches()) {
            return cartMatcher.group(1);
        }

        Matcher userMatcher = USER_PATH_PATTERN.matcher(path);
        if (userMatcher.matches()) {
            return userMatcher.group(1);
        }

        return null;
    }
}
