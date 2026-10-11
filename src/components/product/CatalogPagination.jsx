import ChevronLeftRounded from '@mui/icons-material/ChevronLeftRounded'
import ChevronRightRounded from '@mui/icons-material/ChevronRightRounded'

function pageWindow(current, total) {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i)
  }
  const pages = new Set([0, total - 1, current, current - 1, current + 1])
  return [...pages].filter(p => p >= 0 && p < total).sort((a, b) => a - b)
}

export default function CatalogPagination({
  page,
  size,
  totalPages,
  totalElements,
  last,
  onPageChange,
  disabled = false,
}) {
  if (!totalElements || totalPages <= 1) return null

  const from = page * size + 1
  const to = last ? totalElements : Math.min((page + 1) * size, totalElements)
  const numbers = pageWindow(page, totalPages)

  return (
    <nav className="catalog-pagination" aria-label="Catalog pages">
      <p className="catalog-pagination-meta">
        {from}–{to} of {totalElements}
      </p>
      <div className="catalog-pagination-controls">
        <button
          type="button"
          className="btn btn-ghost btn-sm catalog-pagination-btn"
          onClick={() => onPageChange(page - 1)}
          disabled={disabled || page <= 0}
          aria-label="Previous page"
        >
          <ChevronLeftRounded sx={{ fontSize: 20 }} />
          Prev
        </button>
        {numbers.map((n, i) => {
          const prev = numbers[i - 1]
          const gap = prev != null && n - prev > 1
          return (
            <span key={n} className="catalog-pagination-slot">
              {gap ? <span className="catalog-pagination-ellipsis">…</span> : null}
              <button
                type="button"
                className={`btn btn-sm catalog-pagination-num${n === page ? ' catalog-pagination-num--active' : ' btn-ghost'}`}
                onClick={() => onPageChange(n)}
                disabled={disabled || n === page}
                aria-current={n === page ? 'page' : undefined}
              >
                {n + 1}
              </button>
            </span>
          )
        })}
        <button
          type="button"
          className="btn btn-ghost btn-sm catalog-pagination-btn"
          onClick={() => onPageChange(page + 1)}
          disabled={disabled || last || page >= totalPages - 1}
          aria-label="Next page"
        >
          Next
          <ChevronRightRounded sx={{ fontSize: 20 }} />
        </button>
      </div>
    </nav>
  )
}
