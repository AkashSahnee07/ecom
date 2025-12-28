package com.ecommerce.user.dto;

import com.ecommerce.user.entity.User;

public class LoginResponseDto {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponseDto user;
    
    // Constructors
    public LoginResponseDto() {}
    
    public LoginResponseDto(String accessToken, String refreshToken, Long expiresIn, UserResponseDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    
    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }
}