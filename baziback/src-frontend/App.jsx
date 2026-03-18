import { lazy, Suspense, useState, useEffect } from 'react'
import { Routes, Route } from 'react-router-dom'
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';
import { ToastContainer } from './components/Toast';
import PangleSplashAd from './components/PangleSplashAd';
import { useSSE } from './hooks/useSSE';

// ✅ 使用 lazy 加载页面组件 - 实现代码分割
const Home = lazy(() => import('./pages/Home'))
const YijingPage = lazy(() => import('./pages/YijingPage'))
const TarotPage = lazy(() => import('./pages/TarotPage'))
const TarotCardDetailPage = lazy(() => import('./pages/TarotCardDetailPage'))
const FavoritesPage = lazy(() => import('./pages/FavoritesPage'))
const BaziPage = lazy(() => import('./pages/BaziPage'))
const BaziInterpretationDetailPage = lazy(() => import('./pages/BaziInterpretationDetailPage'))
const AIPage = lazy(() => import('./pages/AIPage'))
const GeminiFacePage = lazy(() => import('./pages/GeminiFacePage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const SelfPage = lazy(() => import('./pages/SelfPage'))
const ReferralPage = lazy(() => import('./pages/ReferralPage'))
const DailyTestPage = lazy(() => import('./pages/DailyTestPage'))
const CalculationRecordPage = lazy(() => import('./pages/CalculationRecordPage'))
const BaziCompatibilityPage = lazy(() => import('./pages/BaziCompatibilityPage'))
const AchievementPage = lazy(() => import('./pages/AchievementPage'))
const MessagesPage = lazy(() => import('./pages/MessagesPage'))
const PostDetailPage = lazy(() => import('./pages/PostDetailPage'))
const ZodiacPage = lazy(() => import('./pages/ZodiacPage'))
const CreditShopPage = lazy(() => import('./pages/CreditShopPage'))
const PrivacyPolicyPage = lazy(() => import('./pages/PrivacyPolicyPage'))
const UserAgreementPage = lazy(() => import('./pages/UserAgreementPage'))

// ✅ 加载中的占位符
function LoadingFallback() {
  return (
    <div className="flex items-center justify-center h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
        <p className="text-gray-600">加载中...</p>
      </div>
    </div>
  )
}

function App() {
  const { isLoading } = useAuth();
  const [splashAdClosed, setSplashAdClosed] = useState(false)
  
  // 建立全局SSE连接，监听成就解锁和积分变化
  useSSE()

  // 获取开屏广告位ID
  const splashAdSlotId = import.meta.env.VITE_PANGLE_SPLASH_SLOT_ID || import.meta.env.VITE_PANGLE_SLOT_ID

  const handleSplashAdClose = () => {
    setSplashAdClosed(true)
  }

  // 如果正在加载，显示加载状态
  if (isLoading) {
    return <LoadingFallback />
  }

  // 如果开屏广告未关闭，显示开屏广告
  if (!splashAdClosed && splashAdSlotId) {
    return (
      <PangleSplashAd
        slotId={splashAdSlotId}
        minDisplayTime={2000}
        maxDisplayTime={5000}
        skipCountdown={3}
        onAdClose={handleSplashAdClose}
        onAdClick={(ad) => {
          console.log('开屏广告被点击:', ad)
        }}
        onAdError={(error) => {
          console.error('开屏广告加载失败:', error)
          // 广告加载失败时，延迟关闭以显示品牌信息
          setTimeout(handleSplashAdClose, 2000)
        }}
      />
    )
  }

  return (
    <>
      <ToastContainer />
      <Suspense fallback={<LoadingFallback />}>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="yijing" element={<YijingPage />} />
            <Route path="tarot" element={<TarotPage />} />
            <Route path="tarot/card/:cardName" element={<TarotCardDetailPage />} />
            <Route path="favorites" element={<FavoritesPage />} />
            <Route path="bazi" element={<BaziPage />} />
            <Route path="bazi/interpretation/:id" element={<BaziInterpretationDetailPage />} />
            <Route path="ai" element={<AIPage />} />
            <Route path="ai/face" element={<PrivateRoute><GeminiFacePage /></PrivateRoute>} />
            <Route path="self" element={<SelfPage />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="referral" element={<ReferralPage />} />
            <Route path="daily-test" element={<DailyTestPage />} />
            <Route path="records" element={<CalculationRecordPage />} />
            <Route path="compatibility" element={<BaziCompatibilityPage />} />
            <Route path="achievement" element={<PrivateRoute><AchievementPage /></PrivateRoute>} />
            <Route path="messages" element={<MessagesPage />} />
            <Route path="post/:id" element={<PostDetailPage />} />
            <Route path="zodiac" element={<ZodiacPage />} />
            <Route path="credit-shop" element={<PrivateRoute><CreditShopPage /></PrivateRoute>} />
            <Route path="privacy-policy" element={<PrivacyPolicyPage />} />
            <Route path="user-agreement" element={<UserAgreementPage />} />
            <Route path="login" element={<LoginPage />} />
          </Route>
        </Routes>
      </Suspense>
    </>
  )
}

export default App
