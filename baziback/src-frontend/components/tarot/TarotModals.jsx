import {
  Coins,
  History,
  Share2,
  Shuffle,
  Sparkles,
  Star,
  X,
} from 'lucide-react'
import ThinkingChain from '../ThinkingChain'
import { POINTS_COST } from '../../utils/pointsConfig'
import { logger } from '../../utils/logger'
import { toast } from '../Toast'
import { formatLocaleDate, safeText } from '../../utils/displayText'
import { getTarotCardName } from '../../utils/tarotText'
import { formatCredits } from '../../pages/tarotPageUtils'
import useTarotDivinationFlow from '../../hooks/useTarotDivinationFlow'

export function DailyDrawModal({ isOpen, onClose, dailyResult, locale, copy }) {
  if (!isOpen || !dailyResult) return null

  const cardName = getTarotCardName(
    dailyResult,
    locale,
    copy.dailyFallbackTitle
  )
  const shareText =
    `${cardName}\n${safeText(dailyResult?.interpretation) || safeText(dailyResult?.meaning) || ''}`.trim()

  const handleShare = async () => {
    try {
      if (navigator.share) {
        await navigator.share({ title: cardName, text: shareText })
      } else {
        await navigator.clipboard.writeText(shareText)
      }
      toast.success(copy.shareSuccess)
    } catch (error) {
      if (error?.name !== 'AbortError') {
        logger.error('Daily draw share failed:', error)
        toast.error(copy.shareFailed)
      }
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-md overflow-y-auto rounded-[32px] border border-white/10 bg-[#120d0c]/95 shadow-[0_24px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center text-[#8f7b66] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>
        <div className="p-6 pt-8">
          <div className="mb-6 flex justify-center">
            <div className={`relative ${dailyResult?.isReversed ? 'rotate-180' : ''}`}>
              {safeText(dailyResult?.imageUrl) ? (
                <img
                  src={dailyResult.imageUrl}
                  alt={cardName}
                  className="h-72 w-48 rounded-xl object-cover shadow-lg"
                />
              ) : (
                <div className="flex h-72 w-48 items-center justify-center rounded-xl bg-[linear-gradient(135deg,#7f2416_0%,#b84e2b_55%,#d6b77a_100%)] shadow-lg">
                  <span className="text-6xl">
                    {safeText(dailyResult?.symbol, '🃏')}
                  </span>
                </div>
              )}
            </div>
          </div>
          <div className="mb-6 text-center">
            <h2 className="inline-flex items-center gap-2 text-2xl font-bold text-[#f4ece1]">
              {cardName}
              <span
                className={`rounded-full px-3 py-1 text-sm ${dailyResult?.isReversed ? 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]' : 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'}`}
              >
                {dailyResult?.isReversed ? copy.reversed : copy.upright}
              </span>
            </h2>
            <p className="mt-2 text-[#8f7b66]">
              {formatLocaleDate(dailyResult?.date, locale)}
            </p>
          </div>
          <div className="space-y-4 text-sm leading-relaxed text-[#e4d6c8]">
            {safeText(dailyResult?.interpretation) && (
              <p>{safeText(dailyResult?.interpretation)}</p>
            )}
            {safeText(dailyResult?.meaning) && (
              <p>{safeText(dailyResult?.meaning)}</p>
            )}
            {safeText(dailyResult?.advice) && (
              <p className="italic text-[#bdaa94]">
                {safeText(dailyResult?.advice)}
              </p>
            )}
          </div>
          <button
            onClick={handleShare}
            className="mt-6 flex w-full items-center justify-center gap-2 rounded-full border border-white/10 bg-white/[0.04] py-3 text-[#f4ece1] hover:bg-white/[0.08]"
          >
            <Share2 size={18} />
            <span>{copy.share}</span>
          </button>
        </div>
      </div>
    </div>
  )
}

export function DivinationModal({
  isOpen,
  onClose,
  spreadCode,
  spreadTitle,
  spreadCost,
  onPointsUpdate,
  authContext,
  initialState,
  onStateChange,
  locale,
  copy,
}) {
  const {
    question,
    setQuestion,
    loading,
    drawnCards,
    aiLoading,
    aiResult,
    handleDrawCards,
    handleAIInterpret,
    handleCardClick,
    handleToggleFavorite,
    handleSaveRecord,
  } = useTarotDivinationFlow({
    isOpen,
    spreadCode,
    spreadTitle,
    spreadCost,
    onPointsUpdate,
    authContext,
    initialState,
    onStateChange,
    locale,
    copy,
  })

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-[32px] border border-white/10 bg-[#120d0c]/95 shadow-[0_24px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center text-[#8f7b66] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>
        <div className="p-6">
          <div className="mb-6 text-center">
            <h2 className="font-serif-title text-xl font-bold text-[#f4ece1]">
              {spreadTitle}
            </h2>
            <p className="mt-1 text-sm text-[#8f7b66]">{copy.modalHint}</p>
            {spreadCost > 0 && (
              <div className="mt-2 inline-flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1">
                <Coins size={14} className="text-[#d0a85b]" />
                <span className="text-sm text-[#dcb86f]">
                  {copy.costHint(spreadCost)}
                </span>
              </div>
            )}
          </div>

          <div className="mb-5">
            <textarea
              placeholder={copy.questionPlaceholder}
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              rows={3}
              className="w-full resize-none rounded-[20px] border border-white/10 bg-white/[0.04] p-4 text-[#f4ece1] placeholder-[#8f7b66] focus:border-[#a34224]/40 focus:outline-none focus:ring-2 focus:ring-[#a34224]/30"
            />
          </div>

          <button
            onClick={handleDrawCards}
            disabled={loading || !safeText(question)}
            className="btn-primary-theme mb-5 flex w-full items-center justify-center space-x-2 py-3 font-bold transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Shuffle size={18} className={loading ? 'animate-spin' : ''} />
            <span>
              {loading
                ? copy.drawLoading
                : spreadCost > 0
                  ? `${copy.drawButton} (${formatCredits(spreadCost, copy)})`
                  : copy.drawButton}
            </span>
          </button>

          {drawnCards.length > 0 && (
            <div className="mb-5 rounded-[28px] border border-white/10 bg-white/[0.04] p-4">
              <h3 className="mb-3 text-base font-bold text-[#f4ece1]">
                {copy.resultTitle}
              </h3>
              <div
                className={`mb-4 grid gap-3 ${drawnCards.length === 1 ? 'mx-auto max-w-[150px] grid-cols-1' : drawnCards.length <= 3 ? 'grid-cols-3' : 'grid-cols-2 sm:grid-cols-3'}`}
              >
                {drawnCards.map((card, index) => (
                  <div
                    key={`${card.id || card.name}-${index}`}
                    onClick={() =>
                      handleCardClick(card.id || card.cardId || card.name)
                    }
                    className="cursor-pointer rounded-[20px] border border-white/10 bg-white/[0.04] p-3 text-center transition-all hover:border-[#d0a85b]/24 hover:bg-white/[0.08]"
                  >
                    <div
                      className={`mb-2 text-4xl ${card.reversed ? 'rotate-180' : ''}`}
                    >
                      {safeText(card.symbol, '🃏')}
                    </div>
                    <div className="mb-1 text-sm font-medium text-[#f4ece1]">
                      {getTarotCardName(card, locale, copy.unknownCard)}
                    </div>
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs ${card.reversed ? 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]' : 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'}`}
                    >
                      {card.reversed ? copy.reversed : copy.upright}
                    </span>
                  </div>
                ))}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={handleAIInterpret}
                  disabled={aiLoading}
                  className="btn-primary-theme flex flex-1 items-center justify-center space-x-2 py-2.5 text-sm font-medium transition-all hover:opacity-90"
                >
                  <Sparkles size={16} className={aiLoading ? 'animate-spin' : ''} />
                  <span>{copy.aiInterpret}</span>
                  <span className="rounded-full bg-white/20 px-1.5 py-0.5 text-xs">
                    {formatCredits(POINTS_COST.AI_INTERPRET, copy)}
                  </span>
                </button>
                <button
                  onClick={handleToggleFavorite}
                  className="rounded-[18px] border border-[#d0a85b]/24 bg-[#7a3218]/14 p-2.5 text-[#dcb86f] transition-all hover:bg-[#7a3218]/24"
                  title={copy.favorite}
                  aria-label={copy.favorite}
                >
                  <Star size={16} />
                </button>
                <button
                  onClick={handleSaveRecord}
                  className="rounded-[18px] border border-white/10 bg-white/[0.04] p-2.5 text-[#f4ece1] transition-all hover:bg-white/[0.08]"
                  title={copy.saveRecord}
                  aria-label={copy.saveRecord}
                >
                  <History size={16} />
                </button>
              </div>
            </div>
          )}

          {safeText(aiResult) && (
            <div className="rounded-[28px] border border-[#d0a85b]/20 bg-[#7a3218]/10 p-4">
              <ThinkingChain
                isThinking={aiLoading}
                finalContent={aiResult}
                lightMode={false}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
