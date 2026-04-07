package com.ecommerce.review.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Review Vote Entity
 * 
 * Represents a user's vote on the helpfulness of a review.
 * Prevents duplicate voting by the same user on the same review.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
@Entity
@Table(name = "review_votes", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_review_vote_user", columnNames = {"review_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_review_vote_review_id", columnList = "review_id"),
           @Index(name = "idx_review_vote_user_id", columnList = "user_id"),
           @Index(name = "idx_review_vote_type", columnList = "vote_type"),
           @Index(name = "idx_review_vote_created_date", columnList = "created_date")
       })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReviewVote {
    
    public static ReviewVoteBuilder builder() {
        return new ReviewVoteBuilder();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Version
    private Long version;

    // Constructors
    public ReviewVote() {}

    public ReviewVote(Long reviewId, Long userId, VoteType voteType) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.voteType = voteType;
    }

    public ReviewVote(Long reviewId, Long userId, VoteType voteType, String ipAddress, String userAgent) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.voteType = voteType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business Methods
    public boolean isHelpful() {
        return voteType == VoteType.HELPFUL;
    }

    public boolean isNotHelpful() {
        return voteType == VoteType.NOT_HELPFUL;
    }

    @Override
    public String toString() {
        return "ReviewVote{" +
                "id=" + id +
                ", reviewId=" + reviewId +
                ", userId=" + userId +
                ", voteType=" + voteType +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewVote)) return false;
        ReviewVote that = (ReviewVote) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Vote Type Enumeration
     */
    public enum VoteType {
        HELPFUL("Helpful", "This review was helpful"),
        NOT_HELPFUL("Not Helpful", "This review was not helpful");

        private final String displayName;
        private final String description;

        VoteType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isHelpful() {
            return this == HELPFUL;
        }

        public boolean isNotHelpful() {
            return this == NOT_HELPFUL;
        }
    }
    
    public static class ReviewVoteBuilder {
        private Long reviewId;
        private Long userId;
        private VoteType voteType;
        private String ipAddress;
        private String userAgent;
        
        public ReviewVoteBuilder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }
        
        public ReviewVoteBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public ReviewVoteBuilder voteType(VoteType voteType) {
            this.voteType = voteType;
            return this;
        }
        
        public ReviewVoteBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public ReviewVoteBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public ReviewVote build() {
            ReviewVote vote = new ReviewVote();
            vote.setReviewId(reviewId);
            vote.setUserId(userId);
            vote.setVoteType(voteType);
            vote.setIpAddress(ipAddress);
            vote.setUserAgent(userAgent);
            return vote;
        }
    }
}