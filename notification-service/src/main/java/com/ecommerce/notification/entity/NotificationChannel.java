package com.ecommerce.notification.entity;

/**
 * Enumeration for different notification delivery channels
 */
public enum NotificationChannel {
    
    EMAIL("Email", "Email notification delivery", true),
    SMS("SMS", "SMS text message delivery", true),
    PUSH("Push Notification", "Mobile push notification delivery", true),
    IN_APP("In-App", "In-application notification", false),
    WEBHOOK("Webhook", "HTTP webhook delivery", true),
    SLACK("Slack", "Slack channel notification", true),
    DISCORD("Discord", "Discord channel notification", true);
    
    private final String displayName;
    private final String description;
    private final boolean requiresExternalService;
    
    NotificationChannel(String displayName, String description, boolean requiresExternalService) {
        this.displayName = displayName;
        this.description = description;
        this.requiresExternalService = requiresExternalService;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean requiresExternalService() {
        return requiresExternalService;
    }
    
    /**
     * Check if this channel supports rich content (HTML, images, etc.)
     */
    public boolean supportsRichContent() {
        return this == EMAIL || this == IN_APP || this == WEBHOOK;
    }
    
    /**
     * Check if this channel has character limits
     */
    public boolean hasCharacterLimit() {
        return this == SMS || this == PUSH;
    }
    
    /**
     * Get the character limit for this channel
     */
    public int getCharacterLimit() {
        switch (this) {
            case SMS:
                return 160; // Standard SMS limit
            case PUSH:
                return 256; // Typical push notification limit
            default:
                return Integer.MAX_VALUE; // No limit
        }
    }
    
    /**
     * Check if this channel supports immediate delivery
     */
    public boolean supportsImmediateDelivery() {
        return this == EMAIL || this == SMS || this == PUSH || 
               this == WEBHOOK || this == SLACK || this == DISCORD;
    }
    
    /**
     * Check if this channel supports scheduled delivery
     */
    public boolean supportsScheduledDelivery() {
        return this == EMAIL || this == SMS || this == PUSH;
    }
    
    /**
     * Get the typical delivery time for this channel
     */
    public String getTypicalDeliveryTime() {
        switch (this) {
            case EMAIL:
                return "1-5 minutes";
            case SMS:
                return "10-30 seconds";
            case PUSH:
                return "1-10 seconds";
            case IN_APP:
                return "Immediate";
            case WEBHOOK:
                return "1-5 seconds";
            case SLACK:
            case DISCORD:
                return "1-3 seconds";
            default:
                return "Unknown";
        }
    }
}
