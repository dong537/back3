import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { isMobile } from '../utils/mobile'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const PANGLE_COPY = {
  'zh-CN': {
    sdkLoadFailed: 'SDK 加载失败',
    adLoadFailed: '广告加载失败',
    adLoading: '广告加载中...',
    adLabel: '广告',
  },
  'en-US': {
    sdkLoadFailed: 'SDK failed to load',
    adLoadFailed: 'Failed to load ad',
    adLoading: 'Loading ad...',
    adLabel: 'Ad',
  },
}

export default function PangleBannerAd({
  slotId,
  width = '100%',
  height = 'auto',
  className = '',
  onAdLoad,
  onAdError,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = PANGLE_COPY[locale]
  const adContainerRef = useRef(null)
  const [adLoaded, setAdLoaded] = useState(false)
  const [adError, setAdError] = useState(false)
  const adInstanceRef = useRef(null)

  useEffect(() => {
    if (!isMobile()) {
      logger.info('Pangle banner is only shown on mobile devices')
      return undefined
    }

    if (!slotId) {
      logger.warn('No Pangle slot id provided')
      return undefined
    }

    if (typeof window === 'undefined' || !window.bytedance) {
      logger.warn('Pangle SDK not loaded yet')
      loadPangleSDK()
    } else {
      initAd()
    }

    return () => {
      if (adInstanceRef.current) {
        try {
          adInstanceRef.current.destroy?.()
        } catch (error) {
          logger.error('Destroy pangle ad instance failed:', error)
        }
      }
    }
  }, [slotId, locale])

  const loadPangleSDK = () => {
    if (document.getElementById('pangle-sdk')) return

    const script = document.createElement('script')
    script.id = 'pangle-sdk'
    script.src =
      'https://sf16-fe-tos-sg.i18n-pglstatp.com/obj/pangle-sdk/pangle-sdk.js'
    script.async = true
    script.onload = () => {
      logger.info('Pangle SDK loaded')
      initAd()
    }
    script.onerror = () => {
      logger.error('Pangle SDK failed to load')
      setAdError(true)
      onAdError?.(copy.sdkLoadFailed)
    }
    document.head.appendChild(script)
  }

  const initAd = () => {
    if (!window.bytedance || !adContainerRef.current || !slotId) return

    try {
      const adInstance = window.bytedance?.createBannerAd?.({
        adUnitId: slotId,
        container: adContainerRef.current,
        style: {
          left: 0,
          top: 0,
          width: '100%',
        },
      })

      adInstanceRef.current = adInstance

      adInstance.onLoad(() => {
        logger.info('Pangle ad loaded successfully')
        setAdLoaded(true)
        onAdLoad?.()
      })

      adInstance.onError((error) => {
        logger.error('Pangle ad failed to load:', error)
        setAdError(true)
        onAdError?.(error || copy.adLoadFailed)
      })

      adInstance.load()
    } catch (error) {
      logger.error('Initialize Pangle ad failed:', error)
      setAdError(true)
      onAdError?.(copy.adLoadFailed)
    }
  }

  if (typeof window !== 'undefined' && !isMobile()) {
    return null
  }

  if (adError) {
    return (
      <div
        className={`panel-soft flex items-center justify-center ${className}`}
        style={{ minHeight: '150px' }}
      >
        <div className="text-center text-sm text-[#8f7b66]">
          <p>{copy.adLoadFailed}</p>
        </div>
      </div>
    )
  }

  return (
    <div className={`relative ${className}`} style={{ width, height }}>
      <div
        ref={adContainerRef}
        className="w-full"
        style={{
          minHeight: adLoaded ? 'auto' : '150px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {!adLoaded && !adError && (
          <div className="flex h-40 w-full animate-pulse items-center justify-center rounded-[24px] border border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.92),rgba(14,11,10,0.8))]">
            <div className="text-sm text-[#8f7b66]">{copy.adLoading}</div>
          </div>
        )}
      </div>

      {adLoaded && (
        <div className="absolute right-2 top-2 rounded-full border border-[#d0a85b]/20 bg-[#6a4a1e]/20 px-2 py-0.5 text-[11px] text-[#dcb86f]">
          {copy.adLabel}
        </div>
      )}
    </div>
  )
}
