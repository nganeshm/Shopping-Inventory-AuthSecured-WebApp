import { useEffect, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { authApi, OTP_PURPOSE } from '../api/authApi'
import ErrorBanner from '../components/common/ErrorBanner'
import InfoBanner from '../components/common/InfoBanner'
import PasswordField from '../components/auth/PasswordField'
import OtpField from '../components/auth/OtpField'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'
import { maskEmail, savePendingAuth } from '../utils/pendingAuth'
import { trimFields } from '../utils/form'

const RESEND_SECONDS = 30

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState(null)
  const [info, setInfo] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [resending, setResending] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  useEffect(() => {
    if (!cooldown) return undefined
    const timer = setInterval(() => setCooldown(n => (n <= 1 ? 0 : n - 1)), 1000)
    return () => clearInterval(timer)
  }, [cooldown])

  const resolvedEmail = email.trim().toLowerCase()

  const handleSendCode = async e => {
    e.preventDefault()
    setError(null)
    const cleaned = trimFields({ email }, ['email']).email.toLowerCase()
    setEmail(cleaned)
    setSubmitting(true)
    try {
      await authApi.forgotPassword({ email: cleaned })
      savePendingAuth({ email: cleaned, purpose: OTP_PURPOSE.PASSWORD_RESET })
      setInfo(USER_MESSAGES.auth.forgotPasswordSent)
      setStep(2)
      setCooldown(RESEND_SECONDS)
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.forgotPasswordFailed))
    } finally {
      setSubmitting(false)
    }
  }

  const handleResend = async () => {
    if (!resolvedEmail || cooldown > 0) return
    setError(null)
    setResending(true)
    try {
      await authApi.resendOtp({ email: resolvedEmail, purpose: OTP_PURPOSE.PASSWORD_RESET })
      setInfo(USER_MESSAGES.auth.otpResent)
      setCooldown(RESEND_SECONDS)
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.resendFailed))
    } finally {
      setResending(false)
    }
  }

  const handleReset = async e => {
    e.preventDefault()
    setError(null)
    if (newPassword !== confirmPassword) {
      setError(USER_MESSAGES.auth.passwordMismatch)
      return
    }
    setSubmitting(true)
    try {
      await authApi.resetPassword({
        email: resolvedEmail,
        otp: otp.trim(),
        newPassword,
      })
      navigate('/login', {
        replace: true,
        state: { success: USER_MESSAGES.auth.passwordUpdated },
      })
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.auth.resetFailed))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-card">
      <nav className="auth-tabs" aria-label="Account">
        <NavLink to="/login" className="active">
          Log in
        </NavLink>
        <NavLink to="/register">Register</NavLink>
      </nav>

      <header className="auth-card-head">
        <h2>{step === 1 ? 'Forgot password' : 'Update password'}</h2>
        <p className="auth-lead">
          {step === 1
            ? 'Enter the email on your account. We’ll send a reset code if it’s registered.'
            : `Enter the code sent to ${maskEmail(resolvedEmail)} and choose a new password.`}
        </p>
      </header>

      <InfoBanner message={info} />
      <ErrorBanner message={error} />

      {step === 1 ? (
        <form onSubmit={handleSendCode} className="auth-form">
          <div className="field">
            <label htmlFor="forgot-email">Email</label>
            <input
              id="forgot-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>
          <button className="btn auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Sending…' : 'Send code'}
          </button>
        </form>
      ) : (
        <form onSubmit={handleReset} className="auth-form">
          <OtpField value={otp} onChange={setOtp} disabled={submitting} />
          <PasswordField
            id="new-password"
            label="New password"
            value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
            autoComplete="new-password"
            showPassword={showPassword}
            onToggle={() => setShowPassword(v => !v)}
            minLength={6}
          />
          <PasswordField
            id="confirm-password"
            label="Confirm password"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            autoComplete="new-password"
            showPassword={showPassword}
            onToggle={() => setShowPassword(v => !v)}
            minLength={6}
          />
          <button className="btn auth-submit" type="submit" disabled={submitting || otp.length < 6}>
            {submitting ? 'Updating…' : 'Update password'}
          </button>
        </form>
      )}

      <div className="auth-secondary">
        {step === 2 ? (
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={handleResend}
            disabled={resending || cooldown > 0}
          >
            {cooldown > 0 ? `Resend code in ${cooldown}s` : resending ? 'Sending…' : 'Resend code'}
          </button>
        ) : (
          <span />
        )}
        <NavLink to="/login">Back to log in</NavLink>
      </div>
    </div>
  )
}
