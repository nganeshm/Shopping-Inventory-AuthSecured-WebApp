import { useState } from 'react'
import TuneRounded from '@mui/icons-material/TuneRounded'
import ExpandMoreRounded from '@mui/icons-material/ExpandMoreRounded'
import { useAuth } from '../../context/AuthContext'
import { createDefaultProductFilters } from '../../utils/productFilters'

export default function ProductFilters({ filters, onChange, resultCount, totalCount }) {
  const { isAdmin } = useAuth()
  const [open, setOpen] = useState(false)
  const set = (key, value) => onChange({ ...filters, [key]: value })

  const handleReset = () => onChange(createDefaultProductFilters())

  const hasActiveFilters =
    filters.nameFilter ||
    filters.nameSort !== 'default' ||
    filters.priceMin !== '' ||
    filters.priceMax !== '' ||
    (isAdmin && filters.stockFilter !== 'all')

  return (
    <section
      className={`product-filters${open ? ' product-filters--open' : ''}`}
      aria-label="Filter products"
    >
      <div className="product-filters-head">
        <button
          type="button"
          className="product-filters-toggle"
          onClick={() => setOpen(prev => !prev)}
          aria-expanded={open}
          aria-controls="product-filters-panel"
        >
          <TuneRounded className="product-filters-toggle-icon" sx={{ fontSize: 18 }} />
          <span className="product-filters-toggle-label">Filters</span>
          {hasActiveFilters ? (
            <span className="product-filters-active-dot" title="Filters applied" />
          ) : null}
          <ExpandMoreRounded
            className={`product-filters-chevron${open ? ' product-filters-chevron--open' : ''}`}
            sx={{ fontSize: 20 }}
          />
        </button>
        <span className="product-filters-count">
          Showing {resultCount} of {totalCount}
        </span>
      </div>

      <div id="product-filters-panel" className="product-filters-panel">
        <div className="product-filters-grid">
          <div className="field product-filters-field">
            <label htmlFor="filter-name">Name</label>
            <input
              id="filter-name"
              type="search"
              placeholder="Contains…"
              value={filters.nameFilter}
              onChange={e => set('nameFilter', e.target.value)}
            />
          </div>

          <div className="field product-filters-field">
            <label htmlFor="filter-name-sort">Name order</label>
            <select
              id="filter-name-sort"
              value={filters.nameSort}
              onChange={e => set('nameSort', e.target.value)}
            >
              <option value="default">Default</option>
              <option value="name-asc">A → Z</option>
              <option value="name-desc">Z → A</option>
            </select>
          </div>

          <div className="field product-filters-field">
            <label htmlFor="filter-price-min">Min price (₹)</label>
            <input
              id="filter-price-min"
              type="number"
              min="0"
              step="1"
              placeholder="0"
              value={filters.priceMin}
              onChange={e => set('priceMin', e.target.value)}
            />
          </div>

          <div className="field product-filters-field">
            <label htmlFor="filter-price-max">Max price (₹)</label>
            <input
              id="filter-price-max"
              type="number"
              min="0"
              step="1"
              placeholder="Any"
              value={filters.priceMax}
              onChange={e => set('priceMax', e.target.value)}
            />
          </div>

          {isAdmin ? (
            <div className="field product-filters-field">
              <label htmlFor="filter-stock">Stock</label>
              <select
                id="filter-stock"
                value={filters.stockFilter}
                onChange={e => set('stockFilter', e.target.value)}
              >
                <option value="all">All</option>
                <option value="in">In stock</option>
                <option value="low">Low stock</option>
                <option value="out">Out of stock</option>
              </select>
            </div>
          ) : null}

          <div className="product-filters-actions">
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={handleReset}
              disabled={!hasActiveFilters}
            >
              Reset filters
            </button>
          </div>
        </div>
      </div>
    </section>
  )
}
