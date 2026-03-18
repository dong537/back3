import { useEffect, useState, useRef } from 'react'
import { X } from 'lucide-react'
import { isMobile } from '../utils/mobile'

/**
 * 穿山甲开屏广告组件
 * 参考文档：https://www.csjplatform.com/union/media/union/download/detail?id=195&docId=27615&locale=zh-CN&osType=android
 */
export default function PangleSplashAd({ 
  slotId,
  minDisplayTime = 2000, // 最小展示时间（毫秒）
  maxDisplayTime = 5000, // 最大展示时间（毫秒）
  skipCountdown = 3, // 跳过按钮倒计时（秒）
  onAdClose,
  onAdClick,
  onAdError
}) {
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
    // 仅在移动端显示
    if (!isMobile()) {
      onAdClose?.()
      return
    }

    // 检查是否已加载穿山甲SDK
    if (typeof window === 'undefined' || !window.bytedance) {
      loadPangleSDK()
      return
    }

    if (!slotId) {
      console.warn('未提供穿山甲开屏广告位ID')
      onAdClose?.()
      return
    }

    // 显示开屏广告
    setShowAd(true)
    initAd()

    return () => {
      cleanup()
    }
  }, [slotId])

  // 倒计时
  useEffect(() => {
    if (!showAd || !canSkip) return

    countdownTimerRef.current = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(countdownTimerRef.current)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => {
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current)
      }
    }
  }, [showAd, canSkip])

  const loadPangleSDK = () => {
    if (document.getElementById('pangle-sdk')) {
      // SDK已加载，等待加载完成
      const checkSDK = setInterval(() => {
        if (window.bytedance) {
          clearInterval(checkSDK)
          setShowAd(true)
          initAd()
        }
      }, 100)
      return
    }

    const script = document.createElement('script')
    script.id = 'pangle-sdk'
    script.src = 'https://sf16-fe-tos-sg.i18n-pglstatp.com/obj/pangle-sdk/pangle-sdk.js'
    script.async = true
    script.onload = () => {
      console.log('穿山甲SDK加载成功')
      setShowAd(true)
      initAd()
    }
    script.onerror = () => {
      console.error('穿山甲SDK加载失败')
      setAdError(true)
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

      // 创建开屏广告实例
      // 注意：实际使用时需要根据穿山甲SDK的最新API调整
      const adInstance = window.bytedance?.createSplashAd?.({
        adUnitId: slotId,
        container: adContainerRef.current,
      }) || window.bytedance?.createBannerAd?.({
        adUnitId: slotId,
        container: adContainerRef.current,
        style: {
          left: 0,
          top: 0,
          width: '100%',
          height: '100%',
        }
      })

      if (!adInstance) {
        console.error('无法创建开屏广告实例')
        setAdError(true)
        handleClose()
        return
      }

      adInstanceRef.current = adInstance

      // 监听广告加载成功
      adInstance.onLoad?.((ad) => {
        console.log('穿山甲开屏广告加载成功')
        setAdLoaded(true)
        
        // 开始倒计时
        setTimeout(() => {
          setCanSkip(true)
        }, minDisplayTime)

        // 设置最大展示时间
        displayTimerRef.current = setTimeout(() => {
          handleClose()
        }, maxDisplayTime)
      })

      // 监听广告加载失败
      adInstance.onError?.((err) => {
        console.error('穿山甲开屏广告加载失败:', err)
        setAdError(true)
        onAdError?.(err)
        handleClose()
      })

      // 监听广告点击
      adInstance.onAdClick?.((ad) => {
        console.log('开屏广告被点击')
        onAdClick?.(ad)
      })

      // 监听广告关闭
      adInstance.onClose?.((ad) => {
        console.log('开屏广告被关闭')
        handleClose()
      })

      // 加载广告
      adInstance.load?.()

    } catch (error) {
      console.error('初始化穿山甲开屏广告失败:', error)
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
      } catch (e) {
        console.error('销毁广告实例失败:', e)
      }
    }
  }

  const handleClose = () => {
    cleanup()
    
    // 确保最小展示时间
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
    <div className="fixed inset-0 z-[9999] bg-white">
      {/* 广告容器 */}
      <div 
        ref={adContainerRef}
        className="w-full h-full relative"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        {/* 加载中占位 */}
        {!adLoaded && !adError && (
          <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-50 flex items-center justify-center">
            <div className="text-center">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center mb-4 mx-auto animate-pulse">
                <span className="text-white text-2xl">☯</span>
              </div>
              <p className="text-gray-600 text-sm">加载中...</p>
            </div>
          </div>
        )}

        {/* 错误占位 */}
        {adError && (
          <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-50 flex items-center justify-center">
            <div className="text-center">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center mb-4 mx-auto">
                <span className="text-white text-2xl">☯</span>
              </div>
              <p className="text-gray-600 text-sm mb-2">广告加载失败</p>
              <button
                onClick={handleClose}
                className="px-4 py-2 bg-purple-600 text-white rounded-lg text-sm hover:bg-purple-700 transition-colors"
              >
                进入应用
              </button>
            </div>
          </div>
        )}

        {/* 跳过按钮 */}
        {canSkip && (
          <button
            onClick={handleSkip}
            disabled={countdown > 0}
            className={`
              absolute top-4 right-4 z-10
              px-4 py-2 rounded-full
              bg-black/50 backdrop-blur-sm
              text-white text-sm font-medium
              transition-all duration-200
              ${countdown > 0 
                ? 'cursor-not-allowed opacity-75' 
                : 'hover:bg-black/70 active:scale-95'
              }
            `}
          >
            {countdown > 0 ? `跳过 ${countdown}s` : '跳过'}
          </button>
        )}

        {/* Logo或品牌标识（可选） */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10">
          <div className="flex items-center space-x-2 text-gray-600">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center">
              <span className="text-white text-sm">☯</span>
            </div>
            <span className="text-sm font-medium">天机明理</span>
          </div>
        </div>
      </div>
    </div>
  )
}
