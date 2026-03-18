import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { User, ChevronRight, BookOpen, Calendar, Brain, MessageCircle, Zap, Radio, Gift, Star, Heart, Briefcase, DollarSign, Activity, GraduationCap, Users, Sparkles, Clock, MapPin, CheckCircle2, XCircle, Trophy } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import CheckinProgress from '../components/CheckinProgress'
import { getDailyOverallScore } from '../utils/dailyRandom'
import { checkin } from '../utils/referral'
import { historyStorage } from '../utils/storage'
import { handleComingSoon, COMING_SOON_FEATURES } from '../utils/comingSoon'
import PangleCarouselAd from '../components/PangleCarouselAd'

export default function Home() {
  const navigate = useNavigate()
  const { isLoggedIn, credits, refreshCredits } = useAuth()
  const [showCheckinProgress, setShowCheckinProgress] = useState(false)
  const [canCheckin, setCanCheckin] = useState(checkin.canCheckin())
  const [fortuneDetail, setFortuneDetail] = useState(null)
  const [loading, setLoading] = useState(true)
  const [recentHistory, setRecentHistory] = useState([])

  useEffect(() => {
    setCanCheckin(checkin.canCheckin())
    loadFortuneDetail()
    setRecentHistory(historyStorage.getAll().slice(0, 5))
    // 保证首页顶部积分与全局 AuthContext 中的积分保持一致
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  const loadFortuneDetail = async () => {
    try {
      setLoading(true)
      // ✅ 改用 sessionStorage（与 AuthContext 保持一致）
      const token = sessionStorage.getItem('token');
      const headers = { 'Content-Type': 'application/json' };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch('/api/daily-fortune-detail/today', {
        method: 'GET',
        headers: headers,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      if (result.success && result.data) {
        setFortuneDetail(result.data);
      } else {
        console.warn('获取运势详情失败:', result.message || '未知错误');
        setFortuneDetail(null);
      }
    } catch (error) {
      console.error('加载运势详情失败:', error);
      setFortuneDetail(null);
    } finally {
      setLoading(false);
    }
  }

  const features = [
    { icon: Calendar, label: '生辰', path: '/bazi', gradient: 'from-amber-400 via-orange-500 to-red-500', shadow: 'shadow-orange-200' },
    { icon: '🃏', label: '塔罗牌', path: '/tarot', gradient: 'from-violet-500 via-purple-500 to-fuchsia-500', shadow: 'shadow-purple-200' },
    { icon: BookOpen, label: '易经', path: '/yijing', gradient: 'from-emerald-400 via-teal-500 to-cyan-500', shadow: 'shadow-teal-200' },
    { icon: Brain, label: 'AI对话', path: '/ai', gradient: 'from-blue-500 via-indigo-500 to-violet-500', shadow: 'shadow-indigo-200', badge: 'AI' },
  ]

  const dailyFortune = {
    score: getDailyOverallScore(70, 95),
    lucky: ['东南方', '紫色', '数字7'],
    advice: '今日宜静心思考，不宜冲动决策',
  }

  const handleCheckin = () => {
    if (!isLoggedIn) { toast.warning('请先登录'); navigate('/login'); return }
    setShowCheckinProgress(true)
  }

  const formatDate = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  }

  const onCheckinClose = () => {
    setShowCheckinProgress(false)
    // 重新从后端刷新积分余额，保持与顶部导航一致
    refreshCredits()
    setCanCheckin(checkin.canCheckin())
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="px-4 pt-4">
        {/* 顶部用户栏 - 玻璃态效果 */}
        <div className="flex items-center justify-between mb-5 p-3 rounded-2xl bg-white/60 backdrop-blur-xl border border-white/50 shadow-lg shadow-indigo-100/50">
          <div className="flex items-center space-x-3">
            <Link to={isLoggedIn ? '/dashboard' : '/login'}>
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-violet-500 via-purple-500 to-fuchsia-500 flex items-center justify-center shadow-lg shadow-purple-200 transform hover:scale-105 transition-all duration-300">
                <User size={22} className="text-white" />
              </div>
            </Link>
            <div>
              <div className="text-sm font-semibold text-gray-800">{isLoggedIn ? '欢迎回来' : '点击登录'}</div>
              <div className="flex items-center space-x-1.5 text-xs">
                <div className="flex items-center space-x-1 px-2 py-0.5 rounded-full bg-gradient-to-r from-amber-100 to-orange-100">
                  <Star size={12} className="text-amber-500" />
                  <span className="text-amber-700 font-medium">{credits ?? 0}</span>
                </div>
              </div>
            </div>
          </div>
          
          <button onClick={handleCheckin} className={`px-5 py-2.5 rounded-2xl text-sm font-semibold flex items-center space-x-2 transition-all duration-300 ${canCheckin ? 'bg-gradient-to-r from-orange-400 via-rose-400 to-pink-500 text-white shadow-lg shadow-rose-200 hover:shadow-xl hover:shadow-rose-300 hover:scale-105' : 'bg-gray-100 text-gray-400'}`}>
            <Gift size={18} />
            <span>{canCheckin ? '签到' : '已签到'}</span>
          </button>
        </div>

        {/* 穿山甲轮播图广告 */}
        <div className="mb-5">
          <PangleCarouselAd
            slotId={import.meta.env.VITE_PANGLE_SLOT_ID || 'YOUR_PANGLE_SLOT_ID'}
            className="w-full"
            autoPlay={true}
            interval={3000}
            showIndicators={true}
            showArrows={true}
            onAdLoad={(ads) => {
              console.log('广告加载成功:', ads)
            }}
            onAdError={(error) => {
              console.error('广告加载失败:', error)
            }}
          />
        </div>

        {/* 每日运势卡片 - 参考新设计 */}
        <div 
          onClick={(e) => handleComingSoon(COMING_SOON_FEATURES.ZODIAC)(e)} 
          className="mb-5 rounded-3xl overflow-hidden shadow-xl cursor-pointer hover:shadow-2xl transition-all duration-300 hover:scale-[1.02] active:scale-[0.98]"
        >
          <div className="relative bg-gradient-to-r from-sky-100 via-blue-50 to-indigo-100 p-5">
            {/* 装饰波浪 */}
            <div className="absolute top-8 left-24 w-16 h-8 opacity-30">
              <svg viewBox="0 0 60 20" className="w-full h-full">
                <path d="M0,10 Q15,0 30,10 T60,10" stroke="#60A5FA" strokeWidth="2" fill="none" />
                <path d="M0,15 Q15,5 30,15 T60,15" stroke="#818CF8" strokeWidth="2" fill="none" />
              </svg>
            </div>
            
            <div className="flex justify-between">
              {/* 左侧内容 */}
              <div className="flex-1">
                <div className="flex items-center justify-between mb-1">
                  <h3 className="text-xl font-bold text-gray-800">自己</h3>
                  <button className="text-sm text-gray-500 flex items-center hover:text-indigo-600 transition-colors">
                    更多 <ChevronRight size={16} />
                  </button>
                </div>
                
                <div className="flex items-baseline mb-2">
                  <span className="text-gray-700 font-medium">今日心情</span>
                  <span className="text-4xl font-bold text-gray-900 ml-3">{dailyFortune.score}</span>
                  <span className="text-lg text-gray-500 ml-1">分</span>
                </div>
                
                <p className="text-sm text-gray-600 leading-relaxed max-w-[180px]">
                  今天可能会突然萌生出更多的野心，会渴望...
                </p>
              </div>
              
              {/* 右侧进度条 */}
              <div className="flex items-end space-x-3 ml-4">
                {[
                  { label: '爱情', value: getDailyOverallScore(60, 85), color: 'from-pink-300 to-pink-400' },
                  { label: '财富', value: getDailyOverallScore(65, 90), color: 'from-amber-200 to-yellow-300' },
                  { label: '事业', value: getDailyOverallScore(55, 80), color: 'from-orange-300 to-orange-400' },
                  { label: '学习', value: getDailyOverallScore(70, 99), color: 'from-indigo-400 to-blue-500' },
                  { label: '人际', value: getDailyOverallScore(65, 95), color: 'from-purple-300 to-violet-400' },
                ].map((item, index) => (
                  <div key={index} className="flex flex-col items-center">
                    <div className="w-5 h-24 bg-gray-200/60 rounded-full overflow-hidden flex flex-col-reverse">
                      <div 
                        className={`w-full bg-gradient-to-t ${item.color} rounded-full transition-all duration-500`}
                        style={{ height: `${item.value}%` }}
                      />
                    </div>
                    <span className="text-sm font-bold text-gray-800 mt-1">{item.value}</span>
                    <span className="text-xs text-gray-500">{item.label}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* 功能图标网格 - 3D效果 */}
        <div className="grid grid-cols-4 gap-3 mb-5">
          {features.map((feature, index) => {
            const IconComponent = typeof feature.icon === 'string' ? null : feature.icon
            return (
              <Link key={index} to={feature.path} className="group">
                <div className="flex flex-col items-center p-3 rounded-2xl bg-white/70 backdrop-blur border border-white/50 hover:bg-white hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1">
                  <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${feature.gradient} flex items-center justify-center mb-2 relative shadow-lg ${feature.shadow} group-hover:scale-110 transition-transform duration-300`}>
                    {IconComponent ? <IconComponent size={26} className="text-white" /> : <span className="text-white text-2xl">{feature.icon}</span>}
                    {feature.badge && <span className="absolute -top-1 -right-1 px-1.5 py-0.5 text-xs bg-gradient-to-r from-rose-500 to-red-500 text-white rounded-lg shadow-md">{feature.badge}</span>}
                  </div>
                  <span className="text-xs font-medium text-gray-700">{feature.label}</span>
                </div>
              </Link>
            )
          })}
        </div>

        {/* 填充空白：快捷入口 + 最近记录 */}
        <div className="grid md:grid-cols-2 gap-4 mb-5">
          {/* 左：快捷入口卡片 */}
          <div className="rounded-3xl bg-white/85 backdrop-blur-xl border border-white/60 shadow-lg p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <Sparkles size={18} className="text-indigo-500" />
                <h3 className="font-bold text-gray-800">快捷入口</h3>
              </div>
              <span className="text-xs text-gray-400">常用</span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <Link
                to="/favorites"
                className="flex items-center justify-between px-3 py-3 rounded-2xl bg-gradient-to-r from-fuchsia-500/15 to-indigo-500/15 border border-fuchsia-500/20 hover:shadow-md transition-all"
              >
                <div className="flex items-center space-x-2">
                  <Heart size={18} className="text-fuchsia-500" />
                  <div>
                    <p className="text-sm font-semibold text-gray-800">我的收藏</p>
                    <p className="text-xs text-gray-500">查看占卜/牌阵/八字</p>
                  </div>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
              <Link
                to="/achievement"
                className="flex items-center justify-between px-3 py-3 rounded-2xl bg-gradient-to-r from-amber-500/15 to-orange-500/15 border border-amber-500/20 hover:shadow-md transition-all"
              >
                <div className="flex items-center space-x-2">
                  <Trophy size={18} className="text-amber-500" />
                  <div>
                    <p className="text-sm font-semibold text-gray-800">我的成就</p>
                    <p className="text-xs text-gray-500">勋章与进度</p>
                  </div>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
              <Link
                to="/dashboard"
                className="flex items-center justify-between px-3 py-3 rounded-2xl bg-gradient-to-r from-emerald-500/15 to-teal-500/15 border border-emerald-500/20 hover:shadow-md transition-all"
              >
                <div className="flex items-center space-x-2">
                  <User size={18} className="text-emerald-500" />
                  <div>
                    <p className="text-sm font-semibold text-gray-800">个人中心</p>
                    <p className="text-xs text-gray-500">积分 / 任务 / 信息</p>
                  </div>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
              <Link
                to="/ai"
                className="flex items-center justify-between px-3 py-3 rounded-2xl bg-gradient-to-r from-blue-500/15 to-purple-500/15 border border-blue-500/20 hover:shadow-md transition-all"
              >
                <div className="flex items-center space-x-2">
                  <Zap size={18} className="text-blue-500" />
                  <div>
                    <p className="text-sm font-semibold text-gray-800">AI对话</p>
                    <p className="text-xs text-gray-500">快速提问与解读</p>
                  </div>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
              <Link
                to="/ai/face"
                className="flex items-center justify-between px-3 py-3 rounded-2xl bg-gradient-to-r from-cyan-500/15 to-sky-500/15 border border-cyan-500/20 hover:shadow-md transition-all"
              >
                <div className="flex items-center space-x-2">
                  <Brain size={18} className="text-cyan-500" />
                  <div>
                    <p className="text-sm font-semibold text-gray-800">人脸分析</p>
                    <p className="text-xs text-gray-500">Gemini 面相文化报告</p>
                  </div>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
            </div>
          </div>

          {/* 右：最近记录/热门 */}
          <div className="rounded-3xl bg-white/85 backdrop-blur-xl border border-white/60 shadow-lg p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <Star size={18} className="text-amber-500" />
                <h3 className="font-bold text-gray-800">最近记录</h3>
              </div>
              <Link to="/records" className="text-xs text-indigo-500 hover:underline cursor-pointer">查看更多</Link>
            </div>

            <div className="space-y-3">
              {recentHistory.slice(0, 4).map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-3 rounded-2xl bg-gradient-to-r from-slate-50 to-white border border-gray-100 hover:shadow-sm transition-all"
                >
                  <div>
                    <p className="text-sm font-semibold text-gray-800 line-clamp-1">
                      {item.question || item.title || '无标题'}
                    </p>
                    <p className="text-xs text-gray-400 mt-1">
                      {formatDate(item.timestamp)}
                    </p>
                  </div>
                  <span className={`px-2 py-1 text-xs rounded-full ${
                    item.type === 'yijing' ? 'bg-amber-100 text-amber-700' : 
                    item.type === 'tarot' ? 'bg-purple-100 text-purple-700' :
                    item.type === 'bazi' ? 'bg-orange-100 text-orange-700' : 
                    'bg-blue-100 text-blue-700'
                  }`}>
                    {item.type === 'yijing' ? '易经' : item.type === 'tarot' ? '塔罗' : item.type === 'bazi' ? '八字' : '其他'}
                  </span>
                </div>
              ))}
              {recentHistory.length === 0 && (
                <div className="text-center text-gray-400 text-sm py-6">
                  暂无记录，去试试占卜或收藏吧
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 每日运势详情卡片 */}
        {loading && (
          <div className="mb-5 rounded-3xl bg-white/80 backdrop-blur-xl border border-white/50 shadow-xl overflow-hidden">
            <div className="p-5">
              <div className="animate-pulse space-y-4">
                <div className="h-6 bg-gray-200 rounded w-1/3"></div>
                <div className="grid grid-cols-2 gap-3">
                  {[1, 2, 3, 4, 5, 6].map((i) => (
                    <div key={i} className="h-24 bg-gray-100 rounded-2xl"></div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
        {!loading && fortuneDetail && (
          <DailyFortuneDetailCard fortuneDetail={fortuneDetail} />
        )}

      </div>

      <CheckinProgress isOpen={showCheckinProgress} onClose={onCheckinClose} />
    </div>
  )
}


// 每日运势详情卡片组件
function DailyFortuneDetailCard({ fortuneDetail }) {
  const aspects = fortuneDetail.aspects || {}
  const luckyElements = fortuneDetail.luckyElements || {}
  const suitableActions = fortuneDetail.suitableActions || []
  const unsuitableActions = fortuneDetail.unsuitableActions || []
  const keywords = fortuneDetail.keywords || []
  
  // 计算平均分
  const scores = Object.values(aspects).map(a => a.score || 0)
  const averageScore = scores.length > 0 
    ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) 
    : 0
  
  // 获取分数等级
  const getScoreLevel = (score) => {
    if (score >= 85) return { level: '极佳', color: 'text-green-600', bgColor: 'bg-green-50' }
    if (score >= 75) return { level: '良好', color: 'text-blue-600', bgColor: 'bg-blue-50' }
    if (score >= 65) return { level: '一般', color: 'text-yellow-600', bgColor: 'bg-yellow-50' }
    return { level: '需注意', color: 'text-orange-600', bgColor: 'bg-orange-50' }
  }
  
  const avgLevel = getScoreLevel(averageScore)

  const aspectConfigs = [
    { key: 'love', label: '爱情', icon: Heart, color: 'from-pink-400 to-rose-500', bgColor: 'bg-pink-50', textColor: 'text-pink-600' },
    { key: 'career', label: '事业', icon: Briefcase, color: 'from-orange-400 to-amber-500', bgColor: 'bg-orange-50', textColor: 'text-orange-600' },
    { key: 'wealth', label: '财富', icon: DollarSign, color: 'from-amber-400 to-yellow-500', bgColor: 'bg-amber-50', textColor: 'text-amber-600' },
    { key: 'health', label: '健康', icon: Activity, color: 'from-emerald-400 to-teal-500', bgColor: 'bg-emerald-50', textColor: 'text-emerald-600' },
    { key: 'study', label: '学习', icon: GraduationCap, color: 'from-blue-400 to-indigo-500', bgColor: 'bg-blue-50', textColor: 'text-blue-600' },
    { key: 'relationship', label: '人际', icon: Users, color: 'from-purple-400 to-violet-500', bgColor: 'bg-purple-50', textColor: 'text-purple-600' },
  ]

  return (
    <div className="mb-5 rounded-3xl bg-white/80 backdrop-blur-xl border border-white/50 shadow-xl overflow-hidden">
      {/* 标题栏 */}
      <div className="bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Sparkles size={20} className="text-white" />
            <h3 className="text-lg font-bold text-white">今日运势详情</h3>
          </div>
          <span className="text-sm text-white/90">{fortuneDetail.date || '今日'}</span>
        </div>
      </div>

      <div className="p-5 space-y-5">
        {/* 综合评分 */}
        <div className={`${avgLevel.bgColor} rounded-2xl p-4 border border-white/50`}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">综合运势</p>
              <div className="flex items-baseline space-x-2">
                <span className={`text-3xl font-bold ${avgLevel.color}`}>{averageScore}</span>
                <span className={`text-sm ${avgLevel.color} opacity-70`}>分</span>
                <span className={`text-sm font-medium ${avgLevel.color} ml-2`}>{avgLevel.level}</span>
              </div>
            </div>
            <div className="text-right">
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center shadow-lg">
                <Sparkles size={32} className="text-white" />
              </div>
            </div>
          </div>
        </div>
        
        {/* 各维度运势详情 */}
        <div className="grid grid-cols-2 gap-3">
          {aspectConfigs.map((config) => {
            const aspect = aspects[config.key] || {}
            const score = aspect.score || 0
            const analysis = aspect.analysis || ''
            const Icon = config.icon

            const scoreLevel = getScoreLevel(score)
            
            return (
              <div key={config.key} className={`${config.bgColor} rounded-2xl p-4 border border-white/50 hover:shadow-md transition-shadow cursor-pointer`}>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center space-x-2">
                    <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${config.color} flex items-center justify-center shadow-md`}>
                      <Icon size={20} className="text-white" />
                    </div>
                    <div>
                      <span className={`font-semibold ${config.textColor} block`}>{config.label}</span>
                      <span className={`text-xs ${scoreLevel.color} font-medium`}>{scoreLevel.level}</span>
                    </div>
                  </div>
                  <div className="flex flex-col items-end">
                    <div className="flex items-baseline">
                      <span className={`text-2xl font-bold ${config.textColor}`}>{score}</span>
                      <span className={`text-xs ${config.textColor} opacity-70 ml-1`}>分</span>
                    </div>
                    {/* 进度条 */}
                    <div className="w-16 h-1.5 bg-gray-200 rounded-full mt-1 overflow-hidden">
                      <div 
                        className={`h-full bg-gradient-to-r ${config.color} rounded-full transition-all duration-500`}
                        style={{ width: `${score}%` }}
                      />
                    </div>
                  </div>
                </div>
                {analysis && (
                  <p className="text-xs text-gray-600 leading-relaxed line-clamp-2 mt-2">{analysis}</p>
                )}
              </div>
            )
          })}
        </div>

        {/* 幸运元素 */}
        {(luckyElements.color || luckyElements.number || luckyElements.direction || luckyElements.time) && (
          <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-2xl p-4 border border-amber-100">
            <div className="flex items-center space-x-2 mb-3">
              <Sparkles size={18} className="text-amber-600" />
              <h4 className="font-semibold text-amber-800">幸运元素</h4>
            </div>
            <div className="grid grid-cols-2 gap-3">
              {luckyElements.color && (
                <div className="flex items-center space-x-2">
                  <div className="w-6 h-6 rounded-full bg-gradient-to-br from-pink-400 to-rose-500 shadow-sm"></div>
                  <span className="text-sm text-gray-700">颜色: <span className="font-medium">{luckyElements.color}</span></span>
                </div>
              )}
              {luckyElements.number && (
                <div className="flex items-center space-x-2">
                  <div className="w-6 h-6 rounded-full bg-gradient-to-br from-blue-400 to-indigo-500 flex items-center justify-center shadow-sm">
                    <span className="text-xs text-white font-bold">{luckyElements.number}</span>
                  </div>
                  <span className="text-sm text-gray-700">数字: <span className="font-medium">{luckyElements.number}</span></span>
                </div>
              )}
              {luckyElements.direction && (
                <div className="flex items-center space-x-2">
                  <MapPin size={16} className="text-emerald-600" />
                  <span className="text-sm text-gray-700">方位: <span className="font-medium">{luckyElements.direction}</span></span>
                </div>
              )}
              {luckyElements.time && (
                <div className="flex items-center space-x-2">
                  <Clock size={16} className="text-purple-600" />
                  <span className="text-sm text-gray-700">时间: <span className="font-medium">{luckyElements.time}</span></span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* 宜忌事项 */}
        {(suitableActions.length > 0 || unsuitableActions.length > 0) && (
          <div className="space-y-3">
            {suitableActions.length > 0 && (
              <div className="bg-emerald-50 rounded-2xl p-4 border border-emerald-100">
                <div className="flex items-center space-x-2 mb-3">
                  <CheckCircle2 size={18} className="text-emerald-600" />
                  <h4 className="font-semibold text-emerald-800">今日宜做</h4>
                </div>
                <div className="flex flex-wrap gap-2">
                  {suitableActions.map((action, index) => (
                    <span key={index} className="px-3 py-1.5 bg-emerald-100 text-emerald-700 text-xs rounded-xl font-medium">
                      {action}
                    </span>
                  ))}
                </div>
              </div>
            )}
            {unsuitableActions.length > 0 && (
              <div className="bg-rose-50 rounded-2xl p-4 border border-rose-100">
                <div className="flex items-center space-x-2 mb-3">
                  <XCircle size={18} className="text-rose-600" />
                  <h4 className="font-semibold text-rose-800">今日忌做</h4>
                </div>
                <div className="flex flex-wrap gap-2">
                  {unsuitableActions.map((action, index) => (
                    <span key={index} className="px-3 py-1.5 bg-rose-100 text-rose-700 text-xs rounded-xl font-medium">
                      {action}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* 综合建议 */}
        {fortuneDetail.overallAdvice && (
          <div className="bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 rounded-2xl p-4 border border-indigo-100">
            <h4 className="font-semibold text-indigo-800 mb-2">综合建议</h4>
            <p className="text-sm text-gray-700 leading-relaxed">{fortuneDetail.overallAdvice}</p>
          </div>
        )}

        {/* 关键词 */}
        {keywords.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {keywords.map((keyword, index) => (
              <span key={index} className="px-3 py-1.5 bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-700 text-xs rounded-full font-medium">
                #{keyword}
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

// 信息流卡片组件 - 精致卡片设计
function FeedCard({ item, onLike, onSave, onNavigate }) {
  const user = item.user || {}
  const userName = user.nickname || user.name || '用户'
  const userAvatar = user.avatar || '👤'
  const userLevel = user.level ? `Lv.${user.level}` : 'Lv.1'
  const isVerified = user.level >= 5
  
  const likes = item.likesCount ?? item.likes ?? 0
  const comments = item.commentsCount ?? item.comments ?? 0
  const shares = item.sharesCount ?? item.shares ?? 0
  const tags = item.tagList || item.tags || []
  const time = formatTime(item.createdAt) || item.time || ''
  
  return (
    <div className="bg-white/70 backdrop-blur rounded-3xl border border-white/50 overflow-hidden hover:bg-white hover:shadow-xl transition-all duration-300 group">
      <div className="p-4">
        {/* 用户信息 */}
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center space-x-3">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-indigo-100 via-purple-100 to-pink-100 flex items-center justify-center text-xl shadow-sm">{userAvatar}</div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-semibold text-gray-800">{userName}</span>
                {isVerified && <span className="w-4 h-4 bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full flex items-center justify-center shadow-sm"><span className="text-white text-xs">✓</span></span>}
                <span className="text-xs px-2 py-0.5 bg-gradient-to-r from-gray-100 to-gray-50 text-gray-500 rounded-lg">{userLevel}</span>
              </div>
              <span className="text-xs text-gray-400">{time}</span>
            </div>
          </div>
          <button className="p-2 hover:bg-gray-100 rounded-xl transition-colors"><MoreHorizontal size={18} className="text-gray-400" /></button>
        </div>

        {item.title && <h3 className="font-bold text-gray-800 mb-2 text-base">{item.title}</h3>}
        <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-line mb-3">{item.content}</p>

        {item.category === 'test' && (
          <div className="bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 rounded-2xl p-4 mb-3 border border-indigo-100/50">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <TrendingUp size={16} className="text-indigo-500" />
                <span className="text-sm text-indigo-600 font-medium">{(item.viewsCount || 0).toLocaleString()} 人已参与</span>
              </div>
              <button onClick={() => onNavigate('/daily-test')} className="px-4 py-2 bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-sm rounded-xl hover:shadow-lg transition-all font-medium">立即测试</button>
            </div>
          </div>
        )}

        {tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-3">
            {tags.map((tag, index) => <span key={index} className="px-3 py-1 bg-gradient-to-r from-indigo-50 to-purple-50 text-indigo-600 text-xs rounded-xl font-medium">#{tag}</span>)}
          </div>
        )}

        {/* 互动栏 */}
        <div className="flex items-center justify-between pt-3 border-t border-gray-100/50">
          <button onClick={onLike} className={`flex items-center space-x-1.5 px-4 py-2 rounded-xl transition-all duration-300 ${item.liked ? 'text-rose-500 bg-rose-50' : 'text-gray-500 hover:bg-gray-100'}`}>
            <Heart size={18} fill={item.liked ? 'currentColor' : 'none'} className={item.liked ? 'animate-pulse' : ''} />
            <span className="text-sm font-medium">{likes}</span>
          </button>
          <button onClick={() => onNavigate(`/post/${item.id}`)} className="flex items-center space-x-1.5 px-4 py-2 rounded-xl text-gray-500 hover:bg-gray-100 transition-colors">
            <MessageSquare size={18} />
            <span className="text-sm font-medium">{comments}</span>
          </button>
          <button className="flex items-center space-x-1.5 px-4 py-2 rounded-xl text-gray-500 hover:bg-gray-100 transition-colors">
            <Share2 size={18} />
            <span className="text-sm font-medium">{shares}</span>
          </button>
          <button onClick={onSave} className={`flex items-center space-x-1.5 px-4 py-2 rounded-xl transition-all duration-300 ${item.saved ? 'text-amber-500 bg-amber-50' : 'text-gray-500 hover:bg-gray-100'}`}>
            <Bookmark size={18} fill={item.saved ? 'currentColor' : 'none'} />
          </button>
        </div>
      </div>
    </div>
  )
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
