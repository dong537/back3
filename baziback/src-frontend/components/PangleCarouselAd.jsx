import { useEffect, useRef, useState } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { isMobile } from '../utils/mobile'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const CAROUSEL_COPY = {
  'zh-CN': {
    sdkFailed: 'SDK 加载失败',
    mobileOnlyTitle: '广告提示',
    mobileOnlyBody: '广告仅在移动端显示',
    mobileOnlyHelp: '请使用移动设备或浏览器移动端模拟模式查看',
    slotMissingTitle: '广告位未配置',
    slotMissingBody: '请在项目根目录的 .env 文件中配置广告位 ID。',
    loadFailedTitle: '广告加载失败',
    loadFailedHint: '请检查广告位、网络状态和拦截设置后重试。',
    retry: '重试',
    loading: '广告加载中...',
    imageFallback: '广告图片',
    previous: '上一张',
    next: '下一张',
    switchTo: (index) => `切换到第 ${index} 张`,
    adLabel: '广告',
  },
  'en-US': {
    sdkFailed: 'SDK failed to load',
    mobileOnlyTitle: 'Ad notice',
    mobileOnlyBody: 'Ads are only shown on mobile devices',
    mobileOnlyHelp:
      'Use a mobile device or browser device emulation to preview',
    slotMissingTitle: 'Ad slot is not configured',
    slotMissingBody: 'Set the ad slot ID in the project root .env file.',
    loadFailedTitle: 'Failed to load ad',
    loadFailedHint:
      'Check the slot id, network status, and ad-block settings, then try again.',
    retry: 'Retry',
    loading: 'Loading ad...',
    imageFallback: 'Ad image',
    previous: 'Previous',
    next: 'Next',
    switchTo: (index) => `Switch to slide ${index}`,
    adLabel: 'Ad',
  },
}

export default function PangleCarouselAd({
  slotId,
  className = '',
  autoPlay = true,
  interval = 3000,
  showIndicators = true,
  showArrows = true,
  onAdLoad,
  onAdError,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = CAROUSEL_COPY[locale]
  const adContainerRef = useRef(null)
  const timerRef = useRef(null)
  const adInstanceRef = useRef(null)
  const [currentIndex, setCurrentIndex] = useState(0)
  const [adData, setAdData] = useState([])
  const [adLoaded, setAdLoaded] = useState(false)
  const [adError, setAdError] = useState(false)

  useEffect(() => {
    if (!isMobile()) {
      return undefined
    }

    if (!slotId || slotId === 'YOUR_PANGLE_SLOT_ID') {
      setAdError(true)
      return undefined
    }

    if (typeof window === 'undefined' || !window.bytedance) {
      loadPangleSDK()
    } else {
      initAd()
    }

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
      if (adInstanceRef.current) {
        try {
          adInstanceRef.current.destroy?.()
        } catch (error) {
          logger.error('Destroy Pangle carousel ad failed:', error)
        }
      }
    }
  }, [slotId, copy.sdkFailed])

  useEffect(() => {
    if (!autoPlay || !adLoaded || adData.length <= 1) {
      return undefined
    }

    timerRef.current = setInterval(() => {
      setCurrentIndex((previous) => (previous + 1) % adData.length)
    }, interval)

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
    }
  }, [adData.length, adLoaded, autoPlay, interval])

  const loadPangleSDK = () => {
    if (document.getElementById('pangle-sdk')) {
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
      logger.info('Pangle carousel SDK loaded')
      initAd()
    }
    script.onerror = () => {
      logger.error('Pangle carousel SDK failed to load')
      setAdError(true)
      onAdError?.(copy.sdkFailed)
    }
    document.head.appendChild(script)
  }

  const initAd = () => {
    if (!window.bytedance || !adContainerRef.current || !slotId) {
      return
    }

    try {
      const adInstance =
        window.bytedance?.createNativeAd?.({
          adUnitId: slotId,
        }) ||
        window.bytedance?.createBannerAd?.({
          adUnitId: slotId,
        })

      if (!adInstance) {
        setAdError(true)
        return
      }

      adInstanceRef.current = adInstance

      adInstance.onLoad?.((ads) => {
        const formattedAds = Array.isArray(ads) ? ads : [ads]
        setAdData(formattedAds)
        setAdLoaded(true)
        setAdError(false)
        onAdLoad?.(formattedAds)
      })

      adInstance.onError?.((error) => {
        logger.error('Pangle carousel ad failed to load:', error)
        setAdError(true)
        onAdError?.(error)
      })

      adInstance.loadAd?.({
        adCount: 3,
      })
    } catch (error) {
      logger.error('Initialize Pangle carousel ad failed:', error)
      setAdError(true)
      onAdError?.(error)
    }
  }

  const retryLoad = () => {
    setAdError(false)
    setAdLoaded(false)
    if (window.bytedance) {
      initAd()
    } else {
      loadPangleSDK()
    }
  }

  const handlePrev = () => {
    setCurrentIndex(
      (previous) => (previous - 1 + adData.length) % adData.length
    )
  }

  const handleNext = () => {
    setCurrentIndex((previous) => (previous + 1) % adData.length)
  }

  const handleIndicatorClick = (index) => {
    setCurrentIndex(index)
  }

  const handleAdClick = (ad) => {
    if (adInstanceRef.current && ad.reportAdClick) {
      adInstanceRef.current.reportAdClick(ad)
    }
  }

  const fallbackImageSvg = `data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="150"%3E%3Crect fill="%23140f0f" width="400" height="150"/%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%23bdaa94" font-size="14"%3E${encodeURIComponent(
    copy.imageFallback
  )}%3C/text%3E%3C/svg%3E`

  if (typeof window !== 'undefined' && !isMobile()) {
    if (!import.meta.env.DEV) {
      return null
    }

    return (
      <div
        className={`rounded-[22px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 p-4 ${className}`}
      >
        <div className="text-center text-sm text-[#dcb86f]">
          <p className="font-medium">{copy.mobileOnlyTitle}</p>
          <p className="mt-1 text-[#bdaa94]">{copy.mobileOnlyBody}</p>
          <p className="mt-1 text-xs text-[#8f7b66]">{copy.mobileOnlyHelp}</p>
        </div>
      </div>
    )
  }

  if (!slotId || slotId === 'YOUR_PANGLE_SLOT_ID') {
    return (
      <div
        className={`rounded-[22px] border border-[#a34224]/20 bg-[#7a3218]/12 p-4 ${className}`}
      >
        <div className="text-center text-sm text-[#e19a84]">
          <p className="font-medium">{copy.slotMissingTitle}</p>
          <p className="mt-1 text-xs text-[#bdaa94]">{copy.slotMissingBody}</p>
          <code className="mt-2 block rounded-[14px] border border-white/10 bg-black/15 p-2 text-xs text-[#f4ece1]">
            VITE_PANGLE_SLOT_ID=your_slot_id_here
          </code>
        </div>
      </div>
    )
  }

  if (adError) {
    return (
      <div
        className={`rounded-[22px] border border-[#a34224]/20 bg-[#7a3218]/12 p-4 ${className}`}
      >
        <div className="text-center text-sm text-[#e19a84]">
          <p className="font-medium">{copy.loadFailedTitle}</p>
          <p className="mt-1 text-xs text-[#bdaa94]">{copy.loadFailedHint}</p>
          <button
            onClick={retryLoad}
            className="mt-2 rounded-[14px] border border-white/10 bg-white/[0.04] px-3 py-1 text-xs text-[#f4ece1] transition hover:bg-white/[0.08]"
          >
            {copy.retry}
          </button>
        </div>
      </div>
    )
  }

  if (!adLoaded || adData.length === 0) {
    return (
      <div
        className={`animate-pulse rounded-[22px] border border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.92),rgba(14,11,10,0.8))] ${className}`}
        style={{ minHeight: '150px' }}
      >
        <div className="flex h-full flex-col items-center justify-center p-4">
          <div className="mb-2 text-sm text-[#8f7b66]">{copy.loading}</div>
          {import.meta.env.DEV ? (
            <div className="text-center text-xs text-[#8f7b66]">
              <p>slot: {slotId}</p>
              <p>sdk: {window.bytedance ? 'ready' : 'missing'}</p>
            </div>
          ) : null}
        </div>
      </div>
    )
  }

  return (
    <div className={`relative overflow-hidden rounded-[22px] ${className}`}>
      <div
        ref={adContainerRef}
        className="relative w-full"
        style={{ aspectRatio: '16/9', minHeight: '150px' }}
      >
        {adData.map((ad, index) => (
          <div
            key={`${ad.title || 'ad'}-${index}`}
            className={`absolute inset-0 transition-opacity duration-500 ${
              index === currentIndex ? 'z-10 opacity-100' : 'z-0 opacity-0'
            }`}
          >
            <a
              href={ad.targetUrl || '#'}
              onClick={(event) => {
                event.preventDefault()
                handleAdClick(ad)
                if (ad.targetUrl) {
                  window.open(ad.targetUrl, '_blank')
                }
              }}
              className="block h-full w-full"
            >
              <img
                src={ad.imageUrl || ad.iconUrl}
                alt={ad.title || copy.adLabel}
                className="h-full w-full object-cover"
                onError={(event) => {
                  event.currentTarget.src = fallbackImageSvg
                }}
              />
              {ad.title ? (
                <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-3">
                  <p className="line-clamp-1 text-sm font-medium text-white">
                    {ad.title}
                  </p>
                </div>
              ) : null}
            </a>
          </div>
        ))}

        {showArrows && adData.length > 1 ? (
          <>
            <button
              onClick={handlePrev}
              className="absolute left-2 top-1/2 z-20 -translate-y-1/2 rounded-full border border-white/10 bg-[#120d0c]/72 p-1.5 text-white transition-all hover:bg-[#1b1412]/88"
              aria-label={copy.previous}
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={handleNext}
              className="absolute right-2 top-1/2 z-20 -translate-y-1/2 rounded-full border border-white/10 bg-[#120d0c]/72 p-1.5 text-white transition-all hover:bg-[#1b1412]/88"
              aria-label={copy.next}
            >
              <ChevronRight size={20} />
            </button>
          </>
        ) : null}

        {showIndicators && adData.length > 1 ? (
          <div className="absolute bottom-3 left-1/2 z-20 flex -translate-x-1/2 space-x-1.5">
            {adData.map((_, index) => (
              <button
                key={`indicator-${index}`}
                onClick={() => handleIndicatorClick(index)}
                className={`h-1.5 rounded-full transition-all ${
                  index === currentIndex
                    ? 'w-6 bg-[#f0d9a5]'
                    : 'w-1.5 bg-white/50 hover:bg-white/75'
                }`}
                aria-label={copy.switchTo(index + 1)}
              />
            ))}
          </div>
        ) : null}

        <div className="absolute right-2 top-2 z-20 rounded-full border border-white/10 bg-[#120d0c]/72 px-2 py-0.5 text-xs text-[#f0d9a5]">
          {copy.adLabel}
        </div>
      </div>
    </div>
  )
}
