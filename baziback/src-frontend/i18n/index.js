import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

import zhCN from './zh-CN.json'
import enUS from './en-US.json'

const STORAGE_KEY = 'lang'
const APP_TITLES = {
  'zh-CN': '天机明理 - 易经 · 星座 · 八字',
  'en-US': 'Mystic Insight - Yijing · Zodiac · Bazi',
}
const APP_SHORT_TITLES = {
  'zh-CN': '天机明理',
  'en-US': 'Mystic Insight',
}

function normalizeLocaleTag(language) {
  return String(language || '')
    .trim()
    .replace('_', '-')
    .toLowerCase()
}

export function resolveAppLanguage(language) {
  const normalized = normalizeLocaleTag(language)
  return normalized.startsWith('en') ? 'en-US' : 'zh-CN'
}

export function getStoredLanguage() {
  try {
    return resolveAppLanguage(localStorage.getItem(STORAGE_KEY))
  } catch {
    return 'zh-CN'
  }
}

function syncDocumentLanguage(language) {
  if (typeof document !== 'undefined') {
    document.documentElement.lang = language
    document.documentElement.setAttribute('translate', 'no')
    document.documentElement.classList.add('notranslate')
    document.body?.setAttribute('translate', 'no')
    document.body?.classList.add('notranslate')
    document.title = APP_TITLES[language] || APP_TITLES['zh-CN']

    const appTitleMeta = document.querySelector(
      'meta[name="apple-mobile-web-app-title"]'
    )
    if (appTitleMeta) {
      appTitleMeta.setAttribute(
        'content',
        APP_SHORT_TITLES[language] || APP_SHORT_TITLES['zh-CN']
      )
    }
  }
  if (typeof window !== 'undefined') {
    window.dispatchEvent(
      new CustomEvent('app-language-change', { detail: { language } })
    )
  }
}

function getInitialLanguage() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) return resolveAppLanguage(saved)
  const nav = (navigator.language || 'zh-CN').toLowerCase()
  if (nav.startsWith('en')) return 'en-US'
  return 'zh-CN'
}

const resources = {
  'zh-CN': { translation: zhCN },
  'en-US': { translation: enUS },
}

i18n.use(initReactI18next).init({
  resources,
  lng: getInitialLanguage(),
  fallbackLng: 'zh-CN',
  supportedLngs: ['zh-CN', 'en-US', 'zh', 'en'],
  nonExplicitSupportedLngs: true,
  load: 'currentOnly',
  interpolation: { escapeValue: false },
})

i18n.on('languageChanged', (language) => {
  const nextLanguage = resolveAppLanguage(language)
  localStorage.setItem(STORAGE_KEY, nextLanguage)
  syncDocumentLanguage(nextLanguage)
})

syncDocumentLanguage(resolveAppLanguage(i18n.resolvedLanguage || i18n.language))

if (typeof window !== 'undefined') {
  window.i18n = i18n
}

export async function setLanguage(lang) {
  const nextLanguage = resolveAppLanguage(lang)
  if (
    resolveAppLanguage(i18n.resolvedLanguage || i18n.language) === nextLanguage
  ) {
    return nextLanguage
  }
  await i18n.changeLanguage(nextLanguage)
  localStorage.setItem(STORAGE_KEY, nextLanguage)
  syncDocumentLanguage(nextLanguage)
  return nextLanguage
}

export default i18n
