import { useEffect, useRef, useState } from 'react'

/**
 * Tracks scroll on the app main content pane (`.content`).
 * @param {{ hideAfter?: number }} [options]
 */
export function useMainContentScroll({ hideAfter = 72 } = {}) {
  const [toolbarHidden, setToolbarHidden] = useState(false)
  const lastScrollTop = useRef(0)

  useEffect(() => {
    const el = document.querySelector('.content')
    if (!el) return undefined

    const onScroll = () => {
      const top = el.scrollTop
      const scrollingDown = top > lastScrollTop.current
      lastScrollTop.current = top

      if (top <= hideAfter) {
        setToolbarHidden(false)
        return
      }
      setToolbarHidden(scrollingDown)
    }

    el.addEventListener('scroll', onScroll, { passive: true })
    return () => el.removeEventListener('scroll', onScroll)
  }, [hideAfter])

  return { toolbarHidden }
}
