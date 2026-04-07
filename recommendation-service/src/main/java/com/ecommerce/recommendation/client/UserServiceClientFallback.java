package com.ecommerce.recommendation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for User Service client
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {
    
    @Override
    public UserDto getUser(Long userId) {
        log.warn("User service is unavailable. Returning fallback user for ID: {}", userId);
        return createFallbackUser(userId);
    }
    
    @Override
    public List<UserDto> getUsers(List<Long> userIds) {
        log.warn("User service is unavailable. Returning empty list for user IDs: {}", userIds);
        return Collections.emptyList();
    }
    
    @Override
    public UserPreferencesDto getUserPreferences(Long userId) {
        log.warn("User service is unavailable. Returning default preferences for user ID: {}", userId);
        return createFallbackPreferences(userId);
    }
    
    @Override
    public List<UserDto> getUsersBySegment(String segment) {
        log.warn("User service is unavailable. Returning empty list for segment: {}", segment);
        return Collections.emptyList();
    }
    
    @Override
    public List<UserDto> getSimilarUsers(Long userId, Integer limit) {
        log.warn("User service is unavailable. Returning empty similar users for ID: {}", userId);
        return Collections.emptyList();
    }
    
    private UserDto createFallbackUser(Long userId) {
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setEmail("unknown@example.com");
        fallbackUser.setFirstName("Unknown");
        fallbackUser.setLastName("User");
        fallbackUser.setGender("Unknown");
        fallbackUser.setAgeGroup("Unknown");
        fallbackUser.setLocation("Unknown");
        fallbackUser.setSegment("default");
        fallbackUser.setRegistrationDate(LocalDateTime.now());
        fallbackUser.setLastLoginDate(LocalDateTime.now());
        fallbackUser.setActive(true);
        fallbackUser.setAttributes(Collections.emptyMap());
        return fallbackUser;
    }
    
    private UserPreferencesDto createFallbackPreferences(Long userId) {
        UserPreferencesDto fallbackPreferences = new UserPreferencesDto();
        fallbackPreferences.setUserId(userId);
        fallbackPreferences.setPreferredCategories(Collections.emptyList());
        fallbackPreferences.setPreferredBrands(Collections.emptyList());
        fallbackPreferences.setMinPrice(0.0);
        fallbackPreferences.setMaxPrice(Double.MAX_VALUE);
        fallbackPreferences.setPriceRange("all");
        fallbackPreferences.setInterests(Collections.emptyList());
        fallbackPreferences.setPreferences(Collections.emptyMap());
        return fallbackPreferences;
    }
}
