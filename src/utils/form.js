/** Trim selected string fields. Password and similar secrets should be omitted. */
export function trimFields(values, keys) {
  const next = { ...values }
  for (const key of keys) {
    if (typeof next[key] === 'string') {
      next[key] = next[key].trim()
    }
  }
  return next
}
