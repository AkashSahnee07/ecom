package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.PaymentStatus;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        OrderResponseDto order = orderService.createOrder(createOrderDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponseDto> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponseDto order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponseDto>> getUserOrders(
            @PathVariable String userId,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponseDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderResponseDto> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus) {
        OrderResponseDto order = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(order);
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId, @RequestParam(defaultValue = "Cancelled by user") String reason) {
        OrderResponseDto order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<OrderSummaryDto> getUserOrderSummary(@PathVariable String userId) {
        OrderSummaryDto summary = orderService.getOrderSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<OrderResponseDto>> getOverdueOrders(@RequestParam int days) {
        List<OrderResponseDto> orders = orderService.getOverdueOrders(days);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<OrderResponseDto>> searchOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.searchOrders(
            userId, status, paymentStatus, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }
}