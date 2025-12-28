package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending email notifications
 */
@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${notification.email.from:noreply@ecommerce.com}")
    private String fromEmail;
    
    @Value("${notification.email.from-name:E-Commerce Platform}")
    private String fromName;
    
    @Value("${notification.email.reply-to:support@ecommerce.com}")
    private String replyToEmail;
    
    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${notification.email.html-enabled:true}")
    private boolean htmlEnabled;
    
    /**
     * Send email notification
     */
    public boolean sendEmail(Notification notification) {
        if (!emailEnabled) {
            logger.warn("Email notifications are disabled");
            return false;
        }
        
        if (notification.getRecipientEmail() == null || notification.getRecipientEmail().trim().isEmpty()) {
            logger.error("No recipient email provided for notification: {}", notification.getId());
            return false;
        }
        
        try {
            if (htmlEnabled && isHtmlContent(notification.getContent())) {
                return sendHtmlEmail(notification);
            } else {
                return sendSimpleEmail(notification);
            }
        } catch (Exception e) {
            logger.error("Failed to send email for notification: {}", notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Send simple text email
     */
    private boolean sendSimpleEmail(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipientEmail());
            message.setSubject(notification.getSubject());
            message.setText(notification.getContent());
            message.setReplyTo(replyToEmail);
            
            mailSender.send(message);
            logger.info("Simple email sent successfully for notification: {}", notification.getId());
            return true;
            
        } catch (MailException e) {
            logger.error("Failed to send simple email for notification: {}", notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Send HTML email
     */
    private boolean sendHtmlEmail(Notification notification) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getContent(), true); // true indicates HTML
            helper.setReplyTo(replyToEmail);
            
            // Add headers for tracking
            mimeMessage.setHeader("X-Notification-ID", notification.getId().toString());
            mimeMessage.setHeader("X-Notification-Type", notification.getType().name());
            if (notification.getCorrelationId() != null) {
                mimeMessage.setHeader("X-Correlation-ID", notification.getCorrelationId());
            }
            
            mailSender.send(mimeMessage);
            logger.info("HTML email sent successfully for notification: {}", notification.getId());
            return true;
            
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            logger.error("Failed to send HTML email for notification: {}", notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Send email asynchronously
     */
    public CompletableFuture<Boolean> sendEmailAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> sendEmail(notification));
    }
    
    /**
     * Send bulk emails (for newsletters, promotions, etc.)
     */
    public void sendBulkEmails(java.util.List<Notification> notifications) {
        logger.info("Sending bulk emails for {} notifications", notifications.size());
        
        for (Notification notification : notifications) {
            try {
                sendEmail(notification);
                // Add small delay to avoid overwhelming the mail server
                Thread.sleep(100);
            } catch (Exception e) {
                logger.error("Failed to send bulk email for notification: {}", notification.getId(), e);
            }
        }
    }
    
    /**
     * Validate email address format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Check if content is HTML
     */
    private boolean isHtmlContent(String content) {
        if (content == null) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("<html>") || 
               lowerContent.contains("<body>") || 
               lowerContent.contains("<div>") || 
               lowerContent.contains("<p>") ||
               lowerContent.contains("<br>") ||
               lowerContent.contains("<table>");
    }
    
    /**
     * Create email template for order confirmation
     */
    public String createOrderConfirmationTemplate(String customerName, String orderId, 
                                                 String orderTotal, String orderItems) {
        return String.format(
            "<html><body>" +
            "<h2>Order Confirmation</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Thank you for your order! Your order has been confirmed.</p>" +
            "<div style='border: 1px solid #ddd; padding: 15px; margin: 20px 0;'>" +
            "<h3>Order Details</h3>" +
            "<p><strong>Order ID:</strong> %s</p>" +
            "<p><strong>Total:</strong> %s</p>" +
            "<p><strong>Items:</strong></p>" +
            "<div>%s</div>" +
            "</div>" +
            "<p>We'll send you another email when your order ships.</p>" +
            "<p>Thank you for shopping with us!</p>" +
            "<p>Best regards,<br>E-Commerce Team</p>" +
            "</body></html>",
            customerName, orderId, orderTotal, orderItems
        );
    }
    
    /**
     * Create email template for shipping notification
     */
    public String createShippingNotificationTemplate(String customerName, String orderId, 
                                                   String trackingNumber, String carrier) {
        return String.format(
            "<html><body>" +
            "<h2>Your Order Has Shipped!</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Great news! Your order has been shipped and is on its way to you.</p>" +
            "<div style='border: 1px solid #ddd; padding: 15px; margin: 20px 0;'>" +
            "<h3>Shipping Details</h3>" +
            "<p><strong>Order ID:</strong> %s</p>" +
            "<p><strong>Tracking Number:</strong> %s</p>" +
            "<p><strong>Carrier:</strong> %s</p>" +
            "</div>" +
            "<p>You can track your package using the tracking number above.</p>" +
            "<p>Thank you for your business!</p>" +
            "<p>Best regards,<br>E-Commerce Team</p>" +
            "</body></html>",
            customerName, orderId, trackingNumber, carrier
        );
    }
    
    /**
     * Create email template for password reset
     */
    public String createPasswordResetTemplate(String customerName, String resetLink, String expirationTime) {
        return String.format(
            "<html><body>" +
            "<h2>Password Reset Request</h2>" +
            "<p>Dear %s,</p>" +
            "<p>We received a request to reset your password. Click the link below to create a new password:</p>" +
            "<div style='margin: 20px 0;'>" +
            "<a href='%s' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Reset Password</a>" +
            "</div>" +
            "<p>This link will expire in %s.</p>" +
            "<p>If you didn't request this password reset, please ignore this email.</p>" +
            "<p>Best regards,<br>E-Commerce Team</p>" +
            "</body></html>",
            customerName, resetLink, expirationTime
        );
    }
    
    /**
     * Create welcome email template
     */
    public String createWelcomeTemplate(String customerName, String verificationLink) {
        return String.format(
            "<html><body>" +
            "<h2>Welcome to E-Commerce Platform!</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Welcome to our e-commerce platform! We're excited to have you as a member.</p>" +
            "<p>To get started, please verify your email address by clicking the link below:</p>" +
            "<div style='margin: 20px 0;'>" +
            "<a href='%s' style='background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verify Email</a>" +
            "</div>" +
            "<p>Once verified, you can start shopping and enjoying our services.</p>" +
            "<p>If you have any questions, feel free to contact our support team.</p>" +
            "<p>Happy shopping!</p>" +
            "<p>Best regards,<br>E-Commerce Team</p>" +
            "</body></html>",
            customerName, verificationLink
        );
    }
}