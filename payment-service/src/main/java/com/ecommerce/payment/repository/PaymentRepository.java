package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentMethod;
import com.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentId(String paymentId);
    
    List<Payment> findByOrderId(String orderId);
    
    Page<Payment> findByUserId(String userId, Pageable pageable);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);
    
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.createdAt BETWEEN :startDate AND :endDate")
    Page<Payment> findByUserIdAndDateRange(@Param("userId") String userId, 
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    Long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountByUserId(@Param("userId") String userId);
    
    @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.userId = :userId AND p.refundAmount IS NOT NULL")
    BigDecimal getTotalRefundAmountByUserId(@Param("userId") String userId);
    
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') AND p.expiresAt < :currentTime")
    List<Payment> findExpiredPayments(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt > :since")
    List<Payment> findRecentFailedPayments(@Param("since") LocalDateTime since);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentGateway = :gateway AND p.status = :status")
    List<Payment> findByPaymentGatewayAndStatus(@Param("gateway") String gateway, 
                                               @Param("status") PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.amount >= :minAmount AND p.status = 'COMPLETED'")
    List<Payment> findLargePayments(@Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.paymentMethod = :method AND p.status = 'COMPLETED' ORDER BY p.createdAt DESC")
    List<Payment> findRecentSuccessfulPaymentsByMethod(@Param("userId") String userId, 
                                                      @Param("method") PaymentMethod method,
                                                      Pageable pageable);
    
    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);
    
    @Query("SELECT DISTINCT p.paymentGateway FROM Payment p WHERE p.paymentGateway IS NOT NULL")
    List<String> findAllPaymentGateways();
    
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionId(@Param("transactionId") String transactionId);
}
