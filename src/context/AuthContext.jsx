import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { authApi } from '../api/authApi'
import { cacheCustomerProfile, customerApi } from '../api/customerApi'
import { decodeJwt, isExpired } from '../utils/jwt'
import { clearPendingAuth, readPendingAuth } from '../utils/pendingAuth'
import { clearSession, readSession, saveSession, sessionEmail } from '../utils/session'

const AuthContext = createContext(null)

function claimsToSession(token, claims) {
  if (!token || !claims) return null
  return {
    token,
    username: claims.sub || null,
    email: claims.email || null,
    roles: claims.roles || [],
    exp: claims.exp || null,
  }
}

function persistSession(token, claims) {
  const details = claimsToSession(token, claims)
  if (details) saveSession(details)
  return details
}

/**
 * JWT in localStorage (axios Bearer header) plus decoded token details in
 * sessionStorage (username, email, roles) for profile lookup by email.
 */
function readStoredAuth() {
  const token = localStorage.getItem('sa_token')
  if (!token) return { token: null, claims: null }
  const claims = decodeJwt(token)
  if (!claims || isExpired(claims)) {
    localStorage.removeItem('sa_token')
    clearSession()
    return { token: null, claims: null }
  }
  if (!readSession()?.email && claims.email) {
    persistSession(token, claims)
  }
  return { token, claims }
}

export function AuthProvider({ children }) {
  const [{ token, claims }, setAuth] = useState(readStoredAuth)
  const [customerId, setCustomerId] = useState(() => {
    const username = readStoredAuth().claims?.sub
    return username ? localStorage.getItem(`sa_customerId_${username}`) : null
  })

  useEffect(() => {
    // Keep state in sync if the token is cleared/expired between renders.
    if (token && isExpired(claims)) {
      logout()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(async (username, password) => {
    const { data } = await authApi.login({ username, password })
    localStorage.setItem('sa_token', data.token)
    const decoded = decodeJwt(data.token)
    setAuth({ token: data.token, claims: decoded })
    persistSession(data.token, decoded)

    const storedCustomerId = localStorage.getItem(`sa_customerId_${username}`)
    const pending = readPendingAuth()
    if (pending?.profile && pending.username === username) {
      try {
        const { data: customer } = await customerApi.register(pending.profile)
        localStorage.setItem(`sa_customerId_${username}`, String(customer.id))
        cacheCustomerProfile(username, customer)
        setCustomerId(String(customer.id))
      } catch {
        const storedCustomerId = localStorage.getItem(`sa_customerId_${username}`)
        setCustomerId(storedCustomerId)
      }
      clearPendingAuth()
      return decoded
    }

    setCustomerId(storedCustomerId)
    return decoded
  }, [])

  const register = useCallback(async (payload) => {
    await authApi.register(payload)
    return { username: payload.username, email: payload.email }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('sa_token')
    clearSession()
    setAuth({ token: null, claims: null })
    setCustomerId(null)
  }, [])

  const linkCustomerId = useCallback((id) => {
    if (claims?.sub) {
      localStorage.setItem(`sa_customerId_${claims.sub}`, String(id))
    }
    setCustomerId(String(id))
  }, [claims])

  const refreshCustomerProfile = useCallback(async () => {
    const username = claims?.sub
    const email = claims?.email || sessionEmail()
    const { data } = await customerApi.getDetailsByEmail(email, { username })
    if (data?.id) {
      if (username) localStorage.setItem(`sa_customerId_${username}`, String(data.id))
      setCustomerId(String(data.id))
    }
    return data
  }, [claims])

  const roles = claims?.roles || []

  const value = {
    token,
    username: claims?.sub || null,
    email: claims?.email || sessionEmail(),
    roles,
    isAdmin: roles.includes('ROLE_ADMIN'),
    isAuthenticated: !!token,
    customerId,
    linkCustomerId,
    refreshCustomerProfile,
    login,
    register,
    logout
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
