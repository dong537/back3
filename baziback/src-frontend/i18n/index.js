import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

import zhCN from './zh-CN.json'
import enUS from './en-US.json'

const STORAGE_KEY = 'lang'

function getInitialLanguage() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) return saved
  const nav = (navigator.language || 'zh-CN').toLowerCase()
  if (nav.startsWith('en')) return 'en-US'
  return 'zh-CN'
}

const resources = {
  'zh-CN': { translation: zhCN },
  'en-US': { translation: enUS },
}

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: getInitialLanguage(),
    fallbackLng: 'zh-CN',
    interpolation: { escapeValue: false },
  })

export function setLanguage(lang) {
  i18n.changeLanguage(lang)
  localStorage.setItem(STORAGE_KEY, lang)
}

export default i18n

