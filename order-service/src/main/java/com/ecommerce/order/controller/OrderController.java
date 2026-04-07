package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.PaymentStatus;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    @Operation(summary = "Create Order", description = "Creates a new order")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        OrderResponseDto order = orderService.createOrder(createOrderDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get Order by ID", description = "Retrieves order details by ID")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable("orderId") Long orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get Order by Number", description = "Retrieves order details by order number")
    public ResponseEntity<OrderResponseDto> getOrderByNumber(@PathVariable("orderNumber") String orderNumber) {
        OrderResponseDto order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User Orders", description = "Retrieves all orders for a specific user")
    public ResponseEntity<Page<OrderResponseDto>> getUserOrders(
            @PathVariable("userId") String userId,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping
    @Operation(summary = "Get All Orders", description = "Retrieves all orders with pagination")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get Orders by Status", description = "Retrieves orders filtered by status")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByStatus(
            @PathVariable("status") OrderStatus status,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get Orders by Date Range", description = "Retrieves orders within a specific date range")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByDateRange(
            @RequestParam(name = "startDate") LocalDateTime startDate,
            @RequestParam(name = "endDate") LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update Order Status", description = "Updates the status of an order")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam(name = "status") OrderStatus status) {
        OrderResponseDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{orderId}/payment-status")
    @Operation(summary = "Update Payment Status", description = "Updates the payment status of an order")
    public ResponseEntity<OrderResponseDto> updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam(name = "paymentStatus") PaymentStatus paymentStatus) {
        OrderResponseDto order = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(order);
    }
    
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel Order", description = "Cancels an order")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId, @RequestParam(name = "reason", defaultValue = "Cancelled by user") String reason) {
        OrderResponseDto order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get User Order Summary", description = "Retrieves a summary of orders for a user")
    public ResponseEntity<OrderSummaryDto> getUserOrderSummary(@PathVariable String userId) {
        OrderSummaryDto summary = orderService.getOrderSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get Overdue Orders", description = "Retrieves orders that are overdue")
    public ResponseEntity<List<OrderResponseDto>> getOverdueOrders(@RequestParam(name = "days") int days) {
        List<OrderResponseDto> orders = orderService.getOverdueOrders(days);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search Orders", description = "Searches orders with multiple criteria")
    public ResponseEntity<Page<OrderResponseDto>> searchOrders(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "paymentStatus", required = false) PaymentStatus paymentStatus,
            @RequestParam(name = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.searchOrders(
            userId, status, paymentStatus, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }
}