import { useState, useEffect } from 'react'
import { X, Calendar, Gift, Flame, Lock } from 'lucide-react'
import Card from './Card'
import Button from './Button'
import { checkinApi, creditApi } from '../api'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from 'react-i18next'
import { logger } from '../utils/logger'

/**
 * 每日签到组件
 */
export default function DailyCheckin({ isOpen, onClose }) {
  const { t } = useTranslation()
  const { isLoggedIn } = useAuth()
  const [loading, setLoading] = useState(false)
  const [checkedIn, setCheckedIn] = useState(false)
  const [streak, setStreak] = useState(0)
  const [reward, setReward] = useState(0)
  const [currentBalance, setCurrentBalance] = useState(0)

  useEffect(() => {
    if (isOpen && isLoggedIn) {
      loadTodayStatus()
    } else if (isOpen && !isLoggedIn) {
      setCheckedIn(false)
      setStreak(0)
    }
  }, [isOpen, isLoggedIn])

  const loadTodayStatus = async () => {
    try {
      const response = await checkinApi.getTodayStatus()
      if (response.data?.code === 200) {
        const data = response.data.data
        setCheckedIn(data.hasCheckedIn || false)
        setStreak(data.streakDays || 0)
        setReward(data.pointsEarned || 0)
      }
      
      // 加载积分余额
      const balanceRes = await creditApi.getBalance()
      if (balanceRes.data?.code === 200) {
        setCurrentBalance(balanceRes.data.data.balance || 0)
      }
    } catch (error) {
      logger.error('Load today status error:', error)
    }
  }

  const handleCheckin = async () => {
    // 检查登录状态
    if (!isLoggedIn) {
      toast.warning('请先登录后再进行签到')
      return
    }

    try {
      setLoading(true)
      const response = await checkinApi.doCheckin()
      
      if (response.data?.code === 200) {
        const result = response.data.data
        setCheckedIn(true)
        setStreak(result.streakDays)
        setReward(result.totalReward)
        toast.success(result.message || `签到成功！获得${result.totalReward}积分，连续签到${result.streakDays}天`)
        
        // 刷新积分余额
        const balanceRes = await creditApi.getBalance()
        if (balanceRes.data?.code === 200) {
          setCurrentBalance(balanceRes.data.data?.balance || 0)
        }
      } else {
        throw new Error(response.data?.message || '签到失败')
      }
    } catch (error) {
      logger.error('Checkin error:', error)
      const errorMessage = error.response?.data?.message || error.message || '签到失败，请稍后重试'
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  const getRewardForStreak = (days) => {
    if (days >= 7) return 30
    if (days >= 3) return 20
    return 10
  }

  const getNextReward = () => {
    const nextStreak = streak + 1
    return getRewardForStreak(nextStreak)
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <Card className="w-full max-w-md relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition"
        >
          <X size={20} />
        </button>

        <div className="p-6">
          <div className="text-center mb-6">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-r from-orange-500 to-red-500 mb-4">
              <Calendar size={32} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold mb-2">每日签到</h2>
            <p className="text-gray-400">连续签到可获得更多奖励</p>
          </div>

          {/* 连续签到天数 */}
          {streak > 0 && (
            <div className="flex items-center justify-center space-x-2 mb-6">
              <Flame className="text-orange-400" size={20} />
              <span className="text-lg font-bold text-orange-400">
                连续签到 {streak} 天
              </span>
            </div>
          )}

          {/* 签到状态 */}
          {checkedIn ? (
            <div className="text-center py-8">
              <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-500/20 mb-4">
                <Gift size={40} className="text-green-400" />
              </div>
              <p className="text-lg font-bold mb-2">今日已签到</p>
              <p className="text-sm text-gray-400">
                明天再来签到可获得 {getNextReward()} 积分
              </p>
            </div>
          ) : (
            <div className="text-center py-8">
              <div className="mb-6">
                <div className="text-4xl font-bold text-skin-primary mb-2">
                  +{getNextReward()} 积分
                </div>
                <p className="text-sm text-gray-400">
                  连续签到 {streak + 1} 天可获得此奖励
                </p>
              </div>
              {!isLoggedIn ? (
                <div className="space-y-4">
                  <div className="flex items-center justify-center space-x-2 text-amber-400 mb-4">
                    <Lock size={20} />
                    <span className="text-sm">请先登录后再进行签到</span>
                  </div>
                  <Button
                    onClick={onClose}
                    className="w-full"
                    size="lg"
                    variant="secondary"
                  >
                    <span>去登录</span>
                  </Button>
                </div>
              ) : (
                <Button
                  onClick={handleCheckin}
                  className="w-full"
                  size="lg"
                  disabled={loading}
                >
                  <Gift size={20} />
                  <span>{loading ? '签到中...' : '立即签到'}</span>
                </Button>
              )}
            </div>
          )}

          {/* 签到奖励说明 */}
          <div className="mt-6 pt-6 border-t border-white/10">
            <h3 className="text-sm font-medium mb-3">签到奖励规则</h3>
            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第1-2天</span>
                <span className="text-skin-primary">10 积分/天</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第3-6天</span>
                <span className="text-skin-primary">20 积分/天</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第7天及以上</span>
                <span className="text-skin-primary">30 积分/天</span>
              </div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}
