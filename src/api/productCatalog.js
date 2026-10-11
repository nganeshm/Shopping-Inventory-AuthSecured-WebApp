import { productApi } from './productApi'

/** Matches product-service getAll: default and max page size is 20. */
export const CATALOG_PAGE_SIZE = 20

/** Successful page payloads — reused across StrictMode remounts. */
const catalogCache = new Map()
/** In-flight GET /api/products?page&size — dedupes concurrent callers per page. */
const catalogInflight = new Map()

function cacheKey(page, size) {
  return `${page}-${size}`
}

export function invalidateProductCatalog() {
  catalogCache.clear()
  catalogInflight.clear()
}

/** @param {unknown} data */
export function normalizeProductList(data) {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.items)) return data.items
  if (Array.isArray(data?.data)) return data.data
  return []
}

/**
 * Normalize GET /api/products (ProductPageResponse) or a raw list.
 * @param {unknown} data
 * @param {{ page?: number, size?: number }} [requested]
 */
export function normalizeProductPage(data, requested = {}) {
  const content = normalizeProductList(data)
  const isPage = data && typeof data === 'object' && !Array.isArray(data)
    && (Array.isArray(data.content) || data.totalElements != null || data.totalPages != null)

  if (isPage) {
    const page = Number.isInteger(data.page) ? data.page : (requested.page ?? 0)
    const size = Number.isInteger(data.size) ? data.size : (requested.size ?? CATALOG_PAGE_SIZE)
    const totalElements = typeof data.totalElements === 'number' ? data.totalElements : content.length
    const totalPages = typeof data.totalPages === 'number'
      ? data.totalPages
      : Math.max(1, Math.ceil(totalElements / Math.max(size, 1)))
    return {
      content,
      page,
      size,
      totalElements,
      totalPages,
      last: typeof data.last === 'boolean' ? data.last : page >= totalPages - 1,
    }
  }

  return {
    content,
    page: requested.page ?? 0,
    size: requested.size ?? content.length,
    totalElements: content.length,
    totalPages: 1,
    last: true,
  }
}

/**
 * GET /api/products?page=&size= — one network call per page until invalidateProductCatalog().
 *
 * @param {{ page?: number, size?: number, force?: boolean }} [options]
 */
export function fetchProductCatalog(options = {}) {
  const page = Math.max(0, options.page ?? 0)
  const size = Math.min(CATALOG_PAGE_SIZE, Math.max(1, options.size ?? CATALOG_PAGE_SIZE))
  const { force = false } = options
  const key = cacheKey(page, size)

  if (force) {
    catalogCache.delete(key)
    catalogInflight.delete(key)
  }

  if (catalogCache.has(key)) {
    return Promise.resolve({ data: catalogCache.get(key), status: 200, statusText: 'OK' })
  }

  if (catalogInflight.has(key)) {
    return catalogInflight.get(key)
  }

  const request = productApi
    .getAll({ page, size })
    .then(response => {
      const pageData = normalizeProductPage(response.data, { page, size })
      catalogCache.set(key, pageData)
      catalogInflight.delete(key)
      return { ...response, data: pageData }
    })
    .catch(error => {
      catalogInflight.delete(key)
      throw error
    })

  catalogInflight.set(key, request)
  return request
}

/**
 * Filter a product list in the browser (current page or search results).
 *
 * @param {Array} products
 * @param {string} keyword
 */
export function filterProductsByKeyword(products, keyword) {
  const q = keyword.trim().toLowerCase()
  if (!q) return products
  return products.filter(product => {
    const haystack = [product.name, product.description, product.sku, product.categoryName]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(q)
  })
}

/**
 * GET /api/products/search — returns a full match list (not paged).
 * @param {string} keyword
 */
export async function fetchProductSearch(keyword) {
  const { data, status, statusText } = await productApi.search(keyword)
  return {
    data: normalizeProductList(data),
    status,
    statusText,
  }
}
