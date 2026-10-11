import { useEffect, useState } from 'react'
import { categoryApi } from '../api/categoryApi'
import LoadingState from '../components/common/LoadingState'
import ErrorBanner from '../components/common/ErrorBanner'
import EmptyState from '../components/common/EmptyState'

export default function CategoriesPage() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    categoryApi.getAll()
      .then(({ data }) => setCategories(data))
      .catch((err) => setError(err.friendlyMessage || 'Could not load categories.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <div className="page-header"><h1>Categories</h1></div>
      <ErrorBanner message={error} />
      {loading && <LoadingState />}

      {!loading && categories.length === 0 && (
        <EmptyState title="No categories yet" hint="An admin can create the first one from the admin panel." />
      )}

      {!loading && categories.length > 0 && (
        <div className="panel">
          <table>
            <thead>
              <tr><th>Name</th><th>Description</th><th>Parent</th><th>Subcategories</th></tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id}>
                  <td><strong>{c.name}</strong></td>
                  <td>{c.description || '—'}</td>
                  <td>{c.parentCategoryName || <span className="badge badge-neutral">top-level</span>}</td>
                  <td>{c.subCategoryNames?.length ? c.subCategoryNames.join(', ') : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
