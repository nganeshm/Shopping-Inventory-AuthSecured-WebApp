import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../api/orderApi'
import { useAuth } from '../context/AuthContext'
import LoadingState from '../components/common/LoadingState'
import ErrorBanner from '../components/common/ErrorBanner'
import EmptyState from '../components/common/EmptyState'

const statusVariant = {
  PENDING: 'badge-amber', CONFIRMED: 'badge-success', SHIPPED: 'badge-success',
  DELIVERED: 'badge-success', CANCELLED: 'badge-danger'
}

export default function OrdersPage() {
  const { customerId } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!customerId) { setLoading(false); return }
    orderApi.getByCustomerId(customerId)
      .then(({ data }) => setOrders(data))
      .catch((err) => setError(err.friendlyMessage || 'Could not load orders.'))
      .finally(() => setLoading(false))
  }, [customerId])

  if (!customerId) {
    return (
      <EmptyState
        title="No customer profile linked"
        hint={<>Go to <Link to="/profile">Profile</Link> and set your customer ID to see your orders.</>}
      />
    )
  }

  return (
    <div>
      <div className="page-header"><h1>My Orders</h1></div>
      <ErrorBanner message={error} />
      {loading && <LoadingState />}

      {!loading && orders.length === 0 && (
        <EmptyState title="No orders yet" hint="Place your first order from the cart." />
      )}

      {!loading && orders.length > 0 && (
        <div className="panel">
          <table>
            <thead><tr><th>Order #</th><th>Status</th><th>Items</th><th>Total</th><th>Date</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td><Link to={`/orders/${o.orderNumber}`}>{o.orderNumber}</Link></td>
                  <td><span className={`badge ${statusVariant[o.status] || 'badge-neutral'}`}>{o.status}</span></td>
                  <td>{o.items.length} item{o.items.length !== 1 ? 's' : ''}</td>
                  <td>${o.totalAmount.toFixed(2)}</td>
                  <td>{new Date(o.orderDate).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
