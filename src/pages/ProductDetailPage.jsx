import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { productApi } from '../api/productApi'
import { useCart } from '../context/CartContext'
import LoadingState from '../components/common/LoadingState'
import ErrorBanner from '../components/common/ErrorBanner'

export default function ProductDetailPage() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [qty, setQty] = useState(1)
  const { addItem } = useCart()

  useEffect(() => {
    setLoading(true)
    productApi.getById(id)
      .then(({ data }) => setProduct(data))
      .catch((err) => setError(err.friendlyMessage || 'Product not found.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <LoadingState />
  if (error) return <ErrorBanner message={error} />
  if (!product) return null

  const available = product.inventory?.quantityAvailable ?? 0

  return (
    <div style={{ maxWidth: 640 }}>
      <Link to="/products">&larr; Back to products</Link>
      <div className="panel" style={{ marginTop: 14 }}>
        <span className="category-tag">{product.categoryName}</span>
        <h1>{product.name}</h1>
        <p>{product.description || 'No description provided for this product.'}</p>

        <div className="inline-row" style={{ margin: '14px 0' }}>
          {available <= 0 ? (
            <span className="badge badge-danger">Out of stock</span>
          ) : product.inventory?.lowStock ? (
            <span className="badge badge-amber">Low stock — {available} left</span>
          ) : (
            <span className="badge badge-success">{available} in stock</span>
          )}
        </div>

        <div className="inline-row" style={{ justifyContent: 'space-between' }}>
          <span className="price">${product.price.toFixed(2)}</span>
          <div className="inline-row">
            <input
              type="number"
              min={1}
              max={Math.max(available, 1)}
              value={qty}
              onChange={(e) => setQty(Number(e.target.value))}
              style={{ width: 64, padding: '8px 10px', border: '1px solid var(--border)', borderRadius: 6 }}
            />
            <button
              className="btn btn-primary"
              disabled={available <= 0}
              onClick={() => addItem(product, qty)}
            >
              Add to cart
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
