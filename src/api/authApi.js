import axios from 'axios'
import { parseApiError } from '../utils/errors'
import { API_BASE_URL } from './client'

/** Public auth POSTs go through the gateway (:8080 → auth-service :8081). */
const publicAuth = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

publicAuth.interceptors.response.use(
  response => response,
  error => Promise.reject({ ...error, friendlyMessage: parseApiError(error) }),
)

export const OTP_PURPOSE = {
  REGISTRATION: 'REGISTRATION',
  PASSWORD_RESET: 'PASSWORD_RESET',
}

export const authApi = {
  register: payload => publicAuth.post('/api/auth/register', payload),
  login: payload => publicAuth.post('/api/auth/login', payload),
  verifyEmail: payload => publicAuth.post('/api/auth/verify-email', payload),
  resendOtp: payload => publicAuth.post('/api/auth/resend-otp', payload),
  forgotPassword: payload => publicAuth.post('/api/auth/forgot-password', payload),
  resetPassword: payload => publicAuth.post('/api/auth/reset-password', payload),
}

export function isEmailUnverifiedError(error) {
  return error?.response?.status === 403
}
