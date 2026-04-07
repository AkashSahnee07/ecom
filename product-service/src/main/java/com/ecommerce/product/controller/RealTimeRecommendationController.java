package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.service.RealTimeRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/real-time-recommendations")
@Tag(name = "Real-Time Recommendation Management", description = "APIs for real-time recommendations")
public class RealTimeRecommendationController {
    
    @Autowired
    private RealTimeRecommendationService realTimeRecommendationService;
    
    // Get fresh real-time recommendations for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductResponseDto>> getFreshRecommendations(
            @PathVariable Long userId,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        try {
            List<ProductResponseDto> recommendations = realTimeRecommendationService
                .getFreshRecommendations(userId, limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Trigger real-time recommendation generation
    @PostMapping("/generate/{userId}")
    public ResponseEntity<String> generateRecommendations(
            @PathVariable Long userId,
            @RequestParam(name = "triggerEvent", defaultValue = "manual") String triggerEvent) {
        try {
            realTimeRecommendationService.generateRealTimeRecommendations(userId, triggerEvent);
            return ResponseEntity.ok("Real-time recommendations generated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error generating recommendations: " + e.getMessage());
        }
    }
    
    // Get recommendation statistics for a user
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendationStats(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = realTimeRecommendationService.getRecommendationStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Clean up old recommendations (admin endpoint)
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupOldRecommendations() {
        try {
            realTimeRecommendationService.cleanupOldRecommendations();
            return ResponseEntity.ok("Old recommendations cleaned up successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error cleaning up recommendations: " + e.getMessage());
        }
    }
    
    // Health check endpoint for real-time recommendation service
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "RealTimeRecommendationService",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
