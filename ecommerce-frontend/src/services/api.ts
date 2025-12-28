import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { AuthResponse, ErrorResponse } from '@/types';

// API Configuration
const API_CONFIG = {
  USER_SERVICE: process.env.NEXT_PUBLIC_USER_SERVICE_URL || 'http://localhost:8081',
  PRODUCT_SERVICE: process.env.NEXT_PUBLIC_PRODUCT_SERVICE_URL || 'http://localhost:8080',
  CART_SERVICE: process.env.NEXT_PUBLIC_CART_SERVICE_URL || 'http://localhost:8082',
  ORDER_SERVICE: process.env.NEXT_PUBLIC_ORDER_SERVICE_URL || 'http://localhost:8083',
  PAYMENT_SERVICE: process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL || 'http://localhost:8084',
};

// Create axios instances for each service
const createApiInstance = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor to add auth token
  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Response interceptor for error handling
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      return response;
    },
    (error) => {
      if (error.response?.status === 401) {
        // Handle unauthorized access
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/auth/login';
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

// API instances
export const userApi = createApiInstance(API_CONFIG.USER_SERVICE);
export const productApi = createApiInstance(API_CONFIG.PRODUCT_SERVICE);
export const cartApi = createApiInstance(API_CONFIG.CART_SERVICE);
export const orderApi = createApiInstance(API_CONFIG.ORDER_SERVICE);
export const paymentApi = createApiInstance(API_CONFIG.PAYMENT_SERVICE);

// Generic API response handler
export const handleApiResponse = <T>(response: AxiosResponse<T>): T => {
  return response.data;
};

// Generic API error handler
export const handleApiError = (error: any): ErrorResponse => {
  if (error.response?.data) {
    return error.response.data as ErrorResponse;
  }
  
  return {
    error: 'Network Error',
    message: error.message || 'An unexpected error occurred',
    status: error.response?.status || 500,
    timestamp: new Date().toISOString(),
  };
};

// Token management utilities
export const setAuthToken = (token: string) => {
  localStorage.setItem('token', token);
};

export const getAuthToken = (): string | null => {
  return localStorage.getItem('token');
};

export const removeAuthToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
};

export const isAuthenticated = (): boolean => {
  const token = getAuthToken();
  if (!token) return false;
  
  try {
    // Basic token validation (you might want to use jwt-decode for more thorough validation)
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Date.now() / 1000;
    return payload.exp > currentTime;
  } catch {
    return false;
  }
};