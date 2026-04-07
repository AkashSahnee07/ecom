package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductSimilarity;
import com.ecommerce.recommendation.repository.ProductSimilarityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing product similarity calculations and recommendations.
 * Handles similarity computation, storage, and retrieval for recommendation algorithms.
 */
@Service
@RequiredArgsConstructor
public class ProductSimilarityService {

    private static final Logger log = LoggerFactory.getLogger(ProductSimilarityService.class);
    private final ProductSimilarityRepository productSimilarityRepository;
    
    /**
     * Find similar products for a given product.
     */
    @Cacheable(value = "similarProducts", key = "#productId + '_' + #limit")
    public List<ProductSimilarity> findSimilarProducts(String productId, int limit) {
        log.debug("Finding similar products for product: {} with limit: {}", productId, limit);
        
        try {
            List<ProductSimilarity> similarities = productSimilarityRepository
            .findBySourceProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(productId);
            
            return similarities.stream()
                .filter(ProductSimilarity::isValidSimilarity)
                .limit(limit)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error finding similar products for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find similar products by similarity type.
     */
    @Cacheable(value = "similarProductsByType", key = "#productId + '_' + #similarityType + '_' + #limit")
    public List<ProductSimilarity> findSimilarProductsByType(String productId, 
                                                           ProductSimilarity.SimilarityType similarityType, 
                                                           int limit) {
        
        log.debug("Finding similar products for product: {} by type: {} with limit: {}", 
                 productId, similarityType, limit);
        
        try {
            List<ProductSimilarity> similarities = productSimilarityRepository
                .findBySourceProductIdAndSimilarityTypeAndIsActiveTrueOrderBySimilarityScoreDesc(
                    productId, similarityType);
            
            return similarities.stream()
                .filter(ProductSimilarity::isValidSimilarity)
                .limit(limit)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error finding similar products by type for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculate and store product similarity.
     */
    public ProductSimilarity calculateAndStoreSimilarity(String sourceProductId, 
                                                       String targetProductId,
                                                       ProductSimilarity.SimilarityType similarityType,
                                                       Map<String, Object> productData) {
        
        log.debug("Calculating similarity between products: {} and {} using type: {}", 
                 sourceProductId, targetProductId, similarityType);
        
        try {
            // Check if similarity already exists
            Optional<ProductSimilarity> existingSimilarity = productSimilarityRepository
                .findBySourceProductIdAndTargetProductIdAndSimilarityType(
                    sourceProductId, targetProductId, similarityType);
            
            ProductSimilarity similarity;
            
            if (existingSimilarity.isPresent()) {
                similarity = existingSimilarity.get();
                log.debug("Updating existing similarity: {}", similarity.getId());
            } else {
                similarity = ProductSimilarity.builder()
                    .sourceProductId(sourceProductId)
                    .targetProductId(targetProductId)
                    .similarityType(similarityType)
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .usageCount(0)
                    // Note: successCount field doesn't exist in entity, using successRate instead
                    .successRate(0.0)
                    .build();
                
                log.debug("Creating new similarity between products: {} and {}", 
                         sourceProductId, targetProductId);
            }
            
            // Calculate similarity score based on type
            double similarityScore = calculateSimilarityScore(similarityType, productData);
            similarity.setSimilarityScore(similarityScore);
            
            // Calculate confidence based on data quality and completeness
            double confidence = calculateConfidence(productData, similarityScore);
            similarity.setConfidence(confidence);
            
            // Extract and set additional metrics
            extractAndSetMetrics(similarity, productData);
            
            similarity.setUpdatedAt(LocalDateTime.now());
            
            ProductSimilarity savedSimilarity = productSimilarityRepository.save(similarity);
            
            log.info("Calculated similarity: id={}, score={}, confidence={}", 
                    savedSimilarity.getId(), similarityScore, confidence);
            
            return savedSimilarity;
            
        } catch (Exception e) {
            log.error("Error calculating similarity between products: {} and {}", 
                     sourceProductId, targetProductId, e);
            throw new RuntimeException("Failed to calculate product similarity", e);
        }
    }
    
    /**
     * Batch calculate similarities for a product.
     */
    public List<ProductSimilarity> batchCalculateSimilarities(String sourceProductId,
                                                            List<String> targetProductIds,
                                                            ProductSimilarity.SimilarityType similarityType,
                                                            Map<String, Map<String, Object>> productsData) {
        
        log.info("Batch calculating {} similarities for product: {} using type: {}", 
                targetProductIds.size(), sourceProductId, similarityType);
        
        List<ProductSimilarity> similarities = new ArrayList<>();
        
        try {
            for (String targetProductId : targetProductIds) {
                if (!sourceProductId.equals(targetProductId)) {
                    Map<String, Object> productData = productsData.get(targetProductId);
                    if (productData != null) {
                        ProductSimilarity similarity = calculateAndStoreSimilarity(
                            sourceProductId, targetProductId, similarityType, productData);
                        similarities.add(similarity);
                    }
                }
            }
            
            log.info("Successfully calculated {} similarities for product: {}", 
                    similarities.size(), sourceProductId);
            
            return similarities;
            
        } catch (Exception e) {
            log.error("Error in batch similarity calculation for product: {}", sourceProductId, e);
            throw new RuntimeException("Failed to batch calculate similarities", e);
        }
    }
    
    /**
     * Update similarity usage statistics.
     */
    public void recordSimilarityUsage(String sourceProductId, String targetProductId, 
                                    ProductSimilarity.SimilarityType similarityType, 
                                    boolean wasSuccessful) {
        
        try {
            Optional<ProductSimilarity> similarityOpt = productSimilarityRepository
                .findBySourceProductIdAndTargetProductIdAndSimilarityType(
                    sourceProductId, targetProductId, similarityType);
            
            if (similarityOpt.isPresent()) {
                ProductSimilarity similarity = similarityOpt.get();
                similarity.incrementUsage();
                
                if (wasSuccessful) {
                    // Note: incrementSuccess method doesn't exist, using updateSuccessRate instead
                    similarity.updateSuccessRate(true);
                }
                
                similarity.setLastUsedAt(LocalDateTime.now());
                productSimilarityRepository.save(similarity);
                
                log.debug("Updated similarity usage: id={}, usage={}, success rate={}", 
                         similarity.getId(), similarity.getUsageCount(), similarity.getSuccessRate());
            }
            
        } catch (Exception e) {
            log.error("Error recording similarity usage for products: {} and {}", 
                     sourceProductId, targetProductId, e);
        }
    }
    
    /**
     * Get similarity statistics for a product.
     */
    public Map<String, Object> getSimilarityStatistics(String productId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total similarities as source - using alternative calculation
            Long totalAsSource = productSimilarityRepository.findAll().stream()
                .filter(ps -> ps.getSourceProductId().equals(productId))
                .count();
            stats.put("totalAsSource", totalAsSource);
            
            // Total similarities as target - using alternative calculation
            Long totalAsTarget = productSimilarityRepository.findAll().stream()
                .filter(ps -> ps.getTargetProductId().equals(productId))
                .count();
            stats.put("totalAsTarget", totalAsTarget);
            
            // Active similarities - using alternative calculation
            Long activeAsSource = productSimilarityRepository.findAll().stream()
                .filter(ps -> ps.getSourceProductId().equals(productId) && ps.getIsActive())
                .count();
            stats.put("activeAsSource", activeAsSource);
            
            // Average similarity score - using alternative calculation since repository method doesn't exist
            List<ProductSimilarity> allSimilarities = productSimilarityRepository
                .findBySourceProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(productId);
            
            Double avgScore = allSimilarities.stream()
                .mapToDouble(ProductSimilarity::getSimilarityScore)
                .average()
                .orElse(0.0);
            stats.put("averageSimilarityScore", avgScore);
            
            // Statistics by similarity type - using alternative calculation
            Map<String, Long> typeStats = new HashMap<>();
            for (ProductSimilarity.SimilarityType type : ProductSimilarity.SimilarityType.values()) {
                Long count = allSimilarities.stream()
                    .filter(ps -> ps.getSimilarityType() == type)
                    .count();
                typeStats.put(type.name(), count);
            }
            stats.put("similaritiesByType", typeStats);
            
            // Performance metrics - using alternative calculation since repository methods don't exist
            List<ProductSimilarity> productSimilarities = productSimilarityRepository
                .findBySourceProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(productId);
            
            Double avgSuccessRate = productSimilarities.stream()
                .filter(ps -> ps.getSuccessRate() != null)
                .mapToDouble(ProductSimilarity::getSuccessRate)
                .average()
                .orElse(0.0);
            stats.put("averageSuccessRate", avgSuccessRate);
            
            Long totalUsage = productSimilarities.stream()
                .mapToLong(ps -> ps.getUsageCount() != null ? ps.getUsageCount() : 0)
                .sum();
            stats.put("totalUsage", totalUsage);
            
        } catch (Exception e) {
            log.error("Error calculating similarity statistics for product: {}", productId, e);
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }
    
    /**
     * Get top performing similarities.
     */
    public List<ProductSimilarity> getTopPerformingSimilarities(int limit) {
        return productSimilarityRepository.findTopPerformingSimilarities(
            org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * Get similarities that need verification.
     */
    public List<ProductSimilarity> getSimilaritiesNeedingVerification(int limit) {
        // Using alternative approach since repository method doesn't exist
        return productSimilarityRepository.findAll().stream()
            .filter(ps -> ps.getIsActive() && !ps.getIsVerified())
            .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Verify similarity accuracy.
     */
    public ProductSimilarity verifySimilarity(String similarityId, boolean isAccurate, String feedback) {
        log.info("Verifying similarity: {} with result: {}", similarityId, isAccurate);
        
        try {
            UUID uuid = UUID.fromString(similarityId);
            ProductSimilarity similarity = productSimilarityRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Similarity not found: " + similarityId));
            
            similarity.setIsVerified(true);
            // Note: setVerificationResult, setVerificationFeedback, setVerifiedAt methods don't exist in entity
            // Using available fields: verificationScore and metadata for feedback
            similarity.setVerificationScore(isAccurate ? 1.0 : 0.0);
            if (similarity.getMetadata() == null) {
                similarity.setMetadata(new HashMap<>());
            }
            similarity.getMetadata().put("verification_feedback", feedback);
            similarity.getMetadata().put("verified_at", LocalDateTime.now().toString());
            similarity.setUpdatedAt(LocalDateTime.now());
            
            // Adjust confidence based on verification
            if (isAccurate) {
                similarity.setConfidence(Math.min(similarity.getConfidence() * 1.1, 1.0));
            } else {
                similarity.setConfidence(similarity.getConfidence() * 0.8);
                // Deactivate if confidence drops too low
                if (similarity.getConfidence() < 0.3) {
                    similarity.setIsActive(false);
                }
            }
            
            ProductSimilarity savedSimilarity = productSimilarityRepository.save(similarity);
            
            log.info("Verified similarity: id={}, accurate={}, new confidence={}", 
                    similarityId, isAccurate, savedSimilarity.getConfidence());
            
            return savedSimilarity;
            
        } catch (Exception e) {
            log.error("Error verifying similarity: {}", similarityId, e);
            throw new RuntimeException("Failed to verify similarity", e);
        }
    }
    
    /**
     * Deactivate low-performing similarities.
     */
    public int deactivateLowPerformingSimilarities(double minSuccessRate, int minUsageCount) {
        log.info("Deactivating similarities with success rate < {} and usage count >= {}", 
                minSuccessRate, minUsageCount);
        
        try {
            // Note: deactivateLowPerformingSimilarities method doesn't exist in repository
            // Using alternative approach to find and deactivate low-performing similarities
            List<ProductSimilarity> lowPerformingSimilarities = productSimilarityRepository
                .findAll().stream()
                .filter(ps -> ps.getIsActive() && 
                        ps.getUsageCount() >= minUsageCount && 
                        ps.getSuccessRate() != null && 
                        ps.getSuccessRate() < minSuccessRate)
                .collect(Collectors.toList());
            
            int deactivatedCount = lowPerformingSimilarities.size();
            lowPerformingSimilarities.forEach(ps -> ps.setIsActive(false));
            productSimilarityRepository.saveAll(lowPerformingSimilarities);
            
            log.info("Deactivated {} low-performing similarities", deactivatedCount);
            
            return deactivatedCount;
            
        } catch (Exception e) {
            log.error("Error deactivating low-performing similarities", e);
            throw new RuntimeException("Failed to deactivate similarities", e);
        }
    }
    
    /**
     * Clean up old similarities.
     */
    public void cleanupOldSimilarities(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        log.info("Cleaning up similarities older than: {}", cutoffDate);
        
        try {
            productSimilarityRepository.deleteOldSimilarities(cutoffDate);
            
            log.info("Cleaned up old similarities");
            
        } catch (Exception e) {
            log.error("Error cleaning up old similarities", e);
            throw new RuntimeException("Failed to cleanup old similarities", e);
        }
    }
    
    /**
     * Refresh similarities for a product.
     */
    public void refreshProductSimilarities(String productId, 
                                         List<ProductSimilarity.SimilarityType> similarityTypes,
                                         Map<String, Object> productData) {
        
        log.info("Refreshing similarities for product: {} with types: {}", 
                productId, similarityTypes);
        
        try {
            // Deactivate existing similarities
            productSimilarityRepository.deactivateSimilaritiesBySourceProduct(productId);
            
            // Recalculate similarities based on current data
            // This would typically involve calling external services or ML models
            // For now, we'll just reactivate existing ones with updated scores
            
            List<ProductSimilarity> existingSimilarities = productSimilarityRepository
                .findBySourceProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(productId);
            
            for (ProductSimilarity similarity : existingSimilarities) {
                if (similarityTypes.contains(similarity.getSimilarityType())) {
                    // Recalculate score
                    double newScore = calculateSimilarityScore(
                        similarity.getSimilarityType(), productData);
                    similarity.setSimilarityScore(newScore);
                    
                    // Recalculate confidence
                    double newConfidence = calculateConfidence(productData, newScore);
                    similarity.setConfidence(newConfidence);
                    
                    // Reactivate if score is good enough
                    if (newScore > 0.3 && newConfidence > 0.5) {
                        similarity.setIsActive(true);
                    }
                    
                    similarity.setUpdatedAt(LocalDateTime.now());
                    productSimilarityRepository.save(similarity);
                }
            }
            
            log.info("Successfully refreshed similarities for product: {}", productId);
            
        } catch (Exception e) {
            log.error("Error refreshing similarities for product: {}", productId, e);
            throw new RuntimeException("Failed to refresh product similarities", e);
        }
    }
    
    // Helper methods for similarity calculation
    
    private double calculateSimilarityScore(ProductSimilarity.SimilarityType similarityType, 
                                          Map<String, Object> productData) {
        
        switch (similarityType) {
            case CONTENT_BASED:
                return calculateContentBasedSimilarity(productData);
            case COLLABORATIVE:
                return calculateCollaborativeSimilarity(productData);
            case SEMANTIC:
                return calculateSemanticSimilarity(productData);
            case VISUAL:
                return calculateVisualSimilarity(productData);
            case CATEGORY:
                return calculateCategorySimilarity(productData);
            case BRAND:
                return calculateBrandSimilarity(productData);
            case PRICE:
                return calculatePriceSimilarity(productData);
            case BEHAVIORAL:
                return calculateBehavioralSimilarity(productData);
            default:
                return 0.5; // Default similarity
        }
    }
    
    private double calculateContentBasedSimilarity(Map<String, Object> productData) {
        // Implement content-based similarity calculation
        // This would analyze product attributes, descriptions, features, etc.
        
        double similarity = 0.0;
        int factors = 0;
        
        // Category similarity
        if (productData.containsKey("categoryMatch")) {
            similarity += (Boolean) productData.get("categoryMatch") ? 0.3 : 0.0;
            factors++;
        }
        
        // Brand similarity
        if (productData.containsKey("brandMatch")) {
            similarity += (Boolean) productData.get("brandMatch") ? 0.2 : 0.0;
            factors++;
        }
        
        // Feature similarity
        if (productData.containsKey("featureSimilarity")) {
            similarity += (Double) productData.getOrDefault("featureSimilarity", 0.0) * 0.3;
            factors++;
        }
        
        // Description similarity
        if (productData.containsKey("descriptionSimilarity")) {
            similarity += (Double) productData.getOrDefault("descriptionSimilarity", 0.0) * 0.2;
            factors++;
        }
        
        return factors > 0 ? similarity : 0.5;
    }
    
    private double calculateCollaborativeSimilarity(Map<String, Object> productData) {
        // Implement collaborative filtering similarity
        // This would analyze user behavior patterns
        
        Double userOverlap = (Double) productData.getOrDefault("userOverlap", 0.0);
        Double ratingCorrelation = (Double) productData.getOrDefault("ratingCorrelation", 0.0);
        Double purchaseCorrelation = (Double) productData.getOrDefault("purchaseCorrelation", 0.0);
        
        return (userOverlap * 0.4 + ratingCorrelation * 0.3 + purchaseCorrelation * 0.3);
    }
    
    private double calculateSemanticSimilarity(Map<String, Object> productData) {
        // Implement semantic similarity using NLP techniques
        return (Double) productData.getOrDefault("semanticSimilarity", 0.5);
    }
    
    private double calculateVisualSimilarity(Map<String, Object> productData) {
        // Implement visual similarity using image analysis
        return (Double) productData.getOrDefault("visualSimilarity", 0.5);
    }
    
    private double calculateCategorySimilarity(Map<String, Object> productData) {
        Boolean sameCategory = (Boolean) productData.getOrDefault("sameCategory", false);
        Boolean relatedCategory = (Boolean) productData.getOrDefault("relatedCategory", false);
        
        if (sameCategory) return 0.9;
        if (relatedCategory) return 0.6;
        return 0.1;
    }
    
    private double calculateBrandSimilarity(Map<String, Object> productData) {
        Boolean sameBrand = (Boolean) productData.getOrDefault("sameBrand", false);
        Boolean relatedBrand = (Boolean) productData.getOrDefault("relatedBrand", false);
        
        if (sameBrand) return 0.8;
        if (relatedBrand) return 0.5;
        return 0.2;
    }
    
    private double calculatePriceSimilarity(Map<String, Object> productData) {
        Double priceRatio = (Double) productData.getOrDefault("priceRatio", 1.0);
        
        // Closer to 1.0 means more similar prices
        return Math.max(0.0, 1.0 - Math.abs(1.0 - priceRatio));
    }
    
    private double calculateBehavioralSimilarity(Map<String, Object> productData) {
        // Implement behavioral similarity based on user actions
        Double viewSimilarity = (Double) productData.getOrDefault("viewSimilarity", 0.0);
        Double purchaseSimilarity = (Double) productData.getOrDefault("purchaseSimilarity", 0.0);
        Double cartSimilarity = (Double) productData.getOrDefault("cartSimilarity", 0.0);
        
        return (viewSimilarity * 0.3 + purchaseSimilarity * 0.5 + cartSimilarity * 0.2);
    }
    
    private double calculateConfidence(Map<String, Object> productData, double similarityScore) {
        double confidence = similarityScore; // Base confidence on similarity score
        
        // Adjust based on data quality
        Integer dataCompleteness = (Integer) productData.getOrDefault("dataCompleteness", 50);
        confidence *= (dataCompleteness / 100.0);
        
        // Adjust based on sample size
        Integer sampleSize = (Integer) productData.getOrDefault("sampleSize", 10);
        if (sampleSize < 10) {
            confidence *= 0.7;
        } else if (sampleSize > 100) {
            confidence *= 1.1;
        }
        
        return Math.min(Math.max(confidence, 0.0), 1.0);
    }
    
    private void extractAndSetMetrics(ProductSimilarity similarity, Map<String, Object> productData) {
        // Extract additional metrics from product data
        if (productData.containsKey("categoryId")) {
            similarity.setCategoryId((String) productData.get("categoryId"));
        }
        
        if (productData.containsKey("brandId")) {
            similarity.setBrandId((String) productData.get("brandId"));
        }
        
        // Note: Price, discount, and rating are not direct fields in ProductSimilarity
        // These would be calculated as similarity scores (priceSimilarity, etc.) or stored in metadata
        if (productData.containsKey("price")) {
            Object price = productData.get("price");
            if (price instanceof Number) {
                // Store in metadata if needed
                if (similarity.getMetadata() == null) {
                    similarity.setMetadata(new HashMap<>());
                }
                similarity.getMetadata().put("price", price.toString());
            }
        }
        
        // Note: ReviewCount is not a field in ProductSimilarity entity
        // This data would be stored in metadata if needed
    }
}