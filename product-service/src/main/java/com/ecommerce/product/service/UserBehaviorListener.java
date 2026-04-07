package com.ecommerce.product.service;

import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.repository.UserPreferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserBehaviorListener {
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private RealTimeRecommendationService realTimeRecommendationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Listen to user view events
    @KafkaListener(topics = "user-product-view", groupId = "product-recommendation-group")
    public void handleProductView(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long userId = event.get("userId").asLong();
            Long productId = event.get("productId").asLong();
            Long categoryId = event.has("categoryId") ? event.get("categoryId").asLong() : null;
            String brand = event.has("brand") ? event.get("brand").asText() : null;
            Double price = event.has("price") ? event.get("price").asDouble() : null;
            
            System.out.println("Processing product view: User " + userId + " viewed product " + productId);
            
            // Update user preferences based on viewed product
            updateUserPreferencesFromView(userId, categoryId, brand, price);
            
            // Trigger real-time recommendation generation
            realTimeRecommendationService.generateRealTimeRecommendations(userId, "view");
            
        } catch (Exception e) {
            System.err.println("Error processing product view event: " + e.getMessage());
        }
    }
    
    // Listen to user purchase events
    @KafkaListener(topics = "user-purchase", groupId = "product-recommendation-group")
    public void handlePurchase(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long userId = event.get("userId").asLong();
            JsonNode items = event.get("items");
            
            System.out.println("Processing purchase: User " + userId + " made a purchase");
            
            // Process each purchased item
            if (items.isArray()) {
                for (JsonNode item : items) {
                    Long productId = item.get("productId").asLong();
                    Long categoryId = item.has("categoryId") ? item.get("categoryId").asLong() : null;
                    String brand = item.has("brand") ? item.get("brand").asText() : null;
                    Double price = item.has("price") ? item.get("price").asDouble() : null;
                    
                    // Update preferences with higher weight for purchases
                    updateUserPreferencesFromPurchase(userId, categoryId, brand, price);
                }
            }
            
            // Regenerate recommendations after purchase
            realTimeRecommendationService.generateRealTimeRecommendations(userId, "purchase");
            
        } catch (Exception e) {
            System.err.println("Error processing purchase event: " + e.getMessage());
        }
    }
    
    // Listen to user search events
    @KafkaListener(topics = "user-search", groupId = "product-recommendation-group")
    public void handleSearch(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long userId = event.get("userId").asLong();
            String searchQuery = event.get("query").asText();
            Long categoryId = event.has("categoryId") ? event.get("categoryId").asLong() : null;
            String brand = event.has("brand") ? event.get("brand").asText() : null;
            Double minPrice = event.has("minPrice") ? event.get("minPrice").asDouble() : null;
            Double maxPrice = event.has("maxPrice") ? event.get("maxPrice").asDouble() : null;
            
            System.out.println("Processing search: User " + userId + " searched for '" + searchQuery + "'");
            
            // Update preferences based on search filters
            updateUserPreferencesFromSearch(userId, categoryId, brand, minPrice, maxPrice);
            
        } catch (Exception e) {
            System.err.println("Error processing search event: " + e.getMessage());
        }
    }
    
    // Listen to user rating events
    @KafkaListener(topics = "user-rating", groupId = "product-recommendation-group")
    public void handleRating(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long userId = event.get("userId").asLong();
            Long productId = event.get("productId").asLong();
            Double rating = event.get("rating").asDouble();
            Long categoryId = event.has("categoryId") ? event.get("categoryId").asLong() : null;
            String brand = event.has("brand") ? event.get("brand").asText() : null;
            
            System.out.println("Processing rating: User " + userId + " rated product " + productId + " with " + rating + " stars");
            
            // Update minimum rating preference based on user's rating behavior
            updateUserPreferencesFromRating(userId, rating, categoryId, brand);
            
            // Regenerate recommendations
            realTimeRecommendationService.generateRealTimeRecommendations(userId, "rating");
            
        } catch (Exception e) {
            System.err.println("Error processing rating event: " + e.getMessage());
        }
    }
    
    // Update user preferences based on product view
    private void updateUserPreferencesFromView(Long userId, Long categoryId, String brand, Double price) {
        try {
            UserPreference preference = getUserPreferenceOrCreate(userId);
            
            // Add category to preferences
            if (categoryId != null) {
                List<Long> categories = preference.getPreferredCategories();
                if (categories == null) {
                    categories = new ArrayList<>();
                }
                if (!categories.contains(categoryId)) {
                    categories.add(categoryId);
                    preference.setPreferredCategories(categories);
                }
            }
            
            // Add brand to preferences
            if (brand != null) {
                List<String> brands = preference.getPreferredBrands();
                if (brands == null) {
                    brands = new ArrayList<>();
                }
                if (!brands.contains(brand)) {
                    brands.add(brand);
                    preference.setPreferredBrands(brands);
                }
            }
            
            // Update price range based on viewed product
            if (price != null) {
                updatePriceRange(preference, price, 0.1); // Light weight for views
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            userPreferenceRepository.save(preference);
            
        } catch (Exception e) {
            System.err.println("Error updating preferences from view: " + e.getMessage());
        }
    }
    
    // Update user preferences based on purchase (higher weight)
    private void updateUserPreferencesFromPurchase(Long userId, Long categoryId, String brand, Double price) {
        try {
            UserPreference preference = getUserPreferenceOrCreate(userId);
            
            // Add category with higher priority
            if (categoryId != null) {
                List<Long> categories = preference.getPreferredCategories();
                if (categories == null) {
                    categories = new ArrayList<>();
                }
                // Move to front if already exists, or add
                categories.remove(categoryId);
                categories.add(0, categoryId);
                preference.setPreferredCategories(categories);
            }
            
            // Add brand with higher priority
            if (brand != null) {
                List<String> brands = preference.getPreferredBrands();
                if (brands == null) {
                    brands = new ArrayList<>();
                }
                // Move to front if already exists, or add
                brands.remove(brand);
                brands.add(0, brand);
                preference.setPreferredBrands(brands);
            }
            
            // Update price range with higher weight for purchases
            if (price != null) {
                updatePriceRange(preference, price, 0.3); // Higher weight for purchases
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            userPreferenceRepository.save(preference);
            
        } catch (Exception e) {
            System.err.println("Error updating preferences from purchase: " + e.getMessage());
        }
    }
    
    // Update user preferences based on search behavior
    private void updateUserPreferencesFromSearch(Long userId, Long categoryId, String brand, Double minPrice, Double maxPrice) {
        try {
            UserPreference preference = getUserPreferenceOrCreate(userId);
            
            // Update category preference from search
            if (categoryId != null) {
                List<Long> categories = preference.getPreferredCategories();
                if (categories == null) {
                    categories = new ArrayList<>();
                }
                if (!categories.contains(categoryId)) {
                    categories.add(categoryId);
                    preference.setPreferredCategories(categories);
                }
            }
            
            // Update brand preference from search
            if (brand != null) {
                List<String> brands = preference.getPreferredBrands();
                if (brands == null) {
                    brands = new ArrayList<>();
                }
                if (!brands.contains(brand)) {
                    brands.add(brand);
                    preference.setPreferredBrands(brands);
                }
            }
            
            // Update price range from search filters
            if (minPrice != null && maxPrice != null) {
                preference.setMinPrice(minPrice);
                preference.setMaxPrice(maxPrice);
            } else if (minPrice != null) {
                preference.setMinPrice(minPrice);
            } else if (maxPrice != null) {
                preference.setMaxPrice(maxPrice);
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            userPreferenceRepository.save(preference);
            
        } catch (Exception e) {
            System.err.println("Error updating preferences from search: " + e.getMessage());
        }
    }
    
    // Update user preferences based on rating behavior
    private void updateUserPreferencesFromRating(Long userId, Double rating, Long categoryId, String brand) {
        try {
            UserPreference preference = getUserPreferenceOrCreate(userId);
            
            // Update minimum rating based on user's rating patterns
            Double currentMinRating = preference.getMinRating();
            if (currentMinRating == null) {
                // Set initial minimum rating slightly below their rating
                preference.setMinRating(Math.max(1.0, rating - 1.0));
            } else {
                // Adjust minimum rating based on their rating behavior
                double adjustedMinRating = (currentMinRating * 0.8) + (rating * 0.2);
                preference.setMinRating(Math.max(1.0, adjustedMinRating - 0.5));
            }
            
            // If they rated highly, strengthen category and brand preferences
            if (rating >= 4.0) {
                if (categoryId != null) {
                    List<Long> categories = preference.getPreferredCategories();
                    if (categories == null) {
                        categories = new ArrayList<>();
                    }
                    if (!categories.contains(categoryId)) {
                        categories.add(0, categoryId); // Add to front
                        preference.setPreferredCategories(categories);
                    }
                }
                
                if (brand != null) {
                    List<String> brands = preference.getPreferredBrands();
                    if (brands == null) {
                        brands = new ArrayList<>();
                    }
                    if (!brands.contains(brand)) {
                        brands.add(0, brand); // Add to front
                        preference.setPreferredBrands(brands);
                    }
                }
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            userPreferenceRepository.save(preference);
            
        } catch (Exception e) {
            System.err.println("Error updating preferences from rating: " + e.getMessage());
        }
    }
    
    // Helper method to get existing preference or create new one
    private UserPreference getUserPreferenceOrCreate(Long userId) {
        Optional<UserPreference> existingPreference = userPreferenceRepository.findByUserId(userId);
        if (existingPreference.isPresent()) {
            return existingPreference.get();
        }
        
        UserPreference newPreference = new UserPreference();
        newPreference.setUserId(userId);
        newPreference.setCreatedAt(LocalDateTime.now());
        newPreference.setUpdatedAt(LocalDateTime.now());
        return newPreference;
    }
    
    // Helper method to update price range based on user behavior
    private void updatePriceRange(UserPreference preference, Double price, double weight) {
        Double currentMin = preference.getMinPrice();
        Double currentMax = preference.getMaxPrice();
        
        if (currentMin == null || currentMax == null) {
            // Set initial range around the price
            preference.setMinPrice(price * 0.7);
            preference.setMaxPrice(price * 1.5);
        } else {
            // Gradually adjust range towards the new price
            double newMin = currentMin * (1 - weight) + (price * 0.8) * weight;
            double newMax = currentMax * (1 - weight) + (price * 1.2) * weight;
            
            preference.setMinPrice(Math.min(newMin, price * 0.5));
            preference.setMaxPrice(Math.max(newMax, price * 2.0));
        }
    }
}
