import { logger } from './utils/logger'

// 全局错误处理，捕获未被React组件树捕获的错误
window.addEventListener('error', function(event) {
  logger.error('全局错误捕获:', event.error);
  // 在生产环境中，你可以在这里将错误上报给监控服务
  return true;
});

// 处理未被捕获的Promise拒绝
window.addEventListener('unhandledrejection', function(event) {
  logger.error('未处理的Promise拒绝:', event.reason);
  event.preventDefault();
});

import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import App from './App'
import ErrorBoundary from './components/ErrorBoundary'
// ToastContainer 已在 App.jsx 中挂载

import { checkReferralCode } from './utils/referralHelper'
import { toast } from './components/Toast'
import { preventDoubleTapZoom, setViewportHeight } from './utils/mobile'
import './i18n'
import './index.css'

// 检测推荐码
checkReferralCode()

// 注册 Service Worker
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/service-worker.js')
      .then(registration => {
        logger.info('Service Worker 注册成功:', registration);
      })
      .catch(error => {
        logger.error('Service Worker 注册失败:', error);
      });
  });
} else if ('serviceWorker' in navigator) {
  // 在开发环境中注销任何现有的service worker，以避免缓存问题
  navigator.serviceWorker.getRegistrations().then(function(registrations) {
    for(let registration of registrations) {
      registration.unregister();
      logger.info('Service Worker 已为开发环境注销。');
    }
  }).catch(function(err) {
    logger.error('Service Worker 注销失败: ', err);
  });
}

// 移动端优化
if (typeof window !== 'undefined') {
  // 防止iOS双击缩放
  preventDoubleTapZoom()
  
  // 设置视口高度
  setViewportHeight()
  
  // 禁用iOS橡皮筋效果（可选）
  document.addEventListener('touchmove', (e) => {
    if (e.target.closest('.scrollable') === null) {
      // 允许滚动容器内的滚动
      return
    }
  }, { passive: true })
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <BrowserRouter>
        <ThemeProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </ThemeProvider>
      </BrowserRouter>
    </ErrorBoundary>
  </React.StrictMode>,
)
