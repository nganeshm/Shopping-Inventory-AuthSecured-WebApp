import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  CATALOG_PAGE_SIZE,
  fetchProductCatalog,
  fetchProductSearch,
  invalidateProductCatalog,
} from '../api/productCatalog'
import ProductCard from '../components/product/ProductCard'
import ProductDetailDialog from '../components/product/ProductDetailDialog'
import ProductFilters from '../components/product/ProductFilters'
import CatalogPagination from '../components/product/CatalogPagination'
import LoadingState from '../components/common/LoadingState'
import ErrorBanner from '../components/common/ErrorBanner'
import EmptyState from '../components/common/EmptyState'
import { useMainContentScroll } from '../hooks/useMainContentScroll'
import SearchRounded from '@mui/icons-material/SearchRounded'
import { applyProductFilters, createDefaultProductFilters } from '../utils/productFilters'
import { getFriendlyErrorMessage, isAbortError } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'

const emptyPage = {
  page: 0,
  size: CATALOG_PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  last: true,
}

export default function ProductsPage() {
  const [sourceProducts, setSourceProducts] = useState([])
  const [pageMeta, setPageMeta] = useState(emptyPage)
  const [page, setPage] = useState(0)
  const [filters, setFilters] = useState(createDefaultProductFilters)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [keyword, setKeyword] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [selectedProduct, setSelectedProduct] = useState(null)
  const searchAbortRef = useRef(null)
  const { toolbarHidden } = useMainContentScroll({ hideAfter: 64 })

  const displayProducts = useMemo(
    () => applyProductFilters(sourceProducts, filters),
    [sourceProducts, filters],
  )

  const loadCatalog = useCallback(async (pageNum = 0, { refresh = false } = {}) => {
    searchAbortRef.current?.abort()
    setLoading(true)
    setError(null)
    try {
      const { data } = await fetchProductCatalog({
        page: pageNum,
        size: CATALOG_PAGE_SIZE,
        force: refresh,
      })
      setSourceProducts(data.content)
      setPageMeta({
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        last: data.last,
      })
      setPage(data.page)
      setActiveSearch('')
    } catch (err) {
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.catalog.loadProducts))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadSearch = useCallback(async searchTerm => {
    searchAbortRef.current?.abort()
    const controller = new AbortController()
    searchAbortRef.current = controller

    const trimmed = searchTerm?.trim() ?? ''
    if (!trimmed) {
      await loadCatalog(0)
      return
    }

    setLoading(true)
    setError(null)
    try {
      const { data } = await fetchProductSearch(trimmed)
      if (controller.signal.aborted) return
      setSourceProducts(data)
      setPageMeta({
        page: 0,
        size: data.length,
        totalElements: data.length,
        totalPages: 1,
        last: true,
      })
      setPage(0)
      setActiveSearch(trimmed)
    } catch (err) {
      if (controller.signal.aborted || isAbortError(err)) return
      setError(getFriendlyErrorMessage(err, USER_MESSAGES.catalog.loadProducts))
    } finally {
      if (!controller.signal.aborted) {
        setLoading(false)
      }
    }
  }, [loadCatalog])

  useEffect(() => {
    if (activeSearch) return
    loadCatalog(page)
  }, [page, loadCatalog, activeSearch])

  useEffect(() => () => searchAbortRef.current?.abort(), [])

  const handleSearch = e => {
    e.preventDefault()
    loadSearch(keyword)
  }

  const handleClearSearch = () => {
    setKeyword('')
    setFilters(createDefaultProductFilters())
    invalidateProductCatalog()
    loadCatalog(0, { refresh: true })
  }

  const handlePageChange = nextPage => {
    if (nextPage === page || nextPage < 0) return
    setPage(nextPage)
  }

  return (
    <div className="products-page">
      <div
        className={`products-page-toolbar-wrap${toolbarHidden ? ' products-page-toolbar-wrap--hidden' : ''}`}
      >
        <form onSubmit={handleSearch} className="catalog-toolbar search-bar search-bar--wide" role="search">
          <span className="search-bar-icon" aria-hidden="true">
            <SearchRounded sx={{ fontSize: 20 }} />
          </span>
          <input
            className="search-bar-input"
            placeholder="Search catalog…"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            aria-label="Search catalog"
          />
          {activeSearch ? (
            <span className="search-bar-chip" title="Active search">
              {activeSearch}
            </span>
          ) : null}
          {activeSearch ? (
            <button className="search-bar-clear" type="button" onClick={handleClearSearch} aria-label="Clear search">
              ×
            </button>
          ) : (
            <button className="btn btn-primary btn-sm search-bar-submit" type="submit">
              Search
            </button>
          )}
        </form>
      </div>

      {!loading && sourceProducts.length > 0 && (
        <ProductFilters
          filters={filters}
          onChange={setFilters}
          resultCount={displayProducts.length}
          totalCount={sourceProducts.length}
        />
      )}

      <ErrorBanner message={error} />
      {loading && <LoadingState label="Loading catalog…" />}

      {!loading && displayProducts.length === 0 && !error && (
        <EmptyState
          title="No products found"
          hint="Try adjusting filters, a different search, or add products from the admin panel."
        />
      )}

      {!loading && displayProducts.length > 0 && (
        <div className="product-grid">
          {displayProducts.map(p => (
            <ProductCard key={p.id} product={p} onOpen={setSelectedProduct} />
          ))}
        </div>
      )}

      {!loading && !activeSearch && (
        <CatalogPagination
          page={pageMeta.page}
          size={pageMeta.size}
          totalPages={pageMeta.totalPages}
          totalElements={pageMeta.totalElements}
          last={pageMeta.last}
          onPageChange={handlePageChange}
          disabled={loading}
        />
      )}

      {selectedProduct ? (
        <ProductDetailDialog product={selectedProduct} onClose={() => setSelectedProduct(null)} />
      ) : null}
    </div>
  )
}
