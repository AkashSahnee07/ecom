import { orderApi, handleApiResponse, handleApiError } from './api';
import {
  Order,
  CreateOrderRequest,
  OrderStatus,
  PaymentStatus,
  PaginatedResponse,
  ApiResponse,
} from '@/types';

export class OrderService {
  // Order Management
  static async createOrder(orderData: CreateOrderRequest): Promise<Order> {
    try {
      const response = await orderApi.post<Order>('/api/v1/orders', orderData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrderById(orderId: number): Promise<Order> {
    try {
      const response = await orderApi.get<Order>(`/api/v1/orders/${orderId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrderByNumber(orderNumber: string): Promise<Order> {
    try {
      const response = await orderApi.get<Order>(`/api/v1/orders/number/${orderNumber}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getAllOrders(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    sortDirection: string = 'desc'
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>('/api/v1/orders', {
        params: { page, size, sortBy, sortDirection },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserOrders(
    userId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>(`/api/v1/orders/user/${userId}`, {
        params: { page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Status Management
  static async updateOrderStatus(
    orderId: number,
    status: OrderStatus
  ): Promise<Order> {
    try {
      const response = await orderApi.put<Order>(`/api/v1/orders/${orderId}/status`, {
        status,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async cancelOrder(orderId: number): Promise<Order> {
    try {
      const response = await orderApi.put<Order>(`/api/v1/orders/${orderId}/cancel`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrdersByStatus(
    status: OrderStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>('/api/v1/orders/status', {
        params: { status, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Status Management
  static async updatePaymentStatus(
    orderId: number,
    paymentStatus: PaymentStatus
  ): Promise<Order> {
    try {
      const response = await orderApi.put<Order>(`/api/v1/orders/${orderId}/payment-status`, {
        paymentStatus,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrdersByPaymentStatus(
    paymentStatus: PaymentStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>('/api/v1/orders/payment-status', {
        params: { paymentStatus, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Filtering and Search
  static async getOrdersByDateRange(
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>('/api/v1/orders/date-range', {
        params: { startDate, endDate, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserOrdersByDateRange(
    userId: string,
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>(
        `/api/v1/orders/user/${userId}/date-range`,
        {
          params: { startDate, endDate, page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserOrdersByStatus(
    userId: string,
    status: OrderStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>(
        `/api/v1/orders/user/${userId}/status`,
        {
          params: { status, page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserOrdersByPaymentStatus(
    userId: string,
    paymentStatus: PaymentStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await orderApi.get<PaginatedResponse<Order>>(
        `/api/v1/orders/user/${userId}/payment-status`,
        {
          params: { paymentStatus, page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Analytics and Statistics
  static async getOrderStatistics(): Promise<{
    totalOrders: number;
    totalRevenue: number;
    averageOrderValue: number;
    ordersByStatus: Record<OrderStatus, number>;
    ordersByPaymentStatus: Record<PaymentStatus, number>;
  }> {
    try {
      const response = await orderApi.get<{
        totalOrders: number;
        totalRevenue: number;
        averageOrderValue: number;
        ordersByStatus: Record<OrderStatus, number>;
        ordersByPaymentStatus: Record<PaymentStatus, number>;
      }>('/api/v1/orders/statistics');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserOrderStatistics(userId: string): Promise<{
    totalOrders: number;
    totalSpent: number;
    averageOrderValue: number;
    favoriteCategories: string[];
    lastOrderDate: string;
  }> {
    try {
      const response = await orderApi.get<{
        totalOrders: number;
        totalSpent: number;
        averageOrderValue: number;
        favoriteCategories: string[];
        lastOrderDate: string;
      }>(`/api/v1/orders/user/${userId}/statistics`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Tracking
  static async trackOrder(orderNumber: string): Promise<{
    orderNumber: string;
    status: OrderStatus;
    trackingNumber?: string;
    estimatedDelivery?: string;
    statusHistory: Array<{
      status: OrderStatus;
      timestamp: string;
      location?: string;
      notes?: string;
    }>;
  }> {
    try {
      const response = await orderApi.get<{
        orderNumber: string;
        status: OrderStatus;
        trackingNumber?: string;
        estimatedDelivery?: string;
        statusHistory: Array<{
          status: OrderStatus;
          timestamp: string;
          location?: string;
          notes?: string;
        }>;
      }>(`/api/v1/orders/track/${orderNumber}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Validation
  static async validateOrder(orderData: CreateOrderRequest): Promise<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }> {
    try {
      const response = await orderApi.post<{
        valid: boolean;
        errors: string[];
        warnings: string[];
      }>('/api/v1/orders/validate', orderData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Invoice and Receipt
  static async getOrderInvoice(orderId: number): Promise<Blob> {
    try {
      const response = await orderApi.get(`/api/v1/orders/${orderId}/invoice`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrderReceipt(orderId: number): Promise<Blob> {
    try {
      const response = await orderApi.get(`/api/v1/orders/${orderId}/receipt`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Notifications
  static async sendOrderConfirmation(orderId: number): Promise<ApiResponse<string>> {
    try {
      const response = await orderApi.post<ApiResponse<string>>(
        `/api/v1/orders/${orderId}/send-confirmation`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async sendShippingNotification(orderId: number): Promise<ApiResponse<string>> {
    try {
      const response = await orderApi.post<ApiResponse<string>>(
        `/api/v1/orders/${orderId}/send-shipping-notification`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Returns and Refunds
  static async initiateReturn(
    orderId: number,
    reason: string,
    items?: Array<{ productId: number; quantity: number }>
  ): Promise<{
    returnId: string;
    status: string;
    returnLabel?: string;
  }> {
    try {
      const response = await orderApi.post<{
        returnId: string;
        status: string;
        returnLabel?: string;
      }>(`/api/v1/orders/${orderId}/return`, {
        reason,
        items,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getReturnStatus(returnId: string): Promise<{
    returnId: string;
    orderId: number;
    status: string;
    refundAmount: number;
    processedDate?: string;
  }> {
    try {
      const response = await orderApi.get<{
        returnId: string;
        orderId: number;
        status: string;
        refundAmount: number;
        processedDate?: string;
      }>(`/api/v1/orders/returns/${returnId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }
}