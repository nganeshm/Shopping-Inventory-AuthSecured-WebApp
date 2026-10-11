const STORAGE_KEY = 'sa_session'

export function saveSession(details) {
  if (!details) return
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(details))
  } catch {
    /* ignore quota / private mode */
  }
}

export function readSession() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function clearSession() {
  sessionStorage.removeItem(STORAGE_KEY)
}

export function sessionEmail() {
  return readSession()?.email || null
}
