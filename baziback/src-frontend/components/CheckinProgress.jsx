import { useEffect, useMemo, useState } from 'react'
import { Check, Flame, Gift, Info, Lock, Wallet, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from './Card'
import Button from './Button'
import { toast } from './Toast'
import { checkinApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'
import {
  getBaseRewardForStreak,
  getBonusRewardForStreak,
  getNextRewardMilestone,
  getTotalRewardForStreak,
} from '../utils/checkinRewards'

const CHECKIN_COPY = {
  'zh-CN': {
    loginFirst: '请先登录后再签到',
    checkinFailed: '签到失败',
    checkinFailedRetry: '签到失败，请稍后重试',
    checkinSuccess: (reward, days) =>
      `签到成功，获得 ${reward} 积分，当前已连续签到 ${days} 天。`,
    weekdays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    title: '连续签到奖励',
    currentCredits: '当前积分',
    checkedStatus: (days) => `今天已完成签到，当前连续 ${days} 天。`,
    nextRewardStatus: (days) => `再签到 ${days} 天即可达到下一个奖励节点。`,
    freshCycle: '今天可以开始新的签到周期。',
    nextMilestone: (day) => `下一个奖励节点：第 ${day} 天`,
    loginPrompt: '请先登录后再签到',
    goLogin: '稍后再说',
    checkedToday: '今天已经签到',
    checkedTodayReward: (points) => `今日已到账 ${points} 积分。`,
    previewBaseReward: (points) => `本次基础奖励 ${points} 积分`,
    previewBonusReward: (points) => `包含连续签到奖励 +${points} 积分`,
    checkingIn: '签到中...',
    checkinNow: '立即签到',
    rulesTitle: '签到奖励规则',
    day12: '第 1-2 天',
    day36: '第 3-6 天',
    day7Plus: '第 7 天及以后',
    perDay10: '10 积分/天',
    perDay20: '20 积分/天',
    perDay30: '30 积分/天',
    streak3: '连续签到 3 天',
    streak7: '连续签到 7 天',
    bonus20: '额外 +20 积分',
    bonus50: '额外 +50 积分',
    creditsUnit: '积分',
  },
  'en-US': {
    loginFirst: 'Please sign in before checking in',
    checkinFailed: 'Check-in failed',
    checkinFailedRetry: 'Check-in failed. Please try again later.',
    checkinSuccess: (reward, days) =>
      `Check-in successful. Earned ${reward} credits and reached a ${days}-day streak.`,
    weekdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    title: 'Streak Rewards',
    currentCredits: 'Current credits',
    checkedStatus: (days) =>
      `Check-in completed today. Current streak: ${days} days.`,
    nextRewardStatus: (days) =>
      `Check in for ${days} more day(s) to reach the next reward milestone.`,
    freshCycle: 'You can start a new check-in cycle today.',
    nextMilestone: (day) => `Next milestone: Day ${day}`,
    loginPrompt: 'Please sign in before checking in',
    goLogin: 'Maybe later',
    checkedToday: 'Already checked in today',
    checkedTodayReward: (points) => `Received ${points} credits today.`,
    previewBaseReward: (points) => `Base reward this time: ${points} credits`,
    previewBonusReward: (points) => `Includes streak bonus: +${points} credits`,
    checkingIn: 'Checking in...',
    checkinNow: 'Check in now',
    rulesTitle: 'Reward Rules',
    day12: 'Days 1-2',
    day36: 'Days 3-6',
    day7Plus: 'Day 7 and after',
    perDay10: '10 credits/day',
    perDay20: '20 credits/day',
    perDay30: '30 credits/day',
    streak3: '3-day streak',
    streak7: '7-day streak',
    bonus20: 'Extra +20 credits',
    bonus50: 'Extra +50 credits',
    creditsUnit: 'credits',
  },
}

export default function CheckinProgress({ isOpen, onClose }) {
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = CHECKIN_COPY[locale]
  const [loading, setLoading] = useState(false)
  const [todayStatus, setTodayStatus] = useState(null)
  const [weeklyProgress, setWeeklyProgress] = useState(null)
  const [streakInfo, setStreakInfo] = useState(null)
  const [currentBalance, setCurrentBalance] = useState(0)

  useEffect(() => {
    if (!isOpen) return

    if (!isLoggedIn) {
      setTodayStatus(null)
      setWeeklyProgress(null)
      setStreakInfo(null)
      setCurrentBalance(0)
      return
    }

    void loadData()
  }, [isLoggedIn, isOpen])

  const loadData = async () => {
    try {
      setLoading(true)
      const response = await checkinApi.getOverview()
      const data = response.data?.data || {}

      setTodayStatus(data.todayStatus || {})
      setWeeklyProgress(data.weeklyProgress || {})
      setStreakInfo(data.streakInfo || {})
      setCurrentBalance(data.balance || 0)
    } catch (error) {
      logger.error('Load checkin progress failed:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCheckin = async () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      return
    }

    try {
      setLoading(true)
      const response = await checkinApi.doCheckin()
      const result = response.data?.data

      if (response.data?.code !== 200 || !result) {
        throw new Error(response.data?.message || copy.checkinFailed)
      }

      toast.success(
        result.message ||
          copy.checkinSuccess(result.totalReward || 0, result.streakDays || 0)
      )

      await loadData()
    } catch (error) {
      logger.error('Checkin failed:', error)
      toast.error(
        error.response?.data?.message ||
          error.message ||
          copy.checkinFailedRetry
      )
    } finally {
      setLoading(false)
    }
  }

  const weekDays = useMemo(() => {
    const today = new Date()
    const currentDay = today.getDay()
    const monday = new Date(today)
    monday.setDate(today.getDate() - (currentDay === 0 ? 6 : currentDay - 1))

    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date(monday)
      date.setDate(monday.getDate() + index)
      const dateStr = toLocalDateString(date)
      const checked = weeklyProgress?.checkins?.some((item) => {
        const checkinDate = item.checkinDate || item.checkin_date
        return checkinDate === dateStr
      })

      return {
        day: index + 1,
        date: dateStr,
        isToday: dateStr === toLocalDateString(today),
        isChecked: Boolean(checked),
        label: copy.weekdays[index],
      }
    })
  }, [weeklyProgress, copy])

  if (!isOpen) return null

  const hasCheckedIn = Boolean(todayStatus?.hasCheckedIn)
  const currentStreak = hasCheckedIn
    ? todayStatus?.streakDays || 0
    : streakInfo?.currentStreak || 0
  const nextMilestone = getNextRewardMilestone(currentStreak)
  const daysToNextReward = Math.max(0, nextMilestone - currentStreak)
  const previewStreak = currentStreak + 1
  const previewBaseReward = getBaseRewardForStreak(previewStreak)
  const previewBonusReward = getBonusRewardForStreak(previewStreak)
  const previewTotalReward = getTotalRewardForStreak(previewStreak)
  const rewardDays = new Set([3, 7])

  return (
    <div className="safe-area-bottom safe-area-top fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel relative max-h-[90vh] w-full max-w-md overflow-y-auto border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.96),rgba(14,11,10,0.88))]">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
        >
          <X size={20} />
        </button>

        <div className="p-6">
          <div className="mb-6 text-center">
            <h2 className="mb-2 text-2xl font-bold text-[#f4ece1]">
              {copy.title}
            </h2>
            {isLoggedIn && (
              <div className="inline-flex items-center gap-2 text-sm text-[#bdaa94]">
                <Wallet size={16} className="text-[#dcb86f]" />
                <span>
                  {copy.currentCredits} {currentBalance}
                </span>
              </div>
            )}
          </div>

          <div className="mb-6">
            <div className="mb-4 grid grid-cols-4 gap-3 sm:grid-cols-7">
              {weekDays.map((day) => {
                const isRewardDay = rewardDays.has(day.day)

                return (
                  <div
                    key={day.date}
                    className="flex flex-col items-center gap-2"
                  >
                    <div
                      className={`flex h-12 w-12 items-center justify-center rounded-full border-2 transition-all ${
                        day.isChecked
                          ? 'border-[#d0a85b]/40 bg-[#6a4a1e]/20 text-[#f0d9a5]'
                          : day.isToday && !hasCheckedIn
                            ? 'border-[#a34224]/40 bg-[#7a3218]/18 text-[#e19a84]'
                            : 'border-white/10 bg-white/[0.03] text-[#8f7b66]'
                      }`}
                    >
                      {day.isChecked ? (
                        <Check size={22} />
                      ) : (
                        <span className="text-sm font-semibold">{day.day}</span>
                      )}
                    </div>
                    <div className="text-xs text-[#8f7b66]">{day.label}</div>
                    {isRewardDay && (
                      <div className="rounded-full border border-[#d0a85b]/18 bg-[#6a4a1e]/16 px-2 py-0.5 text-[10px] text-[#dcb86f]">
                        {day.day === 3 ? '+20' : '+50'}
                      </div>
                    )}
                  </div>
                )
              })}
            </div>

            <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4 text-sm">
              <div className="mb-2 flex items-center gap-2 text-[#e4d6c8]">
                <Info size={16} className="text-[#dcb86f]" />
                <span>
                  {hasCheckedIn
                    ? copy.checkedStatus(currentStreak)
                    : currentStreak > 0
                      ? copy.nextRewardStatus(daysToNextReward)
                      : copy.freshCycle}
                </span>
              </div>
              <div className="flex items-center gap-2 text-[#dcb86f]">
                <Flame size={16} />
                <span>{copy.nextMilestone(nextMilestone)}</span>
              </div>
            </div>
          </div>

          {!isLoggedIn ? (
            <div className="py-6 text-center">
              <div className="mb-4 flex items-center justify-center gap-2 text-[#dcb86f]">
                <Lock size={20} />
                <span className="text-sm">{copy.loginPrompt}</span>
              </div>
              <Button
                onClick={onClose}
                className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
                size="lg"
                variant="secondary"
              >
                {copy.goLogin}
              </Button>
            </div>
          ) : hasCheckedIn ? (
            <div className="py-6 text-center">
              <div className="mb-4 inline-flex h-20 w-20 items-center justify-center rounded-full border border-[#d0a85b]/20 bg-[#6a4a1e]/16">
                <Gift size={40} className="text-[#dcb86f]" />
              </div>
              <p className="mb-2 text-lg font-bold text-[#f4ece1]">
                {copy.checkedToday}
              </p>
              <p className="text-sm text-[#8f7b66]">
                {copy.checkedTodayReward(todayStatus?.pointsEarned || 0)}
              </p>
            </div>
          ) : (
            <div className="py-6 text-center">
              <div className="mb-6">
                <div className="mb-2 text-4xl font-bold text-[#f0d9a5]">
                  +{previewTotalReward} {copy.creditsUnit}
                </div>
                <p className="text-sm text-[#8f7b66]">
                  {copy.previewBaseReward(previewBaseReward)}
                </p>
                {previewBonusReward > 0 && (
                  <p className="mt-2 text-sm text-[#e19a84]">
                    {copy.previewBonusReward(previewBonusReward)}
                  </p>
                )}
              </div>
              <Button
                onClick={handleCheckin}
                className="w-full"
                size="lg"
                loading={loading}
              >
                <Gift size={20} />
                <span>{loading ? copy.checkingIn : copy.checkinNow}</span>
              </Button>
            </div>
          )}

          <div className="mt-6 border-t border-white/10 pt-6">
            <h3 className="mb-3 text-sm font-medium text-[#f4ece1]">
              {copy.rulesTitle}
            </h3>
            <div className="space-y-2 text-sm">
              <RuleRow label={copy.day12} value={copy.perDay10} />
              <RuleRow label={copy.day36} value={copy.perDay20} />
              <RuleRow label={copy.day7Plus} value={copy.perDay30} />
              <RuleRow label={copy.streak3} value={copy.bonus20} highlighted />
              <RuleRow label={copy.streak7} value={copy.bonus50} highlighted />
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}

function RuleRow({ label, value, highlighted = false }) {
  return (
    <div
      className={`flex items-center justify-between ${
        highlighted ? 'border-t border-white/5 pt-2' : ''
      }`}
    >
      <span className={highlighted ? 'text-[#e19a84]' : 'text-[#8f7b66]'}>
        {label}
      </span>
      <span
        className={
          highlighted ? 'font-semibold text-[#f0d9a5]' : 'text-[#dcb86f]'
        }
      >
        {value}
      </span>
    </div>
  )
}

function toLocalDateString(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
