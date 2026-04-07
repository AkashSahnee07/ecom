package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Service for sending SMS notifications using Twilio
 */
@Service
public class SmsNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    
    @Value("${notification.sms.twilio.account-sid:}")
    private String accountSid;
    
    @Value("${notification.sms.twilio.auth-token:}")
    private String authToken;
    
    @Value("${notification.sms.twilio.from-number:}")
    private String fromNumber;
    
    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${notification.sms.max-length:160}")
    private int maxSmsLength;
    
    @Value("${notification.sms.country-code:+1}")
    private String defaultCountryCode;
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    @PostConstruct
    public void init() {
        if (smsEnabled && accountSid != null && !accountSid.isEmpty() && 
            authToken != null && !authToken.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                logger.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio SMS service", e);
                smsEnabled = false;
            }
        } else {
            logger.warn("SMS service is disabled or not properly configured");
        }
    }
    
    /**
     * Send SMS notification
     */
    public boolean sendSms(Notification notification) {
        if (!smsEnabled) {
            logger.warn("SMS notifications are disabled");
            return false;
        }
        
        if (notification.getRecipientPhone() == null || notification.getRecipientPhone().trim().isEmpty()) {
            logger.error("No recipient phone provided for notification: {}", notification.getId());
            return false;
        }
        
        String phoneNumber = formatPhoneNumber(notification.getRecipientPhone());
        if (!isValidPhoneNumber(phoneNumber)) {
            logger.error("Invalid phone number format: {} for notification: {}", 
                        phoneNumber, notification.getId());
            return false;
        }
        
        String content = truncateMessage(notification.getContent());
        
        try {
            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                content
            ).create();
            
            logger.info("SMS sent successfully for notification: {}, Twilio SID: {}", 
                       notification.getId(), message.getSid());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send SMS for notification: {}", notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Send SMS asynchronously
     */
    public CompletableFuture<Boolean> sendSmsAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> sendSms(notification));
    }
    
    /**
     * Send bulk SMS messages
     */
    public void sendBulkSms(java.util.List<Notification> notifications) {
        logger.info("Sending bulk SMS for {} notifications", notifications.size());
        
        for (Notification notification : notifications) {
            try {
                sendSms(notification);
                // Add delay to respect rate limits
                Thread.sleep(200);
            } catch (Exception e) {
                logger.error("Failed to send bulk SMS for notification: {}", notification.getId(), e);
            }
        }
    }
    
    /**
     * Format phone number to international format
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
        
        // If no country code, add default
        if (!cleaned.startsWith("+")) {
            cleaned = defaultCountryCode + cleaned;
        }
        
        return cleaned;
    }
    
    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String formatted = formatPhoneNumber(phoneNumber);
        return PHONE_PATTERN.matcher(formatted).matches();
    }
    
    /**
     * Truncate message to fit SMS length limit
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "";
        }
        
        if (message.length() <= maxSmsLength) {
            return message;
        }
        
        // Truncate and add ellipsis
        return message.substring(0, maxSmsLength - 3) + "...";
    }
    
    /**
     * Create SMS template for order confirmation
     */
    public String createOrderConfirmationSms(String customerName, String orderId, String total) {
        return String.format(
            "Hi %s! Your order #%s for %s has been confirmed. " +
            "We'll notify you when it ships. Thanks for shopping with us!",
            customerName, orderId, total
        );
    }
    
    /**
     * Create SMS template for shipping notification
     */
    public String createShippingSms(String customerName, String orderId, String trackingNumber) {
        return String.format(
            "Hi %s! Your order #%s has shipped. Track it with: %s. " +
            "Expected delivery in 2-3 business days.",
            customerName, orderId, trackingNumber
        );
    }
    
    /**
     * Create SMS template for delivery notification
     */
    public String createDeliverySms(String customerName, String orderId) {
        return String.format(
            "Hi %s! Your order #%s has been delivered. " +
            "Hope you love your purchase! Rate your experience in our app.",
            customerName, orderId
        );
    }
    
    /**
     * Create SMS template for OTP verification
     */
    public String createOtpSms(String otp, String expirationMinutes) {
        return String.format(
            "Your verification code is: %s. " +
            "This code expires in %s minutes. Do not share this code.",
            otp, expirationMinutes
        );
    }
    
    /**
     * Create SMS template for password reset
     */
    public String createPasswordResetSms(String customerName, String resetCode) {
        return String.format(
            "Hi %s! Use code %s to reset your password. " +
            "This code expires in 15 minutes. If you didn't request this, ignore this message.",
            customerName, resetCode
        );
    }
    
    /**
     * Create SMS template for low stock alert
     */
    public String createLowStockSms(String productName, int quantity) {
        return String.format(
            "Alert: %s is running low! Only %d left in stock. " +
            "Order now to avoid disappointment.",
            productName, quantity
        );
    }
    
    /**
     * Create SMS template for promotional offers
     */
    public String createPromotionalSms(String customerName, String offer, String code, String expiry) {
        return String.format(
            "Hi %s! %s Use code: %s. Valid until %s. " +
            "Shop now and save! Reply STOP to opt out.",
            customerName, offer, code, expiry
        );
    }
    
    /**
     * Create SMS template for appointment reminder
     */
    public String createAppointmentReminderSms(String customerName, String appointmentTime, String service) {
        return String.format(
            "Hi %s! Reminder: Your %s appointment is scheduled for %s. " +
            "Reply CONFIRM to confirm or RESCHEDULE to change.",
            customerName, service, appointmentTime
        );
    }
    
    /**
     * Create SMS template for payment reminder
     */
    public String createPaymentReminderSms(String customerName, String amount, String dueDate) {
        return String.format(
            "Hi %s! Payment reminder: %s is due on %s. " +
            "Pay now to avoid late fees. Visit our app or website.",
            customerName, amount, dueDate
        );
    }
    
    /**
     * Check if SMS service is available
     */
    public boolean isServiceAvailable() {
        return smsEnabled && accountSid != null && !accountSid.isEmpty() && 
               authToken != null && !authToken.isEmpty() && 
               fromNumber != null && !fromNumber.isEmpty();
    }
    
    /**
     * Get SMS character count and segment info
     */
    public SmsInfo getSmsInfo(String message) {
        if (message == null) {
            return new SmsInfo(0, 0, 0);
        }
        
        int length = message.length();
        int segments = (int) Math.ceil((double) length / maxSmsLength);
        int remainingChars = maxSmsLength - (length % maxSmsLength);
        
        return new SmsInfo(length, segments, remainingChars);
    }
    
    /**
     * SMS information class
     */
    public static class SmsInfo {
        private final int characterCount;
        private final int segmentCount;
        private final int remainingCharacters;
        
        public SmsInfo(int characterCount, int segmentCount, int remainingCharacters) {
            this.characterCount = characterCount;
            this.segmentCount = segmentCount;
            this.remainingCharacters = remainingCharacters;
        }
        
        public int getCharacterCount() { return characterCount; }
        public int getSegmentCount() { return segmentCount; }
        public int getRemainingCharacters() { return remainingCharacters; }
    }
}
