package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.NotificationType;
import com.ecommerce.notification.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);
    
    private TemplateEngine templateEngine;
    private final Map<String, String> templates = new HashMap<>();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    @PostConstruct
    public void init() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        initializeDefaultTemplates();
        
        logger.info("TemplateService initialized with {} templates", templates.size());
    }
    
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null) {
            return null;
        }
        
        try {
            if (containsThymeleafSyntax(templateContent)) {
                Context context = new Context();
                if (variables != null) {
                    context.setVariables(variables);
                }
                return templateEngine.process(templateContent, context);
            } else {
                return processSimpleVariables(templateContent, variables);
            }
        } catch (Exception e) {
            logger.error("Error processing template: {}", e.getMessage(), e);
            return templateContent;
        }
    }
    
    public String getTemplate(NotificationType type, NotificationChannel channel) {
        String key = type.name() + "_" + channel.name();
        return templates.get(key);
    }
    
    public void setTemplate(NotificationType type, NotificationChannel channel, String template) {
        String key = type.name() + "_" + channel.name();
        templates.put(key, template);
    }
    
    private String processSimpleVariables(String template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            if (value != null) {
                result = result.replace("{{" + variableName + "}}", value.toString());
            }
        }
        
        return result;
    }
    
    private boolean containsThymeleafSyntax(String template) {
        return template.contains("th:") || 
               template.contains("${") || 
               template.contains("*{") || 
               template.contains("#{") || 
               template.contains("@{");
    }
    
    private void initializeDefaultTemplates() {
        templates.put("ORDER_CONFIRMATION_EMAIL", createOrderConfirmationEmailTemplate());
        templates.put("PAYMENT_SUCCESS_EMAIL", createPaymentSuccessEmailTemplate());
        templates.put("WELCOME_EMAIL", createWelcomeEmailTemplate());
        
        templates.put("ORDER_CONFIRMATION_SMS", "Order {{orderNumber}} confirmed. Total: ${{totalAmount}}.");
        templates.put("PAYMENT_SUCCESS_SMS", "Payment of ${{amount}} successful for order {{orderNumber}}.");
        templates.put("WELCOME_SMS", "Welcome {{customerName}}! Your account is ready.");
        
        templates.put("ORDER_CONFIRMATION_PUSH", "Order confirmed! {{orderNumber}} - ${{totalAmount}}");
        templates.put("PAYMENT_SUCCESS_PUSH", "Payment successful for order {{orderNumber}}");
        templates.put("WELCOME_PUSH", "Welcome to our store, {{customerName}}!");
    }
    
    private String createOrderConfirmationEmailTemplate() {
        return "<html><body>" +
               "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<h2 style='color: #28a745;'>Order Confirmation</h2>" +
               "<p>Dear {{customerName}},</p>" +
               "<p>Thank you for your order! Your order has been confirmed.</p>" +
               "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
               "<h3>Order Details</h3>" +
               "<p><strong>Order Number:</strong> {{orderNumber}}</p>" +
               "<p><strong>Total Amount:</strong> ${{totalAmount}}</p>" +
               "</div>" +
               "<p>Best regards,<br>The E-Commerce Team</p>" +
               "</div></body></html>";
    }
    
    private String createPaymentSuccessEmailTemplate() {
        return "<html><body>" +
               "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<h2 style='color: #28a745;'>Payment Successful</h2>" +
               "<p>Dear {{customerName}},</p>" +
               "<p>Your payment has been processed successfully!</p>" +
               "<div style='background-color: #d4edda; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
               "<h3>Payment Details</h3>" +
               "<p><strong>Amount:</strong> ${{amount}}</p>" +
               "<p><strong>Order Number:</strong> {{orderNumber}}</p>" +
               "</div>" +
               "<p>Best regards,<br>The E-Commerce Team</p>" +
               "</div></body></html>";
    }
    
    private String createWelcomeEmailTemplate() {
        return "<html><body>" +
               "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<h2 style='color: #007bff;'>Welcome to Our Store!</h2>" +
               "<p>Dear {{customerName}},</p>" +
               "<p>Welcome to our e-commerce platform!</p>" +
               "<p>Best regards,<br>The E-Commerce Team</p>" +
               "</div></body></html>";
    }
    
    public boolean validateTemplate(String template, Map<String, Object> requiredVariables) {
        if (template == null || requiredVariables == null) {
            return false;
        }
        
        for (String variable : requiredVariables.keySet()) {
            if (!template.contains("{{" + variable + "}}")) {
                logger.warn("Template missing required variable: {}", variable);
                return false;
            }
        }
        
        return true;
    }
    
    public Map<String, String> getAllTemplates() {
        return new HashMap<>(templates);
    }
    
    public void clearTemplates() {
        templates.clear();
    }
}