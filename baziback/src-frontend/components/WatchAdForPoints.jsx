import { useEffect, useRef, useState } from 'react'
import { CheckCircle2, Coins, Play, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import { creditApi } from '../api'
import { toast } from './Toast'
import { POINTS_EARN } from '../utils/pointsConfig'
import { isMobile } from '../utils/mobile'
import PangleBannerAd from './PangleBannerAd'
import { pangleConfig } from '../config/pangle'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const WATCH_AD_COPY = {
  'zh-CN': {
    loginFirst: '请先登录',
    mobileOnly: '观看广告功能仅在移动端可用',
    adNotConfigured: '广告位未配置，暂时无法观看广告',
    earnFailed: '获取积分失败，请稍后重试',
    earnSuccess: (points) => `观看广告成功，获得 ${points} 积分`,
    earnFailedShort: '获取积分失败',
    title: '观看广告获得积分',
    rewardHint: '完整观看广告可获得',
    watching: '广告播放中...',
    earning: '正在领取积分...',
    earnNow: (points) => `立即领取 ${points} 积分`,
    trigger: (points) => `观看广告 +${points}积分`,
    pointsUnit: '积分',
  },
  'en-US': {
    loginFirst: 'Please sign in first',
    mobileOnly: 'Watching ads for credits is only available on mobile devices',
    adNotConfigured: 'Ad slot is not configured, so ads cannot be shown',
    earnFailed: 'Failed to claim credits. Please try again later.',
    earnSuccess: (points) =>
      `Ad watched successfully. Earned ${points} credits`,
    earnFailedShort: 'Failed to claim credits',
    title: 'Watch an Ad to Earn Credits',
    rewardHint: 'Watch the full ad to earn',
    watching: 'Watching ad...',
    earning: 'Claiming credits...',
    earnNow: (points) => `Claim ${points} credits`,
    trigger: (points) => `Watch ad +${points}`,
    pointsUnit: 'credits',
  },
}

export default function WatchAdForPoints({ onSuccess, className = '' }) {
  const { isLoggedIn, refreshCredits } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = WATCH_AD_COPY[locale]
  const [showAd, setShowAd] = useState(false)
  const [adWatched, setAdWatched] = useState(false)
  const [isEarning, setIsEarning] = useState(false)
  const [watchStartTime, setWatchStartTime] = useState(null)
  const adWatchTimerRef = useRef(null)
  const minWatchTime = 5000
  const adSlotId = pangleConfig.rewardSlotId

  useEffect(() => {
    return () => {
      if (adWatchTimerRef.current) {
        clearTimeout(adWatchTimerRef.current)
      }
    }
  }, [])

  const handleStartWatch = () => {
    if (!isLoggedIn) {
      toast.error(copy.loginFirst)
      return
    }

    if (!isMobile()) {
      toast.warning(copy.mobileOnly)
      return
    }

    if (!adSlotId) {
      toast.error(copy.adNotConfigured)
      return
    }

    setShowAd(true)
    setAdWatched(false)
    setWatchStartTime(Date.now())
  }

  const handleAdLoad = () => {
    if (!watchStartTime) return

    const elapsed = Date.now() - watchStartTime
    const remaining = Math.max(0, minWatchTime - elapsed)

    adWatchTimerRef.current = setTimeout(() => {
      setAdWatched(true)
    }, remaining)
  }

  const handleAdClick = () => {
    if (!adWatched && watchStartTime) {
      const elapsed = Date.now() - watchStartTime
      if (elapsed >= 3000) {
        setAdWatched(true)
        if (adWatchTimerRef.current) {
          clearTimeout(adWatchTimerRef.current)
        }
      }
    }
  }

  const handleEarnPoints = async () => {
    if (isEarning) return

    setIsEarning(true)
    try {
      const response = await creditApi.earnByWatchingAd()
      if (response.data?.code === 200) {
        const { points, newBalance } = response.data.data
        await refreshCredits()
        toast.success(copy.earnSuccess(points))
        setShowAd(false)
        setAdWatched(false)
        onSuccess?.(points, newBalance)
      } else {
        toast.error(response.data?.message || copy.earnFailedShort)
      }
    } catch (error) {
      logger.error('Earn watch-ad credits failed:', error)
      toast.error(error?.message || copy.earnFailed)
    } finally {
      setIsEarning(false)
    }
  }

  const handleClose = () => {
    setShowAd(false)
    setAdWatched(false)
    setWatchStartTime(null)
    if (adWatchTimerRef.current) {
      clearTimeout(adWatchTimerRef.current)
    }
  }

  if (showAd) {
    return (
      <div
        className={`fixed inset-0 z-[200] flex items-center justify-center bg-black/70 p-4 backdrop-blur-md ${className}`}
      >
        <div className="glass-dark w-full max-w-md overflow-hidden rounded-[30px] border border-white/10 shadow-[0_24px_80px_rgba(0,0,0,0.42)]">
          <div className="flex items-center justify-between bg-[linear-gradient(135deg,rgba(163,66,36,0.28),rgba(208,168,91,0.16))] p-4">
            <div className="flex items-center space-x-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-[0_14px_28px_rgba(163,66,36,0.18)]">
                <Coins size={18} />
              </div>
              <h3 className="font-bold text-[#f4ece1]">{copy.title}</h3>
            </div>
            <button
              onClick={handleClose}
              className="rounded-full p-1 text-[#bdaa94] transition-colors hover:bg-white/[0.08] hover:text-[#f4ece1]"
            >
              <X size={20} />
            </button>
          </div>

          <div className="p-4">
            <div className="mb-4">
              <p className="mb-2 text-sm text-[#e4d6c8]">
                {copy.rewardHint}{' '}
                <span className="font-bold text-[#dcb86f]">
                  {POINTS_EARN.WATCH_AD}
                </span>{' '}
                {copy.pointsUnit}
              </p>
              {!adWatched && (
                <div className="flex items-center space-x-2 text-xs text-[#8f7b66]">
                  <div className="h-3 w-3 animate-spin rounded-full border-b-2 border-[#dcb86f]"></div>
                  <span>{copy.watching}</span>
                </div>
              )}
            </div>

            <div
              className="mb-4 cursor-pointer overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]"
              style={{ minHeight: '200px' }}
              onClick={handleAdClick}
            >
              <PangleBannerAd slotId={adSlotId} onAdLoad={handleAdLoad} />
            </div>

            {adWatched && (
              <button
                onClick={handleEarnPoints}
                disabled={isEarning}
                className="btn-primary-theme flex w-full items-center justify-center space-x-2 py-3 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isEarning ? (
                  <>
                    <div className="h-4 w-4 animate-spin rounded-full border-b-2 border-white"></div>
                    <span>{copy.earning}</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={18} />
                    <span>{copy.earnNow(POINTS_EARN.WATCH_AD)}</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    )
  }

  return (
    <button
      onClick={handleStartWatch}
      className={`btn-primary-theme ${className}`}
    >
      <Play size={18} />
      <span>{copy.trigger(POINTS_EARN.WATCH_AD)}</span>
    </button>
  )
}
