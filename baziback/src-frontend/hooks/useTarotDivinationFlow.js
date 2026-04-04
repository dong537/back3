import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import {
  tarotApi,
  deepseekApi,
  calculationRecordApi,
  unwrapApiData,
} from '../api'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { safeArray, safeNumber, safeText } from '../utils/displayText'
import { getTarotCardName } from '../utils/tarotText'
import {
  buildCardsSummary,
  buildTarotPrompt,
  normalizeDrawnCard,
  TAROT_MODAL_STATE_KEY,
} from '../pages/tarotPageUtils'

export default function useTarotDivinationFlow({
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
}) {
  const navigate = useNavigate()
  const [question, setQuestion] = useState(initialState?.question || '')
  const [loading, setLoading] = useState(false)
  const [drawnCards, setDrawnCards] = useState(initialState?.drawnCards || [])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState(initialState?.aiResult || '')
  const [pointsPaid, setPointsPaid] = useState(
    initialState?.pointsPaid || false
  )

  const { isLoggedIn, credits, spendCredits, canSpendCredits } =
    authContext || {}

  useEffect(() => {
    if (isOpen && (drawnCards.length > 0 || question)) {
      onStateChange?.({
        question,
        drawnCards,
        aiResult,
        pointsPaid,
        spreadCode,
        spreadTitle,
        spreadCost,
      })
    }
  }, [
    question,
    drawnCards,
    aiResult,
    pointsPaid,
    isOpen,
    spreadCode,
    spreadTitle,
    spreadCost,
    onStateChange,
  ])

  useEffect(() => {
    if (isOpen && initialState) {
      setQuestion(safeText(initialState.question))
      setDrawnCards(safeArray(initialState.drawnCards))
      setAiResult(safeText(initialState.aiResult))
      setPointsPaid(initialState.pointsPaid === true)
    }
  }, [isOpen, initialState])

  useEffect(() => {
    if (isOpen && !initialState) {
      setQuestion('')
      setDrawnCards([])
      setAiResult('')
      setPointsPaid(false)
    }
  }, [isOpen, spreadCode, initialState])

  const handleDrawCards = async () => {
    const trimmedQuestion = safeText(question)
    if (!trimmedQuestion) {
      toast.error(copy.enterQuestion)
      return
    }

    if (spreadCost > 0 && !pointsPaid) {
      if (isLoggedIn) {
        if (!canSpendCredits(spreadCost)) {
          toast.error(
            copy.insufficientPoints(spreadCost, safeNumber(credits, 0))
          )
          return
        }
        const result = await spendCredits(
          spreadCost,
          copy.spreadSpendLabel(spreadTitle)
        )
        if (!result.success) {
          toast.error(safeText(result.message, copy.spendFailed))
          return
        }
        setPointsPaid(true)
        toast.success(copy.spendSuccess(spreadCost))
      } else {
        if (!points.canSpend(spreadCost)) {
          toast.error(copy.insufficientPointsGuest(spreadCost))
          return
        }
        const result = points.spend(spreadCost, copy.spreadSpendLabel(spreadTitle))
        if (!result.success) {
          toast.error(copy.spendFailed)
          return
        }
        setPointsPaid(true)
        onPointsUpdate?.(result.newTotal)
        toast.success(copy.spendSuccess(spreadCost))
      }
    }

    setLoading(true)
    setDrawnCards([])
    setAiResult('')
    try {
      const response = await tarotApi.drawCards(spreadCode, trimmedQuestion)
      const resultData = unwrapApiData(response)
      const cards = safeArray(resultData?.cards)
      if (cards.length === 0) {
        throw new Error(copy.noTarotData)
      }

      const formattedCards = cards.map((card, index) =>
        normalizeDrawnCard(card, locale, copy, index)
      )
      const summary = buildCardsSummary(
        formattedCards,
        locale,
        copy.unknownCard,
        copy
      )

      setDrawnCards(formattedCards)
      historyStorage.add({
        type: 'tarot',
        question: trimmedQuestion,
        dataId:
          formattedCards
            .map((card) => getTarotCardName(card, locale, ''))
            .filter(Boolean)
            .join('-') ||
          spreadCode ||
          copy.defaultSpreadTitle,
        summary: `${spreadTitle} - ${summary}`,
        data: {
          question: trimmedQuestion,
          spreadType: spreadCode,
          cards: formattedCards,
        },
      })
      toast.success(copy.drawSuccess)
    } catch (error) {
      logger.error('Draw cards error:', error)
      toast.error(
        safeText(
          error?.response?.data?.message || error?.message,
          copy.drawFailed
        )
      )
    } finally {
      setLoading(false)
    }
  }

  const handleAIInterpret = async () => {
    if (!drawnCards.length) return

    const cost = POINTS_COST.AI_INTERPRET
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(copy.aiInsufficientPoints(cost, safeNumber(credits, 0)))
        return
      }
    } else if (!points.canSpend(cost)) {
      toast.error(copy.aiInsufficientPointsGuest(cost))
      return
    }

    setAiLoading(true)
    setAiResult('')
    try {
      const cardsDesc = drawnCards
        .map(
          (card, index) =>
            `${index + 1}. ${getTarotCardName(card, locale, copy.unknownCard)} (${card?.reversed ? copy.reversed : copy.upright})`
        )
        .join(locale === 'en-US' ? '; ' : '、')
      const prompt = buildTarotPrompt(safeText(question), cardsDesc, copy)
      const response = await deepseekApi.interpretHexagram(prompt)

      let aiContent = ''
      if (response.data?.code === 200 && response.data?.data)
        aiContent = safeText(response.data.data)
      else if (typeof response.data === 'string')
        aiContent = safeText(response.data)
      else if (response.data?.content)
        aiContent = safeText(response.data.content)
      aiContent = safeText(aiContent, copy.aiGenerationFailed)

      if (isLoggedIn) {
        const result = await spendCredits(cost, copy.aiSpendLabel)
        if (result.success) toast.success(copy.spendSuccess(cost))
        else toast.error(safeText(result.message, copy.spendFailed))
      } else {
        const result = points.spend(cost, copy.aiSpendLabel)
        if (result.success) {
          onPointsUpdate?.(result.newTotal)
          toast.success(copy.spendSuccess(cost))
        }
      }

      setAiResult(aiContent)
    } catch (error) {
      logger.error('AI interpret error:', error)
      const message = safeText(error?.message, copy.aiFailed)
      toast.error(message)
      setAiResult(message)
    } finally {
      setAiLoading(false)
    }
  }

  const handleCardClick = (cardId) => {
    const target = safeText(cardId)
    if (!target) return

    sessionStorage.setItem(
      TAROT_MODAL_STATE_KEY,
      JSON.stringify({
        question,
        drawnCards,
        aiResult,
        pointsPaid,
        spreadCode,
        spreadTitle,
        spreadCost,
        isOpen: true,
      })
    )
    navigate(`/tarot/card/${encodeURIComponent(target)}`)
  }

  const handleToggleFavorite = async () => {
    if (!drawnCards.length) {
      toast.warn(copy.needDrawFirst)
      return
    }

    const summary = buildCardsSummary(
      drawnCards,
      locale,
      copy.unknownCard,
      copy
    )
    const item = {
      type: 'tarot',
      question: safeText(question),
      dataId:
        drawnCards
          .map((card) => getTarotCardName(card, locale, ''))
          .filter(Boolean)
          .join('-') ||
        spreadCode ||
        copy.defaultSpreadTitle,
      title: spreadTitle,
      summary: safeText(aiResult, summary),
      data: { question, spreadType: spreadCode, drawnCards, aiResult },
    }

    try {
      const favorited = await favoritesStorage.toggle(item)
      toast.success(favorited ? copy.favoriteSaved : copy.favoriteRemoved)
    } catch (error) {
      logger.error('Toggle favorite failed:', error)
      toast.error(copy.favoriteFailed)
    }
  }

  const handleSaveRecord = async () => {
    if (!drawnCards.length) {
      toast.warn(copy.needDrawFirst)
      return
    }

    try {
      const record = {
        recordType: 'tarot',
        recordTitle: spreadTitle,
        question: safeText(question),
        summary: safeText(
          aiResult,
          buildCardsSummary(drawnCards, locale, copy.unknownCard, copy)
        ),
        data: JSON.stringify({
          question,
          spreadType: spreadCode,
          spreadTitle,
          drawnCards,
          aiResult,
        }),
      }
      await calculationRecordApi.save(record)
      toast.success(copy.recordSaved)
    } catch (error) {
      logger.error('Save record failed:', error)
      toast.error(copy.saveFailed)
    }
  }

  return {
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
  }
}
