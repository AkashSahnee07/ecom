import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { AuthService } from '@/services/authService';
import { User, AuthState, RegisterRequest } from '@/types';

interface AuthStore extends AuthState {
  // Actions
  login: (username: string, password: string) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  updateProfile: (userData: Partial<User>) => Promise<void>;
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: undefined,

      // Actions
      login: async (username: string, password: string) => {
        set({ isLoading: true, error: undefined });
        try {
          const response = await AuthService.login({ username, password });
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: undefined,
          });
          
          // Store refresh token separately (more secure)
          localStorage.setItem('refreshToken', response.refreshToken);
        } catch (error: any) {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: error.message || 'Login failed',
          });
          throw error;
        }
      },

      register: async (userData: RegisterRequest) => {
        set({ isLoading: true, error: undefined });
        try {
          const response = await AuthService.register(userData);
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: undefined,
          });
          
          localStorage.setItem('refreshToken', response.refreshToken);
        } catch (error: any) {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: error.message || 'Registration failed',
          });
          throw error;
        }
      },

      logout: async () => {
        set({ isLoading: true });
        try {
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            await AuthService.logout();
          }
        } catch (error) {
          console.error('Logout error:', error);
        } finally {
          // Clear all auth data regardless of API call success
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: undefined,
          });
          localStorage.removeItem('refreshToken');
        }
      },

      refreshToken: async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            error: 'No refresh token available',
          });
          return;
        }

        try {
          const response = await AuthService.refreshToken(refreshToken);
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            error: undefined,
          });
          
          localStorage.setItem('refreshToken', response.refreshToken);
        } catch (error: any) {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            error: error.message || 'Token refresh failed',
          });
          localStorage.removeItem('refreshToken');
          throw error;
        }
      },

      updateProfile: async (userData: Partial<User>) => {
        const state = get();
        const { user } = state;
        if (!user) {
          throw new Error('User not authenticated');
        }

        set({ isLoading: true, error: undefined });
        try {
          const updatedUser = await AuthService.updateProfile(userData);
          set({
            user: updatedUser,
            isLoading: false,
            error: undefined,
          });
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Profile update failed',
          });
          throw error;
        }
      },

      changePassword: async (currentPassword: string, newPassword: string) => {
        const state = get();
        const { user } = state;
        if (!user) {
          throw new Error('User not authenticated');
        }

        set({ isLoading: true, error: undefined });
        try {
          await AuthService.changePassword(currentPassword, newPassword);
          set({
            isLoading: false,
            error: undefined,
          });
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Password change failed',
          });
          throw error;
        }
      },

      clearError: () => {
        set({ error: undefined });
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },
    }),
    {
      name: 'auth-store',
      partialize: (state: AuthStore) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Helper hooks for common auth operations
export const useAuth = () => {
  const store = useAuthStore();
  return {
    user: store.user,
    token: store.token,
    isAuthenticated: store.isAuthenticated,
    isLoading: store.isLoading,
    error: store.error,
    login: store.login,
    register: store.register,
    logout: store.logout,
    refreshToken: store.refreshToken,
    updateProfile: store.updateProfile,
    changePassword: store.changePassword,
    clearError: store.clearError,
  };
};

export const useUser = () => {
  return useAuthStore((state) => state.user);
};

export const useIsAuthenticated = () => {
  return useAuthStore((state) => state.isAuthenticated);
};