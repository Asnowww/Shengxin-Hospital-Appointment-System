import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    secure: false,
    port: 5173, // 默认是 5173，可以改
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Spring Boot 后端地址
        changeOrigin: true,
      },
      '/captcha': {
        target: 'http://localhost:8080', // 验证码接口也代理到后端
        changeOrigin: true,
      },
    },
  },
})
