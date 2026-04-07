package com.ecommerce.notification.entity;

/**
 * Enumeration for notification priority levels
 */
public enum NotificationPriority {
    
    LOW(1, "Low", "Non-urgent notifications that can be delayed"),
    MEDIUM(2, "Medium", "Standard priority notifications"),
    HIGH(3, "High", "Important notifications that should be sent promptly"),
    URGENT(4, "Urgent", "Critical notifications requiring immediate attention"),
    CRITICAL(5, "Critical", "System-critical notifications with highest priority");
    
    private final int level;
    private final String displayName;
    private final String description;
    
    NotificationPriority(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this priority is higher than another priority
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Check if this priority is lower than another priority
     */
    public boolean isLowerThan(NotificationPriority other) {
        return this.level < other.level;
    }
    
    /**
     * Get the maximum retry attempts for this priority level
     */
    public int getMaxRetryAttempts() {
        switch (this) {
            case LOW:
                return 1;
            case MEDIUM:
                return 3;
            case HIGH:
                return 5;
            case URGENT:
                return 7;
            case CRITICAL:
                return 10;
            default:
                return 3;
        }
    }
    
    /**
     * Get the retry delay in minutes for this priority level
     */
    public int getRetryDelayMinutes() {
        switch (this) {
            case LOW:
                return 60; // 1 hour
            case MEDIUM:
                return 30; // 30 minutes
            case HIGH:
                return 15; // 15 minutes
            case URGENT:
                return 5;  // 5 minutes
            case CRITICAL:
                return 1;  // 1 minute
            default:
                return 30;
        }
    }
    
    /**
     * Get the processing timeout in minutes for this priority level
     */
    public int getProcessingTimeoutMinutes() {
        switch (this) {
            case LOW:
                return 60;  // 1 hour
            case MEDIUM:
                return 30;  // 30 minutes
            case HIGH:
                return 15;  // 15 minutes
            case URGENT:
                return 5;   // 5 minutes
            case CRITICAL:
                return 2;   // 2 minutes
            default:
                return 30;
        }
    }
    
    /**
     * Check if this priority requires immediate processing
     */
    public boolean requiresImmediateProcessing() {
        return this == URGENT || this == CRITICAL;
    }
    
    /**
     * Check if this priority allows batching with other notifications
     */
    public boolean allowsBatching() {
        return this == LOW || this == MEDIUM;
    }
    
    /**
     * Get priority from notification type
     */
    public static NotificationPriority fromNotificationType(NotificationType type) {
        if (type.isCritical()) {
            return CRITICAL;
        } else if (type.isPaymentRelated()) {
            return HIGH;
        } else if (type.isOrderRelated()) {
            return MEDIUM;
        } else if (type.isPromotional()) {
            return LOW;
        } else {
            return MEDIUM; // Default
        }
    }
    
    /**
     * Get all priorities ordered by level (lowest to highest)
     */
    public static NotificationPriority[] getOrderedPriorities() {
        return new NotificationPriority[]{LOW, MEDIUM, HIGH, URGENT, CRITICAL};
    }
    
    /**
     * Get all priorities ordered by level (highest to lowest)
     */
    public static NotificationPriority[] getOrderedPrioritiesDesc() {
        return new NotificationPriority[]{CRITICAL, URGENT, HIGH, MEDIUM, LOW};
    }
}
