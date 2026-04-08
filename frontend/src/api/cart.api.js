import api from './axios';

export const cartAPI = {
  getCart: (userId) => api.get(`/cart/${userId}`),
  addItem: (userId, data) => api.post(`/cart/${userId}/items`, data),
  updateQuantity: (userId, productId, quantity) =>
    api.put(`/cart/${userId}/items/${productId}?quantity=${quantity}`),
  removeItem: (userId, productId) => api.delete(`/cart/${userId}/items/${productId}`),
  clearCart: (userId) => api.delete(`/cart/${userId}/clear`),
  deleteCart: (userId) => api.delete(`/cart/${userId}`),
  getSummary: (userId) => api.get(`/cart/${userId}/summary`),
  cartExists: (userId) => api.get(`/cart/${userId}/exists`),
  mergeGuestCart: (userId, data) => api.post(`/cart/${userId}/merge`, data),
};
