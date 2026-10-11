import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { categoryApi } from '../../api/categoryApi'
import { productApi } from '../../api/productApi'
import { invalidateProductCatalog } from '../../api/productCatalog'
import ErrorBanner from '../../components/common/ErrorBanner'

export default function AdminProductFormPage() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState({
    sku: '', name: '', description: '', price: '', categoryId: '',
    initialQuantity: 0, warehouseLocation: ''
  })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => { categoryApi.getAll().then(({ data }) => setCategories(data)) }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      // POST /api/products — ROLE_ADMIN only. product-service creates the
      // Product AND its 1:1 Inventory row in one transaction (see
      // ProductServiceImpl.create on the backend).
      await productApi.create({
        sku: form.sku,
        name: form.name,
        description: form.description,
        price: Number(form.price),
        categoryId: Number(form.categoryId),
        initialQuantity: Number(form.initialQuantity),
        warehouseLocation: form.warehouseLocation
      })
      invalidateProductCatalog()
      navigate('/products')
    } catch (err) {
      setError(err.friendlyMessage || 'Could not create product.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="form-shell">
      <h1>New product</h1>
      <div className="panel">
        <ErrorBanner message={error} />
        <form onSubmit={handleSubmit}>
          <div className="field-row">
            <div className="field">
              <label>SKU</label>
              <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} required />
            </div>
            <div className="field">
              <label>Price</label>
              <input type="number" step="0.01" min="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required />
            </div>
          </div>
          <div className="field">
            <label>Name</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="field">
            <label>Description</label>
            <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="field">
            <label>Category</label>
            <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required>
              <option value="">Select a category…</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="field-row">
            <div className="field">
              <label>Initial quantity</label>
              <input type="number" min="0" value={form.initialQuantity} onChange={(e) => setForm({ ...form, initialQuantity: e.target.value })} />
            </div>
            <div className="field">
              <label>Warehouse location</label>
              <input value={form.warehouseLocation} onChange={(e) => setForm({ ...form, warehouseLocation: e.target.value })} placeholder="PUNE-WH-1" />
            </div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create product'}
          </button>
        </form>
      </div>
    </div>
  )
}
