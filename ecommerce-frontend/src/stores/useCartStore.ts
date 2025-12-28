import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { CartService } from '@/services/cartService';
import { Cart, CartItem, CartState, CartSummary } from '@/types';

interface CartStore extends CartState {
  // Actions
  fetchCart: (userId: string) => Promise<void>;
  addItem: (userId: string, productId: number, quantity?: number) => Promise<void>;
  updateItem: (userId: string, productId: number, quantity: number) => Promise<void>;
  removeItem: (userId: string, productId: number) => Promise<void>;
  clearCart: (userId: string) => Promise<void>;
  getCartSummary: (userId: string) => Promise<CartSummary | null>;
  validateCart: (userId: string) => Promise<boolean>;
  mergeCart: (sourceUserId: string, targetUserId: string) => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      // Initial state
      cart: null,
      itemCount: 0,
      isLoading: false,
      error: undefined,

      // Actions
      fetchCart: async (userId: string) => {
        set({ isLoading: true, error: undefined });
        try {
          const cart = await CartService.getCart(userId);
          set({
            cart,
            itemCount: cart.totalItems,
            isLoading: false,
            error: undefined,
          });
        } catch (error: any) {
          // If cart doesn't exist, create a new one
          if (error.status === 404) {
            try {
              const newCart = await CartService.createCart(userId);
              set({
                cart: newCart,
                itemCount: newCart.totalItems,
                isLoading: false,
                error: undefined,
              });
            } catch (createError: any) {
              set({
                cart: null,
                itemCount: 0,
                isLoading: false,
                error: createError.message || 'Failed to create cart',
              });
            }
          } else {
            set({
              cart: null,
              itemCount: 0,
              isLoading: false,
              error: error.message || 'Failed to fetch cart',
            });
          }
        }
      },

      addItem: async (userId: string, productId: number, quantity: number = 1) => {
        set({ isLoading: true, error: undefined });
        try {
          await CartService.addItemToCart(userId, productId, quantity);
          // Refresh cart after adding item
          await get().fetchCart(userId);
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Failed to add item to cart',
          });
          throw error;
        }
      },

      updateItem: async (userId: string, productId: number, quantity: number) => {
        set({ isLoading: true, error: undefined });
        try {
          await CartService.updateCartItem(userId, productId, quantity);
          // Refresh cart after updating item
          await get().fetchCart(userId);
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Failed to update cart item',
          });
          throw error;
        }
      },

      removeItem: async (userId: string, productId: number) => {
        set({ isLoading: true, error: undefined });
        try {
          await CartService.removeItemFromCart(userId, productId);
          // Refresh cart after removing item
          await get().fetchCart(userId);
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Failed to remove cart item',
          });
          throw error;
        }
      },

      clearCart: async (userId: string) => {
        set({ isLoading: true, error: undefined });
        try {
          await CartService.clearCart(userId);
          set({
            cart: null,
            itemCount: 0,
            isLoading: false,
            error: undefined,
          });
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Failed to clear cart',
          });
          throw error;
        }
      },

      getCartSummary: async (userId: string): Promise<CartSummary | null> => {
        try {
          const summary = await CartService.getCartSummary(userId);
          return summary;
        } catch (error: any) {
          set({ error: error.message || 'Failed to get cart summary' });
          return null;
        }
      },

      validateCart: async (userId: string): Promise<boolean> => {
        try {
          const validation = await CartService.validateCart(userId);
          if (!validation.valid) {
            set({ error: 'Cart validation failed: ' + validation.issues.map(i => i.issue).join(', ') });
          }
          return validation.valid;
        } catch (error: any) {
          set({ error: error.message || 'Failed to validate cart' });
          return false;
        }
      },

      mergeCart: async (sourceUserId: string, targetUserId: string) => {
        set({ isLoading: true, error: undefined });
        try {
          await CartService.mergeCarts(sourceUserId, targetUserId);
          // Refresh cart after merging
          await get().fetchCart(targetUserId);
        } catch (error: any) {
          set({
            isLoading: false,
            error: error.message || 'Failed to merge carts',
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
      name: 'cart-store',
      partialize: (state: CartStore) => ({
        cart: state.cart,
        itemCount: state.itemCount,
      }),
    }
  )
);

// Helper hooks for common cart operations
export const useCart = () => {
  const store = useCartStore();
  return {
    cart: store.cart,
    itemCount: store.itemCount,
    isLoading: store.isLoading,
    error: store.error,
    fetchCart: store.fetchCart,
    addItem: store.addItem,
    updateItem: store.updateItem,
    removeItem: store.removeItem,
    clearCart: store.clearCart,
    getCartSummary: store.getCartSummary,
    validateCart: store.validateCart,
    mergeCart: store.mergeCart,
    clearError: store.clearError,
  };
};

export const useCartItemCount = () => {
  return useCartStore((state) => state.itemCount);
};

export const useCartItems = () => {
  return useCartStore((state) => state.cart?.items || []);
};

export const useCartTotal = () => {
  return useCartStore((state) => state.cart?.totalAmount || 0);
};