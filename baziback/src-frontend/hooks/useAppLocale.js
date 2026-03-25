import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

export default function useAppLocale() {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n?.resolvedLanguage || i18n?.language)

  return {
    i18n,
    locale,
  }
}
