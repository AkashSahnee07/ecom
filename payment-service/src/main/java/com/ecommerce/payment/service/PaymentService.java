package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.CreatePaymentDto;
import com.ecommerce.payment.dto.PaymentResponseDto;
import com.ecommerce.payment.dto.RefundRequestDto;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.exception.InvalidPaymentStatusException;
import com.ecommerce.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public PaymentResponseDto createPayment(CreatePaymentDto createPaymentDto) {
        Payment payment = new Payment(
            generatePaymentId(),
            createPaymentDto.getOrderId(),
            createPaymentDto.getUserId(),
            createPaymentDto.getAmount(),
            createPaymentDto.getPaymentMethod(),
            createPaymentDto.getCurrency()
        );
        
        payment.setPaymentGateway(createPaymentDto.getPaymentGateway());
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // 30 minutes expiry
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Publish payment created event
        publishPaymentEvent("payment.created", savedPayment);
        
        return convertToDto(savedPayment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("id", paymentId.toString()));
        return convertToDto(payment);
    }
    
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByPaymentId(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("paymentId", paymentId));
        return convertToDto(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByOrderId(String orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPaymentsByUserId(String userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(this::convertToDto);
    }
    
    public PaymentResponseDto processPayment(String paymentId, String transactionId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("paymentId", paymentId));
        
        if (!payment.isPending()) {
            throw new InvalidPaymentStatusException(
                "Cannot process payment with status: " + payment.getStatus());
        }
        
        payment.markAsProcessing();
        paymentRepository.save(payment);
        
        // Simulate payment processing
        try {
            // Here you would integrate with actual payment gateway
            Thread.sleep(1000); // Simulate processing time
            
            payment.markAsCompleted(transactionId);
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish payment completed event
            publishPaymentEvent("payment.completed", savedPayment);
            
            return convertToDto(savedPayment);
            
        } catch (Exception e) {
            payment.markAsFailed("Payment processing failed: " + e.getMessage());
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish payment failed event
            publishPaymentEvent("payment.failed", savedPayment);
            
            return convertToDto(savedPayment);
        }
    }
    
    public PaymentResponseDto refundPayment(String paymentId, RefundRequestDto refundRequest) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("paymentId", paymentId));
        
        if (!payment.canBeRefunded()) {
            throw new InvalidPaymentStatusException(
                "Cannot refund payment with status: " + payment.getStatus());
        }
        
        BigDecimal refundAmount = refundRequest.getAmount();
        if (refundAmount.compareTo(payment.getRemainingRefundableAmount()) > 0) {
            throw new InvalidPaymentStatusException(
                "Refund amount exceeds remaining refundable amount");
        }
        
        payment.addRefund(refundAmount, refundRequest.getReason());
        Payment savedPayment = paymentRepository.save(payment);
        
        // Publish payment refunded event
        publishPaymentEvent("payment.refunded", savedPayment);
        
        return convertToDto(savedPayment);
    }
    
    public PaymentResponseDto cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("paymentId", paymentId));
        
        if (payment.getStatus().isFinal()) {
            throw new InvalidPaymentStatusException(
                "Cannot cancel payment with status: " + payment.getStatus());
        }
        
        payment.setStatus(PaymentStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);
        
        // Publish payment cancelled event
        publishPaymentEvent("payment.cancelled", savedPayment);
        
        return convertToDto(savedPayment);
    }
    
    @Transactional(readOnly = true)
    public PaymentSummary getUserPaymentSummary(String userId) {
        Long totalPayments = paymentRepository.countByUserIdAndStatus(userId, PaymentStatus.COMPLETED);
        BigDecimal totalAmount = paymentRepository.getTotalAmountByUserId(userId);
        BigDecimal totalRefunds = paymentRepository.getTotalRefundAmountByUserId(userId);
        
        return new PaymentSummary(totalPayments, totalAmount, totalRefunds);
    }
    
    public void processExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now());
        
        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            
            // Publish payment expired event
            publishPaymentEvent("payment.expired", payment);
        }
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now());
        return expiredPayments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public void cleanupExpiredPayments() {
        processExpiredPayments();
    }
    
    @Transactional(readOnly = true)
    public List<DailyPaymentStats> getDailyPaymentStats(LocalDateTime startDate, LocalDateTime endDate) {
        // This would typically use a custom repository method with GROUP BY date
        // For now, return empty list as placeholder
        return List.of();
    }
    
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> searchPayments(PaymentStatus status, com.ecommerce.payment.entity.PaymentMethod paymentMethod, 
                                                 String userId, String orderId, LocalDateTime startDate, 
                                                 LocalDateTime endDate, BigDecimal minAmount, BigDecimal maxAmount, 
                                                 Pageable pageable) {
        // This would typically use a custom repository method with complex filtering
        // For now, return all payments as placeholder
        return getAllPayments(pageable);
    }
    
    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    private void publishPaymentEvent(String eventType, Payment payment) {
        PaymentEvent event = new PaymentEvent(
            eventType,
            payment.getPaymentId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getStatus(),
            LocalDateTime.now()
        );
        
        kafkaTemplate.send("payment-events", payment.getPaymentId(), event);
    }
    
    private PaymentResponseDto convertToDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentGateway(payment.getPaymentGateway());
        dto.setTransactionId(payment.getTransactionId());
        dto.setFailureReason(payment.getFailureReason());
        dto.setRefundAmount(payment.getRefundAmount());
        dto.setRefundReason(payment.getRefundReason());
        dto.setProcessedAt(payment.getProcessedAt());
        dto.setExpiresAt(payment.getExpiresAt());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
    
    // Nested classes for events and summaries
    public static class PaymentEvent {
        private String eventType;
        private String paymentId;
        private String orderId;
        private String userId;
        private BigDecimal amount;
        private PaymentStatus status;
        private LocalDateTime timestamp;
        
        public PaymentEvent(String eventType, String paymentId, String orderId, String userId,
                           BigDecimal amount, PaymentStatus status, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.userId = userId;
            this.amount = amount;
            this.status = status;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getEventType() { return eventType; }
        public String getPaymentId() { return paymentId; }
        public String getOrderId() { return orderId; }
        public String getUserId() { return userId; }
        public BigDecimal getAmount() { return amount; }
        public PaymentStatus getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class PaymentSummary {
        private Long totalPayments;
        private BigDecimal totalAmount;
        private BigDecimal totalRefunds;
        
        public PaymentSummary(Long totalPayments, BigDecimal totalAmount, BigDecimal totalRefunds) {
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.totalRefunds = totalRefunds != null ? totalRefunds : BigDecimal.ZERO;
        }
        
        // Getters
        public Long getTotalPayments() { return totalPayments; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getTotalRefunds() { return totalRefunds; }
        public BigDecimal getNetAmount() { return totalAmount.subtract(totalRefunds); }
    }
    
    public static class DailyPaymentStats {
        private LocalDateTime date;
        private Long totalPayments;
        private BigDecimal totalAmount;
        private Long completedPayments;
        private Long failedPayments;
        
        public DailyPaymentStats(LocalDateTime date, Long totalPayments, BigDecimal totalAmount, 
                               Long completedPayments, Long failedPayments) {
            this.date = date;
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.completedPayments = completedPayments;
            this.failedPayments = failedPayments;
        }
        
        // Getters
        public LocalDateTime getDate() { return date; }
        public Long getTotalPayments() { return totalPayments; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public Long getCompletedPayments() { return completedPayments; }
        public Long getFailedPayments() { return failedPayments; }
    }
}
