package com.ecommerce.review.controller;

import com.ecommerce.review.entity.Review;
import com.ecommerce.review.entity.ReviewStatus;
import com.ecommerce.review.entity.ReviewVote;
import com.ecommerce.review.service.ReviewService;
import com.ecommerce.review.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing product reviews.
 */
@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Product review management operations")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    
    private final ReviewService reviewService;
    private final ImageService imageService;
    
    public ReviewController(ReviewService reviewService, ImageService imageService) {
        this.reviewService = reviewService;
        this.imageService = imageService;
    }
    
    /**
     * Create a new review for a product.
     */
    @PostMapping
    @Operation(summary = "Create review", description = "Create a new product review with optional images")
    public ResponseEntity<Review> createReview(
            @Parameter(description = "Product ID") @RequestParam String productId,
            @Parameter(description = "User ID") @RequestParam String userId,
            @Parameter(description = "Review title") @RequestParam String title,
            @Parameter(description = "Review content") @RequestParam String content,
            @Parameter(description = "Rating (1-5)") @RequestParam Integer rating,
            @Parameter(description = "Is verified purchase") @RequestParam(required = false) Boolean verifiedPurchase,
            @Parameter(description = "Review images") @RequestParam(required = false) List<MultipartFile> images) {
        
        try {
            Review review = reviewService.createReview(productId, userId, title, content, 
                    rating, verifiedPurchase, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating review: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get reviews for a specific product.
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Get reviews for a specific product with filtering and pagination")
    public ResponseEntity<Page<Review>> getProductReviews(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Review status") @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) Integer minRating,
            @Parameter(description = "Maximum rating") @RequestParam(required = false) Integer maxRating,
            @Parameter(description = "Verified purchase only") @RequestParam(required = false) Boolean verifiedOnly,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Review> reviews = reviewService.getProductReviews(productId, status, 
                    minRating, maxRating, verifiedOnly, sortBy, pageable);
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching product reviews for product: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific review by ID.
     */
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Get a specific review by its unique ID")
    public ResponseEntity<Review> getReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId) {
        try {
            Optional<Review> review = reviewService.getReviewById(reviewId);
            return review.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing review.
     */
    @PutMapping("/{reviewId}")
    @Operation(summary = "Update review", description = "Update an existing review")
    public ResponseEntity<Review> updateReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "User ID") @RequestParam String userId,
            @Parameter(description = "Review title") @RequestParam(required = false) String title,
            @Parameter(description = "Review content") @RequestParam(required = false) String content,
            @Parameter(description = "Rating") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Review images") @RequestParam(required = false) List<MultipartFile> images) {
        
        try {
            Review updatedReview = reviewService.updateReview(reviewId, userId, 
                    title, content, rating, images);
            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for updating review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a review.
     */
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "User ID") @RequestParam String userId) {
        
        try {
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for deleting review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Vote on a review (helpful/not helpful).
     */
    @PostMapping("/{reviewId}/vote")
    @Operation(summary = "Vote on review", description = "Vote on a review (helpful/not helpful)")
    public ResponseEntity<Void> voteOnReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "User ID") @RequestParam String userId,
            @Parameter(description = "Vote type") @RequestParam ReviewVote.VoteType voteType,
            HttpServletRequest request) {
        
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            reviewService.voteOnReview(reviewId, userId, voteType, ipAddress, userAgent);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid vote request for review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error voting on review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get review statistics for a product.
     */
    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "Get review stats", description = "Get review statistics for a product")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        try {
            Map<String, Object> stats = reviewService.getProductReviewStats(productId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching review stats for product: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user's reviews.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user reviews", description = "Get reviews created by a specific user")
    public ResponseEntity<Page<Review>> getUserReviews(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Review> reviews = reviewService.getUserReviews(userId, pageable);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching user reviews for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Search reviews by content.
     */
    @GetMapping("/search")
    @Operation(summary = "Search reviews", description = "Search reviews by content keyword")
    public ResponseEntity<Page<Review>> searchReviews(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Product ID filter") @RequestParam(required = false) String productId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Review> reviews = reviewService.searchReviews(query, productId, pageable);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error searching reviews with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get featured reviews for a product.
     */
    @GetMapping("/product/{productId}/featured")
    @Operation(summary = "Get featured reviews", description = "Get featured reviews for a product")
    public ResponseEntity<List<Review>> getFeaturedReviews(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {
        
        try {
            List<Review> reviews = reviewService.getFeaturedReviews(productId, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching featured reviews for product: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get most helpful reviews for a product.
     */
    @GetMapping("/product/{productId}/helpful")
    @Operation(summary = "Get most helpful reviews", description = "Get reviews with the most helpful votes")
    public ResponseEntity<List<Review>> getMostHelpfulReviews(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {
        
        try {
            List<Review> reviews = reviewService.getMostHelpfulReviews(productId, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching helpful reviews for product: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get recent reviews across all products.
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent reviews", description = "Get the most recent reviews across the platform")
    public ResponseEntity<List<Review>> getRecentReviews(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<Review> reviews = reviewService.getRecentReviews(limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching recent reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Serve review images.
     */
    @GetMapping("/images/{year}/{month}/{day}/{filename}")
    @Operation(summary = "Get review image", description = "Retrieve a review image by path")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        
        try {
            String relativePath = String.format("%s/%s/%s/%s", year, month, day, filename);
            byte[] imageData = imageService.getImage(relativePath);
            
            // Determine content type based on file extension
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
        } catch (IOException e) {
            log.warn("Image not found: {}/{}/{}/{}", year, month, day, filename);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error serving image: {}/{}/{}/{}", year, month, day, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Serve review image thumbnails.
     */
    @GetMapping("/images/{year}/{month}/{day}/thumb_{filename}")
    @Operation(summary = "Get image thumbnail", description = "Retrieve a thumbnail for a review image")
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        
        try {
            String relativePath = String.format("%s/%s/%s/%s", year, month, day, filename);
            byte[] imageData = imageService.getThumbnail(relativePath);
            
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
        } catch (IOException e) {
            log.warn("Thumbnail not found: {}/{}/{}/thumb_{}", year, month, day, filename);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error serving thumbnail: {}/{}/{}/thumb_{}", year, month, day, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get image metadata.
     */
    @GetMapping("/images/{year}/{month}/{day}/{filename}/metadata")
    @Operation(summary = "Get image metadata", description = "Retrieve metadata for a review image")
    public ResponseEntity<ImageService.ImageMetadata> getImageMetadata(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        
        try {
            String relativePath = String.format("%s/%s/%s/%s", year, month, day, filename);
            ImageService.ImageMetadata metadata = imageService.getImageMetadata(relativePath);
            return ResponseEntity.ok(metadata);
        } catch (IOException e) {
            log.warn("Image metadata not found: {}/{}/{}/{}", year, month, day, filename);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching image metadata: {}/{}/{}/{}", year, month, day, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check service health status")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "review-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Determine content type based on file extension.
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
