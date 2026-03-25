import { lazy, Suspense, useState } from 'react'
import { Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import PrivateRoute from './components/PrivateRoute'
import { ToastContainer } from './components/Toast'
import PangleSplashAd from './components/PangleSplashAd'
import { pangleConfig } from './config/pangle'
import { useSSE } from './hooks/useSSE'
import useAppLocale from './hooks/useAppLocale'

const Home = lazy(() => import('./pages/Home'))
const YijingPage = lazy(() => import('./pages/YijingPage'))
const TarotPage = lazy(() => import('./pages/TarotPage'))
const TarotCardDetailPage = lazy(() => import('./pages/TarotCardDetailPage'))
const FavoritesPage = lazy(() => import('./pages/FavoritesPage'))
const BaziPage = lazy(() => import('./pages/BaziPage'))
const BaziInterpretationDetailPage = lazy(
  () => import('./pages/BaziInterpretationDetailPage')
)
const AIPage = lazy(() => import('./pages/AIPage'))
const GeminiFacePage = lazy(() => import('./pages/GeminiFacePage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const SelfPage = lazy(() => import('./pages/SelfPage'))
const ReferralPage = lazy(() => import('./pages/ReferralPage'))
const DailyTestPage = lazy(() => import('./pages/DailyTestPage'))
const CalculationRecordPage = lazy(
  () => import('./pages/CalculationRecordPage')
)
const BaziCompatibilityPage = lazy(
  () => import('./pages/BaziCompatibilityPage')
)
const AchievementPage = lazy(() => import('./pages/AchievementPage'))
const MessagesPage = lazy(() => import('./pages/MessagesPage'))
const PostDetailPage = lazy(() => import('./pages/PostDetailPage'))
const ZodiacPage = lazy(() => import('./pages/ZodiacPage'))
const CreditShopPage = lazy(() => import('./pages/CreditShopPage'))
const PrivacyPolicyPage = lazy(() => import('./pages/PrivacyPolicyPage'))
const UserAgreementPage = lazy(() => import('./pages/UserAgreementPage'))

function LoadingFallback({ locale = 'zh-CN' }) {
  const copy = locale === 'en-US' ? 'Loading...' : '加载中...'

  return (
    <div className="page-shell flex items-center justify-center">
      <div className="panel-soft w-full max-w-sm px-8 py-10 text-center">
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-[24px] bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] shadow-[0_18px_40px_rgba(163,66,36,0.24)]">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-white/25 border-t-[#fff7eb]" />
        </div>
        <div className="text-xs uppercase tracking-[0.36em] text-[#dcb86f]">
          Mystic Loading
        </div>
        <p className="mt-2 text-sm text-[#bdaa94]">{copy}</p>
      </div>
    </div>
  )
}

function App() {
  const { isLoading } = useAuth()
  const { locale } = useAppLocale()
  const [splashAdClosed, setSplashAdClosed] = useState(false)

  useSSE()

  const splashAdSlotId = pangleConfig.splashSlotId

  const handleSplashAdClose = () => {
    setSplashAdClosed(true)
  }

  if (isLoading) {
    return <LoadingFallback locale={locale} />
  }

  if (!splashAdClosed && splashAdSlotId) {
    return (
      <PangleSplashAd
        slotId={splashAdSlotId}
        minDisplayTime={2000}
        maxDisplayTime={5000}
        skipCountdown={3}
        onAdClose={handleSplashAdClose}
        onAdClick={(ad) => {
          console.log('Splash ad clicked:', ad)
        }}
        onAdError={(error) => {
          console.error('Splash ad failed to load:', error)
          setTimeout(handleSplashAdClose, 2000)
        }}
      />
    )
  }

  return (
    <>
      <ToastContainer />
      <Suspense fallback={<LoadingFallback locale={locale} />}>
        <Routes key={locale}>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="yijing" element={<YijingPage />} />
            <Route path="tarot" element={<TarotPage />} />
            <Route
              path="tarot/card/:cardName"
              element={<TarotCardDetailPage />}
            />
            <Route path="favorites" element={<FavoritesPage />} />
            <Route path="bazi" element={<BaziPage />} />
            <Route
              path="bazi/interpretation/:id"
              element={<BaziInterpretationDetailPage />}
            />
            <Route path="ai" element={<AIPage />} />
            <Route
              path="ai/face"
              element={
                <PrivateRoute>
                  <GeminiFacePage />
                </PrivateRoute>
              }
            />
            <Route path="self" element={<SelfPage />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="referral" element={<ReferralPage />} />
            <Route path="daily-test" element={<DailyTestPage />} />
            <Route path="records" element={<CalculationRecordPage />} />
            <Route path="compatibility" element={<BaziCompatibilityPage />} />
            <Route
              path="achievement"
              element={
                <PrivateRoute>
                  <AchievementPage />
                </PrivateRoute>
              }
            />
            <Route path="messages" element={<MessagesPage />} />
            <Route path="post/:id" element={<PostDetailPage />} />
            <Route path="zodiac" element={<ZodiacPage />} />
            <Route
              path="credit-shop"
              element={
                <PrivateRoute>
                  <CreditShopPage />
                </PrivateRoute>
              }
            />
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
