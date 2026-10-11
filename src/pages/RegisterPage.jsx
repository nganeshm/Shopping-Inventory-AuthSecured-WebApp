import { useState } from 'react'
import { useNavigate, NavLink } from 'react-router-dom'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useAuth } from '../context/AuthContext'
import ErrorBanner from '../components/common/ErrorBanner'
import PasswordField from '../components/auth/PasswordField'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'
import { trimFields } from '../utils/form'
import { savePendingAuth } from '../utils/pendingAuth'
import { OTP_PURPOSE } from '../api/authApi'
import { COUNTRIES } from '../constants/countries'

const TRIM_KEYS = [
  'username', 'email', 'firstName', 'lastName', 'primaryPhone',
  'line1', 'city', 'state', 'postalCode', 'country',
]

const initialForm = {
  username: '',
  password: '',
  email: '',
  firstName: '',
  lastName: '',
  line1: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
  primaryPhone: '',
}

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [step, setStep] = useState(1)

  const update = key => e => setForm({ ...form, [key]: e.target.value })

  const handleAccountNext = e => {
    e.preventDefault()
    setError(null)
    setForm(trimFields(form, TRIM_KEYS))
    setStep(2)
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    const cleaned = trimFields(form, TRIM_KEYS)
    setForm(cleaned)
    try {
      await register({
        username: cleaned.username,
        email: cleaned.email,
        password: cleaned.password,
        firstName: cleaned.firstName,
        lastName: cleaned.lastName,
        address: {
          addressType: 'HOME',
          line1: cleaned.line1,
          city: cleaned.city,
          state: cleaned.state,
          postalCode: cleaned.postalCode,
          country: cleaned.country,
        },
        contact: {
          primaryPhone: cleaned.primaryPhone,
          preferredContactMethod: 'EMAIL',
        },
      })
      savePendingAuth({
        email: cleaned.email,
        username: cleaned.username,
        purpose: OTP_PURPOSE.REGISTRATION,
        profile: {
          firstName: cleaned.firstName,
          lastName: cleaned.lastName,
          email: cleaned.email,
          primaryAddress: {
            addressType: 'HOME',
            line1: cleaned.line1,
            city: cleaned.city,
            state: cleaned.state,
            postalCode: cleaned.postalCode,
            country: cleaned.country,
            defaultAddress: true,
          },
          contactDetail: {
            primaryPhone: cleaned.primaryPhone,
            preferredContactMethod: 'EMAIL',
          },
        },
      })
      navigate('/verify-email', { replace: true, state: { email: cleaned.email } })
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.registerFailed))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-card auth-card--register">
      <nav className="auth-tabs" aria-label="Account">
        <NavLink to="/login" className={({ isActive }) => (isActive ? 'active' : '')}>
          Log in
        </NavLink>
        <NavLink to="/register" className={({ isActive }) => (isActive ? 'active' : '')} end>
          Register
        </NavLink>
      </nav>

      <header className="auth-card-head">
        <h2>Create account</h2>
        <p className="auth-lead">Create your login, then verify the code we send to your email.</p>
        <ol className="auth-steps" aria-label="Registration progress">
          <li className={step === 1 ? 'is-current' : 'is-done'}>
            <span>1</span> Account
          </li>
          <li className={step === 2 ? 'is-current' : ''}>
            <span>2</span> Address
          </li>
          <li>
            <span>3</span> Verify
          </li>
        </ol>
      </header>

      <ErrorBanner message={error} />

      {step === 1 ? (
        <form onSubmit={handleAccountNext} className="auth-form">
          <div className="field-row">
            <div className="field">
              <label htmlFor="reg-username">Username</label>
              <input
                id="reg-username"
                autoComplete="username"
                value={form.username}
                onChange={update('username')}
                required
              />
            </div>
            <PasswordField
              id="reg-password"
              label="Password"
              value={form.password}
              onChange={update('password')}
              autoComplete="new-password"
              showPassword={showPassword}
              onToggle={() => setShowPassword(v => !v)}
              minLength={6}
            />
          </div>
          <div className="field">
            <label htmlFor="reg-email">Email</label>
            <input
              id="reg-email"
              type="email"
              autoComplete="email"
              value={form.email}
              onChange={update('email')}
              required
            />
          </div>
          <div className="field-row">
            <div className="field">
              <label htmlFor="reg-first">First name</label>
              <input
                id="reg-first"
                autoComplete="given-name"
                value={form.firstName}
                onChange={update('firstName')}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="reg-last">Last name</label>
              <input
                id="reg-last"
                autoComplete="family-name"
                value={form.lastName}
                onChange={update('lastName')}
                required
              />
            </div>
          </div>
          <div className="field">
            <label htmlFor="reg-phone">Phone</label>
            <input
              id="reg-phone"
              autoComplete="tel"
              value={form.primaryPhone}
              onChange={update('primaryPhone')}
              required
              placeholder="+91 98765 43210"
            />
          </div>
          <button className="btn auth-submit" type="submit">
            Continue
          </button>
        </form>
      ) : (
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="field">
            <label htmlFor="reg-line1">Street address</label>
            <input
              id="reg-line1"
              autoComplete="address-line1"
              value={form.line1}
              onChange={update('line1')}
              required
            />
          </div>
          <div className="field-row">
            <div className="field">
              <label htmlFor="reg-city">City</label>
              <input
                id="reg-city"
                autoComplete="address-level2"
                value={form.city}
                onChange={update('city')}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="reg-state">State</label>
              <input
                id="reg-state"
                autoComplete="address-level1"
                value={form.state}
                onChange={update('state')}
                required
              />
            </div>
          </div>
          <div className="field-row">
            <div className="field">
              <label htmlFor="reg-postal">Postal code</label>
              <input
                id="reg-postal"
                autoComplete="postal-code"
                value={form.postalCode}
                onChange={update('postalCode')}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="reg-country">Country</label>
              <select
                id="reg-country"
                autoComplete="country-name"
                value={form.country}
                onChange={update('country')}
                required
              >
                <option value="">Select country</option>
                {COUNTRIES.map(name => (
                  <option key={name} value={name}>
                    {name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="auth-form-actions">
            <button
              type="button"
              className="btn btn-ghost auth-back"
              onClick={() => setStep(1)}
              disabled={submitting}
            >
              Back
            </button>
            <button className="btn auth-submit" type="submit" disabled={submitting}>
              {submitting ? (
                <>
                  <CircularProgress size={18} thickness={5} sx={{ color: '#071018' }} />
                  Sending code…
                </>
              ) : (
                'Create account'
              )}
            </button>
          </div>
        </form>
      )}

      <Backdrop
        open={submitting}
        sx={{
          position: 'absolute',
          inset: 0,
          zIndex: 3,
          borderRadius: '20px',
          backgroundColor: 'rgba(7, 10, 24, 0.55)',
        }}
      >
        <CircularProgress sx={{ color: '#5ef0d2' }} />
      </Backdrop>
    </div>
  )
}
