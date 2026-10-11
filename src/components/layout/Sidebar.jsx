import { NavLink, useNavigate } from 'react-router-dom'
import Inventory2Outlined from '@mui/icons-material/Inventory2Outlined'
import CategoryOutlined from '@mui/icons-material/CategoryOutlined'
import ShoppingCartOutlined from '@mui/icons-material/ShoppingCartOutlined'
import ReceiptLongOutlined from '@mui/icons-material/ReceiptLongOutlined'
import PersonOutlined from '@mui/icons-material/PersonOutlined'
import AddBoxOutlined from '@mui/icons-material/AddBoxOutlined'
import CreateNewFolderOutlined from '@mui/icons-material/CreateNewFolderOutlined'
import ProductionQuantityLimitsOutlined from '@mui/icons-material/ProductionQuantityLimitsOutlined'
import ChevronLeftRounded from '@mui/icons-material/ChevronLeftRounded'
import { useAuth } from '../../context/AuthContext'
import BrandMark from '../brand/BrandMark'

const navIconSx = { fontSize: 20 }

function NavItem({ to, end, icon: Icon, children }) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) => `sidebar-nav-item${isActive ? ' is-active' : ''}`}
    >
      <Icon className="sidebar-nav-icon" sx={navIconSx} />
      <span className="sidebar-nav-label">{children}</span>
    </NavLink>
  )
}

export default function Sidebar({ open, onClose }) {
  const { isAdmin, isAuthenticated, username } = useAuth()
  const navigate = useNavigate()

  return (
    <aside className={`sidebar${open ? '' : ' sidebar--closed'}`} aria-hidden={!open}>
      <div className="sidebar-header">
        <NavLink to="/products" className="sidebar-brand" title="Home">
          <span className="sidebar-brand-mark" aria-hidden="true">
            <BrandMark size={22} />
          </span>
          <span className="sidebar-brand-title">E-Marketplace</span>
        </NavLink>
        <button
          type="button"
          className="sidebar-collapse-btn"
          onClick={onClose}
          aria-label="Collapse sidebar"
          title="Collapse sidebar"
        >
          <ChevronLeftRounded sx={{ fontSize: 22 }} />
        </button>
      </div>

      <nav className="sidebar-nav" aria-label="Main">
        {isAuthenticated && (
          <>
            <p className="sidebar-nav-heading">Shop</p>
            <NavItem to="/products" end icon={Inventory2Outlined}>
              Products
            </NavItem>
            <NavItem to="/categories" icon={CategoryOutlined}>
              Categories
            </NavItem>
            <NavItem to="/cart" icon={ShoppingCartOutlined}>
              Cart
            </NavItem>
            <NavItem to="/orders" icon={ReceiptLongOutlined}>
              My orders
            </NavItem>
          </>
        )}

        {isAdmin && (
          <>
            <p className="sidebar-nav-heading">Admin</p>
            <NavItem to="/admin/products/new" icon={AddBoxOutlined}>
              New product
            </NavItem>
            <NavItem to="/admin/categories/new" icon={CreateNewFolderOutlined}>
              New category
            </NavItem>
            <NavItem to="/admin/low-stock" icon={ProductionQuantityLimitsOutlined}>
              Low stock
            </NavItem>
          </>
        )}
      </nav>

      {isAuthenticated ? (
        <div className="sidebar-footer">
          <NavLink
            to="/profile"
            className={({ isActive }) => `sidebar-profile${isActive ? ' is-active' : ''}`}
            title="Open profile"
            onClick={e => {
              e.preventDefault()
              navigate('/profile', { state: { refreshProfile: Date.now() } })
            }}
          >
            <span className="sidebar-profile-avatar" aria-hidden="true">
              <PersonOutlined sx={{ fontSize: 22 }} />
            </span>
            <span className="sidebar-profile-copy">
              <span className="sidebar-profile-label">Profile</span>
              <span className="sidebar-profile-name">{username || 'Account'}</span>
            </span>
          </NavLink>
        </div>
      ) : null}
    </aside>
  )
}
