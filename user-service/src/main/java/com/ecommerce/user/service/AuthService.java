package com.ecommerce.user.service;

import com.ecommerce.common.security.JwtClaims;
import com.ecommerce.common.security.JwtProperties;
import com.ecommerce.common.security.JwtTokenService;
import com.ecommerce.common.security.TokenBlacklistService;
import com.ecommerce.user.dto.LoginRequestDto;
import com.ecommerce.user.dto.LoginResponseDto;
import com.ecommerce.user.dto.UserResponseDto;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private JwtProperties jwtProperties;

    public LoginResponseDto authenticate(LoginRequestDto loginRequest) {
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());

        UserResponseDto userDto = userService.getUserById(user.getId());

        return new LoginResponseDto(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenTtl() / 1000,
                userDto
        );
    }

    public LoginResponseDto refreshToken(String refreshToken) {
        JwtClaims claims = parseAndValidate(refreshToken);

        User user = userRepository.findByUsername(claims.subject())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        blacklistToken(refreshToken, claims);

        String newAccessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenService.generateRefreshToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());

        UserResponseDto userDto = userService.getUserById(user.getId());

        return new LoginResponseDto(
                newAccessToken,
                newRefreshToken,
                jwtProperties.getAccessTokenTtl() / 1000,
                userDto
        );
    }

    public void logout(String token) {
        JwtClaims claims = parseAndValidate(token);
        blacklistToken(token, claims);
    }

    public boolean validateToken(String token) {
        try {
            JwtClaims claims = parseAndValidate(token);
            return claims.jti() == null || !tokenBlacklistService.isBlacklisted(claims.jti());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseAndValidate(token).subject();
    }

    public JwtClaims parseAndValidate(String token) {
        JwtClaims claims = jwtTokenService.parseAndValidate(token);
        if (claims.jti() != null && tokenBlacklistService.isBlacklisted(claims.jti())) {
            throw new BadCredentialsException("Token is blacklisted");
        }
        return claims;
    }

    private JwtClaims parseClaims(String token) {
        return jwtTokenService.parseAndValidate(token);
    }

    private void blacklistToken(String token, JwtClaims claims) {
        if (claims.jti() != null) {
            tokenBlacklistService.blacklist(claims.jti(), jwtTokenService.remainingTtlMillis(claims));
        }
    }
}
