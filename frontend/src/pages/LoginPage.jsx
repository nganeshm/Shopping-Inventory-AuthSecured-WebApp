import { useState } from 'react'
import { useNavigate, useLocation, NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { isEmailUnverifiedError } from '../api/authApi'
import ErrorBanner from '../components/common/ErrorBanner'
import SuccessBanner from '../components/common/SuccessBanner'
import PasswordField from '../components/auth/PasswordField'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'
import { trimFields } from '../utils/form'
import { readPendingAuth } from '../utils/pendingAuth'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(location.state?.success || null)
  const [submitting, setSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async e => {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    setSubmitting(true)
    try {
      const { username, password } = trimFields(form, ['username'])
      setForm({ username, password })
      await login(username, password)
      const redirectTo = location.state?.from?.pathname || '/products'
      navigate(redirectTo, { replace: true })
    } catch (err) {
      if (isEmailUnverifiedError(err)) {
        const pending = readPendingAuth()
        navigate('/verify-email', {
          replace: true,
          state: {
            email: pending?.email || '',
            notice: USER_MESSAGES.auth.verifyEmailRequired,
          },
        })
        return
      }
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.loginFailed))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-card">
      <nav className="auth-tabs" aria-label="Account">
        <NavLink to="/login" className={({ isActive }) => (isActive ? 'active' : '')} end>
          Log in
        </NavLink>
        <NavLink to="/register" className={({ isActive }) => (isActive ? 'active' : '')}>
          Register
        </NavLink>
      </nav>

      <header className="auth-card-head">
        <h2>Welcome back</h2>
        <p className="auth-lead">Sign in to shop the catalog and manage your orders.</p>
      </header>
      <SuccessBanner message={success} />
      <ErrorBanner message={error} />

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="field">
          <label htmlFor="username">Username</label>
          <input
            id="username"
            autoComplete="username"
            placeholder="Your username"
            value={form.username}
            onChange={e => setForm({ ...form, username: e.target.value })}
            required
          />
        </div>
        <PasswordField
          id="password"
          label="Password"
          value={form.password}
          onChange={e => setForm({ ...form, password: e.target.value })}
          showPassword={showPassword}
          onToggle={() => setShowPassword(v => !v)}
        />
        <div className="auth-forgot-row">
          <NavLink to="/forgot-password" className="auth-forgot-link">
            Forgot password?
          </NavLink>
        </div>
        <button className="btn auth-submit" type="submit" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}
