import { useState } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import ShoppingCartOutlined from '@mui/icons-material/ShoppingCartOutlined'
import LocalMallOutlined from '@mui/icons-material/LocalMallOutlined'
import Inventory2Outlined from '@mui/icons-material/Inventory2Outlined'
import { useAuth } from '../../context/AuthContext'
import BrandMark from '../brand/BrandMark'
import '../../styles/auth.css'

const SHOP_FLOATS = [
  { id: 'cart-1', type: 'cart', tone: 'coral' },
  { id: 'bag-1', type: 'bag', tone: 'gold' },
  { id: 'crate-1', type: 'crate', tone: 'mint' },
  { id: 'bag-2', type: 'bag', tone: 'sky' },
]

function ShopIcon({ type }) {
  const sx = { fontSize: 26 }
  if (type === 'cart') return <ShoppingCartOutlined sx={sx} />
  if (type === 'bag') return <LocalMallOutlined sx={sx} />
  return <Inventory2Outlined sx={sx} />
}

function ShoppingBackdrop() {
  return (
    <div className="shop-backdrop" aria-hidden="true">
      <div className="auth-aurora" />
      <div className="auth-orb auth-orb-a" />
      <div className="auth-orb auth-orb-b" />
      {SHOP_FLOATS.map(item => (
        <span key={item.id} className={`shop-float shop-float-${item.id} shop-tone-${item.tone}`}>
          <ShopIcon type={item.type} />
        </span>
      ))}
    </div>
  )
}

export default function AuthLayout() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const [alreadySignedIn] = useState(() => isAuthenticated)
  const path = location.pathname
  const isRegister = path === '/register' || path === '/verify-email'
  const isForgot = path === '/forgot-password'

  if (alreadySignedIn) {
    return <Navigate to="/products" replace />
  }

  return (
    <div className="auth-stage">
      <ShoppingBackdrop />

      <div className="auth-frame">
        <aside className={`auth-hero ${isRegister ? 'is-register' : 'is-login'}`}>
          <div className="auth-brand-mark">
            <span className="auth-ring" />
            <span className="auth-ring delay" />
            <BrandMark size={32} className="auth-mark" />
          </div>
          <p className="auth-kicker">E-Marketplace</p>
          <h1 key={path} className="auth-hero-title">
            {isForgot
              ? 'Reset access in a minute.'
              : isRegister
                ? 'Start shopping in minutes.'
                : 'Your store is waiting.'}
          </h1>
          <p key={`${path}-copy`} className="auth-hero-copy">
            {isForgot
              ? 'We will email a one-time code so you can set a new password and sign back in.'
              : isRegister
                ? 'Create an account, verify your email, then browse the catalog and check out.'
                : 'Sign in to pick up your cart, track orders, and manage your profile.'}
          </p>
          <ul className="auth-signals">
            <li>Catalog</li>
            <li>Secure checkout</li>
            <li>Order history</li>
          </ul>
        </aside>

        <section className="auth-card-wrap">
          <Outlet />
        </section>
      </div>
    </div>
  )
}
