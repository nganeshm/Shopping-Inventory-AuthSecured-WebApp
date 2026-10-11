import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { orderApi } from '../api/orderApi'
import ErrorBanner from '../components/common/ErrorBanner'
import EmptyState from '../components/common/EmptyState'
import { formatINR } from '../utils/currency'

export default function CartPage() {
  const { items, updateQuantity, removeItem, subtotal, clear } = useCart()
  const { isAuthenticated, customerId } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState(null)
  const [placing, setPlacing] = useState(false)

  const handleCheckout = async () => {
    setError(null)
    if (!customerId) {
      setError('We don\u2019t have your customer profile ID yet — set it on the Profile page, then check out.')
      return
    }
    setPlacing(true)
    try {
      // POST /api/orders on product-service: this single call reserves
      // inventory for every line item, calls customer-service (via Feign +
      // circuit breaker) for the shipping address snapshot, and applies the
      // PricingStrategy (bulk discount at 10+ units) to compute the total.
      const { data } = await orderApi.placeOrder({
        customerId: Number(customerId),
        items: items.map((i) => ({ productId: i.product.id, quantity: i.quantity }))
      })
      clear()
      navigate(`/orders/${data.orderNumber}`, { replace: true })
    } catch (err) {
      setError(err.friendlyMessage || 'Could not place order.')
    } finally {
      setPlacing(false)
    }
  }

  if (items.length === 0) {
    return <EmptyState title="Your cart is empty" hint="Browse the catalog and add something to it." />
  }

  return (
    <div>
      <div className="page-header"><h1>Cart</h1></div>
      <ErrorBanner message={error} />

      <div className="panel">
        <table>
          <thead>
            <tr><th>Product</th><th>Price</th><th>Quantity</th><th>Subtotal</th><th></th></tr>
          </thead>
          <tbody>
            {items.map(({ product, quantity }) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{formatINR(product.price)}</td>
                <td>
                  <input
                    type="number"
                    min={1}
                    value={quantity}
                    onChange={(e) => updateQuantity(product.id, Number(e.target.value))}
                    style={{ width: 56, padding: '5px 8px', border: '1px solid var(--border)', borderRadius: 6 }}
                  />
                </td>
                <td>{formatINR(product.price * quantity)}</td>
                <td>
                  <button className="btn btn-ghost btn-sm" onClick={() => removeItem(product.id)}>Remove</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="panel" style={{ marginTop: 16, maxWidth: 360, marginLeft: 'auto' }}>
        <div className="inline-row" style={{ justifyContent: 'space-between' }}>
          <span>Subtotal</span>
          <strong>{formatINR(subtotal)}</strong>
        </div>
        <p className="field-hint" style={{ margin: '6px 0 14px' }}>
          Final total (incl. any bulk discount) is calculated by product-service at checkout.
        </p>

        {isAuthenticated ? (
          <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleCheckout} disabled={placing}>
            {placing ? 'Placing order…' : 'Place order'}
          </button>
        ) : (
          <Link to="/login" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>
            Log in to check out
          </Link>
        )}
      </div>
    </div>
  )
}
