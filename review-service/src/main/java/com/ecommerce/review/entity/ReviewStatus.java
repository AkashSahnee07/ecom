package com.ecommerce.review.entity;

/**
 * Review Status Enumeration
 * 
 * Defines the possible states of a review in the moderation workflow.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
public enum ReviewStatus {
    
    /**
     * Review has been submitted and is awaiting moderation
     */
    PENDING("Pending", "Review is awaiting moderation"),
    
    /**
     * Review has been approved and is visible to customers
     */
    APPROVED("Approved", "Review has been approved and is visible"),
    
    /**
     * Review has been rejected and is not visible to customers
     */
    REJECTED("Rejected", "Review has been rejected and is not visible"),
    
    /**
     * Review has been flagged as spam
     */
    SPAM("Spam", "Review has been flagged as spam"),
    
    /**
     * Review is under review for potential policy violations
     */
    UNDER_REVIEW("Under Review", "Review is being investigated"),
    
    /**
     * Review has been hidden due to policy violations
     */
    HIDDEN("Hidden", "Review has been hidden due to policy violations"),
    
    /**
     * Review has been deleted
     */
    DELETED("Deleted", "Review has been deleted");
    
    private final String displayName;
    private final String description;
    
    ReviewStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if the review is visible to customers
     * @return true if the review should be displayed to customers
     */
    public boolean isVisible() {
        return this == APPROVED;
    }
    
    /**
     * Check if the review is in a moderation state
     * @return true if the review is pending moderation
     */
    public boolean isPendingModeration() {
        return this == PENDING || this == UNDER_REVIEW;
    }
    
    /**
     * Check if the review has been moderated
     * @return true if the review has been through moderation
     */
    public boolean isModerated() {
        return this == APPROVED || this == REJECTED || this == SPAM || this == HIDDEN;
    }
    
    /**
     * Check if the review is in a negative state
     * @return true if the review is rejected, spam, hidden, or deleted
     */
    public boolean isNegativeState() {
        return this == REJECTED || this == SPAM || this == HIDDEN || this == DELETED;
    }
    
    /**
     * Get all statuses that are visible to customers
     * @return array of visible statuses
     */
    public static ReviewStatus[] getVisibleStatuses() {
        return new ReviewStatus[]{APPROVED};
    }
    
    /**
     * Get all statuses that require moderation
     * @return array of statuses requiring moderation
     */
    public static ReviewStatus[] getModerationStatuses() {
        return new ReviewStatus[]{PENDING, UNDER_REVIEW};
    }
    
    /**
     * Get all statuses that have been moderated
     * @return array of moderated statuses
     */
    public static ReviewStatus[] getModeratedStatuses() {
        return new ReviewStatus[]{APPROVED, REJECTED, SPAM, HIDDEN};
    }
}