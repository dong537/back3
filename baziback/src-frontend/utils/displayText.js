import { resolveUiLocale } from './runtimeLocale'

const INVALID_TEXT_VALUES = new Set(['undefined', 'null', 'nan'])

export function resolvePageLocale(language) {
  return resolveUiLocale(language)
}

export function safeText(value, fallback = '') {
  if (value === undefined || value === null) return fallback
  const text = String(value).trim()
  if (!text) return fallback
  if (INVALID_TEXT_VALUES.has(text.toLowerCase())) return fallback
  return text
}

export function safeArray(value) {
  return Array.isArray(value) ? value : []
}

export function safeNumber(value, fallback = 0) {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

export function formatLocaleDateTime(value, locale, fallback = '') {
  const text = safeText(value)
  if (!text) return fallback
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return fallback
  return date.toLocaleString(locale)
}

export function formatLocaleDate(value, locale, fallback = '') {
  const text = safeText(value)
  if (!text) return fallback
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return fallback
  return date.toLocaleDateString(locale)
}
