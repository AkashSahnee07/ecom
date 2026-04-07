package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.entity.ProductRecommendation;
import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.repository.ProductRecommendationRepository;
import com.ecommerce.product.repository.UserPreferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class RealTimeRecommendationService {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;
    
    @Autowired
    private ContentBasedFilteringService contentBasedFilteringService;
    
    @Autowired
    private ProductRecommendationRepository recommendationRepository;
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    @Autowired
    @Qualifier("stringKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Generate real-time recommendations triggered by user behavior
    public void generateRealTimeRecommendations(Long userId, String triggerEvent) {
        try {
            System.out.println("Generating real-time recommendations for user " + userId + " triggered by " + triggerEvent);
            
            // Get user preferences
            Optional<UserPreference> userPrefOpt = userPreferenceRepository.findByUserId(userId);
            if (!userPrefOpt.isPresent()) {
                System.out.println("No user preferences found for user " + userId + ", creating default preferences");
                return;
            }
            
            UserPreference userPreference = userPrefOpt.get();
            
            // Generate different types of recommendations based on trigger event
            switch (triggerEvent.toLowerCase()) {
                case "view":
                    generateViewBasedRecommendations(userId, userPreference);
                    break;
                case "purchase":
                    generatePurchaseBasedRecommendations(userId, userPreference);
                    break;
                case "search":
                    generateSearchBasedRecommendations(userId, userPreference);
                    break;
                case "rating":
                    generateRatingBasedRecommendations(userId, userPreference);
                    break;
                default:
                    generateGeneralRecommendations(userId, userPreference);
            }
            
            // Send real-time recommendation update event
            sendRecommendationUpdateEvent(userId, triggerEvent);
            
        } catch (Exception e) {
            System.err.println("Error generating real-time recommendations: " + e.getMessage());
        }
    }
    
    // Generate recommendations based on product views
    private void generateViewBasedRecommendations(Long userId, UserPreference userPreference) {
        try {
            // Focus on content-based recommendations for views
            List<ProductRecommendation> contentBasedRecs = contentBasedFilteringService.generateRecommendations(userId, 10);
            
            // Save recommendations with view-based type
            saveRecommendationsFromList(userId, contentBasedRecs, "view_triggered");
            
            System.out.println("Generated " + contentBasedRecs.size() + " view-based recommendations for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error generating view-based recommendations: " + e.getMessage());
        }
    }
    
    // Generate recommendations based on purchases
    private void generatePurchaseBasedRecommendations(Long userId, UserPreference userPreference) {
        try {
            // Use collaborative filtering for purchase-based recommendations
            List<ProductRecommendation> collaborativeRecs = collaborativeFilteringService.generateRecommendations(userId, 15);
            
            // Save recommendations
            saveRecommendationsFromList(userId, collaborativeRecs, "purchase_triggered");
            
            System.out.println("Generated " + collaborativeRecs.size() + " purchase-based recommendations for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error generating purchase-based recommendations: " + e.getMessage());
        }
    }
    
    // Generate recommendations based on search behavior
    private void generateSearchBasedRecommendations(Long userId, UserPreference userPreference) {
        try {
            // Focus on content-based recommendations that match search criteria
            List<ProductRecommendation> searchBasedRecs = contentBasedFilteringService.generateRecommendations(userId, 12);
            
            // Save recommendations
            saveRecommendationsFromList(userId, searchBasedRecs, "search_triggered");
            
            System.out.println("Generated " + searchBasedRecs.size() + " search-based recommendations for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error generating search-based recommendations: " + e.getMessage());
        }
    }
    
    // Generate recommendations based on rating behavior
    private void generateRatingBasedRecommendations(Long userId, UserPreference userPreference) {
        try {
            // Use both collaborative and content-based filtering for rating-based recommendations
            List<ProductRecommendation> collaborativeRecs = collaborativeFilteringService.generateRecommendations(userId, 8);
            List<ProductRecommendation> contentBasedRecs = contentBasedFilteringService.generateRecommendations(userId, 8);
            
            // Save recommendations
            saveRecommendationsFromList(userId, collaborativeRecs, "rating_triggered");
            saveRecommendationsFromList(userId, contentBasedRecs, "rating_triggered");
            
            System.out.println("Generated " + (collaborativeRecs.size() + contentBasedRecs.size()) + " rating-based recommendations for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error generating rating-based recommendations: " + e.getMessage());
        }
    }
    
    // Generate general recommendations
    private void generateGeneralRecommendations(Long userId, UserPreference userPreference) {
        try {
            // Use the main recommendation service for general recommendations
            recommendationService.generateRecommendationsForUser(userId);
            
            System.out.println("Generated general recommendations for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error generating general recommendations: " + e.getMessage());
        }
    }
    
    // Save recommendations from ProductRecommendation list
    private void saveRecommendationsFromList(Long userId, List<ProductRecommendation> recommendations, String reason) {
        try {
            for (ProductRecommendation rec : recommendations) {
                // Check if recommendation already exists
                Optional<ProductRecommendation> existingRec = recommendationRepository
                    .findByUserIdAndProductIdAndRecommendationType(userId, rec.getProductId(), rec.getRecommendationType());
                
                if (existingRec.isPresent()) {
                    ProductRecommendation existing = existingRec.get();
                    existing.setScore(existing.getScore() + 0.1); // Boost existing recommendation
                    existing.setReason(reason);
                    recommendationRepository.save(existing);
                } else {
                    rec.setUserId(userId);
                    rec.setReason(reason);
                    recommendationRepository.save(rec);
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving recommendations: " + e.getMessage());
        }
    }
    
    // Send real-time recommendation update event via Kafka
    private void sendRecommendationUpdateEvent(Long userId, String triggerEvent) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("triggerEvent", triggerEvent);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("eventType", "recommendation_updated");
            
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("recommendation-updates", userId.toString(), eventJson);
            
            System.out.println("Sent recommendation update event for user " + userId);
            
        } catch (Exception e) {
            System.err.println("Error sending recommendation update event: " + e.getMessage());
        }
    }
    
    // Get fresh recommendations for a user (called by API)
    public List<ProductResponseDto> getFreshRecommendations(Long userId, int limit) {
        try {
            // Trigger fresh recommendation generation
            generateRealTimeRecommendations(userId, "api_request");
            
            // Return the latest recommendations
            return recommendationService.getRecommendationsForUser(userId, 0, limit).getContent();
            
        } catch (Exception e) {
            System.err.println("Error getting fresh recommendations: " + e.getMessage());
            return List.of();
        }
    }
    
    // Clean up old real-time recommendations
    public void cleanupOldRecommendations() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // Keep recommendations for 7 days
            
            List<ProductRecommendation> oldRecommendations = recommendationRepository
                .findByCreatedAtBefore(cutoffTime);
            
            if (!oldRecommendations.isEmpty()) {
                recommendationRepository.deleteAll(oldRecommendations);
                System.out.println("Cleaned up " + oldRecommendations.size() + " old recommendations");
            }
            
        } catch (Exception e) {
            System.err.println("Error cleaning up old recommendations: " + e.getMessage());
        }
    }
    
    // Get recommendation statistics for monitoring
    public Map<String, Object> getRecommendationStats(Long userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Count recommendations by type
            long collaborativeCount = recommendationRepository.countByUserIdAndRecommendationType(
                userId, ProductRecommendation.RecommendationType.COLLABORATIVE_FILTERING);
            long contentBasedCount = recommendationRepository.countByUserIdAndRecommendationType(
                userId, ProductRecommendation.RecommendationType.CONTENT_BASED);
            long similarProductsCount = recommendationRepository.countByUserIdAndRecommendationType(
                userId, ProductRecommendation.RecommendationType.SIMILAR_PRODUCTS);
            
            stats.put("collaborativeFilteringCount", collaborativeCount);
            stats.put("contentBasedCount", contentBasedCount);
            stats.put("similarProductsCount", similarProductsCount);
            stats.put("totalRecommendations", collaborativeCount + contentBasedCount + similarProductsCount);
            
            // Get latest recommendation timestamp
            Optional<ProductRecommendation> latestRec = recommendationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId);
            if (latestRec.isPresent()) {
                stats.put("lastUpdated", latestRec.get().getCreatedAt().toString());
            }
            
            return stats;
            
        } catch (Exception e) {
            System.err.println("Error getting recommendation stats: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
