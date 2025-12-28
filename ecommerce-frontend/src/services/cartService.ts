import { cartApi, handleApiResponse, handleApiError } from './api';
import {
  Cart,
  CartItem,
  CartSummary,
  ApiResponse,
} from '@/types';

export class CartService {
  // Cart Management
  static async getCart(userId: string): Promise<Cart> {
    try {
      const response = await cartApi.get<Cart>(`/api/v1/cart/${userId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async createCart(userId: string): Promise<Cart> {
    try {
      const response = await cartApi.post<Cart>('/api/v1/cart', { userId });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async clearCart(userId: string): Promise<ApiResponse<string>> {
    try {
      const response = await cartApi.delete<ApiResponse<string>>(`/api/v1/cart/${userId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Items Management
  static async addItemToCart(
    userId: string,
    productId: number,
    quantity: number = 1
  ): Promise<CartItem> {
    try {
      const response = await cartApi.post<CartItem>(`/api/v1/cart/${userId}/items`, {
        productId,
        quantity,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async updateCartItem(
    userId: string,
    productId: number,
    quantity: number
  ): Promise<CartItem> {
    try {
      const response = await cartApi.put<CartItem>(
        `/api/v1/cart/${userId}/items/${productId}`,
        { quantity }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async removeItemFromCart(
    userId: string,
    productId: number
  ): Promise<ApiResponse<string>> {
    try {
      const response = await cartApi.delete<ApiResponse<string>>(
        `/api/v1/cart/${userId}/items/${productId}`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getCartItems(userId: string): Promise<CartItem[]> {
    try {
      const response = await cartApi.get<CartItem[]>(`/api/v1/cart/${userId}/items`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getCartItem(userId: string, productId: number): Promise<CartItem> {
    try {
      const response = await cartApi.get<CartItem>(`/api/v1/cart/${userId}/items/${productId}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Summary and Calculations
  static async getCartSummary(userId: string): Promise<CartSummary> {
    try {
      const response = await cartApi.get<CartSummary>(`/api/v1/cart/${userId}/summary`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getCartTotal(userId: string): Promise<{ total: number }> {
    try {
      const response = await cartApi.get<{ total: number }>(`/api/v1/cart/${userId}/total`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getCartItemCount(userId: string): Promise<{ count: number }> {
    try {
      const response = await cartApi.get<{ count: number }>(`/api/v1/cart/${userId}/count`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Operations
  static async mergeCarts(
    sourceUserId: string,
    targetUserId: string
  ): Promise<Cart> {
    try {
      const response = await cartApi.post<Cart>('/api/v1/cart/merge', {
        sourceUserId,
        targetUserId,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async validateCart(userId: string): Promise<{
    valid: boolean;
    issues: Array<{
      productId: number;
      issue: string;
      availableQuantity?: number;
    }>;
  }> {
    try {
      const response = await cartApi.get<{
        valid: boolean;
        issues: Array<{
          productId: number;
          issue: string;
          availableQuantity?: number;
        }>;
      }>(`/api/v1/cart/${userId}/validate`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Bulk Operations
  static async addMultipleItems(
    userId: string,
    items: Array<{ productId: number; quantity: number }>
  ): Promise<CartItem[]> {
    try {
      const response = await cartApi.post<CartItem[]>(`/api/v1/cart/${userId}/items/bulk`, {
        items,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async updateMultipleItems(
    userId: string,
    items: Array<{ productId: number; quantity: number }>
  ): Promise<CartItem[]> {
    try {
      const response = await cartApi.put<CartItem[]>(`/api/v1/cart/${userId}/items/bulk`, {
        items,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async removeMultipleItems(
    userId: string,
    productIds: number[]
  ): Promise<ApiResponse<string>> {
    try {
      const response = await cartApi.delete<ApiResponse<string>>(
        `/api/v1/cart/${userId}/items/bulk`,
        {
          data: { productIds },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Persistence
  static async saveCartForLater(userId: string): Promise<ApiResponse<string>> {
    try {
      const response = await cartApi.post<ApiResponse<string>>(`/api/v1/cart/${userId}/save`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async restoreCart(userId: string): Promise<Cart> {
    try {
      const response = await cartApi.post<Cart>(`/api/v1/cart/${userId}/restore`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Sharing and Wishlist
  static async shareCart(userId: string): Promise<{ shareToken: string; shareUrl: string }> {
    try {
      const response = await cartApi.post<{ shareToken: string; shareUrl: string }>(
        `/api/v1/cart/${userId}/share`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getSharedCart(shareToken: string): Promise<Cart> {
    try {
      const response = await cartApi.get<Cart>(`/api/v1/cart/shared/${shareToken}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async moveItemToWishlist(
    userId: string,
    productId: number
  ): Promise<ApiResponse<string>> {
    try {
      const response = await cartApi.post<ApiResponse<string>>(
        `/api/v1/cart/${userId}/items/${productId}/move-to-wishlist`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Cart Analytics
  static async getCartAnalytics(userId: string): Promise<{
    totalValue: number;
    itemCount: number;
    averageItemPrice: number;
    lastUpdated: string;
    abandonmentRisk: 'low' | 'medium' | 'high';
  }> {
    try {
      const response = await cartApi.get<{
        totalValue: number;
        itemCount: number;
        averageItemPrice: number;
        lastUpdated: string;
        abandonmentRisk: 'low' | 'medium' | 'high';
      }>(`/api/v1/cart/${userId}/analytics`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }
}