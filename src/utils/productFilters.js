export function getProductStockStatus(product) {
  const available = product.inventory?.quantityAvailable ?? 0
  if (available <= 0) return 'out'
  if (product.inventory?.lowStock) return 'low'
  return 'in'
}

const defaultFilters = {
  nameFilter: '',
  nameSort: 'default',
  priceMin: '',
  priceMax: '',
  stockFilter: 'all',
}

export function createDefaultProductFilters() {
  return { ...defaultFilters }
}

/**
 * @param {Array} products
 * @param {typeof defaultFilters} filters
 */
export function applyProductFilters(products, filters) {
  if (!Array.isArray(products)) return []

  let list = [...products]

  const nameQuery = filters.nameFilter?.trim().toLowerCase()
  if (nameQuery) {
    list = list.filter(p => p.name?.toLowerCase().includes(nameQuery))
  }

  const min = filters.priceMin === '' ? null : Number(filters.priceMin)
  if (min != null && !Number.isNaN(min)) {
    list = list.filter(p => Number(p.price) >= min)
  }

  const max = filters.priceMax === '' ? null : Number(filters.priceMax)
  if (max != null && !Number.isNaN(max)) {
    list = list.filter(p => Number(p.price) <= max)
  }

  if (filters.stockFilter && filters.stockFilter !== 'all') {
    list = list.filter(p => getProductStockStatus(p) === filters.stockFilter)
  }

  if (filters.nameSort === 'name-asc') {
    list.sort((a, b) => (a.name || '').localeCompare(b.name || '', undefined, { sensitivity: 'base' }))
  } else if (filters.nameSort === 'name-desc') {
    list.sort((a, b) => (b.name || '').localeCompare(a.name || '', undefined, { sensitivity: 'base' }))
  }

  return list
}
