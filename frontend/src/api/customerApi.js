import client from './client'
import { USER_MESSAGES } from '../constants/userMessages'

function profileCacheKey(username) {
  return username ? `sa_customer_profile_${username}` : null
}

export function cacheCustomerProfile(username, customer) {
  const key = profileCacheKey(username)
  if (!key || !customer) return
  try {
    localStorage.setItem(key, JSON.stringify(customer))
  } catch {
    /* ignore */
  }
}

export function readCachedCustomerProfile(username) {
  const key = profileCacheKey(username)
  if (!key) return null
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function looksLikeCustomer(data) {
  return Boolean(data && typeof data === 'object' && (data.id || data.email || data.firstName))
}

const detailsByEmailInflight = new Map()

export const customerApi = {
  register: payload => client.post('/api/customers/register', payload),

  /** GET /api/customers/{id} — cart / orders, not the profile click. */
  getById: async (id, { username } = {}) => {
    const response = await client.get(`/api/customers/${id}`)
    if (username && looksLikeCustomer(response.data)) {
      cacheCustomerProfile(username, response.data)
    }
    return response
  },

  /** GET /api/customers/details/by-email?email= — one in-flight call per email. */
  getDetailsByEmail: (email, { username } = {}) => {
    if (!email) {
      const error = new Error('email is required')
      error.friendlyMessage = USER_MESSAGES.profile.missingEmail
      return Promise.reject(error)
    }

    const key = String(email).trim().toLowerCase()
    const pending = detailsByEmailInflight.get(key)
    if (pending) return pending

    const request = client
      .get('/api/customers/details/by-email', { params: { email } })
      .then(response => {
        if (username && looksLikeCustomer(response.data)) {
          cacheCustomerProfile(username, response.data)
        }
        return response
      })
      .finally(() => {
        detailsByEmailInflight.delete(key)
      })

    detailsByEmailInflight.set(key, request)
    return request
  },

  addAddress: (id, payload) => client.post(`/api/customers/${id}/addresses`, payload),
}
