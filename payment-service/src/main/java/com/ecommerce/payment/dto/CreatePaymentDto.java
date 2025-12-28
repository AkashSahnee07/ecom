package com.ecommerce.payment.dto;

import com.ecommerce.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreatePaymentDto {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;
    
    private String paymentGateway;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    // Card payment specific fields
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    
    // Digital wallet specific fields
    private String walletId;
    private String walletProvider;
    
    // Bank transfer specific fields
    private String bankAccountNumber;
    private String routingNumber;
    private String bankName;
    
    // Constructors
    public CreatePaymentDto() {}
    
    public CreatePaymentDto(String orderId, String userId, BigDecimal amount, 
                           PaymentMethod paymentMethod, String currency) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
    }
    
    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentGateway() { return paymentGateway; }
    public void setPaymentGateway(String paymentGateway) { this.paymentGateway = paymentGateway; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    
    public String getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
    
    public String getExpiryYear() { return expiryYear; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    
    public String getWalletProvider() { return walletProvider; }
    public void setWalletProvider(String walletProvider) { this.walletProvider = walletProvider; }
    
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    
    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
}