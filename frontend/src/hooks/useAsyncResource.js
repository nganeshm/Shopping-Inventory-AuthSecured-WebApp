import { useEffect, useState } from 'react'
import { getFriendlyErrorMessage, isAbortError } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'

/**
 * Load remote data on mount / when deps change — loading, error, and data in one place.
 * @param {() => Promise<T>} loader
 * @param {object} options
 * @param {React.DependencyList} options.deps
 * @param {string} [options.errorFallback]
 * @param {boolean} [options.enabled=true]
 * @template T
 */
export function useAsyncResource(loader, { deps, errorFallback = USER_MESSAGES.generic, enabled = true } = {}) {
  const [data, setData] = useState(/** @type {T | null} */ (null))
  const [loading, setLoading] = useState(Boolean(enabled))
  const [error, setError] = useState(/** @type {string | null} */ (null))

  useEffect(() => {
    if (!enabled) {
      setLoading(false)
      return undefined
    }

    let cancelled = false
    setLoading(true)
    setError(null)

    loader()
      .then(result => {
        if (!cancelled) setData(result)
      })
      .catch(err => {
        if (cancelled || isAbortError(err)) return
        setError(getFriendlyErrorMessage(err, errorFallback))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- caller owns dep list
  }, deps ?? [])

  return { data, loading, error, setError, setData }
}
