import { useEffect, useState } from 'react'
import { productApi } from '../../api/productApi'
import LoadingState from '../../components/common/LoadingState'
import ErrorBanner from '../../components/common/ErrorBanner'
import EmptyState from '../../components/common/EmptyState'

export default function AdminLowStockPage() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [restocking, setRestocking] = useState(null)
  const [amounts, setAmounts] = useState({})

  const load = () => {
    setLoading(true)
    // GET /api/products/low-stock — ROLE_ADMIN only. Backed by a JOIN query
    // on the server (ProductRepository.findLowStockProducts) comparing
    // Inventory.quantityAvailable to Inventory.reorderLevel.
    productApi.lowStock()
      .then(({ data }) => setProducts(data))
      .catch((err) => setError(err.friendlyMessage || 'Could not load low-stock products.'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleRestock = async (productId) => {
    const quantity = Number(amounts[productId] || 0)
    if (quantity <= 0) return
    setRestocking(productId)
    try {
      await productApi.restock(productId, quantity)
      load()
    } catch (err) {
      setError(err.friendlyMessage || 'Restock failed.')
    } finally {
      setRestocking(null)
    }
  }

  return (
    <div>
      <div className="page-header"><h1>Low stock</h1></div>
      <ErrorBanner message={error} />
      {loading && <LoadingState />}

      {!loading && products.length === 0 && (
        <EmptyState title="Nothing is low on stock" hint="Every product is above its reorder level." />
      )}

      {!loading && products.length > 0 && (
        <div className="panel">
          <table>
            <thead><tr><th>Product</th><th>Available</th><th>Reorder level</th><th>Restock by</th><th></th></tr></thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td><span className="badge badge-amber">{p.inventory.quantityAvailable}</span></td>
                  <td>{p.inventory.reorderLevel}</td>
                  <td>
                    <input
                      type="number"
                      min={1}
                      placeholder="qty"
                      value={amounts[p.id] || ''}
                      onChange={(e) => setAmounts({ ...amounts, [p.id]: e.target.value })}
                      style={{ width: 70, padding: '5px 8px', border: '1px solid var(--border)', borderRadius: 6 }}
                    />
                  </td>
                  <td>
                    <button
                      className="btn btn-ghost btn-sm"
                      disabled={restocking === p.id}
                      onClick={() => handleRestock(p.id)}
                    >
                      {restocking === p.id ? 'Restocking…' : 'Restock'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
