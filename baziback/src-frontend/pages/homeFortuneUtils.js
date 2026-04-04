import { safeArray, safeText } from '../utils/displayText'

const DAILY_FORTUNE_CACHE_PREFIX = 'home_daily_fortune_detail'
const CJK_TEXT_PATTERN = /[\u3400-\u9fff]/
const LATIN_TEXT_PATTERN = /[A-Za-z]{3,}/

function getTodayCacheDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function pickLocalizedCandidate(source, locale, key) {
  if (!source || typeof source !== 'object') return ''

  const englishCandidates = [
    source?.[`${key}En`],
    source?.[`${key}_en`],
    source?.[`${key}English`],
    source?.[`${key}_english`],
    source?.[key],
  ]

  const chineseCandidates = [
    source?.[`${key}Zh`],
    source?.[`${key}_zh`],
    source?.[`${key}Cn`],
    source?.[`${key}_cn`],
    source?.[key],
  ]

  const candidates = locale === 'en-US' ? englishCandidates : chineseCandidates
  return candidates.find((candidate) => safeText(candidate)) || ''
}

export function normalizeLocalizedText(value, locale, fallback = '') {
  const sourceText = safeText(value, fallback)
  if (!sourceText) return fallback

  const hasCJKText = CJK_TEXT_PATTERN.test(sourceText)
  const hasLatinText = LATIN_TEXT_PATTERN.test(sourceText)

  if (locale === 'en-US') {
    return hasCJKText ? fallback : sourceText
  }

  if (hasLatinText && !hasCJKText) {
    return fallback
  }

  if (hasLatinText && hasCJKText) {
    return fallback
  }

  return sourceText
}

function normalizeLocalizedList(values, locale) {
  return safeArray(values)
    .map((value) => normalizeLocalizedText(value, locale, ''))
    .filter(Boolean)
}

export function normalizeFortuneDetailData(data, locale) {
  if (!data || typeof data !== 'object') return null

  const aspects = Object.entries(data?.aspects || {}).reduce(
    (result, [key, aspect]) => {
      result[key] = {
        ...aspect,
        analysis: normalizeLocalizedText(
          pickLocalizedCandidate(aspect, locale, 'analysis'),
          locale,
          ''
        ),
      }
      return result
    },
    {}
  )

  return {
    ...data,
    date: normalizeLocalizedText(data?.date, locale, ''),
    overallAdvice: normalizeLocalizedText(
      pickLocalizedCandidate(data, locale, 'overallAdvice'),
      locale,
      ''
    ),
    keywords: normalizeLocalizedList(data?.keywords, locale),
    suitableActions: normalizeLocalizedList(data?.suitableActions, locale),
    unsuitableActions: normalizeLocalizedList(data?.unsuitableActions, locale),
    luckyElements: {
      color: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'color'),
        locale,
        ''
      ),
      number: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'number'),
        locale,
        ''
      ),
      direction: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'direction'),
        locale,
        ''
      ),
      time: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'time'),
        locale,
        ''
      ),
    },
    aspects,
  }
}

function getDailyFortuneCacheKey(userId, locale) {
  return `${DAILY_FORTUNE_CACHE_PREFIX}:${getTodayCacheDate()}:${userId ?? 'guest'}:${locale ?? 'zh-CN'}`
}

export function readDailyFortuneCache(userId, locale) {
  try {
    const raw = sessionStorage.getItem(getDailyFortuneCacheKey(userId, locale))
    if (!raw) return null
    const parsed = JSON.parse(raw)
    return parsed?.data || null
  } catch (error) {
    console.warn('读取首页运势缓存失败:', error)
    return null
  }
}

export function writeDailyFortuneCache(userId, locale, data) {
  try {
    sessionStorage.setItem(
      getDailyFortuneCacheKey(userId, locale),
      JSON.stringify({
        cachedAt: Date.now(),
        data,
      })
    )
  } catch (error) {
    console.warn('写入首页运势缓存失败:', error)
  }
}

export function getScoreLevel(score, copy) {
  if (score >= 85) {
    return {
      label: copy.levels.excellent,
      textClass: 'text-emerald-200',
      badgeClass:
        'border border-emerald-400/20 bg-emerald-500/10 text-emerald-200',
    }
  }

  if (score >= 75) {
    return {
      label: copy.levels.good,
      textClass: 'text-sky-200',
      badgeClass: 'border border-sky-400/20 bg-sky-500/10 text-sky-200',
    }
  }

  if (score >= 65) {
    return {
      label: copy.levels.fair,
      textClass: 'text-amber-100',
      badgeClass: 'border border-amber-400/20 bg-amber-500/10 text-amber-100',
    }
  }

  return {
    label: copy.levels.caution,
    textClass: 'text-orange-200',
    badgeClass: 'border border-orange-400/20 bg-orange-500/10 text-orange-200',
  }
}

export function formatDisplayDate(timestamp, locale) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleDateString(locale, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
