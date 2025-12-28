package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.CreatePaymentDto;
import com.ecommerce.payment.dto.PaymentResponseDto;
import com.ecommerce.payment.dto.RefundRequestDto;
import com.ecommerce.payment.entity.PaymentMethod;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody CreatePaymentDto createPaymentDto) {
        PaymentResponseDto payment = paymentService.createPayment(createPaymentDto);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.getPaymentByPaymentId(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponseDto> processPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.processPayment(paymentId, null);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequestDto refundRequest) {
        PaymentResponseDto payment = paymentService.refundPayment(paymentId, refundRequest);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable String paymentId) {
        PaymentResponseDto payment = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable String orderId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PaymentResponseDto>> getUserPayments(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponseDto> payments = paymentService.getPaymentsByUserId(userId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<PaymentResponseDto>> searchPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponseDto> payments = paymentService.searchPayments(
            status, paymentMethod, userId, orderId, startDate, endDate, 
            minAmount, maxAmount, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<PaymentService.PaymentSummary> getUserPaymentSummary(@PathVariable String userId) {
        PaymentService.PaymentSummary summary = paymentService.getUserPaymentSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<PaymentResponseDto>> getExpiredPayments() {
        List<PaymentResponseDto> expiredPayments = paymentService.getExpiredPayments();
        return ResponseEntity.ok(expiredPayments);
    }
    
    @PostMapping("/expired/cleanup")
    public ResponseEntity<Void> cleanupExpiredPayments() {
        paymentService.cleanupExpiredPayments();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/daily")
    public ResponseEntity<List<PaymentService.DailyPaymentStats>> getDailyPaymentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PaymentService.DailyPaymentStats> stats = paymentService.getDailyPaymentStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }
}