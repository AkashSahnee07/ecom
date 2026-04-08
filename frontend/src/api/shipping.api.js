import api from './axios';

export const shippingAPI = {
  createShipment: (data) => api.post('/shipping/shipments', data),
  getById: (shipmentId) => api.get(`/shipping/shipments/${shipmentId}`),
  track: (trackingNumber) => api.get(`/shipping/shipments/tracking/${trackingNumber}`),
  getByOrderId: (orderId) => api.get(`/shipping/shipments/order/${orderId}`),
  getByStatus: (status, params = {}) => api.get('/shipping/shipments', { params: { status, ...params } }),
  updateStatus: (shipmentId, data) => api.put(`/shipping/shipments/${shipmentId}/status`, data),
  getTrackingEvents: (shipmentId) => api.get(`/shipping/shipments/${shipmentId}/tracking`),
  cancel: (shipmentId, reason) =>
    api.put(`/shipping/shipments/${shipmentId}/cancel?reason=${encodeURIComponent(reason)}`),
  getOverdue: () => api.get('/shipping/shipments/overdue'),
  getNeedingAttention: () => api.get('/shipping/shipments/attention'),
};
