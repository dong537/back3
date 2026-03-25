import { Sparkles } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { isMobile } from '../utils/mobile'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const SPLASH_COPY = {
  'zh-CN': {
    loading: '广告加载中...',
    loadFailed: '广告加载失败',
    enterApp: '进入应用',
    skip: '跳过',
    skipWithCountdown: (countdown) => `${countdown} 秒后可跳过`,
    brand: '天机命理',
    sdkMissing: '未提供穿山甲开屏广告位 ID',
  },
  'en-US': {
    loading: 'Loading ad...',
    loadFailed: 'Failed to load ad',
    enterApp: 'Enter app',
    skip: 'Skip',
    skipWithCountdown: (countdown) => `Skip in ${countdown}s`,
    brand: 'Mystic Insight',
    sdkMissing: 'No Pangle splash slot id provided',
  },
}

export default function PangleSplashAd({
  slotId,
  minDisplayTime = 2000,
  maxDisplayTime = 5000,
  skipCountdown = 3,
  onAdClose,
  onAdClick,
  onAdError,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SPLASH_COPY[locale]
  const [showAd, setShowAd] = useState(false)
  const [canSkip, setCanSkip] = useState(false)
  const [countdown, setCountdown] = useState(skipCountdown)
  const [adLoaded, setAdLoaded] = useState(false)
  const [adError, setAdError] = useState(false)
  const adContainerRef = useRef(null)
  const adInstanceRef = useRef(null)
  const startTimeRef = useRef(null)
  const countdownTimerRef = useRef(null)
  const displayTimerRef = useRef(null)

  useEffect(() => {
    if (!isMobile()) {
      onAdClose?.()
      return undefined
    }

    if (!slotId) {
      logger.warn(copy.sdkMissing)
      onAdClose?.()
      return undefined
    }

    setShowAd(true)

    if (typeof window === 'undefined' || !window.bytedance) {
      loadPangleSDK()
    } else {
      initAd()
    }

    return () => {
      cleanup()
    }
  }, [copy.sdkMissing, slotId])

  useEffect(() => {
    if (!showAd || !canSkip) return undefined

    countdownTimerRef.current = setInterval(() => {
      setCountdown((previous) => {
        if (previous <= 1) {
          clearInterval(countdownTimerRef.current)
          return 0
        }
        return previous - 1
      })
    }, 1000)

    return () => {
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current)
      }
    }
  }, [canSkip, showAd])

  const loadPangleSDK = () => {
    const existingScript = document.getElementById('pangle-sdk')
    if (existingScript) {
      const checkSdk = setInterval(() => {
        if (window.bytedance) {
          clearInterval(checkSdk)
          initAd()
        }
      }, 100)
      return
    }

    const script = document.createElement('script')
    script.id = 'pangle-sdk'
    script.src =
      'https://sf16-fe-tos-sg.i18n-pglstatp.com/obj/pangle-sdk/pangle-sdk.js'
    script.async = true
    script.onload = () => {
      logger.info('Pangle splash SDK loaded')
      initAd()
    }
    script.onerror = () => {
      logger.error('Pangle splash SDK failed to load')
      setAdError(true)
      onAdError?.(copy.loadFailed)
      handleClose()
    }
    document.head.appendChild(script)
  }

  const initAd = () => {
    if (!window.bytedance || !adContainerRef.current || !slotId) {
      return
    }

    try {
      startTimeRef.current = Date.now()

      const adInstance =
        window.bytedance?.createSplashAd?.({
          adUnitId: slotId,
          container: adContainerRef.current,
        }) ||
        window.bytedance?.createBannerAd?.({
          adUnitId: slotId,
          container: adContainerRef.current,
          style: {
            left: 0,
            top: 0,
            width: '100%',
            height: '100%',
          },
        })

      if (!adInstance) {
        logger.error('Unable to create Pangle splash instance')
        setAdError(true)
        handleClose()
        return
      }

      adInstanceRef.current = adInstance

      adInstance.onLoad?.(() => {
        logger.info('Pangle splash ad loaded')
        setAdLoaded(true)
        setTimeout(() => {
          setCanSkip(true)
        }, minDisplayTime)

        displayTimerRef.current = setTimeout(() => {
          handleClose()
        }, maxDisplayTime)
      })

      adInstance.onError?.((error) => {
        logger.error('Pangle splash ad failed to load:', error)
        setAdError(true)
        onAdError?.(error)
        handleClose()
      })

      adInstance.onAdClick?.((ad) => {
        logger.info('Pangle splash ad clicked')
        onAdClick?.(ad)
      })

      adInstance.onClose?.(() => {
        logger.info('Pangle splash ad closed')
        handleClose()
      })

      adInstance.load?.()
    } catch (error) {
      logger.error('Initialize Pangle splash ad failed:', error)
      setAdError(true)
      onAdError?.(error)
      handleClose()
    }
  }

  const cleanup = () => {
    if (countdownTimerRef.current) {
      clearInterval(countdownTimerRef.current)
    }
    if (displayTimerRef.current) {
      clearTimeout(displayTimerRef.current)
    }
    if (adInstanceRef.current) {
      try {
        adInstanceRef.current.destroy?.()
      } catch (error) {
        logger.error('Destroy Pangle splash ad failed:', error)
      }
    }
  }

  const handleClose = () => {
    cleanup()

    const elapsed = Date.now() - (startTimeRef.current || Date.now())
    const remaining = Math.max(0, minDisplayTime - elapsed)

    setTimeout(() => {
      setShowAd(false)
      onAdClose?.()
    }, remaining)
  }

  const handleSkip = () => {
    if (!canSkip || countdown > 0) return
    handleClose()
  }

  if (!showAd) {
    return null
  }

  return (
    <div className="fixed inset-0 z-[9999] bg-[#090706]">
      <div
        ref={adContainerRef}
        className="relative h-full w-full overflow-hidden"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {!adLoaded && !adError ? (
          <div className="absolute inset-0 flex items-center justify-center bg-[radial-gradient(circle_at_18%_18%,rgba(126,41,19,0.24),transparent_24%),radial-gradient(circle_at_82%_14%,rgba(208,168,91,0.14),transparent_22%),linear-gradient(180deg,rgba(9,7,6,0.96),rgba(9,7,6,1))]">
            <div className="panel-soft w-full max-w-sm px-8 py-10 text-center">
              <div className="mx-auto mb-5 flex h-16 w-16 animate-pulse items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb] shadow-[0_18px_40px_rgba(163,66,36,0.24)]">
                <Sparkles className="h-8 w-8" />
              </div>
              <p className="text-xs uppercase tracking-[0.34em] text-[#dcb86f]">
                Opening Omen
              </p>
              <p className="mt-2 text-sm text-[#bdaa94]">{copy.loading}</p>
            </div>
          </div>
        ) : null}

        {adError ? (
          <div className="absolute inset-0 flex items-center justify-center bg-[radial-gradient(circle_at_18%_18%,rgba(126,41,19,0.24),transparent_24%),radial-gradient(circle_at_82%_14%,rgba(208,168,91,0.14),transparent_22%),linear-gradient(180deg,rgba(9,7,6,0.96),rgba(9,7,6,1))]">
            <div className="panel-soft w-full max-w-sm px-8 py-10 text-center">
              <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-[linear-gradient(135deg,#7a3218_0%,#b56b35_52%,#dcb86f_100%)] text-[#fff7eb] shadow-[0_18px_40px_rgba(122,50,24,0.24)]">
                <Sparkles className="h-8 w-8" />
              </div>
              <p className="mb-2 text-sm text-[#f4ece1]">{copy.loadFailed}</p>
              <button
                onClick={handleClose}
                className="btn-primary-theme px-4 py-2 text-sm"
              >
                {copy.enterApp}
              </button>
            </div>
          </div>
        ) : null}

        {canSkip ? (
          <button
            onClick={handleSkip}
            disabled={countdown > 0}
            className={`absolute right-4 top-4 z-10 rounded-full border border-white/10 bg-[#120d0c]/72 px-4 py-2 text-sm font-medium text-[#f4ece1] backdrop-blur-sm transition-all duration-200 ${
              countdown > 0
                ? 'cursor-not-allowed opacity-75'
                : 'hover:border-[#d0a85b]/24 hover:bg-[#1b1412]/88 active:scale-95'
            }`}
          >
            {countdown > 0 ? copy.skipWithCountdown(countdown) : copy.skip}
          </button>
        ) : null}

        <div className="absolute bottom-8 left-1/2 z-10 -translate-x-1/2">
          <div className="flex items-center gap-2 rounded-full border border-white/10 bg-[#140f0f]/78 px-3 py-2 text-[#bdaa94] backdrop-blur-sm">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb]">
              <Sparkles className="h-4 w-4" />
            </div>
            <span className="text-sm font-medium">{copy.brand}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
