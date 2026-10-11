import { useCallback, useEffect, useState } from 'react'

const SIDEBAR_STORAGE_KEY = 'em_sidebar_open'

function readSidebarOpen() {
  try {
    return localStorage.getItem(SIDEBAR_STORAGE_KEY) !== 'false'
  } catch {
    return true
  }
}

/** Sidebar open/close with persisted preference (layout concern only). */
export function useSidebarState() {
  const [open, setOpen] = useState(readSidebarOpen)

  useEffect(() => {
    try {
      localStorage.setItem(SIDEBAR_STORAGE_KEY, String(open))
    } catch {
      /* storage unavailable */
    }
  }, [open])

  const toggle = useCallback(() => {
    setOpen(prev => !prev)
  }, [])

  const close = useCallback(() => {
    setOpen(false)
  }, [])

  const openSidebar = useCallback(() => {
    setOpen(true)
  }, [])

  return { open, toggle, close, openSidebar, setOpen }
}
