import { userApi, handleApiResponse, handleApiError } from './api';
import {
  User,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ApiResponse,
} from '@/types';

export class AuthService {
  // User Authentication
  static async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await userApi.post<AuthResponse>('/api/v1/auth/login', credentials);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async register(userData: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await userApi.post<AuthResponse>('/api/v1/auth/register', userData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async logout(): Promise<void> {
    try {
      await userApi.post('/api/v1/auth/logout');
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async refreshToken(refreshToken: string): Promise<AuthResponse> {
    try {
      const response = await userApi.post<AuthResponse>('/api/v1/auth/refresh', {
        refreshToken,
      });
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // User Profile Management
  static async getCurrentUser(): Promise<User> {
    try {
      const response = await userApi.get<User>('/api/v1/users/me');
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async updateProfile(userData: Partial<User>): Promise<User> {
    try {
      const response = await userApi.put<User>('/api/v1/users/me', userData);
      return handleApiResponse(response);
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    try {
      await userApi.put('/api/v1/users/me/password', {
        currentPassword,
        newPassword,
      });
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async deleteAccount(): Promise<void> {
    try {
      await userApi.delete('/api/v1/users/me');
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Password Reset
  static async forgotPassword(email: string): Promise<void> {
    try {
      await userApi.post('/api/v1/auth/forgot-password', { email });
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async resetPassword(token: string, newPassword: string): Promise<void> {
    try {
      await userApi.post('/api/v1/auth/reset-password', {
        token,
        newPassword,
      });
    } catch (error) {
      throw handleApiError(error);
    }
  }

  // Email Verification
  static async verifyEmail(token: string): Promise<void> {
    try {
      await userApi.post('/api/v1/auth/verify-email', { token });
    } catch (error) {
      throw handleApiError(error);
    }
  }

  static async resendVerificationEmail(): Promise<void> {
    try {
      await userApi.post('/api/v1/auth/resend-verification');
    } catch (error) {
      throw handleApiError(error);
    }
  }
}