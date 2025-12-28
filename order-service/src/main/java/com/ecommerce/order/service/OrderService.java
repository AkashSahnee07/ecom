package com.ecommerce.order.service;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_UPDATED_TOPIC = "order-updated";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    
    public OrderResponseDto createOrder(CreateOrderDto createOrderDto) {
        // Generate unique order number
        String orderNumber = generateOrderNumber();
        
        // Create order entity
        Order order = new Order(orderNumber, createOrderDto.getUserId(), OrderStatus.PENDING);
        order.setShippingAddress(convertToShippingAddress(createOrderDto.getShippingAddress()));
        order.setBillingAddress(convertToBillingAddress(createOrderDto.getBillingAddress()));
        order.setPaymentMethod(createOrderDto.getPaymentMethod());
        order.setNotes(createOrderDto.getNotes());
        order.setCurrency(createOrderDto.getCurrency() != null ? createOrderDto.getCurrency() : "USD");
        
        // Add items to order
        for (OrderItemDto itemDto : createOrderDto.getItems()) {
            OrderItem item = convertToOrderItem(itemDto);
            order.addItem(item);
        }
        
        // Set tax and shipping amounts
        order.setTaxAmount(createOrderDto.getTaxAmount());
        order.setShippingAmount(createOrderDto.getShippingAmount());
        order.setDiscountAmount(createOrderDto.getDiscountAmount());
        
        // Calculate totals
        order.calculateTotals();
        
        // Set estimated delivery date (7 days from now as default)
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(7));
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Publish order created event
        publishOrderEvent("ORDER_CREATED", savedOrder);
        
        return convertToOrderResponseDto(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return convertToOrderResponseDto(order);
    }
    
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return convertToOrderResponseDto(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToOrderResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getUserOrdersPaginated(String userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::convertToOrderResponseDto);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrdersByStatus(String userId, OrderStatus status) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);
        return orders.stream()
                .map(this::convertToOrderResponseDto)
                .collect(Collectors.toList());
    }
    
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // Set actual delivery date if delivered
        if (newStatus == OrderStatus.DELIVERED) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish order status updated event
        publishOrderStatusUpdateEvent(updatedOrder, oldStatus, newStatus);
        
        return convertToOrderResponseDto(updatedOrder);
    }
    
    public OrderResponseDto cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null) {
            order.setNotes(order.getNotes() != null ? order.getNotes() + "; Cancellation reason: " + reason : "Cancellation reason: " + reason);
        }
        
        Order cancelledOrder = orderRepository.save(order);
        
        // Publish order cancelled event
        publishOrderCancelledEvent(cancelledOrder, reason);
        
        return convertToOrderResponseDto(cancelledOrder);
    }
    
    public OrderResponseDto updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        order.setPaymentStatus(paymentStatus);
        
        // Auto-confirm order if payment is completed and order is pending
        if (paymentStatus == PaymentStatus.COMPLETED && order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish payment status updated event
        publishPaymentStatusUpdateEvent(updatedOrder, paymentStatus);
        
        return convertToOrderResponseDto(updatedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderSummaryDto getOrderSummary(String userId) {
        Long totalOrders = orderRepository.countOrdersByUserId(userId);
        Long pendingOrders = orderRepository.countOrdersByUserIdAndStatus(userId, OrderStatus.PENDING);
        Long deliveredOrders = orderRepository.countOrdersByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        Double totalSpent = orderRepository.getTotalSpentByUserId(userId);
        
        return new OrderSummaryDto(
                totalOrders != null ? totalOrders.intValue() : 0,
                pendingOrders != null ? pendingOrders.intValue() : 0,
                deliveredOrders != null ? deliveredOrders.intValue() : 0,
                totalSpent != null ? BigDecimal.valueOf(totalSpent) : BigDecimal.ZERO
        );
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOverdueOrders(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<Order> overdueOrders = orderRepository.findOverdueOrders(cutoffDate);
        return overdueOrders.stream()
                .map(this::convertToOrderResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> searchOrders(String userId, OrderStatus status, PaymentStatus paymentStatus, 
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.searchOrders(userId, status, paymentStatus, startDate, endDate, pageable);
        return orders.map(this::convertToOrderResponseDto);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserId(String userId, Pageable pageable) {
        return getUserOrdersPaginated(userId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderResponseDto);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(this::convertToOrderResponseDto);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        // Convert List to Page manually since repository method returns List
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orders.size());
        List<OrderResponseDto> pageContent = orders.subList(start, end)
                .stream()
                .map(this::convertToOrderResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(pageContent, pageable, orders.size());
    }
    
    // Helper methods
    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }
    
    private OrderItem convertToOrderItem(OrderItemDto dto) {
        OrderItem item = new OrderItem(dto.getProductId(), dto.getProductName(), dto.getUnitPrice(), dto.getQuantity());
        item.setProductSku(dto.getProductSku());
        item.setDiscountAmount(dto.getDiscountAmount());
        item.setTaxAmount(dto.getTaxAmount());
        item.setProductImageUrl(dto.getProductImageUrl());
        item.setBrand(dto.getBrand());
        item.setCategoryId(dto.getCategoryId() != null ? dto.getCategoryId().toString() : null);
        item.setCategoryName(dto.getCategoryName());
        return item;
    }
    
    private ShippingAddress convertToShippingAddress(ShippingAddressDto dto) {
        if (dto == null) return null;
        ShippingAddress address = new ShippingAddress();
        address.setFirstName(dto.getFirstName());
        address.setLastName(dto.getLastName());
        address.setCompany(dto.getCompany());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setPhone(dto.getPhone());
        address.setEmail(dto.getEmail());
        address.setSpecialInstructions(dto.getSpecialInstructions());
        return address;
    }
    
    private BillingAddress convertToBillingAddress(BillingAddressDto dto) {
        if (dto == null) return null;
        BillingAddress address = new BillingAddress();
        address.setFirstName(dto.getFirstName());
        address.setLastName(dto.getLastName());
        address.setCompany(dto.getCompany());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setPhone(dto.getPhone());
        address.setEmail(dto.getEmail());
        return address;
    }
    
    private OrderResponseDto convertToOrderResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setCurrency(order.getCurrency());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(order.getActualDeliveryDate());
        
        // Convert addresses
        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(convertToShippingAddressDto(order.getShippingAddress()));
        }
        if (order.getBillingAddress() != null) {
            dto.setBillingAddress(convertToBillingAddressDto(order.getBillingAddress()));
        }
        
        // Convert items
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(this::convertToOrderItemDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private OrderItemDto convertToOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductSku(item.getProductSku());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setProductImageUrl(item.getProductImageUrl());
        dto.setBrand(item.getBrand());
        dto.setCategoryId(item.getCategoryId() != null ? Long.valueOf(item.getCategoryId()) : null);
        dto.setCategoryName(item.getCategoryName());
        return dto;
    }
    
    private ShippingAddressDto convertToShippingAddressDto(ShippingAddress address) {
        ShippingAddressDto dto = new ShippingAddressDto();
        dto.setFirstName(address.getFirstName());
        dto.setLastName(address.getLastName());
        dto.setCompany(address.getCompany());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setEmail(address.getEmail());
        dto.setSpecialInstructions(address.getSpecialInstructions());
        return dto;
    }
    
    private BillingAddressDto convertToBillingAddressDto(BillingAddress address) {
        BillingAddressDto dto = new BillingAddressDto();
        dto.setFirstName(address.getFirstName());
        dto.setLastName(address.getLastName());
        dto.setCompany(address.getCompany());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setEmail(address.getEmail());
        return dto;
    }
    
    // Event publishing methods
    private void publishOrderEvent(String eventType, Order order) {
        OrderEvent event = new OrderEvent(eventType, order.getId(), order.getOrderNumber(), 
                order.getUserId(), order.getStatus(), order.getTotalAmount());
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event);
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event);
    }
    
    private void publishOrderStatusUpdateEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(order.getId(), order.getOrderNumber(), 
                order.getUserId(), oldStatus, newStatus, LocalDateTime.now());
        kafkaTemplate.send(ORDER_UPDATED_TOPIC, event);
    }
    
    private void publishOrderCancelledEvent(Order order, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(order.getId(), order.getOrderNumber(), 
                order.getUserId(), reason, LocalDateTime.now());
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, event);
    }
    
    private void publishPaymentStatusUpdateEvent(Order order, PaymentStatus paymentStatus) {
        PaymentStatusUpdateEvent event = new PaymentStatusUpdateEvent(order.getId(), order.getOrderNumber(), 
                order.getUserId(), paymentStatus, LocalDateTime.now());
        kafkaTemplate.send("payment-status-updated", event);
    }
    
    // Event classes
    public static class OrderEvent {
        private String eventType;
        private Long orderId;
        private String orderNumber;
        private String userId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private LocalDateTime timestamp;
        
        public OrderEvent(String eventType, Long orderId, String orderNumber, String userId, OrderStatus status, BigDecimal totalAmount) {
            this.eventType = eventType;
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.userId = userId;
            this.status = status;
            this.totalAmount = totalAmount;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class OrderStatusUpdateEvent {
        private Long orderId;
        private String orderNumber;
        private String userId;
        private OrderStatus oldStatus;
        private OrderStatus newStatus;
        private LocalDateTime timestamp;
        
        public OrderStatusUpdateEvent(Long orderId, String orderNumber, String userId, OrderStatus oldStatus, OrderStatus newStatus, LocalDateTime timestamp) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.userId = userId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public OrderStatus getOldStatus() { return oldStatus; }
        public void setOldStatus(OrderStatus oldStatus) { this.oldStatus = oldStatus; }
        public OrderStatus getNewStatus() { return newStatus; }
        public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class OrderCancelledEvent {
        private Long orderId;
        private String orderNumber;
        private String userId;
        private String reason;
        private LocalDateTime timestamp;
        
        public OrderCancelledEvent(Long orderId, String orderNumber, String userId, String reason, LocalDateTime timestamp) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.userId = userId;
            this.reason = reason;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class PaymentStatusUpdateEvent {
        private Long orderId;
        private String orderNumber;
        private String userId;
        private PaymentStatus paymentStatus;
        private LocalDateTime timestamp;
        
        public PaymentStatusUpdateEvent(Long orderId, String orderNumber, String userId, PaymentStatus paymentStatus, LocalDateTime timestamp) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.userId = userId;
            this.paymentStatus = paymentStatus;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}