package com.ecommerce.notification.entity;

/**
 * Enumeration for notification delivery status
 */
public enum NotificationStatus {
    
    PENDING("Pending", "Notification is queued for delivery"),
    PROCESSING("Processing", "Notification is being processed"),
    SENDING("Sending", "Notification is currently being sent"),
    SENT("Sent", "Notification has been sent to the delivery service"),
    DELIVERED("Delivered", "Notification has been successfully delivered"),
    FAILED("Failed", "Notification delivery failed"),
    CANCELLED("Cancelled", "Notification was cancelled before delivery"),
    SCHEDULED("Scheduled", "Notification is scheduled for future delivery"),
    RETRY("Retry", "Notification is being retried after failure"),
    EXPIRED("Expired", "Notification expired before delivery");
    
    private final String displayName;
    private final String description;
    
    NotificationStatus(String displayName, String description) {
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
     * Check if this status indicates the notification is still active
     */
    public boolean isActive() {
        return this == PENDING || this == PROCESSING || this == SENDING || this == SCHEDULED || this == RETRY;
    }
    
    /**
     * Check if this status indicates successful completion
     */
    public boolean isSuccessful() {
        return this == SENT || this == DELIVERED;
    }
    
    /**
     * Check if this status indicates failure
     */
    public boolean isFailed() {
        return this == FAILED || this == EXPIRED;
    }
    
    /**
     * Check if this status indicates the notification can be retried
     */
    public boolean canRetry() {
        return this == FAILED;
    }
    
    /**
     * Check if this status is final (no further processing)
     */
    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == EXPIRED;
    }
    
    /**
     * Get the next possible statuses from current status
     */
    public NotificationStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new NotificationStatus[]{PROCESSING, CANCELLED, SCHEDULED};
            case PROCESSING:
                return new NotificationStatus[]{SENT, FAILED, CANCELLED};
            case SENT:
                return new NotificationStatus[]{DELIVERED, FAILED};
            case SCHEDULED:
                return new NotificationStatus[]{PENDING, CANCELLED, EXPIRED};
            case FAILED:
                return new NotificationStatus[]{RETRY, CANCELLED};
            case RETRY:
                return new NotificationStatus[]{PROCESSING, FAILED, CANCELLED};
            default:
                return new NotificationStatus[]{}; // Final statuses
        }
    }
}