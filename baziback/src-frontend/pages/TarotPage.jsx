import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { resolvePageLocale, safeArray, safeNumber, safeText } from '../utils/displayText'
import { points } from '../utils/referral'
import { getTarotPageCopyOverride } from './tarotPageConfig'
import {
  clearTarotModalState,
  persistTarotModalState,
  restoreTarotModalState,
} from './tarotPageUtils'
import { DailyDrawModal, DivinationModal } from '../components/tarot/TarotModals'
import {
  TarotHeroCarousel,
  TarotPointsPanel,
  TarotQuickConsultGrid,
  TarotSpreadSection,
  TarotTopBar,
} from '../components/tarot/TarotPageSections'

export default function TarotPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = getTarotPageCopyOverride(locale)
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } =
    useAuth()

  const [activeTab, setActiveTab] = useState('card')
  const [userPoints, setUserPoints] = useState(points.get())
  const [dailyDrawLoading, setDailyDrawLoading] = useState(false)
  const [dailyResult, setDailyResult] = useState(null)
  const [showDailyModal, setShowDailyModal] = useState(false)
  const [hasDrawnToday, setHasDrawnToday] = useState(false)
  const [showDivinationModal, setShowDivinationModal] = useState(false)
  const [selectedSpread, setSelectedSpread] = useState(null)
  const [modalInitialState, setModalInitialState] = useState(null)
  const [bannerIndex, setBannerIndex] = useState(0)

  const banners = copy.banners
  const pointsDisplay = isLoggedIn
    ? safeNumber(credits, 0)
    : safeNumber(userPoints, 0)

  useEffect(() => {
    if (isLoggedIn) refreshCredits()
  }, [isLoggedIn, refreshCredits])

  useEffect(() => {
    try {
      const restoredState = restoreTarotModalState(copy.defaultSpreadTitle)
      if (!restoredState) return

      setSelectedSpread(restoredState.selectedSpread)
      setModalInitialState(restoredState.modalInitialState)
      setShowDivinationModal(true)
    } catch (error) {
      logger.error('Failed to restore Tarot modal state:', error)
    } finally {
      clearTarotModalState()
    }
  }, [location, copy.defaultSpreadTitle])

  useEffect(() => {
    const timer = setInterval(() => {
      setBannerIndex((prev) => (prev + 1) % banners.length)
    }, 4000)
    return () => clearInterval(timer)
  }, [banners.length])

  useEffect(() => {
    let cancelled = false

    const loadTodayDraw = async () => {
      try {
        const token = sessionStorage.getItem('token')
        if (!token) {
          if (!cancelled) {
            setDailyResult(null)
            setHasDrawnToday(false)
          }
          return
        }

        const response = await fetch('/api/tarot/daily-draw', {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-Language': locale,
            'Accept-Language': locale,
          },
        })
        const data = await response.json()
        if (cancelled) return

        if (data.code === 200 && data.data) {
          setDailyResult(data.data)
          setHasDrawnToday(true)
        } else {
          setDailyResult(null)
          setHasDrawnToday(false)
        }
      } catch (error) {
        logger.error('Check today draw error:', error)
      }
    }

    loadTodayDraw()

    return () => {
      cancelled = true
    }
  }, [isLoggedIn, locale])

  const handleModalStateChange = (state) => {
    if (state && safeArray(state.drawnCards).length > 0) {
      persistTarotModalState(state)
    }
  }

  const handleCloseModal = () => {
    setShowDivinationModal(false)
    setModalInitialState(null)
    clearTarotModalState()
  }

  const handleDailyDraw = async () => {
    const token = sessionStorage.getItem('token')
    if (!token) {
      toast.error(copy.loginFirst)
      navigate('/login')
      return
    }

    if (hasDrawnToday && dailyResult) {
      setShowDailyModal(true)
      return
    }

    setDailyDrawLoading(true)
    try {
      const response = await fetch('/api/tarot/daily-draw', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Language': locale,
          'Accept-Language': locale,
        },
      })
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setDailyResult(data.data)
        setHasDrawnToday(true)
        setShowDailyModal(true)
        toast.success(copy.drawSuccess)
      } else {
        toast.error(safeText(data?.message, copy.drawFailed))
      }
    } catch (error) {
      logger.error('Daily draw error:', error)
      toast.error(copy.drawFailed)
    } finally {
      setDailyDrawLoading(false)
    }
  }

  const handleSpreadClick = (spread, isAdvanced = false) => {
    if (!safeText(spread?.code)) {
      toast.info(copy.comingSoon)
      return
    }

    if (isAdvanced && safeNumber(spread?.cost, 0) > 0) {
      if (isLoggedIn) {
        if (!canSpendCredits(spread.cost)) {
          toast.error(
            copy.insufficientPoints(spread.cost, safeNumber(credits, 0))
          )
          return
        }
      } else if (!points.canSpend(spread.cost)) {
        toast.error(copy.insufficientPointsGuest(spread.cost))
        return
      }
    }

    setSelectedSpread({ ...spread, isAdvanced })
    setModalInitialState(null)
    setShowDivinationModal(true)
  }

  const handleQuickConsult = (consult) => {
    setSelectedSpread({
      code: consult.spreadCode,
      title: consult.spreadTitle,
      cost: 0,
      icon: consult.icon,
    })
    setModalInitialState(null)
    setShowDivinationModal(true)
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <TarotTopBar
        copy={copy}
        activeTab={activeTab}
        onTabChange={setActiveTab}
        pointsDisplay={pointsDisplay}
        onBack={() => navigate(-1)}
      />

      <div className="app-page-shell pb-24 pt-4">
        <TarotHeroCarousel
          banners={banners}
          bannerIndex={bannerIndex}
          dailyDrawLoading={dailyDrawLoading}
          hasDrawnToday={hasDrawnToday}
          copy={copy}
          onBannerClick={handleDailyDraw}
          onBannerSelect={setBannerIndex}
        />

        <TarotQuickConsultGrid
          items={copy.quickConsults}
          onConsult={handleQuickConsult}
        />

        <TarotSpreadSection
          title={copy.beginnerTitle}
          badge={copy.beginnerBadge}
          accentClass="bg-[linear-gradient(180deg,#a34224_0%,#e3bf73_100%)]"
          items={copy.beginnerSpreads}
          copy={copy}
          onSpreadClick={handleSpreadClick}
        />

        <TarotSpreadSection
          title={copy.advancedTitle}
          badge={copy.advancedBadge}
          badgeWithCoins
          accentClass="bg-[linear-gradient(180deg,#7a3218_0%,#d0a85b_100%)]"
          items={copy.advancedSpreads}
          isAdvanced
          copy={copy}
          onSpreadClick={handleSpreadClick}
        />

        <TarotPointsPanel
          copy={copy}
          pointsDisplay={pointsDisplay}
          onGoDashboard={() => navigate('/dashboard')}
        />
      </div>

      <DailyDrawModal
        isOpen={showDailyModal}
        onClose={() => setShowDailyModal(false)}
        dailyResult={dailyResult}
        locale={locale}
        copy={copy}
      />

      <DivinationModal
        isOpen={showDivinationModal}
        onClose={handleCloseModal}
        spreadCode={selectedSpread?.code}
        spreadTitle={safeText(selectedSpread?.title, copy.defaultSpreadTitle)}
        spreadCost={safeNumber(selectedSpread?.cost, 0)}
        onPointsUpdate={setUserPoints}
        authContext={{ isLoggedIn, credits, spendCredits, canSpendCredits }}
        initialState={modalInitialState}
        onStateChange={handleModalStateChange}
        locale={locale}
        copy={copy}
      />
    </div>
  )
}
