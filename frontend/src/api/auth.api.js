import api from './axios';

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  refresh: (refreshToken) => api.post(`/auth/refresh?refreshToken=${refreshToken}`),
  logout: (token) => api.post('/auth/logout', null, { headers: { Authorization: token } }),
  validateToken: (token) => api.post(`/auth/validate?token=${token}`),
};

export const userAPI = {
  register: (data) => api.post('/users/register', data),
  getById: (id) => api.get(`/users/${id}`),
  getByUsername: (username) => api.get(`/users/username/${username}`),
  getAll: () => api.get('/users'),
  update: (id, data) => api.put(`/users/${id}`, data),
  deactivate: (id) => api.put(`/users/${id}/deactivate`),
  activate: (id) => api.put(`/users/${id}/activate`),
};
