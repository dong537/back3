import { useState, useEffect } from 'react'
import { X, Gift, Check, Lock, Info } from 'lucide-react'
import Card from './Card'
import Button from './Button'
import { checkinApi, creditApi } from '../api'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'

/**
 * 连续打卡奖励进度组件
 * 显示7天打卡进度和奖励
 */
export default function CheckinProgress({ isOpen, onClose }) {
  const { isLoggedIn } = useAuth()
  const [loading, setLoading] = useState(false)
  const [todayStatus, setTodayStatus] = useState(null)
  const [weeklyProgress, setWeeklyProgress] = useState(null)
  const [streakInfo, setStreakInfo] = useState(null)
  const [currentBalance, setCurrentBalance] = useState(0)

  useEffect(() => {
    if (isOpen && isLoggedIn) {
      loadData()
    }
  }, [isOpen, isLoggedIn])

  const loadData = async () => {
    try {
      setLoading(true)
      const [todayRes, weeklyRes, streakRes, balanceRes] = await Promise.all([
        checkinApi.getTodayStatus().catch(e => ({ data: null })),
        checkinApi.getWeeklyProgress().catch(e => ({ data: null })),
        checkinApi.getStreakInfo().catch(e => ({ data: null })),
        creditApi.getBalance().catch(e => ({ data: null }))
      ])

      if (todayRes.data?.code === 200 || todayRes.data?.code === 0) {
        setTodayStatus(todayRes.data.data || todayRes.data)
      }
      if (weeklyRes.data?.code === 200 || weeklyRes.data?.code === 0) {
        setWeeklyProgress(weeklyRes.data.data || weeklyRes.data)
      }
      if (streakRes.data?.code === 200 || streakRes.data?.code === 0) {
        setStreakInfo(streakRes.data.data || streakRes.data)
      }
      if (balanceRes.data?.code === 200 || balanceRes.data?.code === 0) {
        setCurrentBalance(balanceRes.data.data?.balance || 0)
      }
    } catch (error) {
      logger.error('Load checkin data error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCheckin = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录后再进行打卡')
      return
    }

    try {
      setLoading(true)
      const response = await checkinApi.doCheckin()
      
      // 处理响应数据 - 兼容多种响应格式
      const responseData = response.data
      const isSuccess = responseData?.success || responseData?.code === 200 || responseData?.code === 0
      
      if (isSuccess) {
        const result = responseData.data || responseData
        const message = result?.message || responseData?.message || `打卡成功！获得积分`
        toast.success(message)
        
        // 刷新数据
        await loadData()
        
        // 刷新积分余额
        const balanceRes = await creditApi.getBalance()
        if (balanceRes.data?.code === 200 || balanceRes.data?.code === 0) {
          setCurrentBalance(balanceRes.data.data?.balance || 0)
        }
      } else {
        throw new Error(responseData?.message || '打卡失败')
      }
    } catch (error) {
      logger.error('Checkin error:', error)
      const errorMessage = error.response?.data?.message || error.message || '打卡失败，请稍后重试'
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  if (!isOpen) return null

  const hasCheckedIn = todayStatus?.hasCheckedIn || false
  // 如果今天已打卡，使用今天的连续天数；否则使用昨天的连续天数
  const currentStreak = hasCheckedIn 
    ? (todayStatus?.streakDays || 0)
    : (streakInfo?.currentStreak || 0)
  const daysToNextReward = streakInfo?.daysToNextReward || 0

  // 计算7天的打卡状态（从本周一开始）
  const weekDays = []
  const today = new Date()
  // 获取本周一
  const dayOfWeek = today.getDay()
  const monday = new Date(today)
  monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    const dateStr = date.toISOString().split('T')[0]
    const isToday = dateStr === today.toISOString().split('T')[0]
    const isChecked = weeklyProgress?.checkins?.some(c => {
      const checkinDate = c.checkinDate || c.checkin_date
      return checkinDate === dateStr
    }) || false
    
    weekDays.push({
      day: i + 1,
      date: dateStr,
      isToday,
      isChecked,
      label: isToday ? '今天' : `第${i + 1}天`
    })
  }

  // 奖励配置
  const rewards = [
    { day: 3, label: '连签3天', bonus: 20, icon: '🎁' },
    { day: 7, label: '连签7天', bonus: 50, icon: '🎁' }
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm safe-area-top safe-area-bottom">
      <Card className="w-full max-w-md relative max-h-[90vh] overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition z-10"
        >
          <X size={20} />
        </button>

        <div className="p-6">
          {/* 标题 */}
          <div className="text-center mb-6">
            <h2 className="text-2xl font-bold mb-2">连续打卡奖励</h2>
            {isLoggedIn && (
              <p className="text-sm text-gray-400">
                当前积分：<span className="text-purple-400 font-semibold">{currentBalance}</span>
              </p>
            )}
          </div>

          {/* 7天打卡进度 */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-4">
              {weekDays.map((day, index) => {
                const reward = rewards.find(r => r.day === day.day)
                const isRewardDay = reward !== undefined
                const canClaim = day.isChecked && isRewardDay
                
                return (
                  <div key={day.day} className="flex flex-col items-center relative">
                    {/* 奖励标签 */}
                    {isRewardDay && (
                      <div className={`absolute -top-6 px-2 py-0.5 rounded-full text-xs font-semibold ${
                        canClaim ? 'bg-pink-500 text-white' : 'bg-pink-500/30 text-pink-300'
                      }`}>
                        {reward.label}
                      </div>
                    )}
                    
                    {/* 打卡图标 */}
                    <div className={`w-12 h-12 rounded-full flex items-center justify-center border-2 transition-all ${
                      day.isChecked
                        ? 'bg-green-500/20 border-green-500 text-green-400'
                        : day.isToday && !hasCheckedIn
                        ? 'bg-purple-500/20 border-purple-500 text-purple-400'
                        : 'bg-gray-500/20 border-gray-500 text-gray-400'
                    }`}>
                      {day.isChecked ? (
                        <Check size={24} />
                      ) : isRewardDay ? (
                        <span className="text-2xl">{reward.icon}</span>
                      ) : (
                        <span className="text-lg font-bold">{day.day}</span>
                      )}
                    </div>
                    
                    {/* 日期标签 */}
                    <div className="mt-2 text-xs text-gray-400">
                      {day.label}
                    </div>
                  </div>
                )
              })}
            </div>

            {/* 提示信息 */}
            <div className="flex items-center justify-between text-sm">
              <div className="flex items-center space-x-2 text-gray-300">
                <Info size={16} />
                <span>
                  {hasCheckedIn 
                    ? `① 已打卡，连续${currentStreak}天`
                    : currentStreak > 0
                    ? `再打卡${daysToNextReward}天可领取奖励！`
                    : '今天可以打卡'}
                </span>
              </div>
              <button className="text-purple-400 text-sm hover:text-purple-300">
                明天提醒我 &gt;
              </button>
            </div>
          </div>

          {/* 打卡按钮 */}
          {!isLoggedIn ? (
            <div className="text-center py-6">
              <div className="flex items-center justify-center space-x-2 text-amber-400 mb-4">
                <Lock size={20} />
                <span className="text-sm">请先登录后再进行打卡</span>
              </div>
              <Button
                onClick={onClose}
                className="w-full"
                size="lg"
                variant="secondary"
              >
                去登录
              </Button>
            </div>
          ) : hasCheckedIn ? (
            <div className="text-center py-6">
              <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-500/20 mb-4">
                <Gift size={40} className="text-green-400" />
              </div>
              <p className="text-lg font-bold mb-2">今日已打卡</p>
              <p className="text-sm text-gray-400 mb-4">
                连续打卡 {currentStreak} 天
              </p>
              {daysToNextReward > 0 && (
                <p className="text-sm text-purple-400">
                  再打卡 {daysToNextReward} 天可获得额外奖励！
                </p>
              )}
            </div>
          ) : (
            <div className="text-center py-6">
              <div className="mb-6">
                <div className="text-4xl font-bold text-purple-400 mb-2">
                  +{getRewardForStreak(currentStreak + 1)} 积分
                </div>
                {currentStreak + 1 === 3 && (
                  <p className="text-sm text-pink-400">连续3天额外奖励20积分！</p>
                )}
                {currentStreak + 1 === 7 && (
                  <p className="text-sm text-pink-400">连续7天额外奖励50积分！</p>
                )}
              </div>
              <Button
                onClick={handleCheckin}
                className="w-full"
                size="lg"
                disabled={loading}
              >
                <Gift size={20} />
                <span>{loading ? '打卡中...' : '立即打卡'}</span>
              </Button>
            </div>
          )}

          {/* 奖励规则说明 */}
          <div className="mt-6 pt-6 border-t border-white/10">
            <h3 className="text-sm font-medium mb-3">打卡奖励规则</h3>
            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第1-2天</span>
                <span className="text-purple-400">10 积分/天</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第3-6天</span>
                <span className="text-purple-400">20 积分/天</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">第7天及以上</span>
                <span className="text-purple-400">30 积分/天</span>
              </div>
              <div className="flex items-center justify-between mt-2 pt-2 border-t border-white/5">
                <span className="text-pink-400">连续3天额外奖励</span>
                <span className="text-pink-400 font-semibold">+20 积分</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-pink-400">连续7天额外奖励</span>
                <span className="text-pink-400 font-semibold">+50 积分</span>
              </div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}

/**
 * 根据连续天数计算基础奖励
 */
function getRewardForStreak(streakDays) {
  if (streakDays >= 7) return 30
  if (streakDays >= 3) return 20
  return 10
}
