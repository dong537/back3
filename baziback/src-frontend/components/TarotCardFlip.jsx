import { useState, useEffect } from 'react'
import { tarotApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'

/**
 * 塔罗牌翻转卡片组件
 * 支持点击翻转动画，显示今日运势
 * 实现每日抽牌逻辑：每个用户每天只能抽一次
 */
export default function TarotCardFlip({ onCardFlipped }) {
  const { isLoggedIn } = useAuth()
  const [isFlipped, setIsFlipped] = useState(false)
  const [isFlipping, setIsFlipping] = useState(false)
  const [card, setCard] = useState(null)
  const [loading, setLoading] = useState(false)
  const [dailyFortune, setDailyFortune] = useState(null)
  const [loadingFortune, setLoadingFortune] = useState(false)
  const [hasDrawnToday, setHasDrawnToday] = useState(false)

  // 组件加载时检查今日是否已抽牌
  useEffect(() => {
    checkTodayDraw()
  }, [isLoggedIn])

  // 检查今日是否已抽牌
  const checkTodayDraw = async () => {
    if (!isLoggedIn) {
      // 未登录，显示默认背面
      setCard({
        cardId: null,
        cardNameCn: '请登录后抽牌',
        cardNameEn: 'Please Login',
        symbol: '🎴',
        imageUrl: null
      })
      return
    }

    try {
      setLoading(true)
      const response = await tarotApi.getTodayDraw()
      
      if (response.data?.success && response.data.data) {
        // 今天已抽过牌，直接显示结果
        const drawResult = response.data.data
        setCard({
          cardId: drawResult.cardId,
          cardNameCn: drawResult.cardNameCn,
          cardNameEn: drawResult.cardNameEn,
          symbol: drawResult.symbol || '🎴',
          imageUrl: drawResult.imageUrl
        })
        setDailyFortune({
          date: drawResult.date,
          cardName: drawResult.cardNameCn,
          isReversed: drawResult.isReversed,
          interpretation: drawResult.interpretation,
          meaning: drawResult.meaning,
          love: drawResult.love,
          career: drawResult.career,
          wealth: drawResult.wealth,
          health: drawResult.health,
          advice: drawResult.advice,
          keyword: drawResult.keyword
        })
        setIsFlipped(true) // 已抽过，直接显示正面
        setHasDrawnToday(true)
      } else {
        // 今天还没抽牌，显示背面等待抽牌
        setCard({
          cardId: null,
          cardNameCn: '点击翻转',
          cardNameEn: 'Tap to Draw',
          symbol: '🎴',
          imageUrl: null
        })
        setHasDrawnToday(false)
      }
    } catch (error) {
      logger.error('Check today draw error:', error)
      // 如果API返回错误（如未抽过），显示背面
      if (error.response?.data?.message?.includes('还没有抽牌')) {
        setCard({
          cardId: null,
          cardNameCn: '点击翻转',
          cardNameEn: 'Tap to Draw',
          symbol: '🎴',
          imageUrl: null
        })
        setHasDrawnToday(false)
      } else {
        toast.error('检查抽牌记录失败')
      }
    } finally {
      setLoading(false)
    }
  }

  // 处理卡片点击翻转
  const handleCardClick = async () => {
    if (isFlipping || loading) return

    // 如果未登录，提示登录
    if (!isLoggedIn) {
      toast.error('请先登录后再抽牌')
      return
    }

    // 如果今天已抽过，不允许再次抽牌
    if (hasDrawnToday) {
      return
    }

    setIsFlipping(true)
    
    // 翻转动画
    setTimeout(async () => {
      setIsFlipped(true)
      setIsFlipping(false)
      
      // 执行每日抽牌
      await drawDailyCard()
      
      // 通知父组件
      if (onCardFlipped) {
        onCardFlipped(true, card)
      }
    }, 300) // 动画时长的一半
  }

  // 执行每日抽牌
  const drawDailyCard = async () => {
    try {
      setLoadingFortune(true)
      const response = await tarotApi.drawDailyCard()
      
      if (response.data?.success && response.data.data) {
        const drawResult = response.data.data
        setCard({
          cardId: drawResult.cardId,
          cardNameCn: drawResult.cardNameCn,
          cardNameEn: drawResult.cardNameEn,
          symbol: drawResult.symbol || '🎴',
          imageUrl: drawResult.imageUrl
        })
        setDailyFortune({
          date: drawResult.date,
          cardName: drawResult.cardNameCn,
          isReversed: drawResult.isReversed,
          interpretation: drawResult.interpretation,
          meaning: drawResult.meaning,
          love: drawResult.love,
          career: drawResult.career,
          wealth: drawResult.wealth,
          health: drawResult.health,
          advice: drawResult.advice,
          keyword: drawResult.keyword
        })
        setHasDrawnToday(true)
        toast.success('抽牌成功！')
      } else {
        // 检查是否是业务错误（如今天已抽过）
        const errorMsg = response.data?.message || '抽牌失败'
        throw new Error(errorMsg)
      }
    } catch (error) {
      logger.error('Draw daily card error:', error)
      // 更详细的错误处理
      let errorMessage = '抽牌失败，请稍后重试'
      
      if (error.response) {
        // 服务器返回了错误响应
        errorMessage = error.response.data?.message || error.response.data?.error || error.message || errorMessage
      } else if (error.request) {
        // 请求已发出但没有收到响应
        errorMessage = '网络错误，请检查网络连接'
      } else {
        // 其他错误
        errorMessage = error.message || errorMessage
      }
      
      toast.error(errorMessage)
      
      // 抽牌失败，翻转回背面
      setIsFlipped(false)
    } finally {
      setLoadingFortune(false)
    }
  }


  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="text-6xl mb-4 animate-spin">🎴</div>
          <div className="text-gray-400">正在加载塔罗牌...</div>
        </div>
      </div>
    )
  }

  if (!card) {
    return null
  }

  return (
    <div className="relative w-full max-w-md mx-auto tarot-card-flip-container">
      {/* 卡片容器 */}
      <div 
        className="relative w-full cursor-pointer tap-highlight no-select"
        style={{ 
          perspective: '1000px',
          aspectRatio: '2/3',
          maxWidth: '100%',
          margin: '0 auto'
        }}
        onClick={handleCardClick}
        onTouchStart={(e) => {
          // 移动端触摸反馈
          e.currentTarget.style.transform = 'scale(0.98)'
        }}
        onTouchEnd={(e) => {
          e.currentTarget.style.transform = 'scale(1)'
        }}
      >
        {/* 翻转容器 */}
        <div 
          className="relative w-full h-full"
          style={{
            transformStyle: 'preserve-3d',
            transition: 'transform 0.6s',
            transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)'
          }}
        >
          {/* 卡片背面 */}
          <div 
            className="absolute inset-0 w-full h-full rounded-2xl bg-gradient-to-br from-purple-900 via-indigo-900 to-blue-900 border-4 border-purple-400/50 shadow-2xl flex items-center justify-center"
            style={{
              backfaceVisibility: 'hidden',
              WebkitBackfaceVisibility: 'hidden',
              transform: 'rotateY(0deg)'
            }}
          >
            <div className="text-center px-4">
              <div className="text-6xl md:text-8xl mb-3 md:mb-4 filter drop-shadow-2xl">🎴</div>
              <div className="text-white text-lg md:text-xl font-bold mb-2">
                {!isLoggedIn ? '请登录后抽牌' : hasDrawnToday ? '今日已抽牌' : '点击翻转抽牌'}
              </div>
              <div className="text-purple-200 text-xs md:text-sm">
                {!isLoggedIn ? '登录后每天可抽一次' : hasDrawnToday ? '明天再来吧' : '查看今日运势'}
              </div>
            </div>
          </div>

          {/* 卡片正面 */}
          <div 
            className="absolute inset-0 w-full h-full rounded-2xl bg-white border-4 border-purple-400/50 shadow-2xl overflow-hidden"
            style={{
              backfaceVisibility: 'hidden',
              WebkitBackfaceVisibility: 'hidden',
              transform: 'rotateY(180deg)'
            }}
          >
            {card.imageUrl ? (
              <img 
                src={card.imageUrl} 
                alt={card.cardNameCn}
                className={`w-full h-full object-cover ${dailyFortune?.isReversed ? 'rotate-180' : ''}`}
                onError={(e) => {
                  // 如果图片加载失败，显示符号
                  e.target.style.display = 'none'
                }}
              />
            ) : (
              <div className="w-full h-full bg-gradient-to-br from-purple-100 to-blue-100 flex items-center justify-center">
                <div className="text-center px-4">
                  <div className="text-6xl md:text-8xl mb-3 md:mb-4">{card.symbol || '🎴'}</div>
                  <div className="text-xl md:text-2xl font-bold text-gray-800">{card.cardNameCn}</div>
                  {card.cardNameEn && (
                    <div className="text-xs md:text-sm text-gray-600 mt-2">{card.cardNameEn}</div>
                  )}
                </div>
              </div>
            )}
            
            {/* 牌名和逆位标识 */}
            <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-3 md:p-4">
              <div className="flex items-center justify-between">
                <div className="text-white text-lg md:text-xl font-bold">{card.cardNameCn}</div>
                {dailyFortune?.isReversed && (
                  <div className="px-2 md:px-3 py-1 bg-red-500/80 rounded-full text-white text-xs md:text-sm font-semibold">
                    逆位
                  </div>
                )}
              </div>
              {dailyFortune?.date && (
                <div className="text-purple-200 text-xs md:text-sm mt-1">{dailyFortune.date}</div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 今日运势内容 */}
      {isFlipped && dailyFortune && (
        <div className="mt-4 md:mt-6 animate-fade-in scroll-smooth-mobile">
          <div className="bg-gradient-to-br from-purple-900/40 via-indigo-900/30 to-blue-900/40 rounded-2xl p-4 md:p-6 border border-purple-400/30 backdrop-blur-sm">
            {loadingFortune ? (
              <div className="text-center py-8">
                <div className="text-4xl mb-4 animate-spin">✨</div>
                <div className="text-purple-200">正在解读今日运势...</div>
              </div>
            ) : (
              <>
                <div className="text-center mb-4 md:mb-6">
                  <div className="text-xl md:text-2xl font-bold text-white mb-2">今日运势</div>
                  <div className="text-purple-200 text-xs md:text-sm">{dailyFortune.date}</div>
                </div>
                
                <div className="space-y-3 md:space-y-4">
                  {/* 综合运势解读 */}
                  {dailyFortune.interpretation && (
                    <div className="p-3 md:p-4 bg-white/10 rounded-xl border border-white/20">
                      <div className="text-purple-300 font-semibold mb-2 text-sm md:text-base">综合运势</div>
                      <div className="text-white leading-relaxed text-sm md:text-base">
                        {dailyFortune.interpretation}
                      </div>
                    </div>
                  )}
                  
                  {/* 感情/人际 */}
                  {dailyFortune.love && (
                    <div className="p-3 md:p-4 bg-pink-500/20 rounded-xl border border-pink-400/30">
                      <div className="text-pink-200 font-semibold mb-2 text-sm md:text-base">感情/人际</div>
                      <div className="text-pink-50 leading-relaxed text-sm md:text-base">
                        {dailyFortune.love}
                      </div>
                    </div>
                  )}
                  
                  {/* 建议 */}
                  {dailyFortune.advice && (
                    <div className="p-3 md:p-4 bg-yellow-500/20 rounded-xl border border-yellow-400/30">
                      <div className="text-yellow-200 font-semibold mb-2 text-sm md:text-base">箴言</div>
                      <div className="text-yellow-50 leading-relaxed text-sm md:text-base">
                        {dailyFortune.advice}
                      </div>
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* 翻转提示 */}
      {!isFlipped && (
        <div className="mt-3 md:mt-4 text-center">
          <div className="text-gray-400 text-xs md:text-sm animate-pulse">
            {!isLoggedIn ? '请先登录' : hasDrawnToday ? '今日已抽牌，明天再来' : '点击卡片抽牌查看今日运势'}
          </div>
        </div>
      )}
    </div>
  )
}
