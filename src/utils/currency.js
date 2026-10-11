const inrFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Format a numeric amount as Indian Rupees (₹). */
export function formatINR(amount) {
  const value = Number(amount)
  if (!Number.isFinite(value)) return inrFormatter.format(0)
  return inrFormatter.format(value)
}
