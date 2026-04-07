package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.CreatePaymentDto;
import com.ecommerce.payment.dto.PaymentResponseDto;
import com.ecommerce.payment.dto.RefundRequestDto;
import com.ecommerce.payment.entity.PaymentMethod;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    @Operation(summary = "Create Payment", description = "Initiates a new payment")
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody CreatePaymentDto createPaymentDto) {
        PaymentResponseDto payment = paymentService.createPayment(createPaymentDto);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get Payment", description = "Retrieves payment details by payment ID")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process Payment", description = "Processes a pending payment")
    public ResponseEntity<PaymentResponseDto> processPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.processPayment(paymentId, null);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund Payment", description = "Initiates a refund for a payment")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequestDto refundRequest) {
        PaymentResponseDto payment = paymentService.refundPayment(paymentId, refundRequest);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel Payment", description = "Cancels a payment")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get Payments by Order", description = "Retrieves all payments associated with an order")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable String orderId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User Payments", description = "Retrieves all payments for a specific user")
    public ResponseEntity<Page<PaymentResponseDto>> getUserPayments(
            @PathVariable String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponseDto> payments = paymentService.getPaymentsByUserId(userId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search Payments", description = "Searches payments with various criteria")
    public ResponseEntity<Page<PaymentResponseDto>> searchPayments(
            @RequestParam(name = "status", required = false) PaymentStatus status,
            @RequestParam(name = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponseDto> payments = paymentService.searchPayments(
            status, paymentMethod, userId, orderId, startDate, endDate, 
            minAmount, maxAmount, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get User Payment Summary", description = "Retrieves payment summary for a user")
    public ResponseEntity<PaymentService.PaymentSummary> getUserPaymentSummary(@PathVariable String userId) {
        PaymentService.PaymentSummary summary = paymentService.getUserPaymentSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get Expired Payments", description = "Retrieves payments that have expired")
    public ResponseEntity<List<PaymentResponseDto>> getExpiredPayments() {
        List<PaymentResponseDto> expiredPayments = paymentService.getExpiredPayments();
        return ResponseEntity.ok(expiredPayments);
    }
    
    @PostMapping("/expired/cleanup")
    @Operation(summary = "Cleanup Expired Payments", description = "Marks expired payments as failed")
    public ResponseEntity<Void> cleanupExpiredPayments() {
        paymentService.cleanupExpiredPayments();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/daily")
    @Operation(summary = "Get Daily Payment Stats", description = "Retrieves daily payment statistics")
    public ResponseEntity<List<PaymentService.DailyPaymentStats>> getDailyPaymentStats(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PaymentService.DailyPaymentStats> stats = paymentService.getDailyPaymentStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Checks service health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }
}
