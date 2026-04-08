import api from './axios';

export const notificationsAPI = {
  create: (data) => api.post('/notifications', data),
  send: (data) => api.post('/notifications/send', data),
  sendAsync: (data) => api.post('/notifications/send-async', data),
  getById: (id) => api.get(`/notifications/${id}`),
  getByRecipient: (recipientId, params = {}) =>
    api.get(`/notifications/recipient/${recipientId}`, { params }),
  getByStatus: (status, params = {}) => api.get(`/notifications/status/${status}`, { params }),
  getByType: (type, params = {}) => api.get(`/notifications/type/${type}`, { params }),
  getByCorrelationId: (correlationId) => api.get(`/notifications/correlation/${correlationId}`),
  updateStatus: (id, data) => api.put(`/notifications/${id}/status`, data),
  retry: (id) => api.post(`/notifications/${id}/retry`),
  getStats: (params = {}) => api.get('/notifications/stats', { params }),
  getScheduled: (params = {}) => api.get('/notifications/scheduled', { params }),
  cancel: (id, reason) => api.delete(`/notifications/${id}/cancel?reason=${encodeURIComponent(reason)}`),
  cleanup: (daysOld = 30) => api.delete(`/notifications/cleanup?daysOld=${daysOld}`),
};
