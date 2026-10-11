export default function OtpField({
  id = 'otp',
  label = 'Verification code',
  value,
  onChange,
  disabled = false,
}) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        className="auth-otp-input"
        inputMode="numeric"
        autoComplete="one-time-code"
        pattern="[0-9]{6}"
        maxLength={6}
        placeholder="000000"
        value={value}
        onChange={e => onChange(e.target.value.replace(/\D/g, '').slice(0, 6))}
        required
        disabled={disabled}
      />
    </div>
  )
}
