import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/sms': {
        target: 'http://localhost:8006',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8006',
        changeOrigin: true
      }
    }
  }
})