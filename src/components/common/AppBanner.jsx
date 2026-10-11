const VARIANT_CLASS = {
  error: 'alert-error',
  success: 'alert-success',
  info: 'alert-info',
}

/**
 * Accessible inline feedback (errors, success, info). Single presentation layer for all banners.
 */
export default function AppBanner({
  variant = 'error',
  message,
  onDismiss,
  className = '',
}) {
  if (!message) return null

  const isError = variant === 'error'

  return (
    <div
      className={`alert ${VARIANT_CLASS[variant] || VARIANT_CLASS.error} app-banner${className ? ` ${className}` : ''}`}
      role={isError ? 'alert' : 'status'}
      aria-live={isError ? 'assertive' : 'polite'}
    >
      <span className="app-banner__text">{message}</span>
      {onDismiss ? (
        <button
          type="button"
          className="app-banner__dismiss"
          onClick={onDismiss}
          aria-label="Dismiss message"
        >
          ×
        </button>
      ) : null}
    </div>
  )
}
