import { useEffect, useRef, useState } from 'react'
import { isMobile } from '../utils/mobile'
import { ChevronLeft, ChevronRight } from 'lucide-react'

/**
 * 穿山甲轮播图广告组件（自定义实现）
 * 支持多张广告图片轮播
 * 参考文档：https://www.csjplatform.com/union/media/union/download/detail?id=195&docId=27627
 */
export default function PangleCarouselAd({ 
  slotId,
  className = '',
  autoPlay = true,
  interval = 3000,
  showIndicators = true,
  showArrows = true,
  onAdLoad,
  onAdError 
}) {
  const adContainerRef = useRef(null)
  const [currentIndex, setCurrentIndex] = useState(0)
  const [adData, setAdData] = useState([])
  const [adLoaded, setAdLoaded] = useState(false)
  const [adError, setAdError] = useState(false)
  const timerRef = useRef(null)
  const adInstanceRef = useRef(null)

  useEffect(() => {
    // 开发环境调试信息
    if (import.meta.env.DEV) {
      console.log('=== 穿山甲广告调试信息 ===')
      console.log('是否移动端:', isMobile())
      console.log('广告位ID:', slotId)
      console.log('环境变量 VITE_PANGLE_SLOT_ID:', import.meta.env.VITE_PANGLE_SLOT_ID)
      console.log('window.bytedance:', typeof window !== 'undefined' ? window.bytedance : 'undefined')
    }

    if (!isMobile()) {
      if (import.meta.env.DEV) {
        console.warn('广告组件：非移动端，不显示广告')
      }
      return
    }

    if (typeof window === 'undefined' || !window.bytedance) {
      if (import.meta.env.DEV) {
        console.log('广告组件：开始加载穿山甲SDK')
      }
      loadPangleSDK()
      return
    }

    if (!slotId || slotId === 'YOUR_PANGLE_SLOT_ID') {
      console.warn('未提供穿山甲广告位ID，请在 .env 文件中配置 VITE_PANGLE_SLOT_ID')
      setAdError(true)
      return
    }

    if (import.meta.env.DEV) {
      console.log('广告组件：开始初始化广告')
    }
    initAd()

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
      if (adInstanceRef.current) {
        try {
          adInstanceRef.current.destroy?.()
        } catch (e) {
          console.error('销毁广告实例失败:', e)
        }
      }
    }
  }, [slotId])

  // 自动播放
  useEffect(() => {
    if (!autoPlay || !adLoaded || adData.length <= 1) {
      return
    }

    timerRef.current = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % adData.length)
    }, interval)

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current)
      }
    }
  }, [autoPlay, interval, adLoaded, adData.length])

  const loadPangleSDK = () => {
    if (document.getElementById('pangle-sdk')) {
      return
    }

    const script = document.createElement('script')
    script.id = 'pangle-sdk'
    script.src = 'https://sf16-fe-tos-sg.i18n-pglstatp.com/obj/pangle-sdk/pangle-sdk.js'
    script.async = true
    script.onload = () => {
      console.log('穿山甲SDK加载成功')
      initAd()
    }
    script.onerror = () => {
      console.error('穿山甲SDK加载失败')
      setAdError(true)
      onAdError?.('SDK加载失败')
    }
    document.head.appendChild(script)
  }

  const initAd = () => {
    if (!window.bytedance || !adContainerRef.current || !slotId) {
      return
    }

    try {
      // 创建信息流广告实例（用于轮播）
      // 注意：实际使用时需要根据穿山甲SDK的最新API调整
      // 如果SDK API不同，请参考最新文档调整
      const adInstance = window.bytedance?.createNativeAd?.({
        adUnitId: slotId,
      }) || window.bytedance?.createBannerAd?.({
        adUnitId: slotId,
      })

      adInstanceRef.current = adInstance

      adInstance.onLoad((ads) => {
        console.log('穿山甲广告加载成功:', ads)
        // 处理广告数据
        const formattedAds = Array.isArray(ads) ? ads : [ads]
        setAdData(formattedAds)
        setAdLoaded(true)
        onAdLoad?.(formattedAds)
      })

      adInstance.onError((err) => {
        console.error('穿山甲广告加载失败:', err)
        setAdError(true)
        onAdError?.(err)
      })

      // 加载广告
      adInstance.loadAd({
        adCount: 3, // 请求3条广告用于轮播
      })

    } catch (error) {
      console.error('初始化穿山甲广告失败:', error)
      setAdError(true)
      onAdError?.(error)
    }
  }

  const handlePrev = () => {
    setCurrentIndex((prev) => (prev - 1 + adData.length) % adData.length)
  }

  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % adData.length)
  }

  const handleIndicatorClick = (index) => {
    setCurrentIndex(index)
  }

  const handleAdClick = (ad) => {
    // 上报广告点击
    if (adInstanceRef.current && ad.reportAdClick) {
      adInstanceRef.current.reportAdClick(ad)
    }
  }

  // 非移动端显示提示（开发环境）
  if (typeof window !== 'undefined' && !isMobile()) {
    if (import.meta.env.DEV) {
      return (
        <div className={`bg-yellow-50 border border-yellow-200 rounded-lg p-4 ${className}`}>
          <div className="text-center text-yellow-700 text-sm">
            <p className="font-medium">⚠️ 广告提示</p>
            <p className="mt-1">广告仅在移动端显示</p>
            <p className="mt-1 text-xs">请使用移动设备或浏览器移动端模拟模式查看</p>
          </div>
        </div>
      )
    }
    return null
  }

  // 广告位ID未配置
  if (!slotId || slotId === 'YOUR_PANGLE_SLOT_ID') {
    return (
      <div className={`bg-orange-50 border border-orange-200 rounded-lg p-4 ${className}`}>
        <div className="text-center text-orange-700 text-sm">
          <p className="font-medium">⚠️ 广告位ID未配置</p>
          <p className="mt-1 text-xs">请在项目根目录的 .env 文件中添加：</p>
          <code className="mt-1 block text-xs bg-orange-100 p-2 rounded">VITE_PANGLE_SLOT_ID=your_slot_id_here</code>
        </div>
      </div>
    )
  }

  if (adError) {
    return (
      <div className={`bg-red-50 border border-red-200 rounded-lg p-4 ${className}`}>
        <div className="text-center text-red-700 text-sm">
          <p className="font-medium">❌ 广告加载失败</p>
          <p className="mt-1 text-xs">请检查：</p>
          <ul className="mt-1 text-xs text-left list-disc list-inside">
            <li>广告位ID是否正确</li>
            <li>广告位是否已审核通过</li>
            <li>网络连接是否正常</li>
            <li>是否被广告拦截器拦截</li>
          </ul>
          {import.meta.env.DEV && (
            <button
              onClick={() => {
                setAdError(false)
                setAdLoaded(false)
                if (window.bytedance) {
                  initAd()
                } else {
                  loadPangleSDK()
                }
              }}
              className="mt-2 px-3 py-1 bg-red-200 hover:bg-red-300 rounded text-xs"
            >
              重试
            </button>
          )}
        </div>
      </div>
    )
  }

  if (!adLoaded || adData.length === 0) {
    return (
      <div className={`bg-gradient-to-r from-gray-100 to-gray-200 rounded-lg animate-pulse ${className}`} style={{ minHeight: '150px' }}>
        <div className="flex flex-col items-center justify-center h-full p-4">
          <div className="text-gray-400 text-sm mb-2">广告加载中...</div>
          {import.meta.env.DEV && (
            <div className="text-xs text-gray-400 text-center">
              <p>广告位ID: {slotId}</p>
              <p>SDK状态: {window.bytedance ? '已加载' : '未加载'}</p>
            </div>
          )}
        </div>
      </div>
    )
  }

  const currentAd = adData[currentIndex]

  return (
    <div className={`relative overflow-hidden rounded-lg ${className}`}>
      {/* 广告轮播容器 */}
      <div 
        ref={adContainerRef}
        className="relative w-full"
        style={{ aspectRatio: '16/9', minHeight: '150px' }}
      >
        {/* 广告图片 */}
        {adData.map((ad, index) => (
          <div
            key={index}
            className={`absolute inset-0 transition-opacity duration-500 ${
              index === currentIndex ? 'opacity-100 z-10' : 'opacity-0 z-0'
            }`}
          >
            <a
              href={ad.targetUrl || '#'}
              onClick={(e) => {
                e.preventDefault()
                handleAdClick(ad)
                if (ad.targetUrl) {
                  window.open(ad.targetUrl, '_blank')
                }
              }}
              className="block w-full h-full"
            >
              <img
                src={ad.imageUrl || ad.iconUrl}
                alt={ad.title || '广告'}
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="150"%3E%3Crect fill="%23e5e7eb" width="400" height="150"/%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%239ca3af" font-size="14"%3E广告图片%3C/text%3E%3C/svg%3E'
                }}
              />
              {/* 广告标题（可选） */}
              {ad.title && (
                <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-3">
                  <p className="text-white text-sm font-medium line-clamp-1">{ad.title}</p>
                </div>
              )}
            </a>
          </div>
        ))}

        {/* 左右箭头 */}
        {showArrows && adData.length > 1 && (
          <>
            <button
              onClick={handlePrev}
              className="absolute left-2 top-1/2 -translate-y-1/2 z-20 bg-black/30 hover:bg-black/50 text-white rounded-full p-1.5 transition-all"
              aria-label="上一张"
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={handleNext}
              className="absolute right-2 top-1/2 -translate-y-1/2 z-20 bg-black/30 hover:bg-black/50 text-white rounded-full p-1.5 transition-all"
              aria-label="下一张"
            >
              <ChevronRight size={20} />
            </button>
          </>
        )}

        {/* 指示器 */}
        {showIndicators && adData.length > 1 && (
          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-20 flex space-x-1.5">
            {adData.map((_, index) => (
              <button
                key={index}
                onClick={() => handleIndicatorClick(index)}
                className={`h-1.5 rounded-full transition-all ${
                  index === currentIndex 
                    ? 'bg-white w-6' 
                    : 'bg-white/50 w-1.5 hover:bg-white/75'
                }`}
                aria-label={`切换到第${index + 1}张`}
              />
            ))}
          </div>
        )}

        {/* 广告标识 */}
        <div className="absolute top-2 right-2 z-20 bg-black/50 text-white text-xs px-2 py-0.5 rounded">
          广告
        </div>
      </div>
    </div>
  )
}
