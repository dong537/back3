import { useEffect, useState } from 'react'
import { Calendar, Flame, Gift, Lock, Wallet, X } from 'lucide-react'
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
  getTotalRewardForStreak,
} from '../utils/checkinRewards'

const DAILY_COPY = {
  'zh-CN': {
    loginFirst: '请先登录后再签到',
    checkinFailed: '签到失败',
    checkinFailedRetry: '签到失败，请稍后重试',
    checkinSuccess: (reward, days) =>
      `签到成功，获得 ${reward} 积分，当前已连续签到 ${days} 天。`,
    title: '每日签到',
    subtitle: '连续签到可以拿到更高积分和额外奖励。',
    currentCredits: '当前积分',
    streak: (days) => `已连续签到 ${days} 天`,
    checkedToday: '今天已经签到',
    checkedTodayDesc: (reward, next) =>
      `今日已到账 ${reward} 积分，明天可继续领取 ${next} 积分。`,
    nextReward: (reward) => `+${reward} 积分`,
    baseReward: (reward) => `本次基础奖励 ${reward} 积分`,
    bonusReward: (reward) => `包含连续签到奖励 +${reward} 积分`,
    loginPrompt: '请先登录后再签到',
    goLogin: '稍后再说',
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
  },
  'en-US': {
    loginFirst: 'Please sign in before checking in',
    checkinFailed: 'Check-in failed',
    checkinFailedRetry: 'Check-in failed. Please try again later.',
    checkinSuccess: (reward, days) =>
      `Check-in successful. Earned ${reward} credits and reached a ${days}-day streak.`,
    title: 'Daily Check-in',
    subtitle: 'Keep your streak to earn more credits and bonus rewards.',
    currentCredits: 'Current credits',
    streak: (days) => `${days}-day streak`,
    checkedToday: 'Already checked in today',
    checkedTodayDesc: (reward, next) =>
      `Received ${reward} credits today. You can claim ${next} credits tomorrow.`,
    nextReward: (reward) => `+${reward} credits`,
    baseReward: (reward) => `Base reward this time: ${reward} credits`,
    bonusReward: (reward) => `Includes streak bonus: +${reward} credits`,
    loginPrompt: 'Please sign in before checking in',
    goLogin: 'Maybe later',
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
  },
}

export default function DailyCheckin({ isOpen, onClose }) {
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = DAILY_COPY[locale]
  const [loading, setLoading] = useState(false)
  const [checkedIn, setCheckedIn] = useState(false)
  const [streak, setStreak] = useState(0)
  const [reward, setReward] = useState(0)
  const [currentBalance, setCurrentBalance] = useState(0)

  useEffect(() => {
    if (!isOpen) return

    if (!isLoggedIn) {
      setCheckedIn(false)
      setStreak(0)
      setReward(0)
      setCurrentBalance(0)
      return
    }

    void loadTodayStatus()
  }, [isLoggedIn, isOpen])

  const loadTodayStatus = async () => {
    try {
      const response = await checkinApi.getOverview()
      const data = response.data?.data || {}
      const todayStatus = data.todayStatus || {}

      setCheckedIn(Boolean(todayStatus.hasCheckedIn))
      setStreak(todayStatus.streakDays || 0)
      setReward(todayStatus.pointsEarned || 0)
      setCurrentBalance(data.balance || 0)
    } catch (error) {
      logger.error('Load today checkin status failed:', error)
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

      setCheckedIn(true)
      setStreak(result.streakDays || 0)
      setReward(result.totalReward || 0)
      setCurrentBalance(result.currentBalance || 0)

      toast.success(
        result.message ||
          copy.checkinSuccess(result.totalReward || 0, result.streakDays || 0)
      )
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

  if (!isOpen) return null

  const nextStreak = checkedIn ? streak + 1 : Math.max(streak, 0) + 1
  const nextBaseReward = getBaseRewardForStreak(nextStreak)
  const nextBonusReward = getBonusRewardForStreak(nextStreak)
  const nextTotalReward = getTotalRewardForStreak(nextStreak)

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel relative w-full max-w-md border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.96),rgba(14,11,10,0.88))]">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
        >
          <X size={20} />
        </button>

        <div className="p-6">
          <div className="mb-6 text-center">
            <div className="mystic-icon-badge mb-4 inline-flex h-16 w-16 rounded-full">
              <Calendar size={32} className="text-white" />
            </div>
            <h2 className="mb-2 text-2xl font-bold text-[#f4ece1]">
              {copy.title}
            </h2>
            <p className="text-[#8f7b66]">{copy.subtitle}</p>
          </div>

          {isLoggedIn && (
            <div className="mb-5 flex items-center justify-center gap-2 text-sm text-[#bdaa94]">
              <Wallet size={16} className="text-[#dcb86f]" />
              <span>
                {copy.currentCredits} {currentBalance}
              </span>
            </div>
          )}

          {streak > 0 && (
            <div className="mb-6 flex items-center justify-center gap-2">
              <Flame className="text-[#e19a84]" size={20} />
              <span className="text-lg font-bold text-[#e19a84]">
                {copy.streak(streak)}
              </span>
            </div>
          )}

          {checkedIn ? (
            <div className="py-8 text-center">
              <div className="mb-4 inline-flex h-20 w-20 items-center justify-center rounded-full border border-[#d0a85b]/20 bg-[#6a4a1e]/16">
                <Gift size={40} className="text-[#dcb86f]" />
              </div>
              <p className="mb-2 text-lg font-bold text-[#f4ece1]">
                {copy.checkedToday}
              </p>
              <p className="text-sm text-[#8f7b66]">
                {copy.checkedTodayDesc(reward, nextTotalReward)}
              </p>
            </div>
          ) : (
            <div className="py-8 text-center">
              <div className="mb-6">
                <div className="mb-2 text-4xl font-bold text-[#f0d9a5]">
                  {copy.nextReward(nextTotalReward)}
                </div>
                <p className="text-sm text-[#8f7b66]">
                  {copy.baseReward(nextBaseReward)}
                </p>
                {nextBonusReward > 0 && (
                  <p className="mt-2 text-sm text-[#e19a84]">
                    {copy.bonusReward(nextBonusReward)}
                  </p>
                )}
              </div>

              {!isLoggedIn ? (
                <div className="space-y-4">
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
                    <span>{copy.goLogin}</span>
                  </Button>
                </div>
              ) : (
                <Button
                  onClick={handleCheckin}
                  className="w-full"
                  size="lg"
                  loading={loading}
                >
                  <Gift size={20} />
                  <span>{loading ? copy.checkingIn : copy.checkinNow}</span>
                </Button>
              )}
            </div>
          )}

          <div className="mt-6 border-t border-white/10 pt-6">
            <h3 className="mb-3 text-sm font-medium text-[#f4ece1]">
              {copy.rulesTitle}
            </h3>
            <div className="space-y-2 text-sm">
              <DailyRule label={copy.day12} value={copy.perDay10} />
              <DailyRule label={copy.day36} value={copy.perDay20} />
              <DailyRule label={copy.day7Plus} value={copy.perDay30} />
              <DailyRule
                label={copy.streak3}
                value={copy.bonus20}
                highlighted
              />
              <DailyRule
                label={copy.streak7}
                value={copy.bonus50}
                highlighted
              />
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}

function DailyRule({ label, value, highlighted = false }) {
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
