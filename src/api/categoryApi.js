import client from './client'

// Maps to product-service's CategoryController (/api/categories/**).
export const categoryApi = {
  getAll: () => client.get('/api/categories'),
  getById: (id) => client.get(`/api/categories/${id}`),
  create: (payload) => client.post('/api/categories', payload)
}
