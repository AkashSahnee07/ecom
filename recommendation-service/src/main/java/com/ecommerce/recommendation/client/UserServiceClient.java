package com.ecommerce.recommendation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Feign client for User Service
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUser(@PathVariable("id") Long userId);
    
    @GetMapping("/api/users")
    List<UserDto> getUsers(@RequestParam("ids") List<Long> userIds);
    
    @GetMapping("/api/users/{id}/preferences")
    UserPreferencesDto getUserPreferences(@PathVariable("id") Long userId);
    
    @GetMapping("/api/users/segment/{segment}")
    List<UserDto> getUsersBySegment(@PathVariable("segment") String segment);
    
    @GetMapping("/api/users/{id}/similar")
    List<UserDto> getSimilarUsers(
        @PathVariable("id") Long userId,
        @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
    
    /**
     * User DTO for Feign client responses
     */
    class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String gender;
        private String ageGroup;
        private String location;
        private String segment;
        private LocalDateTime registrationDate;
        private LocalDateTime lastLoginDate;
        private Boolean active;
        private Map<String, Object> attributes;
        
        // Constructors
        public UserDto() {}
        
        public UserDto(Long id, String email, String firstName, String lastName, 
                      String gender, String ageGroup, String location, String segment,
                      LocalDateTime registrationDate, LocalDateTime lastLoginDate, 
                      Boolean active, Map<String, Object> attributes) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.gender = gender;
            this.ageGroup = ageGroup;
            this.location = location;
            this.segment = segment;
            this.registrationDate = registrationDate;
            this.lastLoginDate = lastLoginDate;
            this.active = active;
            this.attributes = attributes;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public String getAgeGroup() { return ageGroup; }
        public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getSegment() { return segment; }
        public void setSegment(String segment) { this.segment = segment; }
        
        public LocalDateTime getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
        
        public LocalDateTime getLastLoginDate() { return lastLoginDate; }
        public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }
    
    /**
     * User Preferences DTO
     */
    class UserPreferencesDto {
        private Long userId;
        private List<String> preferredCategories;
        private List<String> preferredBrands;
        private Double minPrice;
        private Double maxPrice;
        private String priceRange;
        private List<String> interests;
        private Map<String, Object> preferences;
        
        // Constructors
        public UserPreferencesDto() {}
        
        public UserPreferencesDto(Long userId, List<String> preferredCategories, 
                                 List<String> preferredBrands, Double minPrice, Double maxPrice,
                                 String priceRange, List<String> interests, 
                                 Map<String, Object> preferences) {
            this.userId = userId;
            this.preferredCategories = preferredCategories;
            this.preferredBrands = preferredBrands;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.priceRange = priceRange;
            this.interests = interests;
            this.preferences = preferences;
        }
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public List<String> getPreferredCategories() { return preferredCategories; }
        public void setPreferredCategories(List<String> preferredCategories) { this.preferredCategories = preferredCategories; }
        
        public List<String> getPreferredBrands() { return preferredBrands; }
        public void setPreferredBrands(List<String> preferredBrands) { this.preferredBrands = preferredBrands; }
        
        public Double getMinPrice() { return minPrice; }
        public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
        
        public Double getMaxPrice() { return maxPrice; }
        public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
        
        public String getPriceRange() { return priceRange; }
        public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
        
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
        
        public Map<String, Object> getPreferences() { return preferences; }
        public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }
    }
}
