import api from './axios';

export const paymentsAPI = {
  create: (data) => api.post('/payments', data),
  getById: (paymentId) => api.get(`/payments/${paymentId}`),
  process: (paymentId) => api.post(`/payments/${paymentId}/process`),
  refund: (paymentId, data) => api.post(`/payments/${paymentId}/refund`, data),
  cancel: (paymentId) => api.post(`/payments/${paymentId}/cancel`),
  getByOrderId: (orderId) => api.get(`/payments/order/${orderId}`),
  getUserPayments: (userId, params = {}) => api.get(`/payments/user/${userId}`, { params }),
  search: (params = {}) => api.get('/payments/search', { params }),
  getUserSummary: (userId) => api.get(`/payments/user/${userId}/summary`),
  getExpired: () => api.get('/payments/expired'),
  getDailyStats: (startDate, endDate) =>
    api.get('/payments/stats/daily', { params: { startDate, endDate } }),
};
