import client from './client'

// Maps to product-service's ProductController + InventoryController.
export const productApi = {
  getAll: ({ page = 0, size = 20 } = {}) =>
    client.get('/api/products', { params: { page, size } }),
  getById: (id) => client.get(`/api/products/${id}`),
  search: (keyword) => client.get('/api/products/search', { params: { keyword } }),
  lowStock: () => client.get('/api/products/low-stock'),
  create: (payload) => client.post('/api/products', payload),
  restock: (productId, quantity) =>
    client.post(`/api/inventory/product/${productId}/restock`, null, { params: { quantity } })
}
