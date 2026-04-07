package com.ecommerce.notification.entity;

/**
 * Enumeration for different types of notifications
 */
public enum NotificationType {
    
    // Order related notifications
    ORDER_CREATED("Order Created", "Notification sent when an order is created"),
    ORDER_CONFIRMATION("Order Confirmation", "Notification sent when an order is confirmed"),
    ORDER_SHIPPED("Order Shipped", "Notification sent when an order is shipped"),
    ORDER_DELIVERED("Order Delivered", "Notification sent when an order is delivered"),
    ORDER_CANCELLED("Order Cancelled", "Notification sent when an order is cancelled"),
    ORDER_REFUNDED("Order Refunded", "Notification sent when an order is refunded"),
    
    // Payment related notifications
    PAYMENT_SUCCESS("Payment Success", "Notification sent when payment is successful"),
    PAYMENT_FAILED("Payment Failed", "Notification sent when payment fails"),
    REFUND_PROCESSED("Refund Processed", "Notification sent when refund is processed"),
    PAYMENT_REFUND("Payment Refund", "Notification sent when payment is refunded"),
    
    // User account notifications
    WELCOME("Welcome", "Welcome notification for new users"),
    EMAIL_VERIFICATION("Email Verification", "Email verification notification"),
    ACCOUNT_VERIFICATION("Account Verification", "Email verification notification"),
    PASSWORD_RESET("Password Reset", "Password reset notification"),
    ACCOUNT_LOCKED("Account Locked", "Account locked notification"),
    PROFILE_UPDATED("Profile Updated", "Profile update confirmation"),
    
    // Inventory notifications
    LOW_STOCK("Low Stock", "Notification when product stock is low"),
    OUT_OF_STOCK("Out of Stock", "Notification when product is out of stock"),
    BACK_IN_STOCK("Back in Stock", "Notification when product is back in stock"),
    LOW_STOCK_ALERT("Low Stock Alert", "Alert notification for low stock levels"),
    STOCK_REPLENISHED("Stock Replenished", "Notification when stock is replenished"),
    
    // Promotional notifications
    PROMOTIONAL("Promotional", "General promotional notification"),
    DISCOUNT_OFFER("Discount Offer", "Discount offer notification"),
    PROMOTION_STARTED("Promotion Started", "Notification sent when a promotion starts"),
    NEWSLETTER("Newsletter", "Newsletter and updates"),
    PRODUCT_RECOMMENDATION("Product Recommendation", "Personalized product recommendations"),
    FLASH_SALE("Flash Sale", "Limited time flash sale notifications"),
    
    // System notifications
    SYSTEM_MAINTENANCE("System Maintenance", "System maintenance notifications"),
    SECURITY_ALERT("Security Alert", "Security related alerts"),
    
    // Review and feedback
    REVIEW_REQUEST("Review Request", "Request for product or service review"),
    REVIEW_RESPONSE("Review Response", "Response to user review"),
    
    // Cart notifications
    CART_ABANDONMENT("Cart Abandonment", "Reminder for abandoned cart"),
    WISHLIST_ITEM_SALE("Wishlist Item Sale", "Notification when wishlist item is on sale"),
    
    // General notifications
    GENERAL("General", "General purpose notification"),
    CUSTOM("Custom", "Custom notification type");
    
    private final String displayName;
    private final String description;
    
    NotificationType(String displayName, String description) {
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
     * Check if this notification type is order related
     */
    public boolean isOrderRelated() {
        return this == ORDER_CONFIRMATION || this == ORDER_SHIPPED || 
               this == ORDER_DELIVERED || this == ORDER_CANCELLED || 
               this == ORDER_REFUNDED;
    }
    
    /**
     * Check if this notification type is payment related
     */
    public boolean isPaymentRelated() {
        return this == PAYMENT_SUCCESS || this == PAYMENT_FAILED || 
               this == PAYMENT_REFUND;
    }
    
    /**
     * Check if this notification type is promotional
     */
    public boolean isPromotional() {
        return this == PROMOTIONAL || this == NEWSLETTER || 
               this == PRODUCT_RECOMMENDATION || this == WISHLIST_ITEM_SALE ||
               this == FLASH_SALE;
    }
    
    /**
     * Check if this notification type is critical (requires immediate attention)
     */
    public boolean isCritical() {
        return this == PAYMENT_FAILED || this == ACCOUNT_LOCKED || 
               this == SECURITY_ALERT || this == SYSTEM_MAINTENANCE;
    }
}
