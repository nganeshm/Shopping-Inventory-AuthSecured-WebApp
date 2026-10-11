import { USER_MESSAGES } from '../constants/userMessages'

/**
 * Normalize axios / gateway ErrorResponse into one readable string.
 * @param {unknown} error
 */
export function parseApiError(error) {
  if (!error || typeof error !== 'object') {
    return USER_MESSAGES.generic
  }

  if ('friendlyMessage' in error && typeof error.friendlyMessage === 'string') {
    return isJavaParameterBindingError(error.friendlyMessage)
      ? USER_MESSAGES.generic
      : error.friendlyMessage
  }

  const axiosError = /** @type {{ response?: { data?: { message?: string; details?: string[] } }; message?: string; code?: string }} */ (
    error
  )

  if (axiosError.code === 'ERR_NETWORK') {
    return USER_MESSAGES.network
  }

  const data = axiosError.response?.data
  const raw =
    (data?.details?.length && data.details.join(' ')) ||
    (typeof data?.message === 'string' && data.message.trim()) ||
    ''

  if (isJavaParameterBindingError(raw)) {
    return USER_MESSAGES.generic
  }
  if (raw) {
    return raw
  }
  if (typeof axiosError.message === 'string' && axiosError.message.trim()) {
    return isJavaParameterBindingError(axiosError.message)
      ? USER_MESSAGES.generic
      : axiosError.message
  }

  return USER_MESSAGES.generic
}

/**
 * @param {unknown} error
 * @param {string} [fallback=USER_MESSAGES.generic]
 */
export function getFriendlyErrorMessage(error, fallback = USER_MESSAGES.generic) {
  const parsed = parseApiError(error)
  if (parsed === USER_MESSAGES.generic && fallback) {
    return fallback
  }
  return parsed
}

/** Backend built without javac -parameters; path/query names cannot be bound. */
export function isJavaParameterBindingError(message) {
  if (typeof message !== 'string') return false
  return (
    message.includes('-parameters') ||
    message.includes('parameter name information not available') ||
    message.includes('Name for argument of type')
  )
}

/** @param {unknown} error */
export function isAbortError(error) {
  if (!error || typeof error !== 'object') return false
  const code = /** @type {{ code?: string; name?: string }} */ (error).code
  const name = /** @type {{ name?: string }} */ (error).name
  return code === 'ERR_CANCELED' || name === 'AbortError' || name === 'CanceledError'
}
