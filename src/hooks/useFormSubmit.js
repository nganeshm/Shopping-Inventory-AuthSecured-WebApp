import { useCallback, useState } from 'react'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'

/**
 * Wraps async form actions with submitting state and normalized error messages.
 * @param {( ...args: unknown[]) => Promise<unknown>} submitFn
 * @param {{ errorFallback?: string }} [options]
 */
export function useFormSubmit(submitFn, { errorFallback = USER_MESSAGES.generic } = {}) {
  const [error, setError] = useState(/** @type {string | null} */ (null))
  const [submitting, setSubmitting] = useState(false)

  const clearError = useCallback(() => setError(null), [])

  const submit = useCallback(
    async (...args) => {
      setError(null)
      setSubmitting(true)
      try {
        return await submitFn(...args)
      } catch (err) {
        setError(getFriendlyErrorMessage(err, errorFallback))
        return null
      } finally {
        setSubmitting(false)
      }
    },
    [submitFn, errorFallback],
  )

  return { submit, submitting, error, setError, clearError }
}
