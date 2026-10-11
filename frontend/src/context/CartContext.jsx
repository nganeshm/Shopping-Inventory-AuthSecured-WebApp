import { createContext, useContext, useMemo, useState } from 'react'

const CartContext = createContext(null)

/**
 * Pure client-side cart state — intentionally NOT persisted on the backend
 * until checkout. This mirrors how most storefronts work: browsing and
 * cart-building don't need a server round trip for every click; the backend
 * only gets involved at the moment of truth (POST /api/orders), which is
 * where product-service actually reserves inventory and computes the final
 * total via its PricingStrategy.
 */
export function CartProvider({ children }) {
  const [items, setItems] = useState([]) // [{ product, quantity }]

  const addItem = (product, quantity = 1) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.product.id === product.id)
      if (existing) {
        return prev.map((i) =>
          i.product.id === product.id ? { ...i, quantity: i.quantity + quantity } : i
        )
      }
      return [...prev, { product, quantity }]
    })
  }

  const updateQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      removeItem(productId)
      return
    }
    setItems((prev) => prev.map((i) => (i.product.id === productId ? { ...i, quantity } : i)))
  }

  const removeItem = (productId) => {
    setItems((prev) => prev.filter((i) => i.product.id !== productId))
  }

  const clear = () => setItems([])

  const totalItems = useMemo(() => items.reduce((sum, i) => sum + i.quantity, 0), [items])
  const subtotal = useMemo(
    () => items.reduce((sum, i) => sum + i.product.price * i.quantity, 0),
    [items]
  )

  return (
    <CartContext.Provider value={{ items, addItem, updateQuantity, removeItem, clear, totalItems, subtotal }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
