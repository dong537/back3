import { useCallback, useState } from 'react'
import { setLanguage } from '../i18n'
import useAppLocale from './useAppLocale'

export default function useLanguageToggle() {
  const { locale } = useAppLocale()
  const [isPending, setIsPending] = useState(false)

  const nextLocale = locale === 'en-US' ? 'zh-CN' : 'en-US'
  const shortLabel = locale === 'en-US' ? '中文' : 'English'
  const label = locale === 'en-US' ? '切换到中文' : 'Switch to English'

  const toggleLanguage = useCallback(async () => {
    if (isPending) return nextLocale

    setIsPending(true)
    try {
      return await setLanguage(nextLocale)
    } finally {
      setIsPending(false)
    }
  }, [isPending, nextLocale])

  return {
    locale,
    nextLocale,
    label,
    shortLabel,
    isPending,
    toggleLanguage,
  }
}
