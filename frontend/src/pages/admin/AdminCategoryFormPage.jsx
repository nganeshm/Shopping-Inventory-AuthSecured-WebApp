import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { categoryApi } from '../../api/categoryApi'
import ErrorBanner from '../../components/common/ErrorBanner'

export default function AdminCategoryFormPage() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState({ name: '', description: '', parentCategoryId: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => { categoryApi.getAll().then(({ data }) => setCategories(data)) }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      // POST /api/categories — requires ROLE_ADMIN, enforced by both the
      // gateway's token check AND product-service's own @PreAuthorize.
      await categoryApi.create({
        name: form.name,
        description: form.description,
        parentCategoryId: form.parentCategoryId ? Number(form.parentCategoryId) : null
      })
      navigate('/categories')
    } catch (err) {
      setError(err.friendlyMessage || 'Could not create category.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="form-shell">
      <h1>New category</h1>
      <div className="panel">
        <ErrorBanner message={error} />
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Name</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="field">
            <label>Description</label>
            <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="field">
            <label>Parent category (optional)</label>
            <select value={form.parentCategoryId} onChange={(e) => setForm({ ...form, parentCategoryId: e.target.value })}>
              <option value="">None — top level</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create category'}
          </button>
        </form>
      </div>
    </div>
  )
}
