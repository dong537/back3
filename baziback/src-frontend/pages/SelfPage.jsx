import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ChevronRight, Star, Clock, Navigation, TrendingUp, TrendingDown, User as UserIcon, Crown, Coins, Calendar, Compass, Settings, LogOut, History, Heart, Gift, UserPlus, FileText, ScrollText } from 'lucide-react'
import Card from '../components/Card'
import Button from '../components/Button'
import HistoryModal from '../components/HistoryModal'
import CheckinProgress from '../components/CheckinProgress'
import WatchAdForPoints from '../components/WatchAdForPoints'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { checkin } from '../utils/referral'
import { POINTS_COST, POINTS_EARN } from '../utils/pointsConfig'
import { handleComingSoon, COMING_SOON_FEATURES } from '../utils/comingSoon'

export default function SelfPage() {
  const navigate = useNavigate()
  const { isLoggedIn, user, credits, logout, refreshCredits } = useAuth()
  const [activePeriod, setActivePeriod] = useState('日')
  const [selectedDate, setSelectedDate] = useState('今天')
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
    
    const todayCount = history.filter(item => {
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
    toast.success('已退出登录')
    navigate('/')
  }

  const formatDate = (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const menuItems = [
    { icon: History, label: '历史记录', onClick: () => setShowHistory(true) },
    { icon: Heart, label: '我的收藏', path: '/favorites' },
    { icon: Settings, label: '设置', onClick: handleComingSoon(COMING_SOON_FEATURES.SETTINGS) },
  ]

  const legalItems = [
    { icon: FileText, label: '隐私政策', path: '/privacy-policy' },
    { icon: ScrollText, label: '用户协议', path: '/user-agreement' },
  ]

  const displayName = user?.nickname || user?.username || '未登录用户'
  const userLevel =
    credits >= 10000 ? '星耀会员' : credits >= 3000 ? '高级会员' : credits > 0 ? '体验会员' : '普通用户'

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-slate-950/80 backdrop-blur-xl border-b border-white/10 safe-area-top">
        <div className="px-4 py-3 flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-white/10 rounded-xl transition-colors"
          >
            <ArrowLeft size={20} className="text-white" />
          </button>
          <h1 className="text-lg font-bold text-white">个人中心</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="px-4 pb-20 pt-2">
        {/* 用户信息卡片 - 合并版本 */}
        <Card className="mt-4 mb-4 bg-gradient-to-r from-indigo-700 via-purple-700 to-slate-900 border-0 shadow-2xl">
          <div className="p-6 text-white">
            <div className="flex items-center space-x-4">
              <div className="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center shadow-md cursor-pointer hover:bg-white/30 transition-colors">
                {user?.avatar ? (
                  <img src={user.avatar} alt="avatar" className="w-full h-full object-cover rounded-full" />
                ) : (
                  <UserIcon size={32} className="text-white" />
                )}
              </div>
              <div className="flex-1">
                {isLoggedIn ? (
                  <>
                    <h2 className="text-2xl font-extrabold tracking-tight drop-shadow-sm">
                      {user?.nickname || user?.username || '用户'}
                    </h2>
                    <p className="text-blue-50 text-sm mt-1">ID: {user?.id || '-'}</p>
                  </>
                ) : (
                  <>
                    <h2 className="text-2xl font-extrabold tracking-tight">未登录</h2>
                    <button onClick={() => navigate('/login')} className="text-blue-50 text-sm underline mt-1">
                      点击登录
                    </button>
                  </>
                )}
              </div>
            </div>
            
            {/* 统计数据 */}
            <div className="grid grid-cols-3 gap-4 mt-6">
              <div className="text-center">
                <div className="inline-flex items-baseline px-4 py-1.5 rounded-2xl bg-black/35 backdrop-blur-sm shadow-lg">
                  <span className="text-4xl font-black tracking-tight text-yellow-300 drop-shadow-lg">
                    {stats.totalDivinations}
                  </span>
                </div>
                <div className="mt-2 text-xs font-semibold text-white/90 tracking-[0.15em] uppercase drop-shadow">
                  总占卜
                </div>
              </div>
              <div className="text-center">
                <div className="inline-flex items-baseline px-4 py-1.5 rounded-2xl bg-black/35 backdrop-blur-sm shadow-lg">
                  <span className="text-4xl font-black tracking-tight text-pink-200 drop-shadow-lg">
                    {stats.favorites}
                  </span>
                </div>
                <div className="mt-2 text-xs font-semibold text-white/90 tracking-[0.15em] uppercase drop-shadow">
                  收藏
                </div>
              </div>
              <div className="text-center">
                <div className="inline-flex items-baseline px-4 py-1.5 rounded-2xl bg-black/35 backdrop-blur-sm shadow-lg">
                  <span className="text-4xl font-black tracking-tight text-emerald-200 drop-shadow-lg">
                    {credits ?? 0}
                  </span>
                </div>
                <div className="mt-2 text-xs font-semibold text-white/90 tracking-[0.15em] uppercase drop-shadow">
                  积分
                </div>
              </div>
            </div>
          </div>
        </Card>

        {/* 签到卡片 */}
        <Card
          className="mb-4 bg-slate-900/70 border border-white/10 cursor-pointer hover:bg-slate-800/80 hover:shadow-lg transition-all"
          onClick={() => {
            if (isLoggedIn) {
              setShowCheckinProgress(true)
            } else {
              toast.warning('请先登录')
              navigate('/login')
            }
          }}
        >
          <div className="p-4 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-orange-400 to-red-500 flex items-center justify-center">
                <Calendar size={24} className="text-white" />
              </div>
              <div>
                <h3 className="font-bold text-white">每日签到</h3>
                <p className="text-sm text-gray-300">
                  {checkin.canCheckin() ? '今日未签到' : '今日已签到'} · 连续{checkin.getStreak()}天
                </p>
              </div>
            </div>
            <ChevronRight size={20} className="text-gray-400" />
          </div>
        </Card>

        {/* 积分中心 */}
        <Card className="mb-4 bg-slate-900/70 border border-white/10 shadow-md">
          <div className="p-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-white flex items-center">
                <Coins size={18} className="text-amber-400 mr-2" />
                积分中心
              </h3>
              <span className="text-2xl font-bold text-amber-300">{credits ?? 0}</span>
            </div>
            
            {/* 积分消费说明 */}
            <div className="bg-amber-500/10 rounded-lg p-3 mb-4 border border-amber-400/30">
              <h4 className="text-sm font-medium text-amber-200 mb-2">积分消费</h4>
              <div className="space-y-1 text-xs text-amber-100">
                <div className="flex justify-between">
                  <span>AI解读（塔罗/易经/八字）</span>
                  <span className="font-medium">{POINTS_COST.AI_INTERPRET} 积分/次</span>
                </div>
                <div className="flex justify-between">
                  <span>AI智能对话</span>
                  <span className="font-medium">{POINTS_COST.AI_CHAT} 积分/次</span>
                </div>
                <div className="flex justify-between text-green-600">
                  <span>基础占卜（抽牌/起卦/排盘）</span>
                  <span className="font-medium">免费</span>
                </div>
              </div>
            </div>
            
            {/* 积分获取方式 */}
            <div className="bg-blue-500/10 rounded-lg p-3 border border-blue-400/30 mt-3">
              <h4 className="text-sm font-medium text-blue-100 mb-2">获取积分</h4>
              <div className="space-y-1 text-xs text-blue-100">
                <div className="flex justify-between">
                  <span>每日签到</span>
                  <span className="font-medium">+{POINTS_EARN.DAILY_CHECKIN} 积分</span>
                </div>
                <div className="flex justify-between">
                  <span>连续签到3天</span>
                  <span className="font-medium">+{POINTS_EARN.CHECKIN_STREAK_3} 积分</span>
                </div>
                <div className="flex justify-between">
                  <span>连续签到7天</span>
                  <span className="font-medium">+{POINTS_EARN.CHECKIN_STREAK_7} 积分</span>
                </div>
                <div className="flex justify-between">
                  <span>邀请好友</span>
                  <span className="font-medium">+{POINTS_EARN.INVITE_FRIEND} 积分</span>
                </div>
                <div className="flex justify-between">
                  <span>新用户注册</span>
                  <span className="font-medium">+{POINTS_EARN.REGISTER} 积分</span>
                </div>
                <div className="flex justify-between">
                  <span>观看广告</span>
                  <span className="font-medium">+{POINTS_EARN.WATCH_AD} 积分</span>
                </div>
              </div>
            </div>
          </div>
        </Card>

        {/* 观看广告获得积分 */}
        {isLoggedIn && (
          <Card className="mb-4 bg-gradient-to-r from-purple-600/20 to-pink-600/20 border border-purple-500/30 shadow-md">
            <div className="p-4">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-2">
                  <div className="w-10 h-10 rounded-full bg-gradient-to-r from-purple-600 to-pink-600 flex items-center justify-center">
                    <Coins size={20} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-bold text-white">观看广告获得积分</h3>
                    <p className="text-xs text-gray-300">观看完整广告可获得 {POINTS_EARN.WATCH_AD} 积分</p>
                  </div>
                </div>
              </div>
              <WatchAdForPoints
                onSuccess={(points, newBalance) => {
                  toast.success(`成功获得 ${points} 积分！当前余额：${newBalance}`)
                  loadStats()
                }}
                className="w-full"
              />
            </div>
          </Card>
        )}

        {/* 功能菜单 */}
        <Card className="mb-4 bg-slate-900/70 border border-white/10 shadow-md">
          <div className="divide-y divide-slate-800">
            {menuItems.map((item, index) => {
              const Icon = item.icon
              const content = (
                <div className="p-4 flex items-center justify-between hover:bg-slate-800 transition-colors cursor-pointer">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 rounded-full bg-blue-500/10 flex items-center justify-center">
                      <Icon size={20} className="text-blue-300" />
                    </div>
                    <span className="font-medium text-gray-100">{item.label}</span>
                  </div>
                  <ChevronRight size={20} className="text-gray-500" />
                </div>
              )
              
              if (item.path) {
                return <button key={index} onClick={() => navigate(item.path)} className="w-full text-left">{content}</button>
              }
              return <div key={index} onClick={item.onClick}>{content}</div>
            })}
          </div>
        </Card>

        {/* 法律条款 */}
        <Card className="mb-4 bg-slate-900/70 border border-white/10 shadow-md">
          <div className="divide-y divide-slate-800">
            {legalItems.map((item, index) => {
              const Icon = item.icon
              return (
                <button 
                  key={index} 
                  onClick={() => navigate(item.path)} 
                  className="w-full text-left"
                >
                  <div className="p-4 flex items-center justify-between hover:bg-slate-800 transition-colors cursor-pointer">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-full bg-purple-500/10 flex items-center justify-center">
                        <Icon size={20} className="text-purple-300" />
                      </div>
                      <span className="font-medium text-gray-100">{item.label}</span>
                    </div>
                    <ChevronRight size={20} className="text-gray-500" />
                  </div>
                </button>
              )
            })}
          </div>
        </Card>

        {/* 最近记录 */}
        <Card className="mb-4 bg-slate-900/70 border border-white/10 shadow-md">
          <div className="p-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-white">最近占卜</h3>
              <button 
                onClick={() => setShowHistory(true)}
                className="text-sm text-blue-300"
              >
                查看全部
              </button>
            </div>
            
            {recentHistory.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Compass size={40} className="mx-auto mb-2 opacity-50" />
                <p>暂无历史记录</p>
              </div>
            ) : (
              <div className="space-y-3">
                {recentHistory.map((item) => (
                  <div
                    key={item.id}
                    className="flex items-center justify-between p-3 rounded-lg bg-slate-900 hover:bg-slate-800 transition-colors cursor-pointer"
                  >
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-100 mb-1 line-clamp-1">
                        {item.question || item.title || '无标题'}
                      </p>
                      <p className="text-xs text-gray-500">
                        {formatDate(item.timestamp)}
                      </p>
                    </div>
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      item.type === 'yijing' ? 'bg-amber-500/20 text-amber-200' : 
                      item.type === 'tarot' ? 'bg-purple-500/20 text-purple-200' :
                      item.type === 'bazi' ? 'bg-orange-500/20 text-orange-200' : 
                      'bg-blue-500/20 text-blue-200'
                    }`}>
                      {item.type === 'yijing' ? '易经' : 
                       item.type === 'tarot' ? '塔罗' :
                       item.type === 'bazi' ? '八字' : '星座'}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>

        {/* 退出登录 */}
        {isLoggedIn && (
          <button
            onClick={handleLogout}
            className="w-full p-4 bg-red-500/10 rounded-xl border border-red-400/40 flex items-center justify-center space-x-2 text-red-300 hover:bg-red-500/20 transition-colors"
          >
            <LogOut size={20} />
            <span>退出登录</span>
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
