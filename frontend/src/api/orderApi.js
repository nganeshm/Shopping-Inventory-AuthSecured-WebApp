import client from './client'

// Maps to product-service's OrderController (orders live inside
// product-service — see the backend README's architecture section).
export const orderApi = {
  placeOrder: (payload) => client.post('/api/orders', payload),
  getByOrderNumber: (orderNumber) => client.get(`/api/orders/${orderNumber}`),
  getByCustomerId: (customerId) => client.get(`/api/orders/customer/${customerId}`)
}
