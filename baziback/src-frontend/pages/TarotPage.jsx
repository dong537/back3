import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Sparkles, Shuffle, Star, ArrowLeft, Coins, X, Share2, Gift, ChevronRight, Search, Lock, History } from 'lucide-react'
import ThinkingChain from '../components/ThinkingChain'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { tarotApi, deepseekApi, calculationRecordApi, unwrapApiData } from '../api'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'

// 弹窗状态存储key
const MODAL_STATE_KEY = 'tarot_divination_modal_state'

// 顶部导航标签
const navTabs = [
  { key: 'card', label: '卡牌' },
  { key: 'astro', label: '星图' },
  { key: 'fate', label: '缘分' },
  { key: 'birth', label: '生辰' },
  { key: 'chat', label: '聊愈' },
]

// 快捷咨询入口
const quickConsults = [
  { title: '爱情提问', icon: '💗', desc: '恋人关系 / 复合 / 婚姻' },
  { title: '事业学业', icon: '🎯', desc: '职场发展 / 升职跳槽' },
  { title: '财富启示', icon: '💰', desc: '财运投资 / 收支规划' },
]

// 入门牌阵（免费）
const beginnerSpreads = [
  { title: '单牌', desc: '快速解答', code: 'SINGLE', badge: '免费', icon: '🎴', cost: 0 },
  { title: '三牌阵', desc: '过去 · 现在 · 未来', code: 'PAST_PRESENT_FUTURE', badge: '免费', icon: '⏳', cost: 0 },
  { title: '恋人阵', desc: '感情分析', code: 'LOVE_TRIAD', badge: '热', icon: '💕', cost: 0 },
  { title: '权杖二', desc: '关系观望', code: 'WANDS_TWO', badge: '免费', icon: '🪄', cost: 0 },
  { title: '圣杯骑士', desc: '情感消息', code: 'CUPS_KNIGHT', badge: '免费', icon: '🏆', cost: 0 },
  { title: '宝剑三', desc: '情绪疗愈', code: 'SWORDS_THREE', badge: '免费', icon: '⚔️', cost: 0 },
]

// 进阶牌阵（需要积分）
const advancedSpreads = [
  { title: '凯尔特十字', desc: '深度解读', code: 'CELTIC_CROSS', badge: '经典', icon: '☘️', cost: 20 },
  { title: '马赛大十字', desc: '多维分析', code: 'MARSEILLE_CROSS', badge: 'Pro', icon: '✝️', cost: 30 },
  { title: '命运之轮', desc: '周期运势', code: 'WHEEL_OF_FORTUNE', badge: 'Pro', icon: '🎡', cost: 30 },
  { title: '二擎六辉', desc: '年度规划', code: 'TWO_PILLARS_SIX_STARS', badge: 'Pro', icon: '✨', cost: 50 },
  { title: '星盘全息', desc: '全局洞察', code: 'ASTRO_HOLOGRAM', badge: 'Pro', icon: '🌟', cost: 50 },
  { title: '神圣时间线', desc: '重要节点', code: 'SACRED_TIMELINE', badge: 'Pro', icon: '⏰', cost: 50 },
]

// 每日抽牌弹窗组件
function DailyDrawModal({ isOpen, onClose, dailyResult }) {
  if (!isOpen || !dailyResult) return null
  
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="bg-gray-100 rounded-3xl w-full max-w-md max-h-[90vh] overflow-y-auto relative">
        <button onClick={onClose} className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 z-10">
          <X size={20} />
        </button>
        <div className="p-6 pt-8">
          <div className="flex justify-center mb-6">
            <div className={`relative ${dailyResult.isReversed ? 'rotate-180' : ''}`}>
              {dailyResult.imageUrl ? (
                <img src={dailyResult.imageUrl} alt={dailyResult.cardNameCn} className="w-48 h-72 object-cover rounded-xl shadow-lg" />
              ) : (
                <div className="w-48 h-72 bg-gradient-to-br from-purple-400 to-pink-400 rounded-xl shadow-lg flex items-center justify-center">
                  <span className="text-6xl">{dailyResult.symbol || '🎴'}</span>
                </div>
              )}
            </div>
          </div>
          <div className="text-center mb-6">
            <h2 className="text-2xl font-bold text-gray-800 inline-flex items-center gap-2">
              {dailyResult.cardNameCn}
              <span className={`text-sm px-3 py-1 rounded-full ${dailyResult.isReversed ? 'bg-purple-100 text-purple-600' : 'bg-green-100 text-green-600'}`}>
                {dailyResult.isReversed ? '逆位' : '正位'}
              </span>
            </h2>
            <p className="text-gray-500 mt-2">{formatDate(dailyResult.date)}</p>
          </div>
          <div className="space-y-4 text-gray-700 leading-relaxed text-sm">
            {dailyResult.interpretation && <p>{dailyResult.interpretation}</p>}
            {dailyResult.meaning && <p>{dailyResult.meaning}</p>}
            {dailyResult.advice && <p className="text-gray-600 italic">{dailyResult.advice}</p>}
          </div>
          <button className="w-full mt-6 py-3 border border-gray-300 rounded-full text-gray-600 flex items-center justify-center gap-2 hover:bg-gray-50">
            <Share2 size={18} /><span>分享</span>
          </button>
        </div>
      </div>
    </div>
  )
}

// 占卜弹窗组件
function DivinationModal({ isOpen, onClose, spreadCode, spreadTitle, spreadCost, onPointsUpdate, authContext, initialState, onStateChange }) {
  const navigate = useNavigate()
  const [question, setQuestion] = useState(initialState?.question || '')
  const [loading, setLoading] = useState(false)
  const [drawnCards, setDrawnCards] = useState(initialState?.drawnCards || [])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState(initialState?.aiResult || '')
  const [pointsPaid, setPointsPaid] = useState(initialState?.pointsPaid || false)
  
  const { isLoggedIn, credits, spendCredits, canSpendCredits } = authContext || {}

  // 当状态变化时通知父组件保存
  useEffect(() => {
    if (isOpen && (drawnCards.length > 0 || question)) {
      onStateChange?.({
        question,
        drawnCards,
        aiResult,
        pointsPaid,
        spreadCode,
        spreadTitle,
        spreadCost
      })
    }
  }, [question, drawnCards, aiResult, pointsPaid, isOpen, spreadCode, spreadTitle, spreadCost, onStateChange])

  // 重置状态（仅当切换牌阵且没有初始状态时）
  useEffect(() => {
    if (isOpen && !initialState) {
      setQuestion('')
      setDrawnCards([])
      setAiResult('')
      setPointsPaid(false)
    }
  }, [isOpen, spreadCode, initialState])

  if (!isOpen) return null

  const handleDrawCards = async () => {
    if (!question.trim()) {
      toast.error('请输入您的问题')
      return
    }
    
    // 检查是否需要消耗积分（进阶牌阵）
    if (spreadCost > 0 && !pointsPaid) {
      if (isLoggedIn) {
        // 已登录用户使用后端积分
        if (!canSpendCredits(spreadCost)) {
          toast.error(`积分不足，该牌阵需要 ${spreadCost} 积分，当前余额：${credits}`)
          return
        }
        const result = await spendCredits(spreadCost, `塔罗牌阵-${spreadTitle}`)
        if (!result.success) {
          toast.error(result.message || '积分扣除失败')
          return
        }
        setPointsPaid(true)
        toast.success(`消耗 ${spreadCost} 积分`)
      } else {
        // 未登录用户使用本地积分
        if (!points.canSpend(spreadCost)) {
          toast.error(`积分不足，该牌阵需要 ${spreadCost} 积分`)
          return
        }
        const result = points.spend(spreadCost, `塔罗牌阵-${spreadTitle}`)
        if (!result.success) {
          toast.error('积分扣除失败')
          return
        }
        setPointsPaid(true)
        onPointsUpdate?.(result.newTotal)
        toast.success(`消耗 ${spreadCost} 积分`)
      }
    }

    setLoading(true)
    setDrawnCards([])
    setAiResult('')
    try {
      const response = await tarotApi.drawCards(spreadCode, question)
      const resultData = unwrapApiData(response)
      const cards = resultData?.cards || []
      if (!Array.isArray(cards) || cards.length === 0) {
        throw new Error('未获取到塔罗牌数据')
      }
      const formattedCards = cards.map(card => ({
        id: card.cardId ?? card.id,
        name: card.name || card.cardNameCn || '',
        reversed: card.reversed === true || card.reversed === 'true' || card.orientation === 'REVERSED' || false,
        symbol: card.symbol || '🎴',
        position: card.position || '',
        positionMeaning: card.positionMeaning || ''
      }))
      setDrawnCards(formattedCards)
      historyStorage.add({
        type: 'tarot',
        question: question,
        dataId: formattedCards.map(card => card.name).join('-'),
        summary: `塔罗牌占卜 - ${formattedCards.map(card => card.name).join(', ')}`,
        data: { question, spreadType: spreadCode, cards: formattedCards }
      })
      toast.success('抽牌成功！')
    } catch (error) {
      logger.error('Draw cards error:', error)
      toast.error(error?.response?.data?.message || error?.message || '抽取塔罗牌失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAIInterpret = async () => {
    if (!drawnCards.length) return
    const cost = POINTS_COST.AI_INTERPRET
    
    // 检查积分
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分，当前余额：${credits}`)
        return
      }
    } else {
      if (!points.canSpend(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分`)
        return
      }
    }
    
    setAiLoading(true)
    setAiResult('')
    try {
      const cardsDesc = drawnCards.map((card, i) => 
        `第${i + 1}张: ${card.name} (${card.reversed ? '逆位' : '正位'})`
      ).join('；')
      
      // 构建提示词 - 要求纯文本输出
      const prompt = `你是一位专业的塔罗牌解读师，请为用户解读以下塔罗牌阵。

【重要格式要求】
仅输出纯文本，不使用任何 Markdown 或富文本格式。
禁止出现以下符号：井号、星号、反引号、下划线、波浪号、大于号、小于号、方括号、圆括号内的链接格式、竖线，以及以连字符作为项目符号的列表。
使用自然段落和换行来组织内容，用数字序号（如1、2、3）代替符号列表。

【占卜信息】
用户问题：${question}
抽到的牌：${cardsDesc}

【解读要求】
请从以下几个方面进行解读：
1、整体牌阵解读：分析牌阵的整体能量和主题
2、逐张牌解读：解释每张牌在当前位置的含义，以及正逆位的影响
3、牌与牌之间的关联：分析牌之间的相互作用和故事线
4、针对问题的回答：直接回应用户的问题
5、行动建议：给出具体可行的建议
6、时间预测：如果适用，给出大致的时间参考

请用温暖、富有洞察力的语气，帮助用户获得内心的指引。`

      const response = await deepseekApi.interpretHexagram(prompt)
      
      // 解析AI响应 - 后端返回 Result { code, message, data }
      let aiContent = ''
      if (response.data?.code === 200 && response.data?.data) {
        aiContent = response.data.data
      } else if (typeof response.data === 'string') {
        aiContent = response.data
      } else if (response.data?.content) {
        aiContent = response.data.content
      } else {
        aiContent = '生成报告失败'
      }
      
      // 扣除积分
      if (isLoggedIn) {
        const result = await spendCredits(cost, 'AI塔罗解读')
        if (result.success) {
          toast.success(`消耗 ${cost} 积分`)
        } else {
          toast.error(result.message || '积分扣除失败')
        }
      } else {
        const result = points.spend(cost, 'AI塔罗解读')
        if (result.success) {
          onPointsUpdate?.(result.newTotal)
          toast.success(`消耗 ${cost} 积分`)
        }
      }
      
      setAiResult(aiContent)
    } catch (error) {
      logger.error('AI interpret error:', error)
      toast.error('AI解读失败，请稍后重试')
      setAiResult('AI解读失败，请稍后重试')
    } finally {
      setAiLoading(false)
    }
  }

  const handleCardClick = (cardId) => {
    // 保存当前弹窗状态到sessionStorage，以便返回时恢复
    const modalState = {
      question,
      drawnCards,
      aiResult,
      pointsPaid,
      spreadCode,
      spreadTitle,
      spreadCost,
      isOpen: true
    }
    sessionStorage.setItem(MODAL_STATE_KEY, JSON.stringify(modalState))
    navigate(`/tarot/card/${encodeURIComponent(cardId)}`)
  }

  const handleToggleFavorite = async () => {
    if (!drawnCards.length) {
      toast.warn('请先抽取塔罗牌')
      return
    }
    const item = {
      type: 'tarot',
      question: question,
      dataId: drawnCards.map(card => card.name).join('-'),
      title: `塔罗牌占卜`,
      summary: aiResult || drawnCards.map(card => card.name).join(', '),
      data: { question, spreadType: spreadCode, drawnCards, aiResult }
    }
    try {
      const favorited = await favoritesStorage.toggle(item)
      toast.success(favorited ? '已收藏' : '已取消收藏')
    } catch (error) {
      toast.error('收藏操作失败')
    }
  }

  const handleSaveRecord = async () => {
    if (!drawnCards.length) {
      toast.warn('请先抽取塔罗牌')
      return
    }
    try {
      const record = {
        recordType: 'tarot',
        recordTitle: `${spreadTitle}占卜`,
        question: question,
        summary: aiResult || drawnCards.map(card => card.name).join(', '),
        data: JSON.stringify({ question, spreadType: spreadCode, spreadTitle, drawnCards, aiResult })
      }
      await calculationRecordApi.save(record)
      toast.success('记录已保存')
    } catch (error) {
      toast.error('保存记录失败')
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="bg-white rounded-3xl w-full max-w-lg max-h-[90vh] overflow-y-auto relative">
        <button onClick={onClose} className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 z-10">
          <X size={20} />
        </button>
        <div className="p-6">
          <div className="text-center mb-6">
            <h2 className="text-xl font-bold text-gray-800">{spreadTitle}</h2>
            <p className="text-gray-500 text-sm mt-1">心诚则灵，专注于您想问的事情</p>
            {spreadCost > 0 && (
              <div className="mt-2 inline-flex items-center space-x-1 bg-amber-50 px-3 py-1 rounded-full border border-amber-200">
                <Coins size={14} className="text-amber-500" />
                <span className="text-sm text-amber-600">需消耗 {spreadCost} 积分</span>
              </div>
            )}
          </div>

          {/* 输入问题 */}
          <div className="mb-5">
            <textarea
              placeholder="请输入您想占卜的问题，例如：我的感情运势如何？"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              rows={3}
              className="w-full bg-gray-50 border border-gray-200 text-gray-800 placeholder-gray-400 focus:border-purple-400 focus:ring-purple-200 rounded-xl p-4 resize-none focus:outline-none focus:ring-2"
            />
          </div>

          {/* 抽牌按钮 */}
          <button
            onClick={handleDrawCards}
            disabled={loading || !question.trim()}
            className="w-full py-3 rounded-xl font-bold text-white bg-gradient-to-r from-purple-500 to-pink-500 hover:opacity-90 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg flex items-center justify-center space-x-2 mb-5"
          >
            <Shuffle size={18} className={loading ? 'animate-spin' : ''} />
            <span>{loading ? '抽牌中...' : spreadCost > 0 ? `开始抽牌 (${spreadCost}积分)` : '开始抽牌'}</span>
          </button>

          {/* 抽牌结果 */}
          {drawnCards.length > 0 && (
            <div className="bg-gray-50 rounded-2xl p-4 mb-5">
              <h3 className="text-base font-bold text-gray-800 mb-3">抽牌结果</h3>
              <div className={`grid gap-3 mb-4 ${
                drawnCards.length === 1 ? 'grid-cols-1 max-w-[150px] mx-auto' 
                : drawnCards.length <= 3 ? 'grid-cols-3' 
                : 'grid-cols-2 sm:grid-cols-3'
              }`}>
                {drawnCards.map((card, index) => (
                  <div
                    key={index}
                    onClick={() => handleCardClick(card.id ?? card.name)}
                    className="bg-white rounded-xl p-3 text-center cursor-pointer hover:shadow-md transition-all border border-gray-100"
                  >
                    <div className={`text-4xl mb-2 ${card.reversed ? 'rotate-180' : ''}`}>
                      {card.symbol || '🎴'}
                    </div>
                    <div className="text-sm font-medium text-gray-800 mb-1">{card.name}</div>
                    <span className={`text-xs px-2 py-0.5 rounded-full ${
                      card.reversed ? 'bg-orange-100 text-orange-600' : 'bg-green-100 text-green-600'
                    }`}>
                      {card.reversed ? '逆位' : '正位'}
                    </span>
                  </div>
                ))}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={handleAIInterpret}
                  disabled={aiLoading}
                  className="flex-1 py-2.5 rounded-xl font-medium text-white bg-gradient-to-r from-purple-500 to-pink-500 hover:opacity-90 transition-all flex items-center justify-center space-x-2 text-sm"
                >
                  <Sparkles size={16} className={aiLoading ? 'animate-spin' : ''} />
                  <span>AI解读</span>
                  <span className="px-1.5 py-0.5 bg-white/20 rounded-full text-xs">{POINTS_COST.AI_INTERPRET}积分</span>
                </button>
                <button onClick={handleToggleFavorite} className="p-2.5 rounded-xl bg-amber-100 text-amber-600 hover:bg-amber-200 transition-all" title="收藏">
                  <Star size={16} />
                </button>
                <button onClick={handleSaveRecord} className="p-2.5 rounded-xl bg-blue-100 text-blue-600 hover:bg-blue-200 transition-all" title="保存记录">
                  <History size={16} />
                </button>
              </div>
            </div>
          )}

          {/* AI 解读结果 */}
          {aiResult && (
            <div className="bg-purple-50 rounded-2xl p-4 border border-purple-200">
              <ThinkingChain isThinking={aiLoading} finalContent={aiResult} lightMode={true} />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default function TarotPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()
  const [activeTab, setActiveTab] = useState('card')
  const [userPoints, setUserPoints] = useState(points.get())
  
  // 每日抽牌状态
  const [dailyDrawLoading, setDailyDrawLoading] = useState(false)
  const [dailyResult, setDailyResult] = useState(null)
  const [showDailyModal, setShowDailyModal] = useState(false)
  const [hasDrawnToday, setHasDrawnToday] = useState(false)

  // 占卜弹窗状态
  const [showDivinationModal, setShowDivinationModal] = useState(false)
  const [selectedSpread, setSelectedSpread] = useState(null)
  const [modalInitialState, setModalInitialState] = useState(null)

  // 轮播图当前索引
  const [bannerIndex, setBannerIndex] = useState(0)
  const banners = [
    { id: 1, title: '每日一卡', subtitle: '开启今日能量指引', gradient: 'from-purple-400 via-pink-400 to-rose-400', icon: '🎴' },
    { id: 2, title: '爱情塔罗', subtitle: '解读你的感情密码', gradient: 'from-pink-400 via-rose-400 to-red-400', icon: '💕' },
    { id: 3, title: '事业指引', subtitle: '职场发展全解析', gradient: 'from-blue-400 via-indigo-400 to-purple-400', icon: '🎯' },
  ]

  // 页面加载时同步一次后端积分余额
  useEffect(() => {
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  // 从详情页返回时恢复弹窗状态
  useEffect(() => {
    const savedState = sessionStorage.getItem(MODAL_STATE_KEY)
    if (savedState) {
      try {
        const state = JSON.parse(savedState)
        if (state.isOpen && state.drawnCards?.length > 0) {
          setSelectedSpread({
            code: state.spreadCode,
            title: state.spreadTitle,
            cost: state.spreadCost
          })
          setModalInitialState(state)
          setShowDivinationModal(true)
          // 清除保存的状态，避免重复恢复
          sessionStorage.removeItem(MODAL_STATE_KEY)
        }
      } catch (e) {
        logger.error('恢复弹窗状态失败:', e)
        sessionStorage.removeItem(MODAL_STATE_KEY)
      }
    }
  }, [location])

  // 保存弹窗状态的回调
  const handleModalStateChange = (state) => {
    if (state && state.drawnCards?.length > 0) {
      sessionStorage.setItem(MODAL_STATE_KEY, JSON.stringify({ ...state, isOpen: true }))
    }
  }

  // 关闭弹窗时清除保存的状态
  const handleCloseModal = () => {
    setShowDivinationModal(false)
    setModalInitialState(null)
    sessionStorage.removeItem(MODAL_STATE_KEY)
  }

  // 自动轮播
  useEffect(() => {
    const timer = setInterval(() => {
      setBannerIndex((prev) => (prev + 1) % banners.length)
    }, 4000)
    return () => clearInterval(timer)
  }, [banners.length])

  // 检查今日是否已抽牌
  useEffect(() => {
    checkTodayDraw()
  }, [])

  const checkTodayDraw = async () => {
    try {
      // ✅ 改用 sessionStorage（与 AuthContext 保持一致）
      const token = sessionStorage.getItem('token')
      if (!token) return
      const response = await fetch('/api/tarot/daily-draw', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setDailyResult(data.data)
        setHasDrawnToday(true)
      }
    } catch (e) {
      logger.error('Check today draw error:', e)
    }
  }

  const handleDailyDraw = async () => {
    // ✅ 改用 sessionStorage（与 AuthContext 保持一致）
    const token = sessionStorage.getItem('token')
    if (!token) {
      toast.error('请先登录')
      navigate('/login')
      return
    }
    if (hasDrawnToday && dailyResult) {
      setShowDailyModal(true)
      return
    }
    setDailyDrawLoading(true)
    try {
      const response = await fetch('/api/tarot/daily-draw', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setDailyResult(data.data)
        setHasDrawnToday(true)
        setShowDailyModal(true)
        toast.success('抽牌成功！')
      } else {
        toast.error(data.message || '抽牌失败')
      }
    } catch (e) {
      logger.error('Daily draw error:', e)
      toast.error('抽牌失败，请稍后重试')
    } finally {
      setDailyDrawLoading(false)
    }
  }

  const handleSpreadClick = (spread, isAdvanced = false) => {
    if (!spread.code) {
      toast.info('该牌阵即将上线，敬请期待')
      return
    }
    // 进阶牌阵检查积分
    if (isAdvanced && spread.cost > 0) {
      if (isLoggedIn) {
        if (!canSpendCredits(spread.cost)) {
          toast.error(`积分不足，该牌阵需要 ${spread.cost} 积分，当前余额：${credits}`)
          return
        }
      } else {
        if (!points.canSpend(spread.cost)) {
          toast.error(`积分不足，该牌阵需要 ${spread.cost} 积分`)
          return
        }
      }
    }
    setSelectedSpread({ ...spread, isAdvanced })
    setModalInitialState(null) // 新开弹窗时清除初始状态
    setShowDivinationModal(true)
  }

  const handleQuickConsult = (consult) => {
    const spreadMap = {
      '爱情提问': { code: 'LOVE_TRIAD', title: '爱情三角阵', cost: 0 },
      '事业学业': { code: 'PAST_PRESENT_FUTURE', title: '时间之流', cost: 0 },
      '财富启示': { code: 'SINGLE', title: '单牌占卜', cost: 0 },
    }
    const spread = spreadMap[consult.title] || { code: 'SINGLE', title: '单牌占卜', cost: 0 }
    setSelectedSpread({ ...spread, icon: consult.icon })
    setShowDivinationModal(true)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-purple-50 to-pink-50">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-gray-100">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-100 rounded-xl transition-all">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <div className="flex space-x-5">
            {navTabs.map(tab => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`text-sm font-medium pb-1 border-b-2 transition-colors ${
                  activeTab === tab.key 
                    ? 'text-purple-600 border-purple-500' 
                    : 'text-gray-500 border-transparent hover:text-gray-700'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <div className="flex items-center space-x-2">
            <button className="p-2 hover:bg-gray-100 rounded-xl transition-all">
              <Search size={20} className="text-gray-500" />
            </button>
            <div className="flex items-center space-x-1 bg-amber-50 px-3 py-1.5 rounded-full border border-amber-200">
              <Coins size={14} className="text-amber-500" />
              <span className="text-sm font-bold text-amber-600">
                {isLoggedIn ? (credits ?? 0) : userPoints}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="px-4 pb-24 pt-4">
        {/* 轮播图Banner */}
        <div className="relative mb-5 rounded-2xl overflow-hidden shadow-lg">
          <div 
            className="flex transition-transform duration-500 ease-out"
            style={{ transform: `translateX(-${bannerIndex * 100}%)` }}
          >
            {banners.map((banner) => (
              <div
                key={banner.id}
                className={`w-full flex-shrink-0 bg-gradient-to-r ${banner.gradient} p-6 min-h-[140px] flex items-center justify-between cursor-pointer`}
                onClick={handleDailyDraw}
              >
                <div>
                  <h3 className="text-white text-xl font-bold mb-1">{banner.title}</h3>
                  <p className="text-white/80 text-sm">{banner.subtitle}</p>
                  <button className="mt-3 px-4 py-1.5 bg-white/20 backdrop-blur rounded-full text-white text-sm font-medium hover:bg-white/30 transition-all">
                    {hasDrawnToday ? '查看今日' : '立即抽取'}
                  </button>
                </div>
                <span className="text-6xl opacity-80">{banner.icon}</span>
              </div>
            ))}
          </div>
          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex space-x-1.5">
            {banners.map((_, idx) => (
              <button
                key={idx}
                onClick={() => setBannerIndex(idx)}
                className={`w-2 h-2 rounded-full transition-all ${idx === bannerIndex ? 'bg-white w-4' : 'bg-white/50'}`}
              />
            ))}
          </div>
        </div>

        {/* 快捷咨询入口 */}
        <div className="grid grid-cols-3 gap-3 mb-6">
          {quickConsults.map((item, index) => (
            <button
              key={index}
              onClick={() => handleQuickConsult(item)}
              className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 hover:shadow-md hover:border-purple-200 transition-all text-left group"
            >
              <span className="text-3xl mb-2 block group-hover:scale-110 transition-transform">{item.icon}</span>
              <h4 className="font-bold text-gray-800 text-sm mb-0.5">{item.title}</h4>
              <p className="text-xs text-gray-500 leading-tight">{item.desc}</p>
            </button>
          ))}
        </div>

        {/* 入门牌阵（免费） */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-2">
              <div className="w-1 h-5 bg-gradient-to-b from-purple-500 to-pink-500 rounded-full"></div>
              <h3 className="font-bold text-gray-800">入门牌阵</h3>
              <span className="px-2 py-0.5 bg-green-100 text-green-600 text-xs rounded-full">免费</span>
            </div>
            <button className="text-sm text-gray-500 flex items-center hover:text-purple-600 transition-colors">
              更多 <ChevronRight size={16} />
            </button>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {beginnerSpreads.map((spread, index) => (
              <button
                key={index}
                onClick={() => handleSpreadClick(spread, false)}
                className={`bg-white rounded-2xl p-3 shadow-sm border border-gray-100 hover:shadow-md transition-all text-left relative overflow-hidden group ${
                  !spread.code ? 'opacity-60' : 'hover:border-purple-200'
                }`}
              >
                {spread.badge && (
                  <span className={`absolute top-2 right-2 px-1.5 py-0.5 text-xs rounded-md font-medium ${
                    spread.badge === '热' ? 'bg-red-100 text-red-600' :
                    spread.badge === '免费' ? 'bg-green-100 text-green-600' :
                    'bg-gray-100 text-gray-500'
                  }`}>
                    {spread.badge}
                  </span>
                )}
                <span className="text-2xl mb-2 block group-hover:scale-110 transition-transform">{spread.icon}</span>
                <h4 className="font-bold text-gray-800 text-sm">{spread.title}</h4>
                <p className="text-xs text-gray-500 mt-0.5">{spread.desc}</p>
              </button>
            ))}
          </div>
        </div>

        {/* 进阶牌阵（需要积分） */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-2">
              <div className="w-1 h-5 bg-gradient-to-b from-indigo-500 to-purple-500 rounded-full"></div>
              <h3 className="font-bold text-gray-800">进阶牌阵</h3>
              <span className="px-2 py-0.5 bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-xs rounded-full flex items-center">
                <Coins size={10} className="mr-1" />积分兑换
              </span>
            </div>
            <button className="text-sm text-gray-500 flex items-center hover:text-purple-600 transition-colors">
              更多 <ChevronRight size={16} />
            </button>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {advancedSpreads.map((spread, index) => (
              <button
                key={index}
                onClick={() => handleSpreadClick(spread, true)}
                className={`bg-white rounded-2xl p-3 shadow-sm border border-gray-100 hover:shadow-md transition-all text-left relative overflow-hidden group ${
                  !spread.code ? 'opacity-60' : 'hover:border-purple-200'
                }`}
              >
                {spread.badge && (
                  <span className={`absolute top-2 right-2 px-1.5 py-0.5 text-xs rounded-md font-medium ${
                    spread.badge === '经典' ? 'bg-amber-100 text-amber-600' :
                    spread.badge === 'Pro' ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white' :
                    'bg-gray-100 text-gray-500'
                  }`}>
                    {spread.badge}
                  </span>
                )}
                <span className="text-2xl mb-2 block group-hover:scale-110 transition-transform">{spread.icon}</span>
                <h4 className="font-bold text-gray-800 text-sm">{spread.title}</h4>
                <p className="text-xs text-gray-500 mt-0.5">{spread.desc}</p>
                {spread.cost > 0 && (
                  <div className="flex items-center mt-1 text-xs text-amber-600">
                    <Coins size={10} className="mr-1" />
                    <span>{spread.cost}积分</span>
                  </div>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* 积分提示 */}
        <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-2xl p-4 border border-amber-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Coins size={18} className="text-amber-500" />
              <span className="text-sm text-amber-700">
                当前积分：{isLoggedIn ? (credits ?? 0) : userPoints}
              </span>
            </div>
            <button onClick={() => navigate('/dashboard')} className="text-sm text-amber-600 font-medium hover:text-amber-700 transition-colors">
              获取更多积分 →
            </button>
          </div>
        </div>
      </div>

      {/* 每日抽牌弹窗 */}
      <DailyDrawModal 
        isOpen={showDailyModal} 
        onClose={() => setShowDailyModal(false)} 
        dailyResult={dailyResult} 
      />

      {/* 占卜弹窗 */}
      <DivinationModal
        isOpen={showDivinationModal}
        onClose={handleCloseModal}
        spreadCode={selectedSpread?.code}
        spreadTitle={selectedSpread?.title || '塔罗占卜'}
        spreadCost={selectedSpread?.cost || 0}
        onPointsUpdate={setUserPoints}
        authContext={{ isLoggedIn, credits, spendCredits, canSpendCredits }}
        initialState={modalInitialState}
        onStateChange={handleModalStateChange}
      />
    </div>
  )
}
