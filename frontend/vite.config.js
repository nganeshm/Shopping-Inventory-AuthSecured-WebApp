import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Plain Vite + React setup — no extra build complexity needed for a
// frontend that just calls a REST API over HTTP.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  }
})
