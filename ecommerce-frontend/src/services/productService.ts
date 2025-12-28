import { productApi, handleApiResponse, handleApiError } from './api';
import {
  Product,
  Category,
  ProductFilter,
  PaginatedResponse,
} from '@/types';

export class ProductService {
  // Product Management
  static async getAllProducts(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'name',
    sortDirection: string = 'asc'
  ): Promise<PaginatedResponse<Product>> {
    try {
      const response = await productApi.get<PaginatedResponse<Product>>('/api/v1/products', {
        params: { page, size, sortBy, sortDirection },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getProductById(id: number): Promise<Product> {
    try {
      const response = await productApi.get<Product>(`/api/v1/products/${id}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async searchProducts(
    query: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Product>> {
    try {
      const response = await productApi.get<PaginatedResponse<Product>>('/api/v1/products/search', {
        params: { query, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async filterProducts(
    filters: ProductFilter,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Product>> {
    try {
      const params = {
        page,
        size,
        ...filters,
      };
      const response = await productApi.get<PaginatedResponse<Product>>('/api/v1/products/filter', {
        params,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getProductsByCategory(
    categoryId: number,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Product>> {
    try {
      const response = await productApi.get<PaginatedResponse<Product>>(
        `/api/v1/products/category/${categoryId}`,
        {
          params: { page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getProductsByBrand(
    brand: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Product>> {
    try {
      const response = await productApi.get<PaginatedResponse<Product>>('/api/v1/products/brand', {
        params: { brand, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getProductsByPriceRange(
    minPrice: number,
    maxPrice: number,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Product>> {
    try {
      const response = await productApi.get<PaginatedResponse<Product>>('/api/v1/products/price-range', {
        params: { minPrice, maxPrice, page, size },
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Get featured products
  static async getFeaturedProducts(limit?: number): Promise<Product[]> {
    const response = await productApi.get('/featured', {
      params: { limit }
    });
    return response.data;
  }

  // Get new arrivals
  static async getNewArrivals(limit?: number): Promise<Product[]> {
    const response = await productApi.get('/new-arrivals', {
      params: { limit }
    });
    return response.data;
  }

  static async getBestSellers(): Promise<Product[]> {
    try {
      const response = await productApi.get<Product[]>('/api/v1/products/best-sellers');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Category Management
  static async getAllCategories(): Promise<Category[]> {
    try {
      const response = await productApi.get<Category[]>('/api/v1/categories');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getCategoryById(id: number): Promise<Category> {
    try {
      const response = await productApi.get<Category>(`/api/v1/categories/${id}`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getParentCategories(): Promise<Category[]> {
    try {
      const response = await productApi.get<Category[]>('/api/v1/categories/parent');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getSubCategories(parentId: number): Promise<Category[]> {
    try {
      const response = await productApi.get<Category[]>(`/api/v1/categories/parent/${parentId}/children`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Product Recommendations
  static async getRecommendedProducts(userId?: string): Promise<Product[]> {
    try {
      const params = userId ? { userId } : {};
      const response = await productApi.get<Product[]>('/api/v1/products/recommendations', {
        params,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getRelatedProducts(productId: number): Promise<Product[]> {
    try {
      const response = await productApi.get<Product[]>(`/api/v1/products/${productId}/related`);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Product Availability
  static async checkProductAvailability(productId: number): Promise<{ available: boolean; quantity: number }> {
    try {
      const response = await productApi.get<{ available: boolean; quantity: number }>(
        `/api/v1/products/${productId}/availability`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Product Reviews and Ratings
  static async getProductReviews(
    productId: number,
    page: number = 0,
    size: number = 10
  ): Promise<PaginatedResponse<any>> {
    try {
      const response = await productApi.get<PaginatedResponse<any>>(
        `/api/v1/products/${productId}/reviews`,
        {
          params: { page, size },
        }
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getProductRating(productId: number): Promise<{ averageRating: number; reviewCount: number }> {
    try {
      const response = await productApi.get<{ averageRating: number; reviewCount: number }>(
        `/api/v1/products/${productId}/rating`
      );
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Utility Methods
  static async getAllBrands(): Promise<string[]> {
    try {
      const response = await productApi.get<string[]>('/api/v1/products/brands');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async getPriceRange(): Promise<{ minPrice: number; maxPrice: number }> {
    try {
      const response = await productApi.get<{ minPrice: number; maxPrice: number }>('/api/v1/products/price-range');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }
}