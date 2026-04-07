package com.ecommerce.review.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Review Entity
 * 
 * Represents a product review with rating, content, and metadata.
 * Includes support for review moderation, helpfulness voting, and media attachments.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_product_id", columnList = "product_id"),
    @Index(name = "idx_review_user_id", columnList = "user_id"),
    @Index(name = "idx_review_status", columnList = "status"),
    @Index(name = "idx_review_rating", columnList = "rating"),
    @Index(name = "idx_review_created_date", columnList = "created_date"),
    @Index(name = "idx_review_verified_purchase", columnList = "verified_purchase"),
    @Index(name = "idx_review_product_user", columnList = "product_id, user_id")
})
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 100)
    @Column(name = "title", length = 100)
    private String title;

    @Size(max = 2000)
    @Column(name = "content", length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "verified_purchase")
    private Boolean verifiedPurchase = false;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count")
    private Integer notHelpfulCount = 0;

    @Column(name = "total_votes")
    private Integer totalVotes = 0;

    @Column(name = "spam_score")
    private Double spamScore = 0.0;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment")
    private ReviewSentiment sentiment;

    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "moderated_by")
    private Long moderatedBy;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Size(max = 500)
    @Column(name = "moderation_notes", length = 500)
    private String moderationNotes;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @Column(name = "parent_review_id")
    private Long parentReviewId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version;

    // Constructors
    public Review() {}

    public Review(Long productId, Long userId, String userName, Integer rating, String title, String content) {
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.status = ReviewStatus.PENDING;
    }

    public static ReviewBuilder builder() {
        return new ReviewBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Boolean getVerifiedPurchase() {
        return verifiedPurchase;
    }

    public void setVerifiedPurchase(Boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public Integer getNotHelpfulCount() {
        return notHelpfulCount;
    }

    public void setNotHelpfulCount(Integer notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
    }

    public Integer getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Integer totalVotes) {
        this.totalVotes = totalVotes;
    }

    public Double getSpamScore() {
        return spamScore;
    }

    public void setSpamScore(Double spamScore) {
        this.spamScore = spamScore;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public ReviewSentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(ReviewSentiment sentiment) {
        this.sentiment = sentiment;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getModeratedBy() {
        return moderatedBy;
    }

    public void setModeratedBy(Long moderatedBy) {
        this.moderatedBy = moderatedBy;
    }

    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public String getModerationNotes() {
        return moderationNotes;
    }

    public void setModerationNotes(String moderationNotes) {
        this.moderationNotes = moderationNotes;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Long getParentReviewId() {
        return parentReviewId;
    }

    public void setParentReviewId(Long parentReviewId) {
        this.parentReviewId = parentReviewId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business Methods
    public void approve(Long moderatorId, String notes) {
        this.status = ReviewStatus.APPROVED;
        this.moderatedBy = moderatorId;
        this.moderatedAt = LocalDateTime.now();
        this.moderationNotes = notes;
    }

    public void reject(Long moderatorId, String notes) {
        this.status = ReviewStatus.REJECTED;
        this.moderatedBy = moderatorId;
        this.moderatedAt = LocalDateTime.now();
        this.moderationNotes = notes;
    }

    public void markAsSpam(Long moderatorId, String notes) {
        this.status = ReviewStatus.SPAM;
        this.moderatedBy = moderatorId;
        this.moderatedAt = LocalDateTime.now();
        this.moderationNotes = notes;
    }

    public void addHelpfulVote() {
        this.helpfulCount++;
        this.totalVotes++;
    }

    public void addNotHelpfulVote() {
        this.notHelpfulCount++;
        this.totalVotes++;
    }

    public void removeHelpfulVote() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
            this.totalVotes--;
        }
    }

    public void removeNotHelpfulVote() {
        if (this.notHelpfulCount > 0) {
            this.notHelpfulCount--;
            this.totalVotes--;
        }
    }

    public double getHelpfulnessRatio() {
        if (totalVotes == 0) return 0.0;
        return (double) helpfulCount / totalVotes;
    }

    public boolean isReply() {
        return parentReviewId != null;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public void incrementReplyCount() {
        this.replyCount++;
    }

    public void decrementReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", productId=" + productId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", verifiedPurchase=" + verifiedPurchase +
                ", helpfulCount=" + helpfulCount +
                ", totalVotes=" + totalVotes +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review = (Review) o;
        return id != null && id.equals(review.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public static class ReviewBuilder {
        private Long productId;
        private Long userId;
        private String userName;
        private Integer rating;
        private String title;
        private String content;
        private ReviewStatus status = ReviewStatus.PENDING;
        private Boolean verifiedPurchase = false;
        private Long orderId;

        public ReviewBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public ReviewBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ReviewBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ReviewBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public ReviewBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ReviewBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ReviewBuilder status(ReviewStatus status) {
            this.status = status;
            return this;
        }

        public ReviewBuilder verifiedPurchase(Boolean verifiedPurchase) {
            this.verifiedPurchase = verifiedPurchase;
            return this;
        }

        public ReviewBuilder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Review build() {
            Review review = new Review();
            review.productId = this.productId;
            review.userId = this.userId;
            review.userName = this.userName;
            review.rating = this.rating;
            review.title = this.title;
            review.content = this.content;
            review.status = this.status;
            review.verifiedPurchase = this.verifiedPurchase;
            review.orderId = this.orderId;
            return review;
        }
    }
}