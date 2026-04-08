import api from './axios';

export const reviewsAPI = {
  create: (data) => {
    const formData = new FormData();
    Object.entries(data).forEach(([k, v]) => {
      if (v !== undefined && v !== null) formData.append(k, v);
    });
    return api.post('/reviews', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getByProduct: (productId, params = {}) =>
    api.get(`/reviews/product/${productId}`, { params }),
  getById: (reviewId) => api.get(`/reviews/${reviewId}`),
  update: (reviewId, data) => {
    const params = new URLSearchParams();
    Object.entries(data).forEach(([k, v]) => { if (v) params.append(k, v); });
    return api.put(`/reviews/${reviewId}?${params.toString()}`);
  },
  delete: (reviewId, userId) => api.delete(`/reviews/${reviewId}?userId=${userId}`),
  vote: (reviewId, userId, voteType) =>
    api.post(`/reviews/${reviewId}/vote?userId=${userId}&voteType=${voteType}`),
  getProductStats: (productId) => api.get(`/reviews/product/${productId}/stats`),
  getUserReviews: (userId, params = {}) => api.get(`/reviews/user/${userId}`, { params }),
  search: (query, productId, params = {}) =>
    api.get('/reviews/search', { params: { query, productId, ...params } }),
  getFeatured: (productId, limit = 5) =>
    api.get(`/reviews/product/${productId}/featured?limit=${limit}`),
  getMostHelpful: (productId, limit = 5) =>
    api.get(`/reviews/product/${productId}/helpful?limit=${limit}`),
  getRecent: (limit = 10) => api.get(`/reviews/recent?limit=${limit}`),
};
