// Minimal JWT payload decoder — we only need to READ the claims client-side
// (username, roles, expiry) to drive the UI; the actual trust decision
// (signature verification) always happens on the backend, never here.
export function decodeJwt(token) {
  try {
    const payloadBase64 = token.split('.')[1]
    const normalized = payloadBase64.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function isExpired(claims) {
  if (!claims?.exp) return true
  return Date.now() >= claims.exp * 1000
}
