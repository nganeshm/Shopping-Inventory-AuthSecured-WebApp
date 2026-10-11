import VisibilityOutlined from '@mui/icons-material/VisibilityOutlined'
import VisibilityOffOutlined from '@mui/icons-material/VisibilityOffOutlined'

export default function PasswordField({
  id,
  label,
  value,
  onChange,
  autoComplete = 'current-password',
  placeholder = '••••••••',
  showPassword,
  onToggle,
  required = true,
  minLength,
}) {
  return (
    <div className="field auth-password">
      <label htmlFor={id}>{label}</label>
      <div className="auth-password-wrap">
        <input
          id={id}
          type={showPassword ? 'text' : 'password'}
          autoComplete={autoComplete}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          required={required}
          minLength={minLength}
        />
        <button
          type="button"
          className="auth-password-toggle"
          onClick={onToggle}
          aria-label={showPassword ? 'Hide password' : 'Show password'}
        >
          {showPassword ? (
            <VisibilityOffOutlined sx={{ fontSize: 20 }} />
          ) : (
            <VisibilityOutlined sx={{ fontSize: 20 }} />
          )}
        </button>
      </div>
    </div>
  )
}
