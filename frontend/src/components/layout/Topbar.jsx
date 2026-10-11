import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import MenuRounded from '@mui/icons-material/MenuRounded'
import PersonOutlined from '@mui/icons-material/PersonOutlined'
import ShoppingCartRounded from '@mui/icons-material/ShoppingCartRounded'
import LogoutRounded from '@mui/icons-material/LogoutRounded'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import ConfirmDialog from '../common/ConfirmDialog'
import BrandMark from '../brand/BrandMark'

export default function Topbar({ sidebarOpen, onToggleSidebar }) {
  const { isAuthenticated, logout } = useAuth()
  const { totalItems } = useCart()
  const navigate = useNavigate()
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false)

  const handleLogoutConfirm = () => {
    setLogoutConfirmOpen(false)
    logout()
    navigate('/login')
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-start">
          <button
            type="button"
            className="sidebar-toggle-btn"
            onClick={onToggleSidebar}
            aria-label={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
            aria-expanded={sidebarOpen}
            title={sidebarOpen ? 'Close sidebar' : 'Open menu'}
          >
            <MenuRounded sx={{ fontSize: 22 }} />
          </button>

          {isAuthenticated ? (
            <Link to="/products" className="topbar-brand" title="E-Marketplace home">
              <span className="topbar-brand-mark">
                <BrandMark size={28} />
              </span>
              <span className="topbar-brand-name">E-Marketplace</span>
            </Link>
          ) : null}
        </div>

        {isAuthenticated ? (
          <div className="topbar-actions">
            <button
              type="button"
              className="topbar-icon-btn"
              onClick={() => navigate('/profile', { state: { refreshProfile: Date.now() } })}
              aria-label="Open profile"
              title="Profile"
            >
              <PersonOutlined sx={{ fontSize: 22 }} />
            </button>
            <Link to="/cart" className="topbar-icon-btn" aria-label="Open cart" title="Cart">
              <ShoppingCartRounded sx={{ fontSize: 22 }} />
              {totalItems > 0 ? <span className="topbar-icon-badge">{totalItems}</span> : null}
            </Link>
            <button
              type="button"
              className="topbar-icon-btn topbar-icon-btn--logout"
              onClick={() => setLogoutConfirmOpen(true)}
              aria-label="Log out"
              title="Log out"
            >
              <LogoutRounded sx={{ fontSize: 22 }} />
            </button>
          </div>
        ) : (
          <span />
        )}
      </header>

      <ConfirmDialog
        open={logoutConfirmOpen}
        title="Log out?"
        message="You will need to sign in again to access your cart, orders, and profile. Do you want to log out now?"
        confirmLabel="Log out"
        cancelLabel="Stay signed in"
        confirmVariant="danger"
        onConfirm={handleLogoutConfirm}
        onCancel={() => setLogoutConfirmOpen(false)}
      />
    </>
  )
}
