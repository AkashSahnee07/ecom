import api from './axios';

export const productsAPI = {
  getAll: (params = {}) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  getBySku: (sku) => api.get(`/products/sku/${sku}`),
  search: (keyword, params = {}) => api.get('/products/search', { params: { keyword, ...params } }),
  advancedSearch: (data, params = {}) => api.post('/products/search/advanced', data, { params }),
  getByCategory: (categoryId, params = {}) => api.get(`/products/category/${categoryId}`, { params }),
  getByBrand: (brand, params = {}) => api.get(`/products/brand/${brand}`, { params }),
  getByPriceRange: (minPrice, maxPrice, params = {}) =>
    api.get('/products/price-range', { params: { minPrice, maxPrice, ...params } }),
  getFeatured: (params = {}) => api.get('/products/featured', { params }),
  getTopRated: (params = {}) => api.get('/products/top-rated', { params }),
  getRecent: (params = {}) => api.get('/products/recent', { params }),
  getLowStock: (threshold = 10) => api.get(`/products/low-stock?threshold=${threshold}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  updateStock: (id, quantity) => api.patch(`/products/${id}/stock?quantity=${quantity}`),
  activate: (id) => api.patch(`/products/${id}/activate`),
  deactivate: (id) => api.patch(`/products/${id}/deactivate`),
};

export const categoriesAPI = {
  getAll: () => api.get('/products/categories'),
  getById: (id) => api.get(`/products/categories/${id}`),
  getHierarchy: () => api.get('/products/categories/hierarchy'),
  create: (data) => api.post('/products/categories', data),
  update: (id, data) => api.put(`/products/categories/${id}`, data),
  delete: (id) => api.delete(`/products/categories/${id}`),
};
