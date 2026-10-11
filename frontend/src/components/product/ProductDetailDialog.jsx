import { useEffect, useState } from 'react'
import CloseRounded from '@mui/icons-material/CloseRounded'
import ShoppingCartOutlined from '@mui/icons-material/ShoppingCartOutlined'
import Inventory2Outlined from '@mui/icons-material/Inventory2Outlined'
import CategoryOutlined from '@mui/icons-material/CategoryOutlined'
import WarehouseOutlined from '@mui/icons-material/WarehouseOutlined'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import { formatINR } from '../../utils/currency'

function productInitials(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/).slice(0, 2)
  return parts.map(w => w[0]?.toUpperCase() ?? '').join('') || '?'
}

export default function ProductDetailDialog({ product, onClose }) {
  const { isAdmin } = useAuth()
  const { addItem } = useCart()
  const available = product.inventory?.quantityAvailable ?? 0
  const outOfStock = available <= 0
  const lowStock = Boolean(product.inventory?.lowStock)
  const [qty, setQty] = useState(1)
  const [added, setAdded] = useState(false)

  useEffect(() => {
    const onKeyDown = e => {
      if (e.key === 'Escape') onClose?.()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  useEffect(() => {
    setQty(1)
    setAdded(false)
  }, [product.id])

  const handleAdd = () => {
    addItem(product, qty)
    setAdded(true)
  }

  const stockLabel = outOfStock
    ? 'Out of stock'
    : lowStock
      ? `Low stock · ${available} left`
      : `${available} in stock`

  return (
    <div className="product-dialog-backdrop" role="presentation" onClick={onClose}>
      <div
        className="product-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-dialog-title"
        onClick={e => e.stopPropagation()}
      >
        <button type="button" className="product-dialog-close" onClick={onClose} aria-label="Close">
          <CloseRounded sx={{ fontSize: 22 }} />
        </button>

        <div className="product-dialog-media">
          <span className="product-dialog-initials">{productInitials(product.name)}</span>
        </div>

        <div className="product-dialog-body">
          <div className="product-dialog-meta">
            <span className="category-tag">{product.categoryName || 'General'}</span>
            {isAdmin ? (
              <span className={`badge ${outOfStock ? 'badge-danger' : lowStock ? 'badge-amber' : 'badge-success'}`}>
                {stockLabel}
              </span>
            ) : null}
          </div>

          <h2 id="product-dialog-title">{product.name}</h2>
          <p className="product-dialog-desc">
            {product.description || 'No description provided for this product.'}
          </p>

          {isAdmin ? (
            <dl className="product-dialog-specs">
              <div>
                <dt>
                  <Inventory2Outlined sx={{ fontSize: 16 }} /> SKU
                </dt>
                <dd>{product.sku || '—'}</dd>
              </div>
              <div>
                <dt>
                  <CategoryOutlined sx={{ fontSize: 16 }} /> Category
                </dt>
                <dd>{product.categoryName || 'General'}</dd>
              </div>
              <div>
                <dt>
                  <WarehouseOutlined sx={{ fontSize: 16 }} /> Warehouse
                </dt>
                <dd>{product.inventory?.warehouseLocation || '—'}</dd>
              </div>
            </dl>
          ) : null}

          <div className="product-dialog-footer">
            <span className="price product-dialog-price">{formatINR(product.price)}</span>
            <div className="product-dialog-actions">
              <label className="product-dialog-qty">
                <span>Qty</span>
                <input
                  type="number"
                  min={1}
                  max={Math.max(available, 1)}
                  value={qty}
                  disabled={outOfStock}
                  onChange={e => setQty(Math.max(1, Number(e.target.value) || 1))}
                />
              </label>
              <button
                type="button"
                className="btn btn-primary"
                disabled={outOfStock}
                onClick={handleAdd}
              >
                <ShoppingCartOutlined sx={{ fontSize: 18 }} />
                {outOfStock ? 'Unavailable' : added ? 'Added' : 'Add to cart'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
