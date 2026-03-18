import { useEffect, useRef, useState } from 'react'
import { isMobile } from '../utils/mobile'

/**
 * 穿山甲轮播图广告组件
 * 参考文档：https://www.csjplatform.com/union/media/union/download/detail?id=195&docId=27627
 */
export default function PangleBannerAd({ 
  slotId, 
  width = '100%', 
  height = 'auto',
  className = '',
  onAdLoad,
  onAdError 
}) {
  const adContainerRef = useRef(null)
  const [adLoaded, setAdLoaded] = useState(false)
  const [adError, setAdError] = useState(false)
  const adInstanceRef = useRef(null)

  useEffect(() => {
    // 检查是否在移动端
    if (!isMobile()) {
      console.log('穿山甲广告：仅在移动端显示')
      return
    }

    // 检查是否已加载穿山甲SDK
    if (typeof window === 'undefined' || !window.bytedance) {
      console.warn('穿山甲SDK未加载，请确保已引入SDK脚本')
      // 动态加载SDK（如果需要）
      loadPangleSDK()
      return
    }

    // 如果没有提供广告位ID，使用占位符
    if (!slotId) {
      console.warn('未提供穿山甲广告位ID')
      return
    }

    // 初始化广告
    initAd()

    // 清理函数
    return () => {
      if (adInstanceRef.current) {
        try {
          adInstanceRef.current.destroy?.()
        } catch (e) {
          console.error('销毁广告实例失败:', e)
        }
      }
    }
  }, [slotId])

  // 加载穿山甲SDK
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

  // 初始化广告
  const initAd = () => {
    if (!window.bytedance || !adContainerRef.current || !slotId) {
      return
    }

    try {
      // 创建Banner广告实例
      // 注意：实际使用时需要根据穿山甲SDK的最新API调整
      // 如果SDK API不同，请参考最新文档调整
      const adInstance = window.bytedance?.createBannerAd?.({
        adUnitId: slotId,
        container: adContainerRef.current,
        style: {
          left: 0,
          top: 0,
          width: '100%',
        }
      })

      adInstanceRef.current = adInstance

      // 监听广告加载成功
      adInstance.onLoad(() => {
        console.log('穿山甲广告加载成功')
        setAdLoaded(true)
        onAdLoad?.()
      })

      // 监听广告加载失败
      adInstance.onError((err) => {
        console.error('穿山甲广告加载失败:', err)
        setAdError(true)
        onAdError?.(err)
      })

      // 加载广告
      adInstance.load()

    } catch (error) {
      console.error('初始化穿山甲广告失败:', error)
      setAdError(true)
      onAdError?.(error)
    }
  }

  // 如果不在移动端，不显示广告
  if (typeof window !== 'undefined' && !isMobile()) {
    return null
  }

  // 如果广告加载失败，显示占位图（可选）
  if (adError) {
    return (
      <div className={`bg-gray-100 rounded-lg flex items-center justify-center ${className}`} style={{ minHeight: '150px' }}>
        <div className="text-center text-gray-400 text-sm">
          <p>广告加载失败</p>
        </div>
      </div>
    )
  }

  return (
    <div className={`relative ${className}`} style={{ width, height }}>
      {/* 广告容器 */}
      <div 
        ref={adContainerRef}
        className="w-full"
        style={{ 
          minHeight: adLoaded ? 'auto' : '150px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        {/* 加载中占位 */}
        {!adLoaded && !adError && (
          <div className="w-full h-40 bg-gradient-to-r from-gray-100 to-gray-200 rounded-lg animate-pulse flex items-center justify-center">
            <div className="text-gray-400 text-sm">广告加载中...</div>
          </div>
        )}
      </div>
      
      {/* 广告标识（可选） */}
      {adLoaded && (
        <div className="absolute top-1 right-1 text-xs text-gray-400 bg-white/80 px-1 rounded">
          广告
        </div>
      )}
    </div>
  )
}
