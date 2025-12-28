package com.ecommerce.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RefundRequestDto {
    
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    private String reason;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    private String refundMethod; // Optional: specify refund method if different from original
    
    // Constructors
    public RefundRequestDto() {}
    
    public RefundRequestDto(BigDecimal amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }
    
    // Getters and setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
}