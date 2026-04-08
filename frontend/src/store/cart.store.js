import { create } from 'zustand';
import { cartAPI } from '../api/cart.api';

const useCartStore = create((set, get) => ({
  cart: null,
  loading: false,
  error: null,

  fetchCart: async (userId) => {
    if (!userId) return;
    set({ loading: true });
    try {
      const res = await cartAPI.getCart(userId);
      set({ cart: res.data, loading: false });
    } catch {
      set({ loading: false });
    }
  },

  addItem: async (userId, productId, quantity = 1) => {
    set({ loading: true });
    try {
      const res = await cartAPI.addItem(userId, { productId, quantity });
      set({ cart: res.data, loading: false });
      return { success: true };
    } catch (err) {
      set({ loading: false });
      return { success: false, error: err.response?.data?.message };
    }
  },

  updateQuantity: async (userId, productId, quantity) => {
    try {
      const res = await cartAPI.updateQuantity(userId, productId, quantity);
      set({ cart: res.data });
    } catch (err) {
      console.error('Update quantity error:', err);
    }
  },

  removeItem: async (userId, productId) => {
    try {
      const res = await cartAPI.removeItem(userId, productId);
      set({ cart: res.data });
    } catch (err) {
      console.error('Remove item error:', err);
    }
  },

  clearCart: async (userId) => {
    try {
      await cartAPI.clearCart(userId);
      set({ cart: null });
    } catch (err) {
      console.error('Clear cart error:', err);
    }
  },

  getItemCount: () => {
    const cart = get().cart;
    if (!cart?.items) return 0;
    return cart.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
  },

  getTotal: () => {
    const cart = get().cart;
    return cart?.totalAmount || cart?.totalPrice || 0;
  },
}));

export default useCartStore;
