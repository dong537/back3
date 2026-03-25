import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  ArrowLeft,
  Clock,
  Trash2,
  Star,
  Search,
  CheckSquare,
  Square,
  Download,
  AlertTriangle,
} from 'lucide-react'
import { favoritesStorage } from '../utils/storage'
import { toast } from '../components/Toast'
import { favoriteApi, tarotApi, unwrapApiData } from '../api'
import { resolvePageLocale, safeArray, safeText } from '../utils/displayText'
import { buildTarotCardsText, getTarotCardName, getTarotSpreadTitle } from '../utils/tarotText'

const FAVORITES_COPY = {
  'zh-CN': {
    back: '返回',
    title: '\u6211\u7684\u6536\u85cf',
    manage: '\u7ba1\u7406',
    cancel: '\u53d6\u6d88',
    searchLabel: '\u641c\u7d22\u6536\u85cf',
    searchPlaceholder: '\u641c\u7d22\uff1a\u6807\u9898/\u6458\u8981/\u95ee\u9898',
    all: '\u5168\u90e8',
    totalPrefix: '\u5171',
    totalSuffix: '\u6761',
    export: '\u5bfc\u51fa',
    clearAll: '\u6e05\u7a7a',
    loading: '\u52a0\u8f7d\u4e2d...',
    emptyTitle: '\u6682\u65e0\u6536\u85cf',
    emptyDescription: '\u53bb\u62bd\u724c/\u5360\u535c\u540e\uff0c\u70b9\u661f\u661f\u5373\u53ef\u6536\u85cf',
    unnamed: '\u672a\u547d\u540d\u6536\u85cf',
    unknownType: '\u672a\u77e5',
    removeTitle: '\u53d6\u6d88\u6536\u85cf',
    removed: '\u5df2\u53d6\u6d88\u6536\u85cf',
    removeFailed: '\u53d6\u6d88\u6536\u85cf\u5931\u8d25',
    chooseDelete: '\u8bf7\u9009\u62e9\u8981\u5220\u9664\u7684\u6536\u85cf',
    batchDeleteConfirmPrefix: '\u786e\u5b9a\u8981\u5220\u9664\u9009\u4e2d\u7684 ',
    batchDeleteConfirmSuffix: ' \u6761\u6536\u85cf\u5417\uff1f',
    batchDeleteSuccessPrefix: '\u6210\u529f\u5220\u9664 ',
    batchDeleteSuccessSuffix: ' \u6761\u6536\u85cf',
    batchDeleteFailed: '\u6279\u91cf\u5220\u9664\u5931\u8d25',
    clearConfirm: '\u786e\u5b9a\u8981\u6e05\u7a7a\u6240\u6709\u6536\u85cf\u5417\uff1f\u6b64\u64cd\u4f5c\u4e0d\u53ef\u6062\u590d\u3002',
    clearSuccess: '\u5df2\u6e05\u7a7a\u6240\u6709\u6536\u85cf',
    clearFailed: '\u6e05\u7a7a\u5931\u8d25',
    exportStarted: '\u5df2\u5f00\u59cb\u5bfc\u51fa',
    selectAll: '\u5168\u9009',
    deleteSelected: '\u5220\u9664\u9009\u4e2d',
    types: {
      all: '\u5168\u90e8',
      tarot: '\u5854\u7f57',
      yijing: '\u6613\u7ecf',
      bazi: '\u516b\u5b57',
      zodiac: '\u661f\u5ea7',
    },
  },
  'en-US': {
    back: 'Back',
    title: 'My Favorites',
    manage: 'Manage',
    cancel: 'Cancel',
    searchLabel: 'Search favorites',
    searchPlaceholder: 'Search: title / summary / question',
    all: 'All',
    totalPrefix: 'Total ',
    totalSuffix: '',
    export: 'Export',
    clearAll: 'Clear',
    loading: 'Loading...',
    emptyTitle: 'No favorites yet',
    emptyDescription: 'Draw cards or finish a divination, then tap the star to save it.',
    unnamed: 'Untitled favorite',
    unknownType: 'Unknown',
    removeTitle: 'Remove favorite',
    removed: 'Removed from favorites',
    removeFailed: 'Failed to remove favorite',
    chooseDelete: 'Please choose favorites to delete',
    batchDeleteConfirmPrefix: 'Delete ',
    batchDeleteConfirmSuffix: ' selected favorites?',
    batchDeleteSuccessPrefix: 'Deleted ',
    batchDeleteSuccessSuffix: ' favorites',
    batchDeleteFailed: 'Batch delete failed',
    clearConfirm: 'Clear all favorites? This action cannot be undone.',
    clearSuccess: 'All favorites cleared',
    clearFailed: 'Failed to clear favorites',
    exportStarted: 'Export started',
    selectAll: 'Select all',
    deleteSelected: 'Delete selected',
    types: {
      all: 'All',
      tarot: 'Tarot',
      yijing: 'Yijing',
      bazi: 'Bazi',
      zodiac: 'Zodiac',
    },
  },
}

const FILTER_TYPES = ['all', 'tarot', 'yijing', 'bazi']

function resolveLocale(language) {
  return resolvePageLocale(language)
}

function normalizeText(value) {
  return (value == null ? '' : String(value)).toLowerCase()
}

function parseFavoriteData(item) {
  if (!item) return null
  if (typeof item.data === 'string') {
    try {
      return JSON.parse(item.data)
    } catch {
      return item.data
    }
  }
  return item.data
}

function buildNavTarget(item, locale, tarotCardLookup) {
  if (!item) return null

  const type = item.favoriteType || item.type
  const data = parseFavoriteData(item)

  if (type === 'tarot') {
    const cards = data?.drawnCards || data?.cards || []
    const first = cards?.[0]
    const cardId = first?.cardId || first?.id
    const cardName = getTarotCardName(first, locale, '', tarotCardLookup)
    const param = cardId != null ? String(cardId) : cardName ? encodeURIComponent(cardName) : ''
    if (!param) return null
    return { pathname: `/tarot/card/${param}` }
  }

  if (type === 'yijing') {
    return {
      pathname: '/yijing',
      state: {
        fromFavorite: true,
        question: item.question,
        result: data,
      },
    }
  }

  if (type === 'bazi') {
    return {
      pathname: '/bazi',
      state: {
        fromFavorite: true,
        result: data,
      },
    }
  }

  return null
}

function formatDate(value, locale) {
  try {
    const date = new Date(value)
    return date.toLocaleString(locale, {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return ''
  }
}

function getTypeLabel(type, copy) {
  return copy.types[type] || type || copy.unknownType
}

function getTypeBadgeClass(type) {
  if (type === 'tarot') return 'bg-[#7a3218]/20 text-[#e19a84] border-[#a34224]/30'
  if (type === 'yijing') return 'bg-[#6a4a1e]/20 text-[#f0d9a5] border-[#d0a85b]/30'
  if (type === 'bazi') return 'bg-[#8b4a1e]/20 text-[#efbb84] border-[#c97842]/30'
  if (type === 'zodiac') return 'bg-[#5e4a36]/20 text-[#dcb86f] border-[#b88a3d]/30'
  return 'bg-white/[0.05] text-[#e4d6c8] border-white/10'
}

function buildTarotCardLookup(cards) {
  return safeArray(cards).reduce((acc, card) => {
    const cardId = safeText(card?.cardId ?? card?.id)
    if (cardId) {
      acc[cardId] = card
    }
    return acc
  }, {})
}

function getFavoriteDisplayContent(item, locale, copy, tarotCardLookup) {
  const type = item.favoriteType || item.type
  const data = parseFavoriteData(item)

  if (type !== 'tarot') {
    return {
      title: item.title || item.question || copy.unnamed,
      summary: item.summary || '',
    }
  }

  const cards = safeArray(data?.drawnCards || data?.cards)
  const localizedTitle = getTarotSpreadTitle(data?.spreadType, locale, item.title || item.question || copy.unnamed)
  const localizedSummary = buildTarotCardsText(cards, locale, {
    cardLookup: tarotCardLookup,
    fallback: item.summary || '',
    includeOrientation: true,
    uprightLabel: locale === 'en-US' ? 'Upright' : '正位',
    reversedLabel: locale === 'en-US' ? 'Reversed' : '逆位',
  })

  return {
    title: localizedTitle,
    summary: localizedSummary,
  }
}

export default function FavoritesPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolveLocale(i18n.language)
  const copy = FAVORITES_COPY[locale]
  const [items, setItems] = useState([])
  const [typeFilter, setTypeFilter] = useState('all')
  const [query, setQuery] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSelectionMode, setIsSelectionMode] = useState(false)
  const [selectedIds, setSelectedIds] = useState(new Set())
  const [tarotCardLookup, setTarotCardLookup] = useState({})

  useEffect(() => {
    const loadItems = async () => {
      setIsLoading(true)
      const favorites = await favoritesStorage.getAll()
      setItems(favorites)
      setIsLoading(false)
    }
    loadItems()
  }, [])

  useEffect(() => {
    const loadTarotCards = async () => {
      try {
        const response = await tarotApi.getAllCards()
        const cards = unwrapApiData(response)
        setTarotCardLookup(buildTarotCardLookup(cards))
      } catch {
        setTarotCardLookup({})
      }
    }

    loadTarotCards()
  }, [])

  const filtered = useMemo(() => {
    const normalizedQuery = normalizeText(query).trim()

    return items
      .filter((item) => {
        if (!item) return false
        if (typeFilter === 'all') return true
        return (item.favoriteType || item.type) === typeFilter
      })
      .filter((item) => {
        if (!normalizedQuery) return true
        const display = getFavoriteDisplayContent(
          item,
          locale,
          copy,
          tarotCardLookup
        )
        const haystack = [
          item?.title,
          item?.summary,
          item?.question,
          item?.favoriteType,
          item?.type,
          display?.title,
          display?.summary,
        ]
          .map(normalizeText)
          .join(' ')

        return haystack.includes(normalizedQuery)
      })
      .sort((left, right) => {
        const leftTime = new Date(left.createTime || left.timestamp || 0).getTime()
        const rightTime = new Date(right.createTime || right.timestamp || 0).getTime()
        return rightTime - leftTime
      })
  }, [copy, items, locale, query, tarotCardLookup, typeFilter])

  const handleRemove = async (id, event) => {
    event?.preventDefault?.()
    event?.stopPropagation?.()
    const success = await favoritesStorage.remove(id)
    if (success) {
      setItems(await favoritesStorage.getAll(true))
      toast.success(copy.removed)
    } else {
      toast.error(copy.removeFailed)
    }
  }

  const handleSelectionChange = (id) => {
    setSelectedIds((current) => {
      const next = new Set(current)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  const handleBatchDelete = async () => {
    if (selectedIds.size === 0) {
      toast.info(copy.chooseDelete)
      return
    }

    const confirmed = window.confirm(
      `${copy.batchDeleteConfirmPrefix}${selectedIds.size}${copy.batchDeleteConfirmSuffix}`
    )

    if (!confirmed) return

    try {
      const ids = Array.from(selectedIds)
      await favoriteApi.removeBatch(ids)
      setItems(await favoritesStorage.getAll(true))
      setSelectedIds(new Set())
      setIsSelectionMode(false)
      toast.success(`${copy.batchDeleteSuccessPrefix}${ids.length}${copy.batchDeleteSuccessSuffix}`)
    } catch {
      toast.error(copy.batchDeleteFailed)
    }
  }

  const handleClearAll = async () => {
    if (!window.confirm(copy.clearConfirm)) return

    try {
      await favoriteApi.clearAll()
      setItems([])
      setSelectedIds(new Set())
      setIsSelectionMode(false)
      toast.success(copy.clearSuccess)
    } catch {
      toast.error(copy.clearFailed)
    }
  }

  const handleExport = () => {
    const dataString = JSON.stringify(filtered, null, 2)
    const dataUri = `data:application/json;charset=utf-8,${encodeURIComponent(dataString)}`
    const exportFileName = locale === 'en-US' ? 'favorites.json' : 'shoucang.json'
    const link = document.createElement('a')
    link.setAttribute('href', dataUri)
    link.setAttribute('download', exportFileName)
    link.click()
    toast.success(copy.exportStarted)
  }

  const toggleSelectAll = () => {
    if (selectedIds.size === filtered.length) {
      setSelectedIds(new Set())
      return
    }
    setSelectedIds(new Set(filtered.map((item) => item.id)))
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-all hover:bg-white/10"
            title={copy.back}
            aria-label={copy.back}
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <div className="flex items-center space-x-2">
            <Star size={18} className="text-[#d0a85b]" />
            <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          </div>
          <button
            onClick={() => setIsSelectionMode(!isSelectionMode)}
            className="rounded-lg p-2 text-sm text-[#f4ece1] hover:bg-white/10"
            title={isSelectionMode ? copy.cancel : copy.manage}
            aria-label={isSelectionMode ? copy.cancel : copy.manage}
          >
            {isSelectionMode ? copy.cancel : copy.manage}
          </button>
        </div>
      </div>

      <div className="app-page-shell-narrow pb-20 pt-6">
        <div className="panel mb-4 p-4">
          <div className="mb-3 flex items-center gap-2">
            <div className="flex flex-1 items-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2">
              <Search size={16} className="text-[#8f7b66]" />
              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={copy.searchPlaceholder}
                aria-label={copy.searchLabel}
                className="w-full bg-transparent text-sm text-[#f4ece1] outline-none placeholder-[#8f7b66]"
              />
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {FILTER_TYPES.map((type) => (
              <button
                key={type}
                type="button"
                onClick={() => setTypeFilter(type)}
                title={type === 'all' ? copy.all : getTypeLabel(type, copy)}
                aria-label={type === 'all' ? copy.all : getTypeLabel(type, copy)}
                className={`rounded-full border px-3 py-1.5 text-xs font-medium transition-all ${
                  typeFilter === type
                    ? 'border-[#d0a85b]/30 bg-[#7a3218]/18 text-[#fff7eb]'
                    : 'border-white/10 bg-white/[0.04] text-[#bdaa94] hover:bg-white/[0.08]'
                }`}
              >
                {type === 'all' ? copy.all : getTypeLabel(type, copy)}
              </button>
            ))}
          </div>

          <div className="mt-3 flex items-center justify-between border-t border-white/10 pt-3 text-xs text-[#8f7b66]">
            <span>
              {copy.totalPrefix}
              {filtered.length}
              {copy.totalSuffix}
            </span>
            <div className="flex gap-3">
              <button
                onClick={handleExport}
                className="flex items-center gap-1 hover:text-[#f4ece1]"
                title={copy.export}
                aria-label={copy.export}
              >
                <Download size={14} />
                {copy.export}
              </button>
              <button
                onClick={handleClearAll}
                className="flex items-center gap-1 text-[#e19a84] hover:text-[#f0b2a2]"
                title={copy.clearAll}
                aria-label={copy.clearAll}
              >
                <AlertTriangle size={14} />
                {copy.clearAll}
              </button>
            </div>
          </div>
        </div>

        {isLoading ? (
          <div className="py-10 text-center text-[#8f7b66]">{copy.loading}</div>
        ) : filtered.length === 0 ? (
          <div className="panel p-8 text-center">
            <div className="mb-3 text-5xl">{'\u2B50'}</div>
            <div className="font-semibold text-[#f4ece1]">{copy.emptyTitle}</div>
            <div className="mt-2 text-sm text-[#8f7b66]">{copy.emptyDescription}</div>
          </div>
        ) : (
          <div className="space-y-3">
            {filtered.map((item) => {
              const target = buildNavTarget(item, locale, tarotCardLookup)
              const Wrapper = !isSelectionMode && target ? Link : 'div'
              const wrapperProps = !isSelectionMode && target ? { to: target.pathname, state: target.state } : {}
              const type = item.favoriteType || item.type
              const display = getFavoriteDisplayContent(item, locale, copy, tarotCardLookup)

              return (
                <Wrapper
                  key={item.id}
                  {...wrapperProps}
                  className={`block rounded-2xl border bg-white/[0.04] p-4 backdrop-blur-xl transition-all ${
                    isSelectionMode
                      ? 'cursor-pointer border-[#d0a85b]/24'
                      : 'border-white/10 hover:bg-white/[0.08]'
                  }`}
                  onClick={() => isSelectionMode && handleSelectionChange(item.id)}
                >
                  <div className="flex items-center justify-between gap-3">
                    {isSelectionMode && (
                      <div className="text-[#dcb86f]">
                        {selectedIds.has(item.id) ? <CheckSquare size={20} /> : <Square size={20} />}
                      </div>
                    )}
                    <div className="min-w-0 flex-1">
                      <div className="mb-2 flex items-center gap-2">
                        <span className={`rounded-full border px-2 py-1 text-[10px] ${getTypeBadgeClass(type)}`}>
                          {getTypeLabel(type, copy)}
                        </span>
                        <span className="flex items-center gap-1 text-xs text-[#8f7b66]">
                          <Clock size={12} />
                          {formatDate(item.createTime || item.timestamp, locale)}
                        </span>
                      </div>
                      <div className="line-clamp-1 font-semibold text-white">
                        {display.title}
                      </div>
                      {display.summary && (
                        <div className="mt-1 line-clamp-2 text-sm text-[#bdaa94]">{display.summary}</div>
                      )}
                    </div>
                    {!isSelectionMode && (
                      <button
                        type="button"
                        onClick={(event) => handleRemove(item.id, event)}
                        className="rounded-xl border border-white/10 bg-white/[0.04] p-2 text-[#bdaa94] transition-all hover:bg-white/[0.08] hover:text-[#e19a84]"
                        title={copy.removeTitle}
                        aria-label={copy.removeTitle}
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </div>
                </Wrapper>
              )
            })}
          </div>
        )}
      </div>

      {isSelectionMode && (
        <div className="fixed bottom-0 left-0 right-0 z-50 flex items-center justify-between border-t border-white/10 bg-[#0f0a09]/90 p-4 backdrop-blur-lg">
          <button
            onClick={toggleSelectAll}
            className="flex items-center gap-2 text-sm text-[#f4ece1]"
            title={copy.selectAll}
            aria-label={copy.selectAll}
          >
            {selectedIds.size === filtered.length && filtered.length > 0 ? <CheckSquare /> : <Square />}
            {copy.selectAll} ({selectedIds.size})
          </button>
          <div className="flex gap-3">
            <button
              onClick={() => setIsSelectionMode(false)}
              className="rounded-lg border border-white/10 bg-white/[0.04] px-4 py-2 text-sm text-[#f4ece1]"
              title={copy.cancel}
              aria-label={copy.cancel}
            >
              {copy.cancel}
            </button>
            <button
              onClick={handleBatchDelete}
              className="flex items-center gap-2 rounded-lg border border-[#a34224]/30 bg-[#7a3218]/18 px-4 py-2 text-sm text-[#f0b2a2]"
              title={copy.deleteSelected}
              aria-label={copy.deleteSelected}
            >
              <Trash2 size={16} />
              {copy.deleteSelected}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
