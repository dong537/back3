import { useState, useRef, useEffect } from 'react'
import { Play, Coins, X, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { creditApi } from '../api'
import { toast } from './Toast'
import { POINTS_EARN } from '../utils/pointsConfig'
import { isMobile } from '../utils/mobile'
import PangleBannerAd from './PangleBannerAd'

/**
 * 观看广告获得积分组件
 * 用户观看完整广告后获得10积分
 */
export default function WatchAdForPoints({ 
  onSuccess,
  className = '' 
}) {
  const { isLoggedIn, refreshCredits } = useAuth()
  const [showAd, setShowAd] = useState(false)
  const [adWatched, setAdWatched] = useState(false)
  const [isEarning, setIsEarning] = useState(false)
  const [watchStartTime, setWatchStartTime] = useState(null)
  const adWatchTimerRef = useRef(null)
  const minWatchTime = 5000 // 最少观看5秒才能获得积分

  // 获取广告位ID
  const adSlotId = import.meta.env.VITE_PANGLE_SLOT_ID || import.meta.env.VITE_PANGLE_CAROUSEL_SLOT_ID

  useEffect(() => {
    return () => {
      if (adWatchTimerRef.current) {
        clearTimeout(adWatchTimerRef.current)
      }
    }
  }, [])

  const handleStartWatch = () => {
    if (!isLoggedIn) {
      toast.error('请先登录')
      return
    }

    if (!isMobile()) {
      toast.warning('观看广告功能仅在移动端可用')
      return
    }

    if (!adSlotId || adSlotId === 'YOUR_PANGLE_SLOT_ID') {
      toast.error('广告位未配置，无法观看广告')
      return
    }

    setShowAd(true)
    setAdWatched(false)
    setWatchStartTime(Date.now())
  }

  const handleAdLoad = () => {
    // 广告加载成功，开始计时
    if (watchStartTime) {
      const elapsed = Date.now() - watchStartTime
      const remaining = Math.max(0, minWatchTime - elapsed)
      
      adWatchTimerRef.current = setTimeout(() => {
        setAdWatched(true)
      }, remaining)
    }
  }

  const handleAdClick = () => {
    // 用户点击广告，视为观看完成
    if (!adWatched && watchStartTime) {
      const elapsed = Date.now() - watchStartTime
      // 至少观看3秒才能点击获得积分
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
      const res = await creditApi.earnByWatchingAd()
      if (res.data?.code === 200) {
        const { points, newBalance } = res.data.data
        await refreshCredits()
        toast.success(`观看广告成功！获得 ${points} 积分`)
        setShowAd(false)
        setAdWatched(false)
        onSuccess?.(points, newBalance)
      } else {
        toast.error(res.data?.message || '获得积分失败')
      }
    } catch (error) {
      console.error('获得积分失败:', error)
      toast.error(error?.message || '获得积分失败，请稍后重试')
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
      <div className={`fixed inset-0 z-[200] bg-black/50 flex items-center justify-center p-4 ${className}`}>
        <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden">
          {/* 头部 */}
          <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-4 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Coins className="text-white" size={20} />
              <h3 className="text-white font-bold">观看广告获得积分</h3>
            </div>
            <button
              onClick={handleClose}
              className="text-white hover:bg-white/20 rounded-full p-1 transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          {/* 广告区域 */}
          <div className="p-4">
            <div className="mb-4">
              <p className="text-sm text-gray-600 mb-2">
                观看完整广告可获得 <span className="font-bold text-purple-600">{POINTS_EARN.WATCH_AD}</span> 积分
              </p>
              {!adWatched && (
                <div className="flex items-center space-x-2 text-xs text-gray-500">
                  <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-purple-600"></div>
                  <span>请观看广告...</span>
                </div>
              )}
            </div>

            <div 
              className="bg-gray-100 rounded-lg overflow-hidden mb-4 cursor-pointer" 
              style={{ minHeight: '200px' }}
              onClick={handleAdClick}
            >
              <PangleBannerAd
                slotId={adSlotId}
                onAdLoad={handleAdLoad}
              />
            </div>

            {/* 获得积分按钮 */}
            {adWatched && (
              <button
                onClick={handleEarnPoints}
                disabled={isEarning}
                className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white py-3 rounded-lg font-medium flex items-center justify-center space-x-2 hover:from-purple-500 hover:to-pink-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isEarning ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>正在获得积分...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={18} />
                    <span>获得 {POINTS_EARN.WATCH_AD} 积分</span>
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
      className={`bg-gradient-to-r from-purple-600 to-pink-600 text-white px-4 py-2 rounded-lg font-medium flex items-center space-x-2 hover:from-purple-500 hover:to-pink-500 transition-all ${className}`}
    >
      <Play size={18} />
      <span>观看广告 +{POINTS_EARN.WATCH_AD}积分</span>
    </button>
  )
}
