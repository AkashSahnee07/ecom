package com.ecommerce.user.service;

import com.ecommerce.user.dto.LoginRequestDto;
import com.ecommerce.user.dto.LoginResponseDto;
import com.ecommerce.user.dto.UserResponseDto;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    
    @Value("${jwt.secret:bXlTdXBlclNlY3JldEtleUZvckpXVEhTQTI1Ng==}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours
    private Long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 days
    private Long refreshExpiration;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;
    
    // In-memory blacklist for logged out tokens (in production, use Redis)
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    
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
        
        String accessToken = generateToken(user, jwtExpiration);
        String refreshToken = generateToken(user, refreshExpiration);
        
        UserResponseDto userDto = userService.getUserById(user.getId());
        
        return new LoginResponseDto(accessToken, refreshToken, jwtExpiration / 1000, userDto);
    }
    
    public LoginResponseDto refreshToken(String refreshToken) {
        if (isTokenBlacklisted(refreshToken)) {
            throw new BadCredentialsException("Token is blacklisted");
        }
        
        try {
            Claims claims = validateTokenAndGetClaims(refreshToken);
            String username = claims.getSubject();
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            if (!user.getIsActive()) {
                throw new BadCredentialsException("Account is deactivated");
            }
            
            String newAccessToken = generateToken(user, jwtExpiration);
            String newRefreshToken = generateToken(user, refreshExpiration);
            
            // Blacklist the old refresh token
            tokenBlacklist.add(refreshToken);
            
            UserResponseDto userDto = userService.getUserById(user.getId());
            
            return new LoginResponseDto(newAccessToken, newRefreshToken, jwtExpiration / 1000, userDto);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }
    
    public void logout(String token) {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        tokenBlacklist.add(token);
    }
    
    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                return false;
            }
            validateTokenAndGetClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        return claims.getSubject();
    }
    
    private String generateToken(User user, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    private Claims validateTokenAndGetClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}
