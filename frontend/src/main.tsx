// 导入 StrictMode，在开发环境帮助识别不安全的副作用。
import { StrictMode } from 'react'
// 导入 React 18 创建根节点的方法。
import { createRoot } from 'react-dom/client'
// 导入页面根组件。
import App from './App'
// 导入全局样式表。
import './styles.css'

// 找到 HTML 中的 root 节点，并渲染应用组件树。
createRoot(document.getElementById('root')!).render(
  // 使用严格模式包裹应用，仅影响开发期检查。
  <StrictMode>
    {/* 渲染票务平台页面。 */}
    <App />
  </StrictMode>,
)
