import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ChevronRight, Sparkles, Heart, Briefcase, GraduationCap, Users, Wallet, Clock, Share2, Gift } from 'lucide-react'
import { getDailyOverallScore } from '../utils/dailyRandom'

// 生成基于日期的稳定随机数
const getDailyValue = (seed, min, max) => {
  const today = new Date().toDateString()
  const hash = (today + seed).split('').reduce((a, b) => ((a << 5) - a) + b.charCodeAt(0), 0)
  return min + Math.abs(hash) % (max - min + 1)
}

export default function ZodiacPage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('日')
  const [selectedDate, setSelectedDate] = useState('今天')
  
  // 生成今日运势数据
  const fortuneData = {
    overallScore: getDailyOverallScore(70, 92),
    dimensions: [
      { label: '爱情', value: getDailyValue('love', 60, 95), color: 'from-pink-300 to-pink-400', icon: Heart },
      { label: '财富', value: getDailyValue('wealth', 65, 95), color: 'from-amber-200 to-yellow-300', icon: Wallet },
      { label: '事业', value: getDailyValue('career', 55, 90), color: 'from-orange-300 to-orange-400', icon: Briefcase },
      { label: '学习', value: getDailyValue('study', 70, 99), color: 'from-indigo-400 to-blue-500', icon: GraduationCap },
      { label: '人际', value: getDailyValue('social', 65, 95), color: 'from-purple-300 to-violet-400', icon: Users },
    ],
    luckyItems: [
      { label: '幸运色', value: '宝石蓝', icon: '🔷' },
      { label: '幸运配饰', value: '白水晶', icon: '💎' },
      { label: '幸运时辰', value: '05-07点', icon: '🌅' },
      { label: '幸运方位', value: '正东', icon: '➡️' },
    ],
    luckyNumbers: [9, 10],
    suggestions: ['小步试错、设止损点'],
    avoidances: ['沉迷造梦、表面模仿'],
    mainAdvice: '今天可能会突然萌生出更多的野心，会渴望去更远的地方闯荡，或是在新的领域挑战自我，实现更大的价值。',
    loveAdvice: '今日和伴侣聊天要专注倾听，别在对方刚开口时就打断，自顾自说起自己的事。比如TA聊兴趣爱好时，耐心听完再回应。另外，也可以趁着今...',
    question: '如何提升我的学习效率？',
  }

  // 生成日期选择器数据
  const generateDates = () => {
    const dates = []
    const today = new Date()
    const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    
    for (let i = -2; i <= 4; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() + i)
      dates.push({
        day: dayNames[date.getDay()],
        date: date.getDate(),
        isToday: i === 0,
        label: i === 0 ? '今天' : `${date.getMonth() + 1}/${date.getDate()}`
      })
    }
    return dates
  }

  const dates = generateDates()
  const tabs = ['日', '周', '月', '年']

  return (
    <div className="min-h-screen bg-gradient-to-b from-sky-50 via-blue-50 to-white">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-gray-100">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <div className="flex items-center space-x-2">
            <span className="text-lg font-bold text-gray-800">自己</span>
            <span className="text-gray-400">♈</span>
          </div>
          <div className="flex items-center space-x-2">
            <button className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
              <Clock size={20} className="text-gray-500" />
            </button>
            <button className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
              <Share2 size={20} className="text-gray-500" />
            </button>
          </div>
        </div>

        {/* 时间维度切换 */}
        <div className="px-4 pb-3 flex items-center justify-between">
          <div className="flex space-x-6">
            {tabs.map(tab => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`text-sm font-medium pb-1 border-b-2 transition-colors ${
                  activeTab === tab 
                    ? 'text-blue-600 border-blue-600' 
                    : 'text-gray-500 border-transparent hover:text-gray-700'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
          <button className="text-sm text-blue-600 flex items-center hover:text-blue-700 transition-colors">
            查看星盘 <ChevronRight size={16} />
          </button>
        </div>

        {/* 日期选择器 */}
        <div className="px-4 pb-3 flex space-x-2 overflow-x-auto scrollbar-hide">
          {dates.map((item, index) => (
            <button
              key={index}
              onClick={() => setSelectedDate(item.label)}
              className={`flex flex-col items-center min-w-[48px] py-2 px-3 rounded-xl transition-all ${
                item.isToday
                  ? 'bg-blue-500 text-white shadow-lg shadow-blue-200'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              <span className="text-xs">{item.day}</span>
              <span className={`text-sm font-semibold ${item.isToday ? '' : 'mt-0.5'}`}>
                {item.isToday ? '今天' : item.date}
              </span>
            </button>
          ))}
        </div>
      </div>

      <div className="px-4 pb-24">
        {/* 综合分数卡片 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-gray-500 text-sm mb-2">综合分数</h3>
              <div className="flex items-baseline">
                <span className="text-5xl font-bold text-gray-900">{fortuneData.overallScore}</span>
                <span className="text-2xl text-gray-400 ml-1">分</span>
              </div>
            </div>
            
            {/* 维度进度条 */}
            <div className="flex items-end space-x-3">
              {fortuneData.dimensions.map((dim, index) => (
                <div key={index} className="flex flex-col items-center">
                  <div className="w-6 h-28 bg-gray-100 rounded-full overflow-hidden flex flex-col-reverse">
                    <div 
                      className={`w-full bg-gradient-to-t ${dim.color} rounded-full transition-all duration-700`}
                      style={{ height: `${dim.value}%` }}
                    />
                  </div>
                  <span className="text-sm font-bold text-gray-800 mt-2">{dim.value}</span>
                  <span className="text-xs text-gray-500">{dim.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 运气提示卡片 */}
        <div className="mt-4 bg-gradient-to-r from-blue-50 to-cyan-50 rounded-2xl p-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-cyan-400 flex items-center justify-center">
              <Sparkles size={20} className="text-white" />
            </div>
            <div>
              <p className="font-semibold text-gray-800">今天运气还不错</p>
              <p className="text-sm text-gray-500">抽张卡获得启发，提升能量吧!</p>
            </div>
          </div>
          <button className="px-4 py-2 bg-gradient-to-r from-green-400 to-emerald-500 text-white text-sm font-medium rounded-full shadow-lg shadow-green-200 hover:shadow-xl transition-all">
            <span className="flex items-center space-x-1">
              <Gift size={16} />
              <span>免费抽卡</span>
            </span>
          </button>
        </div>

        {/* 今日运势详情 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm">
          <p className="text-gray-800 leading-relaxed text-[15px]">
            {fortuneData.mainAdvice}
          </p>
        </div>

        {/* 爱情提醒 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm">
          <div className="flex items-center space-x-2 mb-3">
            <span className="font-semibold text-gray-800">爱情提醒</span>
            <span className="text-xs text-gray-400">- 来自生辰</span>
          </div>
          <p className="text-gray-700 leading-relaxed text-[15px]">
            {fortuneData.loveAdvice}
            <button className="text-blue-500 ml-1">...全文</button>
          </p>
        </div>

        {/* 建议与避免 */}
        <div className="mt-4 flex space-x-3">
          <div className="flex-1 bg-white rounded-2xl p-4 shadow-sm">
            <div className="flex items-center space-x-2 mb-2">
              <span className="font-semibold text-gray-800">建议</span>
              <span className="text-green-500">👍</span>
            </div>
            <p className="text-sm text-gray-600">{fortuneData.suggestions.join('、')}</p>
          </div>
          <div className="flex-1 bg-white rounded-2xl p-4 shadow-sm">
            <div className="flex items-center space-x-2 mb-2">
              <span className="font-semibold text-gray-800">避免</span>
              <span className="text-orange-500">⚠️</span>
            </div>
            <p className="text-sm text-gray-600">{fortuneData.avoidances.join('、')}</p>
          </div>
        </div>

        {/* AI问答入口 */}
        <div className="mt-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl p-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-sm font-bold">问</div>
            <span className="text-gray-700">{fortuneData.question}</span>
          </div>
          <ChevronRight size={20} className="text-gray-400" />
        </div>

        {/* 幸运元素 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm">
          <div className="grid grid-cols-4 gap-4">
            {fortuneData.luckyItems.map((item, index) => (
              <div key={index} className="flex flex-col items-center">
                <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-50 to-indigo-50 flex items-center justify-center text-2xl mb-2">
                  {item.icon}
                </div>
                <span className="text-sm font-medium text-gray-800">{item.value}</span>
                <span className="text-xs text-gray-400">{item.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* 幸运数字 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm">
          <div className="flex items-center space-x-4">
            <div className="flex items-baseline space-x-2">
              {fortuneData.luckyNumbers.map((num, index) => (
                <span key={index} className="text-4xl font-bold text-gray-800">{num}</span>
              ))}
            </div>
            <div className="flex-1 grid grid-cols-4 gap-3">
              {[
                { icon: '😊', label: '心情' },
                { icon: '🏃', label: '运动' },
                { icon: '🎁', label: '惊喜' },
                { icon: '🌹', label: '桃花' },
              ].map((item, index) => (
                <div key={index} className="flex flex-col items-center">
                  <span className="text-2xl mb-1">{item.icon}</span>
                  <span className="text-xs text-gray-500">{item.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 各维度详情 */}
        <div className="mt-4 space-y-3">
          {fortuneData.dimensions.map((dim, index) => {
            const Icon = dim.icon
            return (
              <div key={index} className="bg-white rounded-2xl p-4 shadow-sm">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${dim.color} flex items-center justify-center`}>
                      <Icon size={20} className="text-white" />
                    </div>
                    <div>
                      <span className="font-semibold text-gray-800">{dim.label}运势</span>
                      <div className="flex items-center space-x-2 mt-1">
                        <div className="w-24 h-2 bg-gray-100 rounded-full overflow-hidden">
                          <div 
                            className={`h-full bg-gradient-to-r ${dim.color} rounded-full`}
                            style={{ width: `${dim.value}%` }}
                          />
                        </div>
                        <span className="text-sm font-bold text-gray-700">{dim.value}分</span>
                      </div>
                    </div>
                  </div>
                  <ChevronRight size={20} className="text-gray-400" />
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
