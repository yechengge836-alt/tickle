// 导入 Vite 配置辅助函数。
import { defineConfig } from 'vite'
// 导入 React 的 Vite 插件。
import react from '@vitejs/plugin-react'

// 导出 Vite 开发与构建配置。
export default defineConfig({
  // 启用 React JSX 转换和热更新。
  plugins: [react()],
  // 配置本地开发服务器。
  server: {
    // 前端开发服务器端口。
    port: 5173,
    // 将 API 请求转发给 Spring Boot，避免浏览器跨域限制。
    proxy: {
      // 匹配所有以 /api 开头的请求。
      '/api': {
        // 后端 API 的本机地址。
        target: 'http://localhost:4154',
        // 将请求 Host 改为目标服务地址。
        changeOrigin: true,
      },
    },
  },
})
