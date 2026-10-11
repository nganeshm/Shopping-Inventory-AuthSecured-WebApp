import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { orderApi } from '../api/orderApi'
import LoadingState from '../components/common/LoadingState'
import ErrorBanner from '../components/common/ErrorBanner'

export default function OrderDetailPage() {
  const { orderNumber } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    orderApi.getByOrderNumber(orderNumber)
      .then(({ data }) => setOrder(data))
      .catch((err) => setError(err.friendlyMessage || 'Order not found.'))
      .finally(() => setLoading(false))
  }, [orderNumber])

  if (loading) return <LoadingState />
  if (error) return <ErrorBanner message={error} />
  if (!order) return null

  return (
    <div style={{ maxWidth: 640 }}>
      <Link to="/orders">&larr; Back to orders</Link>

      <div className="panel" style={{ marginTop: 14 }}>
        <div className="page-header">
          <h1>{order.orderNumber}</h1>
          <span className="badge badge-success">{order.status}</span>
        </div>

        <h3>Shipping to</h3>
        <p>
          {order.shippingLine1}<br />
          {order.shippingCity}, {order.shippingState} {order.shippingPostalCode}<br />
          {order.shippingCountry}
        </p>
        <p className="field-hint">
          This address was captured as a snapshot at the moment the order was placed —
          it won't change even if the customer updates their address later.
        </p>

        <h3 style={{ marginTop: 20 }}>Items</h3>
        <table>
          <thead><tr><th>Product</th><th>Qty</th><th>Unit price</th><th>Subtotal</th></tr></thead>
          <tbody>
            {order.items.map((item, idx) => (
              <tr key={idx}>
                <td>{item.productName}</td>
                <td>{item.quantity}</td>
                <td>${item.unitPriceAtPurchase.toFixed(2)}</td>
                <td>${item.subtotal.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="inline-row" style={{ justifyContent: 'flex-end', marginTop: 14 }}>
          <strong style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
            Total: ${order.totalAmount.toFixed(2)}
          </strong>
        </div>
      </div>
    </div>
  )
}
