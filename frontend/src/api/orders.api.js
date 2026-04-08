import api from './axios';

export const ordersAPI = {
  create: (data) => api.post('/orders', data),
  getById: (orderId) => api.get(`/orders/${orderId}`),
  getByNumber: (orderNumber) => api.get(`/orders/number/${orderNumber}`),
  getUserOrders: (userId, params = {}) => api.get(`/orders/user/${userId}`, { params }),
  getAll: (params = {}) => api.get('/orders', { params }),
  getByStatus: (status, params = {}) => api.get(`/orders/status/${status}`, { params }),
  updateStatus: (orderId, status) => api.put(`/orders/${orderId}/status?status=${status}`),
  updatePaymentStatus: (orderId, paymentStatus) =>
    api.put(`/orders/${orderId}/payment-status?paymentStatus=${paymentStatus}`),
  cancel: (orderId, reason = 'Cancelled by user') =>
    api.delete(`/orders/${orderId}?reason=${encodeURIComponent(reason)}`),
  getUserSummary: (userId) => api.get(`/orders/user/${userId}/summary`),
  getOverdue: (days = 7) => api.get(`/orders/overdue?days=${days}`),
  search: (params) => api.get('/orders/search', { params }),
};
