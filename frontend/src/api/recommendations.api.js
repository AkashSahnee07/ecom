import api from './axios';

export const recommendationsAPI = {
  getForUser: (userId, params = {}) => api.get(`/recommendations/users/${userId}`, { params }),
  getSimilarProducts: (productId, params = {}) =>
    api.get(`/recommendations/products/${productId}/similar`, { params }),
  getCategoryRecs: (categoryId, params = {}) =>
    api.get(`/recommendations/categories/${categoryId}`, { params }),
  getCrossSell: (cartProductIds, userId, limit = 5) =>
    api.get('/recommendations/cross-sell', {
      params: { cartProductIds, userId, limit },
    }),
  getUpSell: (cartProductIds, userId, limit = 5) =>
    api.get('/recommendations/up-sell', {
      params: { cartProductIds, userId, limit },
    }),
  getAbandonedCart: (userId, limit = 5) =>
    api.get(`/recommendations/users/${userId}/abandoned-cart`, { params: { limit } }),
  trackBehavior: (data) => api.post('/recommendations/behavior', data),
  getHistory: (userId, params = {}) =>
    api.get(`/recommendations/users/${userId}/history`, { params }),
  getUserAnalytics: (userId) => api.get(`/recommendations/users/${userId}/analytics`),
  getRecentlyViewed: (userId, limit = 10) =>
    api.get(`/recommendations/users/${userId}/recently-viewed`, { params: { limit } }),
  getPreferredCategories: (userId, limit = 5) =>
    api.get(`/recommendations/users/${userId}/preferred-categories`, { params: { limit } }),
};
