import axios from 'axios'
import { parseApiError } from '../utils/errors'

/**
 * Browser always calls the API gateway. Downstream ports are for local ops only.
 *
 *   8080  api-gateway          ← VITE_API_BASE_URL (this client)
 *   8081  auth-service         /api/auth, /api/users
 *   8082  product-service      /api/products, /api/categories, /api/orders
 *   8083  customer-service     /api/customers/{id}
 *   8084  email-service
 *   8761  eureka-server
 *   5173  storefront (Vite)
 */
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

// Attach the JWT (if we have one) to every outgoing request. The gateway's
// JwtAuthenticationFilter checks this header first; each downstream service
// then independently re-verifies it (see JwtValidationFilter on the backend).
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('sa_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Normalize backend error shape (ErrorResponse: { message, details, ... })
// into a single readable string so every page can handle errors the same way.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = parseApiError(error)
    return Promise.reject({ ...error, friendlyMessage: message })
  }
)

export default client
