import api from './axios';

export const inventoryAPI = {
  createOrUpdate: (params) => api.post('/inventory', null, { params }),
  getByProductAndWarehouse: (productId, warehouseId) =>
    api.get(`/inventory/product/${productId}/warehouse/${warehouseId}`),
  getByProduct: (productId) => api.get(`/inventory/product/${productId}`),
  getByWarehouse: (warehouseId, params = {}) =>
    api.get(`/inventory/warehouse/${warehouseId}`, { params }),
  reserveStock: (data) => api.post('/inventory/reserve', data),
  releaseReservation: (params) => api.post('/inventory/release', null, { params }),
  confirmReservation: (params) => api.post('/inventory/confirm', null, { params }),
  adjustStock: (data) => api.post('/inventory/adjust', data),
  getLowStock: () => api.get('/inventory/low-stock'),
  getReorderItems: () => api.get('/inventory/reorder'),
  getOverstock: () => api.get('/inventory/overstock'),
  checkAvailability: (productId, requiredQuantity) =>
    api.get(`/inventory/availability/${productId}?requiredQuantity=${requiredQuantity}`),
  getSummary: (productId) => api.get(`/inventory/summary/${productId}`),
  getTotalQuantity: (productId) => api.get(`/inventory/total/${productId}`),
};
