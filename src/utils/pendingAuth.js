const STORAGE_KEY = 'sa_pending_auth'

export function savePendingAuth(next) {
  const current = readPendingAuth() || {}
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ ...current, ...next }))
  } catch {
    /* ignore quota / private mode */
  }
}

export function readPendingAuth() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function clearPendingAuth() {
  sessionStorage.removeItem(STORAGE_KEY)
}

export function maskEmail(email) {
  if (!email || !email.includes('@')) return email || ''
  const [name, domain] = email.split('@')
  const visible = name.slice(0, 1)
  return `${visible}${'•'.repeat(Math.max(name.length - 1, 2))}@${domain}`
}
