import { safeArray, safeNumber, safeText } from '../utils/displayText'
import { buildTarotCardsText, getTarotCardName } from '../utils/tarotText'

export const TAROT_MODAL_STATE_KEY = 'tarot_divination_modal_state'

export function buildTarotPrompt(question, cardsDesc, copy) {
  return [
    copy.promptIntro,
    '',
    copy.promptRulesTitle,
    ...copy.promptRules,
    '',
    copy.promptInfoTitle,
    `${copy.promptQuestionLabel} ${question}`,
    `${copy.promptCardsLabel} ${cardsDesc}`,
    '',
    copy.promptRequestTitle,
    ...copy.promptRequests,
    '',
    copy.promptClosing,
  ].join('\n')
}

export function getBadgeClass(tone) {
  if (tone === 'hot') return 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]'
  if (tone === 'free') return 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'
  if (tone === 'classic') return 'border border-[#d0a85b]/30 bg-[#d0a85b]/12 text-[#dcb86f]'
  if (tone === 'pro')
    return 'border border-[#f3d8a8]/10 bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb]'
  return 'border border-white/10 bg-white/[0.05] text-[#bdaa94]'
}

export function normalizeDrawnCard(card, locale, copy, index) {
  const fallbackName = `${copy.unknownCard} ${index + 1}`
  return {
    id: safeText(card?.cardId ?? card?.id ?? card?.index, ''),
    cardId: safeText(card?.cardId ?? card?.id ?? card?.index, ''),
    cardNameCn: safeText(card?.cardNameCn ?? card?.card_name_cn),
    cardNameEn: safeText(card?.cardNameEn ?? card?.card_name_en),
    name: getTarotCardName(card, locale, fallbackName),
    reversed:
      card?.reversed === true ||
      card?.reversed === 'true' ||
      card?.orientation === 'REVERSED' ||
      card?.isReversed === true,
    symbol: safeText(card?.symbol, '🃏'),
    position: safeText(card?.position),
    positionMeaning: safeText(card?.positionMeaning),
  }
}

export function buildCardsSummary(cards, locale, fallback, copy) {
  return buildTarotCardsText(cards, locale, {
    fallback,
    includeOrientation: false,
    uprightLabel: copy?.upright,
    reversedLabel: copy?.reversed,
  })
}

export function formatCredits(value, copy) {
  return `${safeNumber(value, 0)}${copy.creditUnitCompact}`
}

export function restoreTarotModalState(defaultSpreadTitle) {
  const savedState = sessionStorage.getItem(TAROT_MODAL_STATE_KEY)
  if (!savedState) return null

  const state = JSON.parse(savedState)
  if (!state?.isOpen || safeArray(state?.drawnCards).length === 0) {
    return null
  }

  return {
    selectedSpread: {
      code: state.spreadCode,
      title: safeText(state.spreadTitle, defaultSpreadTitle),
      cost: safeNumber(state.spreadCost, 0),
    },
    modalInitialState: {
      ...state,
      drawnCards: safeArray(state.drawnCards),
      question: safeText(state.question),
      aiResult: safeText(state.aiResult),
    },
  }
}

export function persistTarotModalState(state) {
  sessionStorage.setItem(
    TAROT_MODAL_STATE_KEY,
    JSON.stringify({ ...state, isOpen: true })
  )
}

export function clearTarotModalState() {
  sessionStorage.removeItem(TAROT_MODAL_STATE_KEY)
}
