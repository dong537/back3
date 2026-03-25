import { safeArray, safeText } from './displayText'

const TAROT_SPREAD_TITLES = {
  SINGLE: {
    'zh-CN': '单牌占卜',
    'en-US': 'Single Card Reading',
  },
  PAST_PRESENT_FUTURE: {
    'zh-CN': '时间之流',
    'en-US': 'Flow of Time',
  },
  LOVE_TRIAD: {
    'zh-CN': '爱情三角阵',
    'en-US': 'Love Triangle Spread',
  },
  WANDS_TWO: {
    'zh-CN': '权杖二',
    'en-US': 'Two of Wands',
  },
  CUPS_KNIGHT: {
    'zh-CN': '圣杯骑士',
    'en-US': 'Knight of Cups',
  },
  SWORDS_THREE: {
    'zh-CN': '宝剑三',
    'en-US': 'Three of Swords',
  },
  CELTIC_CROSS: {
    'zh-CN': '凯尔特十字',
    'en-US': 'Celtic Cross',
  },
  MARSEILLE_CROSS: {
    'zh-CN': '马赛大十字',
    'en-US': 'Marseille Cross',
  },
  WHEEL_OF_FORTUNE: {
    'zh-CN': '命运之轮',
    'en-US': 'Wheel of Fortune',
  },
  TWO_PILLARS_SIX_STARS: {
    'zh-CN': '二柱六芒',
    'en-US': 'Two Pillars Six Stars',
  },
  ASTRO_HOLOGRAM: {
    'zh-CN': '星盘全息',
    'en-US': 'Astro Hologram',
  },
  SACRED_TIMELINE: {
    'zh-CN': '神圣时间线',
    'en-US': 'Sacred Timeline',
  },
}

function isEnglishLocale(locale) {
  return safeText(locale).startsWith('en')
}

function readLookupCard(card, cardLookup) {
  if (!cardLookup) return null
  const cardId = safeText(card?.cardId ?? card?.id)
  if (cardId && cardLookup[cardId]) return cardLookup[cardId]
  return null
}

export function getTarotCardName(card, locale, fallback = '', cardLookup = null) {
  const lookupCard = readLookupCard(card, cardLookup)
  const isEn = isEnglishLocale(locale)

  if (isEn) {
    return safeText(
      card?.cardNameEn ||
        card?.card_name_en ||
        card?.nameEn ||
        card?.name_en ||
        lookupCard?.cardNameEn ||
        lookupCard?.card_name_en ||
        card?.name ||
        card?.cardNameCn ||
        card?.card_name_cn,
      fallback
    )
  }

  return safeText(
    card?.cardNameCn ||
      card?.card_name_cn ||
      card?.nameCn ||
      card?.name_cn ||
      lookupCard?.cardNameCn ||
      lookupCard?.card_name_cn ||
      card?.name ||
      card?.cardNameEn ||
      card?.card_name_en,
    fallback
  )
}

export function getTarotOrientationLabel(card, locale, uprightLabel = '正位', reversedLabel = '逆位') {
  const reversed =
    card?.reversed === true ||
    card?.reversed === 'true' ||
    card?.orientation === 'REVERSED' ||
    card?.isReversed === true

  return reversed ? reversedLabel : uprightLabel
}

export function getTarotSpreadTitle(spreadCode, locale, fallback = '') {
  const code = safeText(spreadCode).toUpperCase()
  if (!code) return fallback
  const localized = TAROT_SPREAD_TITLES[code]?.[isEnglishLocale(locale) ? 'en-US' : 'zh-CN']
  return safeText(localized, fallback)
}

export function buildTarotCardsText(cards, locale, options = {}) {
  const {
    cardLookup = null,
    fallback = '',
    includeOrientation = false,
    uprightLabel = '正位',
    reversedLabel = '逆位',
    separator,
  } = options

  const localizedSeparator = separator || (isEnglishLocale(locale) ? ', ' : '、')
  const texts = safeArray(cards)
    .map((card) => {
      const name = getTarotCardName(card, locale, '', cardLookup)
      if (!name) return ''
      if (!includeOrientation) return name
      const orientation = getTarotOrientationLabel(card, locale, uprightLabel, reversedLabel)
      return `${name} (${orientation})`
    })
    .filter(Boolean)

  return safeText(texts.join(localizedSeparator), fallback)
}
