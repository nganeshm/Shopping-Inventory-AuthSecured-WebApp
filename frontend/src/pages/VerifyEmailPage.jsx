import { useEffect, useState } from 'react'
import { NavLink, useLocation, useNavigate } from 'react-router-dom'
import { authApi, OTP_PURPOSE } from '../api/authApi'
import ErrorBanner from '../components/common/ErrorBanner'
import InfoBanner from '../components/common/InfoBanner'
import OtpField from '../components/auth/OtpField'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'
import { maskEmail, readPendingAuth, savePendingAuth } from '../utils/pendingAuth'

const RESEND_SECONDS = 30

export default function VerifyEmailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const pending = readPendingAuth()
  const [email, setEmail] = useState(
    () => location.state?.email || pending?.email || '',
  )
  const [otp, setOtp] = useState('')
  const [error, setError] = useState(null)
  const [info, setInfo] = useState(location.state?.notice || null)
  const [submitting, setSubmitting] = useState(false)
  const [resending, setResending] = useState(false)
  const [cooldown, setCooldown] = useState(location.state?.email || pending?.email ? RESEND_SECONDS : 0)

  useEffect(() => {
    if (!cooldown) return undefined
    const timer = setInterval(() => setCooldown(n => (n <= 1 ? 0 : n - 1)), 1000)
    return () => clearInterval(timer)
  }, [cooldown])

  const resolvedEmail = email.trim().toLowerCase()

  const handleVerify = async e => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await authApi.verifyEmail({ email: resolvedEmail, otp: otp.trim() })
      savePendingAuth({ email: resolvedEmail, purpose: OTP_PURPOSE.REGISTRATION })
      navigate('/login', {
        replace: true,
        state: { success: USER_MESSAGES.auth.emailVerified },
      })
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.verifyFailed))
    } finally {
      setSubmitting(false)
    }
  }

  const handleResend = async () => {
    if (!resolvedEmail || cooldown > 0) return
    setError(null)
    setResending(true)
    try {
      await authApi.resendOtp({ email: resolvedEmail, purpose: OTP_PURPOSE.REGISTRATION })
      savePendingAuth({ email: resolvedEmail, purpose: OTP_PURPOSE.REGISTRATION })
      setInfo(USER_MESSAGES.auth.otpResent)
      setCooldown(RESEND_SECONDS)
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.resendFailed))
    } finally {
      setResending(false)
    }
  }

  return (
    <div className="auth-card">
      <nav className="auth-tabs" aria-label="Account">
        <NavLink to="/login">Log in</NavLink>
        <NavLink to="/register" className="active">
          Register
        </NavLink>
      </nav>

      <header className="auth-card-head">
        <h2>Verify your email</h2>
        <p className="auth-lead">
          {resolvedEmail
            ? `Enter the 6-digit code sent to ${maskEmail(resolvedEmail)}.`
            : 'Enter the email you registered with, then the code we sent.'}
        </p>
      </header>

      <InfoBanner message={info} />
      <ErrorBanner message={error} />

      <form onSubmit={handleVerify} className="auth-form">
        {!location.state?.email && !pending?.email ? (
          <div className="field">
            <label htmlFor="verify-email">Email</label>
            <input
              id="verify-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>
        ) : null}
        <OtpField value={otp} onChange={setOtp} disabled={submitting} />
        <button className="btn auth-submit" type="submit" disabled={submitting || otp.length < 6}>
          {submitting ? 'Verifying…' : 'Verify'}
        </button>
      </form>

      <div className="auth-secondary">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          onClick={handleResend}
          disabled={resending || cooldown > 0 || !resolvedEmail}
        >
          {cooldown > 0 ? `Resend code in ${cooldown}s` : resending ? 'Sending…' : 'Resend code'}
        </button>
        <NavLink to="/login">Back to log in</NavLink>
      </div>
    </div>
  )
}
