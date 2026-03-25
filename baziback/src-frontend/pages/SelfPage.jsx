import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  ChevronRight,
  User as UserIcon,
  Coins,
  Calendar,
  Compass,
  Settings,
  LogOut,
  History,
  Heart,
  FileText,
  ScrollText,
  PlayCircle,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from '../components/Card'
import HistoryModal from '../components/HistoryModal'
import CheckinProgress from '../components/CheckinProgress'
import WatchAdForPoints from '../components/WatchAdForPoints'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { checkin } from '../utils/referral'
import { POINTS_COST, POINTS_EARN } from '../utils/pointsConfig'
import { handleComingSoon, COMING_SOON_FEATURES } from '../utils/comingSoon'
import { resolvePageLocale } from '../utils/displayText'

const SELF_COPY = {
  'zh-CN': {
    title: '个人中心',
    signIn: '点击登录',
    guest: '未登录',
    guestUser: '未登录用户',
    memberLevels: {
      premium: '星耀会员',
      advanced: '高级会员',
      trial: '体验会员',
      standard: '普通用户',
    },
    stats: {
      totalDivinations: '总占卜',
      favorites: '收藏',
      credits: '积分',
    },
    menu: {
      history: '历史记录',
      favorites: '我的收藏',
      settings: '设置',
      privacy: '隐私政策',
      agreement: '用户协议',
    },
    checkinTitle: '每日签到',
    checkinReady: (days) => `今日未签到，当前连续 ${days} 天`,
    checkinDone: (days) => `今日已签到，当前连续 ${days} 天`,
    checkinLoginFirst: '请先登录',
    creditsTitle: '积分中心',
    spending: '积分消耗',
    earning: '获取积分',
    aiInterpret: 'AI 解读',
    aiChat: 'AI 智能对话',
    basicDivination: '基础占卜',
    free: '免费',
    dailyCheckin: '每日签到',
    streak3: '连续签到 3 天',
    streak7: '连续签到 7 天',
    inviteFriend: '邀请好友',
    register: '新用户注册',
    watchAd: '观看广告',
    watchAdTitle: '观看广告获得积分',
    watchAdDesc: (points) => `完整观看广告可获得 ${points} 积分`,
    watchAdSuccess: (points, balance) =>
      `成功获得 ${points} 积分，当前余额：${balance}`,
    recentTitle: '最近占卜',
    viewAll: '查看全部',
    noHistory: '暂无历史记录',
    untitled: '无标题',
    recordTypes: {
      yijing: '易经',
      tarot: '塔罗',
      bazi: '八字',
      other: '其他',
    },
    logout: '退出登录',
    logoutSuccess: '已退出登录',
  },
  'en-US': {
    title: 'Profile',
    signIn: 'Sign in',
    guest: 'Guest',
    guestUser: 'Guest User',
    memberLevels: {
      premium: 'Premium Member',
      advanced: 'Advanced Member',
      trial: 'Trial Member',
      standard: 'Standard Member',
    },
    stats: {
      totalDivinations: 'Readings',
      favorites: 'Favorites',
      credits: 'Credits',
    },
    menu: {
      history: 'History',
      favorites: 'My Favorites',
      settings: 'Settings',
      privacy: 'Privacy Policy',
      agreement: 'User Agreement',
    },
    checkinTitle: 'Daily Check-in',
    checkinReady: (days) =>
      `Not checked in today, current streak: ${days} days`,
    checkinDone: (days) => `Checked in today, current streak: ${days} days`,
    checkinLoginFirst: 'Please sign in first',
    creditsTitle: 'Credits Center',
    spending: 'Spending',
    earning: 'Ways to Earn',
    aiInterpret: 'AI Interpretation',
    aiChat: 'AI Chat',
    basicDivination: 'Basic Divination',
    free: 'Free',
    dailyCheckin: 'Daily check-in',
    streak3: '3-day streak',
    streak7: '7-day streak',
    inviteFriend: 'Invite friends',
    register: 'New user registration',
    watchAd: 'Watch ad',
    watchAdTitle: 'Earn Credits by Watching Ads',
    watchAdDesc: (points) => `Watch a full ad to earn ${points} credits`,
    watchAdSuccess: (points, balance) =>
      `Earned ${points} credits. Current balance: ${balance}`,
    recentTitle: 'Recent Readings',
    viewAll: 'View all',
    noHistory: 'No history yet',
    untitled: 'Untitled',
    recordTypes: {
      yijing: 'Yijing',
      tarot: 'Tarot',
      bazi: 'Bazi',
      other: 'Other',
    },
    logout: 'Sign out',
    logoutSuccess: 'Signed out successfully',
  },
}

function getUserLevel(credits, copy) {
  if (credits >= 10000) return copy.memberLevels.premium
  if (credits >= 3000) return copy.memberLevels.advanced
  if (credits > 0) return copy.memberLevels.trial
  return copy.memberLevels.standard
}

export default function SelfPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SELF_COPY[locale]
  const { isLoggedIn, user, credits, logout, refreshCredits } = useAuth()
  const [stats, setStats] = useState({
    totalDivinations: 0,
    todayDivinations: 0,
    favorites: 0,
  })
  const [recentHistory, setRecentHistory] = useState([])
  const [showHistory, setShowHistory] = useState(false)
  const [showCheckinProgress, setShowCheckinProgress] = useState(false)

  useEffect(() => {
    loadStats()
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  const loadStats = () => {
    const history = historyStorage.getAll()
    const favorites = favoritesStorage.getAll()
    const today = new Date().toDateString()

    const todayCount = history.filter((item) => {
      const itemDate = new Date(item.timestamp).toDateString()
      return itemDate === today
    }).length

    setStats({
      totalDivinations: history.length,
      todayDivinations: todayCount,
      favorites: favorites.length,
    })

    setRecentHistory(history.slice(0, 5))
  }

  const handleLogout = () => {
    logout()
    toast.success(copy.logoutSuccess)
    navigate('/')
  }

  const formatDate = (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleDateString(locale, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const menuItems = useMemo(
    () => [
      {
        icon: History,
        label: copy.menu.history,
        onClick: () => setShowHistory(true),
      },
      { icon: Heart, label: copy.menu.favorites, path: '/favorites' },
      {
        icon: Settings,
        label: copy.menu.settings,
        onClick: handleComingSoon(COMING_SOON_FEATURES.SETTINGS),
      },
    ],
    [copy]
  )

  const legalItems = useMemo(
    () => [
      { icon: FileText, label: copy.menu.privacy, path: '/privacy-policy' },
      { icon: ScrollText, label: copy.menu.agreement, path: '/user-agreement' },
    ],
    [copy]
  )

  const displayName =
    user?.nickname ||
    user?.username ||
    (isLoggedIn ? copy.guest : copy.guestUser)
  const userLevel = getUserLevel(credits ?? 0, copy)
  const streak = checkin.getStreak()

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="safe-area-top sticky top-0 z-50 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="flex items-center justify-between px-4 py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-colors hover:bg-white/10"
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="font-serif-title text-lg font-bold text-white">{copy.title}</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="mx-auto max-w-5xl px-1 pb-20 pt-1">
        <Card className="mb-4 mt-1 border border-white/10 bg-[linear-gradient(135deg,rgba(53,22,19,0.98),rgba(92,36,24,0.9),rgba(34,20,17,0.95))] shadow-[0_24px_80px_rgba(0,0,0,0.35)]">
          <div className="p-6 text-white">
            <div className="flex items-center space-x-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-white/15 shadow-md">
                {user?.avatar ? (
                  <img
                    src={user.avatar}
                    alt="avatar"
                    className="h-full w-full rounded-full object-cover"
                  />
                ) : (
                  <UserIcon size={32} className="text-white" />
                )}
              </div>
              <div className="flex-1">
                {isLoggedIn ? (
                  <>
                    <h2 className="text-2xl font-extrabold tracking-tight">
                      {displayName}
                    </h2>
                    <p className="mt-1 text-sm text-[#f1ddbc]">
                      ID: {user?.id || '-'}
                    </p>
                    <p className="mt-2 inline-flex rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs text-[#f1ddbc]">
                      {userLevel}
                    </p>
                  </>
                ) : (
                  <>
                    <h2 className="text-2xl font-extrabold tracking-tight">
                      {copy.guest}
                    </h2>
                    <button
                      onClick={() => navigate('/login')}
                      className="mt-2 text-sm text-[#f1ddbc] underline"
                    >
                      {copy.signIn}
                    </button>
                  </>
                )}
              </div>
            </div>

            <div className="mt-6 grid grid-cols-3 gap-4">
              <StatBubble
                value={stats.totalDivinations}
                label={copy.stats.totalDivinations}
                color="text-[#f1d396]"
              />
              <StatBubble
                value={stats.favorites}
                label={copy.stats.favorites}
                color="text-[#f4e2c7]"
              />
              <StatBubble
                value={credits ?? 0}
                label={copy.stats.credits}
                color="text-[#e8c170]"
              />
            </div>
          </div>
        </Card>

        <Card
          className="panel mb-4 cursor-pointer transition-all hover:bg-white/[0.06] hover:shadow-lg"
          onClick={() => {
            if (isLoggedIn) {
              setShowCheckinProgress(true)
            } else {
              toast.warning(copy.checkinLoginFirst)
              navigate('/login')
            }
          }}
        >
          <div className="flex items-center justify-between p-4">
            <div className="flex items-center space-x-3">
              <div className="mystic-icon-badge h-12 w-12 rounded-full">
                <Calendar size={24} className="text-white" />
              </div>
              <div>
                <h3 className="font-bold text-white">{copy.checkinTitle}</h3>
                <p className="text-sm text-[#bdaa94]">
                  {checkin.canCheckin()
                    ? copy.checkinReady(streak)
                    : copy.checkinDone(streak)}
                </p>
              </div>
            </div>
            <ChevronRight size={20} className="text-[#bdaa94]" />
          </div>
        </Card>

        <Card className="panel mb-4">
          <div className="p-4">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="flex items-center font-bold text-white">
                <Coins size={18} className="mr-2 text-[#dcb86f]" />
                {copy.creditsTitle}
              </h3>
              <span className="text-2xl font-bold text-[#e8c170]">
                {credits ?? 0}
              </span>
            </div>

            <div className="mb-4 rounded-[20px] border border-[#d0a85b]/25 bg-[#7a3218]/12 p-3">
              <h4 className="mb-2 text-sm font-medium text-[#f0d9a5]">
                {copy.spending}
              </h4>
              <div className="space-y-1 text-xs text-[#f5eadb]">
                <InfoRow
                  label={copy.aiInterpret}
                  value={`${POINTS_COST.AI_INTERPRET} ${copy.stats.credits}`}
                />
                <InfoRow
                  label={copy.aiChat}
                  value={`${POINTS_COST.AI_CHAT} ${copy.stats.credits}`}
                />
                <InfoRow label={copy.basicDivination} value={copy.free} />
              </div>
            </div>

            <div className="mt-3 rounded-[20px] border border-white/10 bg-white/[0.04] p-3">
              <h4 className="mb-2 text-sm font-medium text-[#f0d9a5]">
                {copy.earning}
              </h4>
              <div className="space-y-1 text-xs text-[#f5eadb]">
                <InfoRow
                  label={copy.dailyCheckin}
                  value={`+${POINTS_EARN.DAILY_CHECKIN}`}
                />
                <InfoRow
                  label={copy.streak3}
                  value={`+${POINTS_EARN.CHECKIN_STREAK_3}`}
                />
                <InfoRow
                  label={copy.streak7}
                  value={`+${POINTS_EARN.CHECKIN_STREAK_7}`}
                />
                <InfoRow
                  label={copy.inviteFriend}
                  value={`+${POINTS_EARN.INVITE_FRIEND}`}
                />
                <InfoRow
                  label={copy.register}
                  value={`+${POINTS_EARN.REGISTER}`}
                />
                <InfoRow
                  label={copy.watchAd}
                  value={`+${POINTS_EARN.WATCH_AD}`}
                />
              </div>
            </div>
          </div>
        </Card>

        {isLoggedIn && (
          <Card className="mb-4 border border-[#d0a85b]/20 bg-[linear-gradient(135deg,rgba(122,50,24,0.22),rgba(29,20,17,0.84))] shadow-md">
            <div className="p-4">
              <div className="mb-3 flex items-center space-x-3">
                <div className="mystic-icon-badge h-10 w-10 rounded-full">
                  <PlayCircle size={20} className="text-white" />
                </div>
                <div>
                  <h3 className="font-bold text-white">{copy.watchAdTitle}</h3>
                  <p className="text-xs text-[#bdaa94]">
                    {copy.watchAdDesc(POINTS_EARN.WATCH_AD)}
                  </p>
                </div>
              </div>
              <WatchAdForPoints
                onSuccess={(points, newBalance) => {
                  toast.success(copy.watchAdSuccess(points, newBalance))
                  loadStats()
                }}
                className="w-full"
              />
            </div>
          </Card>
        )}

        <MenuCard
          items={menuItems}
          navigate={navigate}
          iconColor="text-[#dcb86f]"
        />
        <MenuCard
          items={legalItems}
          navigate={navigate}
          iconColor="text-[#f0d9a5]"
        />

        <Card className="panel mb-4">
          <div className="p-4">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="font-bold text-white">{copy.recentTitle}</h3>
              <button
                onClick={() => setShowHistory(true)}
                className="text-sm text-[#dcb86f]"
              >
                {copy.viewAll}
              </button>
            </div>

            {recentHistory.length === 0 ? (
              <div className="py-8 text-center text-[#8f7b66]">
                <Compass size={40} className="mx-auto mb-2 opacity-50" />
                <p>{copy.noHistory}</p>
              </div>
            ) : (
              <div className="space-y-3">
                {recentHistory.map((item) => (
                  <div
                    key={item.id}
                    className="mystic-muted-box flex cursor-pointer items-center justify-between transition-colors hover:bg-white/[0.05]"
                  >
                    <div className="flex-1">
                      <p className="mb-1 line-clamp-1 text-sm font-medium text-[#f4ece1]">
                        {item.question || item.title || copy.untitled}
                      </p>
                      <p className="text-xs text-[#8f7b66]">
                        {formatDate(item.timestamp)}
                      </p>
                    </div>
                    <span
                      className={`rounded-full px-2 py-1 text-xs ${
                        item.type === 'yijing'
                          ? 'bg-[#7a3218]/20 text-[#f0d9a5]'
                          : item.type === 'tarot'
                            ? 'bg-white/10 text-[#f4ece1]'
                            : item.type === 'bazi'
                              ? 'bg-[#8d4a22]/20 text-[#e8c170]'
                              : 'bg-white/10 text-[#dcb86f]'
                      }`}
                    >
                      {copy.recordTypes[item.type] || copy.recordTypes.other}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>

        {isLoggedIn && (
          <button
            onClick={handleLogout}
            className="flex w-full items-center justify-center space-x-2 rounded-[20px] border border-[#a34224]/35 bg-[#7a3218]/12 p-4 text-[#f0b48d] transition-colors hover:bg-[#7a3218]/20"
          >
            <LogOut size={20} />
            <span>{copy.logout}</span>
          </button>
        )}
      </div>

      <HistoryModal
        isOpen={showHistory}
        onClose={() => setShowHistory(false)}
        type={null}
      />

      <CheckinProgress
        isOpen={showCheckinProgress}
        onClose={() => {
          setShowCheckinProgress(false)
          loadStats()
        }}
      />
    </div>
  )
}

function StatBubble({ value, label, color }) {
  return (
    <div className="text-center">
      <div className="inline-flex items-baseline rounded-2xl bg-white/[0.08] px-4 py-1.5 shadow-lg backdrop-blur-sm">
        <span
          className={`text-4xl font-black tracking-tight drop-shadow-lg ${color}`}
        >
          {value}
        </span>
      </div>
      <div className="mt-2 text-xs font-semibold uppercase tracking-[0.15em] text-[#f4e6cf]">
        {label}
      </div>
    </div>
  )
}

function InfoRow({ label, value }) {
  return (
    <div className="flex justify-between">
      <span>{label}</span>
      <span className="font-medium">{value}</span>
    </div>
  )
}

function MenuCard({ items, navigate, iconColor }) {
  return (
    <Card className="panel mb-4">
      <div className="divide-y divide-white/5">
        {items.map((item) => {
          const Icon = item.icon
          const content = (
            <div className="flex cursor-pointer items-center justify-between p-4 transition-colors hover:bg-white/[0.04]">
              <div className="flex items-center space-x-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-white/[0.04]">
                  <Icon size={20} className={iconColor} />
                </div>
                <span className="font-medium text-[#f4ece1]">{item.label}</span>
              </div>
              <ChevronRight size={20} className="text-[#8f7b66]" />
            </div>
          )

          if (item.path) {
            return (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className="w-full text-left"
              >
                {content}
              </button>
            )
          }

          return (
            <div key={item.label} onClick={item.onClick}>
              {content}
            </div>
          )
        })}
      </div>
    </Card>
  )
}
