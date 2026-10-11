import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import FestivePromoBanner from './FestivePromoBanner'
import { useSidebarState } from '../../hooks/useSidebarState'

export default function AppLayout() {
  const { open: sidebarOpen, toggle: toggleSidebar, close: closeSidebar } = useSidebarState()

  return (
    <div className={`app-shell${sidebarOpen ? '' : ' app-shell--sidebar-closed'}`}>
      {sidebarOpen && (
        <button
          type="button"
          className="sidebar-backdrop"
          onClick={closeSidebar}
          aria-label="Close sidebar"
        />
      )}
      <Sidebar open={sidebarOpen} onClose={closeSidebar} />
      <div className="main-col">
        <FestivePromoBanner />
        <Topbar sidebarOpen={sidebarOpen} onToggleSidebar={toggleSidebar} />
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
