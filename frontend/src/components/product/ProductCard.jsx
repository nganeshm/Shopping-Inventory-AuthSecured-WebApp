import { useEffect, useRef, useState } from 'react'
import AddShoppingCartRounded from '@mui/icons-material/AddShoppingCartRounded'
import CheckRounded from '@mui/icons-material/CheckRounded'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import { formatINR } from '../../utils/currency'

const STOCK_LABEL = {
  out: 'Out of stock',
  low: 'Low stock',
  in: 'In stock',
}

function productInitials(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/).slice(0, 2)
  return parts.map(w => w[0]?.toUpperCase() ?? '').join('') || '?'
}

export default function ProductCard({ product, onOpen }) {
  const { isAdmin } = useAuth()
  const { addItem } = useCart()
  const [added, setAdded] = useState(false)
  const addedTimer = useRef(null)
  const lowStock = product.inventory?.lowStock
  const outOfStock = (product.inventory?.quantityAvailable ?? 0) <= 0
  const stockKey = outOfStock ? 'out' : lowStock ? 'low' : 'in'
  const cardTone = isAdmin ? stockKey : 'shopper'

  const handleAddToCart = event => {
    event.preventDefault()
    event.stopPropagation()
    if (outOfStock) return
    addItem(product, 1)
    setAdded(true)
    window.clearTimeout(addedTimer.current)
    addedTimer.current = window.setTimeout(() => setAdded(false), 1200)
  }

  useEffect(() => () => window.clearTimeout(addedTimer.current), [])

  return (
    <article className={`product-card product-card--${cardTone}`}>
      <button type="button" className="product-card-hit" onClick={() => onOpen?.(product)}>
        <span className="product-card-media" aria-hidden="true">
          <span className="product-card-initials">{productInitials(product.name)}</span>
          <span className="product-card-media-shine" />
        </span>

        <span className="product-card-body">
          <span className="category-tag">{product.categoryName || 'General'}</span>
          <h3 className="product-card-title">{product.name}</h3>
          <p className="product-card-desc">
            {product.description?.slice(0, 80) || 'No description provided.'}
          </p>
          {isAdmin ? (
            <span className={`badge badge-stock badge-stock--${stockKey}`}>{STOCK_LABEL[stockKey]}</span>
          ) : null}
        </span>
      </button>

      <div className="product-card-footer">
        <span className="price">{formatINR(product.price)}</span>
        <button
          type="button"
          className={`product-card-cart${added ? ' is-added' : ''}`}
          onClick={handleAddToCart}
          disabled={outOfStock}
          aria-label={outOfStock ? `${product.name} is out of stock` : `Add ${product.name} to cart`}
          title={outOfStock ? 'Out of stock' : added ? 'Added' : 'Add to cart'}
        >
          {added ? <CheckRounded sx={{ fontSize: 20 }} /> : <AddShoppingCartRounded sx={{ fontSize: 20 }} />}
        </button>
      </div>
    </article>
  )
}
