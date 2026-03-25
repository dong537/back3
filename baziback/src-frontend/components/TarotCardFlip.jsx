import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { tarotApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'
import { resolvePageLocale, safeText } from '../utils/displayText'

function defaultCard(isLoggedIn) {
  if (!isLoggedIn) {
    return {
      cardId: null,
      cardNameCn: '登录后抽牌',
      cardNameEn: 'Please sign in first',
      symbol: '✦',
      imageUrl: null,
    }
  }

  return {
    cardId: null,
    cardNameCn: '点击翻牌',
    cardNameEn: 'Tap to Draw',
    symbol: '✦',
    imageUrl: null,
  }
}

export default function TarotCardFlip({ onCardFlipped }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const t = (zh, en) => (isEn ? en : zh)
  const { isLoggedIn } = useAuth()

  const [isFlipped, setIsFlipped] = useState(false)
  const [isFlipping, setIsFlipping] = useState(false)
  const [card, setCard] = useState(null)
  const [loading, setLoading] = useState(false)
  const [dailyFortune, setDailyFortune] = useState(null)
  const [loadingFortune, setLoadingFortune] = useState(false)
  const [hasDrawnToday, setHasDrawnToday] = useState(false)

  useEffect(() => {
    void checkTodayDraw()
  }, [isLoggedIn, isEn])

  const checkTodayDraw = async () => {
    if (!isLoggedIn) {
      setCard(defaultCard(false))
      setHasDrawnToday(false)
      setIsFlipped(false)
      return
    }

    try {
      setLoading(true)
      const response = await tarotApi.getTodayDraw()

      if (response.data?.success && response.data.data) {
        const drawResult = response.data.data
        setCard({
          cardId: drawResult.cardId,
          cardNameCn: drawResult.cardNameCn,
          cardNameEn: drawResult.cardNameEn,
          symbol: drawResult.symbol || '✦',
          imageUrl: drawResult.imageUrl,
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
          keyword: drawResult.keyword,
        })
        setIsFlipped(true)
        setHasDrawnToday(true)
      } else {
        setCard(defaultCard(true))
        setHasDrawnToday(false)
      }
    } catch (error) {
      logger.error('Check today draw error:', error)
      const message = safeText(error.response?.data?.message)
      if (message && (message.includes('还没') || message.includes('not'))) {
        setCard(defaultCard(true))
        setHasDrawnToday(false)
      } else {
        toast.error(t('检查今日抽牌记录失败', 'Failed to check today draw'))
      }
    } finally {
      setLoading(false)
    }
  }

  const drawDailyCard = async () => {
    try {
      setLoadingFortune(true)
      const response = await tarotApi.drawDailyCard()

      if (response.data?.success && response.data.data) {
        const drawResult = response.data.data
        const nextCard = {
          cardId: drawResult.cardId,
          cardNameCn: drawResult.cardNameCn,
          cardNameEn: drawResult.cardNameEn,
          symbol: drawResult.symbol || '✦',
          imageUrl: drawResult.imageUrl,
        }
        setCard(nextCard)
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
          keyword: drawResult.keyword,
        })
        setHasDrawnToday(true)
        toast.success(t('抽牌成功', 'Card drawn successfully'))
        onCardFlipped?.(true, nextCard)
      } else {
        throw new Error(response.data?.message || t('抽牌失败', 'Draw failed'))
      }
    } catch (error) {
      logger.error('Draw daily card error:', error)
      let errorMessage = t(
        '抽牌失败，请稍后重试',
        'Failed to draw the card, please try again later'
      )

      if (error.response) {
        errorMessage =
          error.response.data?.message ||
          error.response.data?.error ||
          error.message ||
          errorMessage
      } else if (error.request) {
        errorMessage = t(
          '网络错误，请检查网络连接',
          'Network error, please check your connection'
        )
      } else {
        errorMessage = error.message || errorMessage
      }

      toast.error(errorMessage)
      setIsFlipped(false)
    } finally {
      setLoadingFortune(false)
    }
  }

  const handleCardClick = async () => {
    if (isFlipping || loading) return

    if (!isLoggedIn) {
      toast.error(t('请先登录后再抽牌', 'Please sign in before drawing a card'))
      return
    }

    if (hasDrawnToday) return

    setIsFlipping(true)
    setTimeout(async () => {
      setIsFlipped(true)
      setIsFlipping(false)
      await drawDailyCard()
    }, 300)
  }

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="text-center">
          <div className="mb-4 text-6xl text-[#dcb86f] animate-spin">✦</div>
          <div className="text-[#8f7b66]">
            {t('正在加载塔罗牌...', 'Loading tarot card...')}
          </div>
        </div>
      </div>
    )
  }

  if (!card) return null

  const cardTitle = isEn
    ? card.cardNameEn || card.cardNameCn
    : card.cardNameCn || card.cardNameEn

  return (
    <div className="tarot-card-flip-container relative mx-auto w-full max-w-md">
      <div
        className="tap-highlight no-select relative mx-auto w-full cursor-pointer"
        style={{ perspective: '1000px', aspectRatio: '2/3', maxWidth: '100%' }}
        onClick={handleCardClick}
        onTouchStart={(event) => {
          event.currentTarget.style.transform = 'scale(0.98)'
        }}
        onTouchEnd={(event) => {
          event.currentTarget.style.transform = 'scale(1)'
        }}
      >
        <div
          className="relative h-full w-full"
          style={{
            transformStyle: 'preserve-3d',
            transition: 'transform 0.6s',
            transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
          }}
        >
          <div
            className="absolute inset-0 flex h-full w-full items-center justify-center rounded-[28px] border border-[#d0a85b]/28 bg-[linear-gradient(180deg,rgba(34,22,18,0.98),rgba(15,11,10,0.92))] shadow-[0_24px_60px_rgba(0,0,0,0.34)]"
            style={{
              backfaceVisibility: 'hidden',
              WebkitBackfaceVisibility: 'hidden',
              transform: 'rotateY(0deg)',
            }}
          >
            <div className="px-6 text-center">
              <div className="mb-3 text-6xl text-[#dcb86f] drop-shadow-2xl md:mb-4 md:text-8xl">
                ✦
              </div>
              <div className="mb-2 text-lg font-bold text-[#f4ece1] md:text-xl">
                {!isLoggedIn
                  ? t('登录后抽牌', 'Please sign in first')
                  : hasDrawnToday
                    ? t('今日已抽牌', 'Already drawn today')
                    : t('点击翻转抽牌', 'Tap to draw')}
              </div>
              <div className="text-xs text-[#bdaa94] md:text-sm">
                {!isLoggedIn
                  ? t('登录后每天可抽一次', 'Sign in to draw once per day')
                  : hasDrawnToday
                    ? t(
                        '明天再来看看新的指引',
                        'Come back tomorrow for a new message'
                      )
                    : t('查看今日运势', "Reveal today's guidance")}
              </div>
            </div>
          </div>

          <div
            className="absolute inset-0 h-full w-full overflow-hidden rounded-[28px] border border-[#d0a85b]/28 bg-[#f4ece1] shadow-[0_24px_60px_rgba(0,0,0,0.34)]"
            style={{
              backfaceVisibility: 'hidden',
              WebkitBackfaceVisibility: 'hidden',
              transform: 'rotateY(180deg)',
            }}
          >
            {card.imageUrl ? (
              <img
                src={card.imageUrl}
                alt={cardTitle}
                className={`h-full w-full object-cover ${
                  dailyFortune?.isReversed ? 'rotate-180' : ''
                }`}
                onError={(event) => {
                  event.currentTarget.style.display = 'none'
                }}
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center bg-[linear-gradient(180deg,#f4ece1_0%,#e6d9c8_100%)]">
                <div className="px-4 text-center">
                  <div className="mb-3 text-6xl text-[#a34224] md:mb-4 md:text-8xl">
                    {card.symbol || '✦'}
                  </div>
                  <div className="text-xl font-bold text-[#221917] md:text-2xl">
                    {cardTitle}
                  </div>
                  {card.cardNameEn && !isEn && (
                    <div className="mt-2 text-xs text-[#7f6c5d] md:text-sm">
                      {card.cardNameEn}
                    </div>
                  )}
                </div>
              </div>
            )}

            <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-3 md:p-4">
              <div className="flex items-center justify-between">
                <div className="text-lg font-bold text-white md:text-xl">
                  {cardTitle}
                </div>
                {dailyFortune?.isReversed && (
                  <div className="rounded-full border border-[#e19a84]/30 bg-[#7a3218]/70 px-2 py-1 text-xs font-semibold text-white md:px-3 md:text-sm">
                    {t('逆位', 'Reversed')}
                  </div>
                )}
              </div>
              {dailyFortune?.date && (
                <div className="mt-1 text-xs text-[#e4d6c8] md:text-sm">
                  {dailyFortune.date}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {isFlipped && dailyFortune && (
        <div className="scroll-smooth-mobile mt-4 animate-fade-in md:mt-6">
          <div className="panel-soft rounded-[26px] border border-white/10 p-4 md:p-6">
            {loadingFortune ? (
              <div className="py-8 text-center">
                <div className="mb-4 text-4xl text-[#dcb86f] animate-spin">
                  ✦
                </div>
                <div className="text-[#bdaa94]">
                  {t("正在解读今日运势...", "Reading today's guidance...")}
                </div>
              </div>
            ) : (
              <>
                <div className="mb-4 text-center md:mb-6">
                  <div className="mb-2 text-xl font-bold text-[#f4ece1] md:text-2xl">
                    {t('今日运势', "Today's Guidance")}
                  </div>
                  <div className="text-xs text-[#8f7b66] md:text-sm">
                    {dailyFortune.date}
                  </div>
                </div>

                <div className="space-y-3 md:space-y-4">
                  {safeText(dailyFortune.interpretation) && (
                    <div className="rounded-[22px] border border-white/10 bg-white/[0.04] p-3 md:p-4">
                      <div className="mb-2 text-sm font-semibold text-[#dcb86f] md:text-base">
                        {t('综合运势', 'Overall Reading')}
                      </div>
                      <div className="text-sm leading-relaxed text-[#f4ece1] md:text-base">
                        {dailyFortune.interpretation}
                      </div>
                    </div>
                  )}

                  {safeText(dailyFortune.love) && (
                    <div className="rounded-[22px] border border-[#a34224]/24 bg-[#7a3218]/14 p-3 md:p-4">
                      <div className="mb-2 text-sm font-semibold text-[#e19a84] md:text-base">
                        {t('感情 / 人际', 'Love / Social')}
                      </div>
                      <div className="text-sm leading-relaxed text-[#f4ece1] md:text-base">
                        {dailyFortune.love}
                      </div>
                    </div>
                  )}

                  {safeText(dailyFortune.advice) && (
                    <div className="rounded-[22px] border border-[#d0a85b]/24 bg-[#6a4a1e]/14 p-3 md:p-4">
                      <div className="mb-2 text-sm font-semibold text-[#f0d9a5] md:text-base">
                        {t('建议', 'Advice')}
                      </div>
                      <div className="text-sm leading-relaxed text-[#f4ece1] md:text-base">
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

      {!isFlipped && (
        <div className="mt-3 text-center md:mt-4">
          <div className="animate-pulse text-xs text-[#8f7b66] md:text-sm">
            {!isLoggedIn
              ? t('请先登录', 'Please sign in first')
              : hasDrawnToday
                ? t('今日已抽牌，明天再来', 'Already drawn today, come back tomorrow')
                : t(
                    '点击卡牌抽牌，查看今日运势',
                    "Tap the card to reveal today's guidance"
                  )}
          </div>
        </div>
      )}
    </div>
  )
}
