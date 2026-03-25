import zhCN from '../i18n/zh-CN.json'
import enUS from '../i18n/en-US.json'

const STORAGE_KEY = 'lang'

function normalizeLocaleTag(language) {
  return String(language || '')
    .trim()
    .replace('_', '-')
    .toLowerCase()
}

export function resolveUiLocale(language) {
  const normalized = normalizeLocaleTag(language)
  return normalized.startsWith('en') ? 'en-US' : 'zh-CN'
}

function resolveTranslationKey(locale) {
  return locale === 'en-US' ? 'en' : 'zh'
}

export function getStoredUiLocale() {
  try {
    return resolveUiLocale(localStorage.getItem(STORAGE_KEY))
  } catch {
    return 'zh-CN'
  }
}

function collectTranslationPairs(zhValue, enValue, result = new Map()) {
  if (typeof zhValue === 'string' && typeof enValue === 'string') {
    const zh = zhValue.trim()
    const en = enValue.trim()
    if (zh && en) {
      result.set(`${zh}:::${en}`, { zh, en })
    }
    return result
  }

  if (Array.isArray(zhValue) && Array.isArray(enValue)) {
    const length = Math.min(zhValue.length, enValue.length)
    for (let index = 0; index < length; index += 1) {
      collectTranslationPairs(zhValue[index], enValue[index], result)
    }
    return result
  }

  if (
    zhValue &&
    enValue &&
    typeof zhValue === 'object' &&
    typeof enValue === 'object' &&
    !Array.isArray(zhValue) &&
    !Array.isArray(enValue)
  ) {
    Object.keys(zhValue).forEach((key) => {
      if (Object.hasOwn(enValue, key)) {
        collectTranslationPairs(zhValue[key], enValue[key], result)
      }
    })
  }

  return result
}

const RESOURCE_TRANSLATIONS = Array.from(
  collectTranslationPairs(zhCN, enUS).values()
)

const MANUAL_TRANSLATIONS = [
  { zh: '加载中...', en: 'Loading...' },
  { zh: '正在加载...', en: 'Loading...' },
  { zh: '请先登录', en: 'Please sign in first' },
  { zh: '请先登录后再签到', en: 'Please sign in before checking in' },
  { zh: '请先登录后再进行签到', en: 'Please sign in before checking in' },
  { zh: '签到失败', en: 'Check-in failed' },
  { zh: '签到失败，请稍后重试', en: 'Check-in failed, please try again later' },
  { zh: '登录解锁 AI 解读', en: 'Sign in to unlock AI interpretation' },
  { zh: '今天', en: 'Today' },
  { zh: '日', en: 'Day' },
  { zh: '周', en: 'Week' },
  { zh: '月', en: 'Month' },
  { zh: '年', en: 'Year' },
  { zh: '查看星盘', en: 'View chart' },
  { zh: '综合分数', en: 'Overall Score' },
  { zh: '今天运气还不错', en: 'Your luck looks good today' },
  {
    zh: '抽张卡获得启发，提升能量吧',
    en: 'Draw a card for inspiration and a boost of energy',
  },
  { zh: '免费抽卡', en: 'Free draw' },
  { zh: '爱情提醒', en: 'Love Reminder' },
  { zh: '来自生肖', en: 'From zodiac' },
  { zh: '全文', en: 'Full text' },
  { zh: '建议', en: 'Advice' },
  { zh: '避免', en: 'Avoid' },
  { zh: '宝石蓝', en: 'Sapphire blue' },
  { zh: '白水晶', en: 'Clear quartz' },
  { zh: '正东', en: 'East' },
  { zh: '周日', en: 'Sun' },
  { zh: '周一', en: 'Mon' },
  { zh: '周二', en: 'Tue' },
  { zh: '周三', en: 'Wed' },
  { zh: '周四', en: 'Thu' },
  { zh: '周五', en: 'Fri' },
  { zh: '周六', en: 'Sat' },
  { zh: '最后更新：', en: 'Last updated: ' },
  { zh: '隐私政策', en: 'Privacy Policy' },
  { zh: '用户协议', en: 'User Agreement' },
  { zh: '用户服务协议', en: 'Terms of Service' },
]

const TRANSLATION_PAIRS = [...RESOURCE_TRANSLATIONS, ...MANUAL_TRANSLATIONS]
const ZH_EXACT_LOOKUP = new Map()
const EN_EXACT_LOOKUP = new Map()
const ZH_REPLACE_ENTRIES = []
const EN_REPLACE_ENTRIES = []

for (const pair of TRANSLATION_PAIRS) {
  if (!pair?.zh || !pair?.en) continue
  ZH_EXACT_LOOKUP.set(pair.zh, pair)
  EN_EXACT_LOOKUP.set(pair.en, pair)
  if (pair.zh.length >= 2) {
    ZH_REPLACE_ENTRIES.push(pair)
  }
  if (pair.en.length >= 2) {
    EN_REPLACE_ENTRIES.push(pair)
  }
}

ZH_REPLACE_ENTRIES.sort((left, right) => right.zh.length - left.zh.length)
EN_REPLACE_ENTRIES.sort((left, right) => right.en.length - left.en.length)

const REGEX_TRANSLATIONS = [
  {
    pattern: /^共(\d+)条$/,
    zh: (_, count) => `共${count}条`,
    en: (_, count) => `Total ${count}`,
  },
  {
    pattern: /^(\d+) 条未读消息$/,
    zh: (_, count) => `${count} 条未读消息`,
    en: (_, count) => `${count} unread notifications`,
  },
  {
    pattern: /^(\d+)分钟前$/,
    zh: (_, count) => `${count}分钟前`,
    en: (_, count) => `${count} min ago`,
  },
  {
    pattern: /^(\d+)小时前$/,
    zh: (_, count) => `${count}小时前`,
    en: (_, count) => `${count} hr ago`,
  },
  {
    pattern: /^(\d+)天前$/,
    zh: (_, count) => `${count}天前`,
    en: (_, count) => `${count} day${count === '1' ? '' : 's'} ago`,
  },
  {
    pattern: /^确定要删除选中的(\d+)条收藏吗？$/,
    zh: (_, count) => `确定要删除选中的${count}条收藏吗？`,
    en: (_, count) => `Delete ${count} selected favorites?`,
  },
  {
    pattern: /^成功删除(\d+)条收藏$/,
    zh: (_, count) => `成功删除${count}条收藏`,
    en: (_, count) => `Deleted ${count} favorites`,
  },
]

function escapeRegExp(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function isLatinPhrase(value) {
  return /^[A-Za-z0-9][A-Za-z0-9\s\-/:&,.()'"!?%+]*[A-Za-z0-9]$/.test(
    String(value || '').trim()
  )
}

function replaceByEntries(value, locale) {
  const translationKey = resolveTranslationKey(locale)
  const entries =
    translationKey === 'en' ? ZH_REPLACE_ENTRIES : EN_REPLACE_ENTRIES
  let nextValue = value

  for (const pair of entries) {
    const source = translationKey === 'en' ? pair.zh : pair.en
    const target = pair[translationKey]
    if (!source || !target || source === target || !nextValue.includes(source))
      continue

    if (translationKey === 'zh' && isLatinPhrase(source)) {
      const pattern = new RegExp(`\\b${escapeRegExp(source)}\\b`, 'g')
      nextValue = nextValue.replace(pattern, target)
      continue
    }

    nextValue = nextValue.split(source).join(target)
  }

  return nextValue
}

export function translateUiText(value, locale = getStoredUiLocale()) {
  if (typeof value !== 'string') return value

  const trimmed = value.trim()
  if (!trimmed) return value

  const normalizedLocale = resolveUiLocale(locale)
  const translationKey = resolveTranslationKey(normalizedLocale)
  const lookup = translationKey === 'en' ? ZH_EXACT_LOOKUP : EN_EXACT_LOOKUP

  const exact = lookup.get(trimmed)
  if (exact) {
    const translated = exact[translationKey] ?? exact.zh ?? exact.en ?? trimmed
    return value.replace(trimmed, translated)
  }

  for (const rule of REGEX_TRANSLATIONS) {
    const match = trimmed.match(rule.pattern)
    if (!match) continue

    const formatter = rule[translationKey]
    if (typeof formatter === 'function') {
      return value.replace(trimmed, formatter(...match))
    }
  }

  return replaceByEntries(value, normalizedLocale)
}
