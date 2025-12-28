import { paymentApi, handleApiResponse, handleApiError } from './api';
import {
  Payment,
  CreatePaymentRequest,
  PaymentStatus,
  PaymentMethod,
  PaginatedResponse,
  ApiResponse,
} from '@/types';

export class PaymentService {
  // Payment Management
  static async createPayment(paymentData: CreatePaymentRequest): Promise<Payment> {
    try {
      const response = await paymentApi.post<Payment>('/api/v1/payments', paymentData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getPaymentById(paymentId: number): Promise<Payment> {
    try {
      const response = await paymentApi.get<Payment>(`/api/v1/payments/${paymentId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getPaymentByPaymentId(paymentId: string): Promise<Payment> {
    try {
      const response = await paymentApi.get<Payment>(`/api/v1/payments/payment-id/${paymentId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getAllPayments(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'createdAt',
    sortDirection: string = 'desc'
  ): Promise<PaginatedResponse<Payment>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<Payment>>('/api/v1/payments', {
        params: { page, size, sortBy, sortDirection },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Processing
  static async processPayment(paymentId: number): Promise<Payment> {
    try {
      const response = await paymentApi.post<Payment>(`/api/v1/payments/${paymentId}/process`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async cancelPayment(paymentId: number): Promise<Payment> {
    try {
      const response = await paymentApi.post<Payment>(`/api/v1/payments/${paymentId}/cancel`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async confirmPayment(paymentId: number): Promise<Payment> {
    try {
      const response = await paymentApi.post<Payment>(`/api/v1/payments/${paymentId}/confirm`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Status Management
  static async updatePaymentStatus(
    paymentId: number,
    status: PaymentStatus
  ): Promise<Payment> {
    try {
      const response = await paymentApi.put<Payment>(`/api/v1/payments/${paymentId}/status`, {
        status,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getPaymentsByStatus(
    status: PaymentStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Payment>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<Payment>>('/api/v1/payments/status', {
        params: { status, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // User Payment Management
  static async getUserPayments(
    userId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Payment>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<Payment>>(`/api/v1/payments/user/${userId}`, {
        params: { page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserPaymentsByStatus(
    userId: string,
    status: PaymentStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Payment>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<Payment>>(
        `/api/v1/payments/user/${userId}/status`,
        {
          params: { status, page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Order Payment Management
  static async getOrderPayments(orderId: string): Promise<Payment[]> {
    try {
      const response = await paymentApi.get<Payment[]>(`/api/v1/payments/order/${orderId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getOrderPaymentsByStatus(
    orderId: string,
    status: PaymentStatus
  ): Promise<Payment[]> {
    try {
      const response = await paymentApi.get<Payment[]>(
        `/api/v1/payments/order/${orderId}/status`,
        {
          params: { status },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Refund Management
  static async createRefund(
    paymentId: number,
    amount: number,
    reason: string
  ): Promise<{
    refundId: string;
    status: string;
    amount: number;
    estimatedProcessingTime: string;
  }> {
    try {
      const response = await paymentApi.post<{
        refundId: string;
        status: string;
        amount: number;
        estimatedProcessingTime: string;
      }>(`/api/v1/payments/${paymentId}/refund`, {
        amount,
        reason,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getRefundStatus(refundId: string): Promise<{
    refundId: string;
    paymentId: number;
    status: string;
    amount: number;
    processedDate?: string;
    reason: string;
  }> {
    try {
      const response = await paymentApi.get<{
        refundId: string;
        paymentId: number;
        status: string;
        amount: number;
        processedDate?: string;
        reason: string;
      }>(`/api/v1/payments/refunds/${refundId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getAllRefunds(
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<any>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<any>>('/api/v1/payments/refunds', {
        params: { page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserRefunds(
    userId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<any>> {
    try {
      const response = await paymentApi.get<PaginatedResponse<any>>(
        `/api/v1/payments/user/${userId}/refunds`,
        {
          params: { page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Methods Management
  static async getUserPaymentMethods(userId: string): Promise<Array<{
    id: string;
    type: PaymentMethod;
    last4?: string;
    expiryMonth?: number;
    expiryYear?: number;
    brand?: string;
    isDefault: boolean;
    createdAt: string;
  }>> {
    try {
      const response = await paymentApi.get<Array<{
        id: string;
        type: PaymentMethod;
        last4?: string;
        expiryMonth?: number;
        expiryYear?: number;
        brand?: string;
        isDefault: boolean;
        createdAt: string;
      }>>(`/api/v1/payments/user/${userId}/payment-methods`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async addPaymentMethod(
    userId: string,
    paymentMethodData: {
      type: PaymentMethod;
      token: string;
      isDefault?: boolean;
    }
  ): Promise<{
    id: string;
    type: PaymentMethod;
    last4?: string;
    brand?: string;
    isDefault: boolean;
  }> {
    try {
      const response = await paymentApi.post<{
        id: string;
        type: PaymentMethod;
        last4?: string;
        brand?: string;
        isDefault: boolean;
      }>(`/api/v1/payments/user/${userId}/payment-methods`, paymentMethodData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async removePaymentMethod(
    userId: string,
    paymentMethodId: string
  ): Promise<ApiResponse<string>> {
    try {
      const response = await paymentApi.delete<ApiResponse<string>>(
        `/api/v1/payments/user/${userId}/payment-methods/${paymentMethodId}`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async setDefaultPaymentMethod(
    userId: string,
    paymentMethodId: string
  ): Promise<ApiResponse<string>> {
    try {
      const response = await paymentApi.put<ApiResponse<string>>(
        `/api/v1/payments/user/${userId}/payment-methods/${paymentMethodId}/default`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Analytics
  static async getPaymentAnalytics(): Promise<{
    totalPayments: number;
    totalRevenue: number;
    averagePaymentAmount: number;
    paymentsByStatus: Record<PaymentStatus, number>;
    paymentsByMethod: Record<PaymentMethod, number>;
    successRate: number;
  }> {
    try {
      const response = await paymentApi.get<{
        totalPayments: number;
        totalRevenue: number;
        averagePaymentAmount: number;
        paymentsByStatus: Record<PaymentStatus, number>;
        paymentsByMethod: Record<PaymentMethod, number>;
        successRate: number;
      }>('/api/v1/payments/analytics');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getUserPaymentAnalytics(userId: string): Promise<{
    totalPayments: number;
    totalSpent: number;
    averagePaymentAmount: number;
    preferredPaymentMethod: PaymentMethod;
    successRate: number;
    lastPaymentDate: string;
  }> {
    try {
      const response = await paymentApi.get<{
        totalPayments: number;
        totalSpent: number;
        averagePaymentAmount: number;
        preferredPaymentMethod: PaymentMethod;
        successRate: number;
        lastPaymentDate: string;
      }>(`/api/v1/payments/user/${userId}/analytics`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Validation and Security
  static async validatePaymentData(paymentData: CreatePaymentRequest): Promise<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }> {
    try {
      const response = await paymentApi.post<{
        valid: boolean;
        errors: string[];
        warnings: string[];
      }>('/api/v1/payments/validate', paymentData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async checkFraudRisk(paymentData: CreatePaymentRequest): Promise<{
    riskLevel: 'low' | 'medium' | 'high';
    riskScore: number;
    riskFactors: string[];
    requiresManualReview: boolean;
  }> {
    try {
      const response = await paymentApi.post<{
        riskLevel: 'low' | 'medium' | 'high';
        riskScore: number;
        riskFactors: string[];
        requiresManualReview: boolean;
      }>('/api/v1/payments/fraud-check', paymentData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Payment Gateway Integration
  static async getPaymentGateways(): Promise<Array<{
    id: string;
    name: string;
    supportedMethods: PaymentMethod[];
    isActive: boolean;
    processingFee: number;
  }>> {
    try {
      const response = await paymentApi.get<Array<{
        id: string;
        name: string;
        supportedMethods: PaymentMethod[];
        isActive: boolean;
        processingFee: number;
      }>>('/api/v1/payments/gateways');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async createPaymentIntent(
    amount: number,
    currency: string,
    paymentMethod: PaymentMethod,
    orderId?: string
  ): Promise<{
    clientSecret: string;
    paymentIntentId: string;
    amount: number;
    currency: string;
  }> {
    try {
      const response = await paymentApi.post<{
        clientSecret: string;
        paymentIntentId: string;
        amount: number;
        currency: string;
      }>('/api/v1/payments/create-intent', {
        amount,
        currency,
        paymentMethod,
        orderId,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Webhook and Notifications
  static async handleWebhook(
    gatewayId: string,
    webhookData: any
  ): Promise<ApiResponse<string>> {
    try {
      const response = await paymentApi.post<ApiResponse<string>>(
        `/api/v1/payments/webhook/${gatewayId}`,
        webhookData
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }
}