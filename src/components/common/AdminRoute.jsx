import { Navigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

/**
 * Mirrors the backend's @PreAuthorize("hasRole('ADMIN')") on the UI side —
 * a convenience that hides admin-only screens from non-admins. It is NOT
 * the real security boundary: the backend re-checks the role on every
 * request regardless of what this component does (defense in depth, same
 * principle as the gateway + per-service JWT checks).
 */
export default function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (!isAdmin) return <Navigate to="/products" replace />
  return children
}
