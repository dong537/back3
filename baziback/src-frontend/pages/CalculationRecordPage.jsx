import { useEffect, useMemo, useState } from 'react'
import {
  Download,
  History,
  RefreshCw,
  Search,
  Share2,
  Star,
  Trash2,
  X,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Card, { CardContent } from '../components/Card'
import Button from '../components/Button'
import { toast } from '../components/Toast'
import { calculationRecordApi, unwrapApiData } from '../api'
import { useAuth } from '../context/AuthContext'
import { favoritesStorage } from '../utils/storage'
import { logger } from '../utils/logger'
import {
  formatLocaleDateTime,
  resolvePageLocale,
  safeText,
} from '../utils/displayText'

const PAGE_SIZE = 20

const CALCULATION_COPY = {
  'zh-CN': {
    title: '我的测算记录',
    badge: '记录管理',
    subtitle: '查看、搜索、收藏与删除你的历史测算记录',
    loginTitle: '历史记录',
    loginDesc: '登录后可查看、管理和删除你的测算记录。',
    loginAction: '前往登录',
    searchPlaceholder: '搜索标题、问题或摘要...',
    refresh: '刷新',
    loading: '加载中...',
    noMatch: '没有匹配的历史记录',
    empty: '暂无历史记录',
    loadMore: '加载更多',
    downloadSuccess: '下载成功',
    downloadFailed: '下载失败',
    favoriteAdded: '已收藏',
    favoriteRemoved: '已取消收藏',
    favoriteFailed: '收藏操作失败',
    shareSuccess: '记录摘要已复制',
    shareFailed: '分享失败',
    deleteSuccess: '删除成功',
    deleteFailed: '删除失败',
    deleteConfirm: (title) => `确定删除“${title}”吗？`,
    unknownRecord: '这条记录',
    question: '问题',
    type: '类型',
    createdAt: '创建时间',
    actions: {
      favorite: '收藏',
      unfavorite: '取消收藏',
      share: '复制摘要',
      download: '下载 JSON',
      recalculate: '重新测算',
      delete: '删除',
    },
    types: {
      all: '全部',
      bazi: '八字',
      yijing: '易经',
      tarot: '塔罗',
      compatibility: '合盘',
      zodiac: '星座',
    },
  },
  'en-US': {
    title: 'My Reading Records',
    badge: 'Record Manager',
    subtitle: 'View, search, favorite, and delete your past reading records.',
    loginTitle: 'History Records',
    loginDesc:
      'Sign in to view, manage, and remove your saved reading records.',
    loginAction: 'Go to sign in',
    searchPlaceholder: 'Search titles, questions, or summaries...',
    refresh: 'Refresh',
    loading: 'Loading...',
    noMatch: 'No matching records found',
    empty: 'No records yet',
    loadMore: 'Load more',
    downloadSuccess: 'Download complete',
    downloadFailed: 'Download failed',
    favoriteAdded: 'Added to favorites',
    favoriteRemoved: 'Removed from favorites',
    favoriteFailed: 'Favorite action failed',
    shareSuccess: 'Record summary copied',
    shareFailed: 'Share failed',
    deleteSuccess: 'Record deleted',
    deleteFailed: 'Delete failed',
    deleteConfirm: (title) => `Delete "${title}"?`,
    unknownRecord: 'this record',
    question: 'Question',
    type: 'Type',
    createdAt: 'Created',
    actions: {
      favorite: 'Favorite',
      unfavorite: 'Unfavorite',
      share: 'Copy summary',
      download: 'Download JSON',
      recalculate: 'Run again',
      delete: 'Delete',
    },
    types: {
      all: 'All',
      bazi: 'Bazi',
      yijing: 'Yijing',
      tarot: 'Tarot',
      compatibility: 'Compatibility',
      zodiac: 'Zodiac',
    },
  },
}

const TYPE_ROUTE_MAP = {
  bazi: '/bazi',
  tarot: '/tarot',
  yijing: '/yijing',
  zodiac: '/zodiac',
  compatibility: '/compatibility',
}

export default function CalculationRecordPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = CALCULATION_COPY[locale]
  const [records, setRecords] = useState([])
  const [selectedType, setSelectedType] = useState('all')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [hasMore, setHasMore] = useState(false)

  const recordTypes = useMemo(
    () => [
      { value: 'all', label: copy.types.all },
      { value: 'bazi', label: copy.types.bazi },
      { value: 'yijing', label: copy.types.yijing },
      { value: 'tarot', label: copy.types.tarot },
      { value: 'compatibility', label: copy.types.compatibility },
    ],
    [copy.types]
  )

  useEffect(() => {
    if (!isLoggedIn) {
      setRecords([])
      setHasMore(false)
      return
    }
    loadRecords(1, false)
  }, [isLoggedIn, selectedType])

  const filteredRecords = useMemo(() => {
    const keyword = searchKeyword.trim().toLowerCase()
    if (!keyword) return records

    return records.filter((record) => {
      const title = String(record.recordTitle || '').toLowerCase()
      const question = String(record.question || '').toLowerCase()
      const summary = String(record.summary || '').toLowerCase()
      return (
        title.includes(keyword) ||
        question.includes(keyword) ||
        summary.includes(keyword)
      )
    })
  }, [records, searchKeyword])

  const normalizeRecordType = (type) => {
    const text = safeText(type)
    return copy.types[text] || text || copy.types.all
  }

  const loadRecords = async (targetPage = 1, append = false) => {
    if (!isLoggedIn) return

    const setBusy = append ? setLoadingMore : setLoading
    setBusy(true)

    try {
      const response = await calculationRecordApi.getAll(
        selectedType === 'all' ? null : selectedType,
        targetPage,
        PAGE_SIZE
      )
      const data = unwrapApiData(response)
      const serverRecords = Array.isArray(data)
        ? data
        : Array.isArray(data?.records)
          ? data.records
          : Array.isArray(data?.list)
            ? data.list
            : []

      const favorites = await favoritesStorage.getAll()
      const favoriteKeys = new Set(
        favorites.map((item) => {
          const favoriteType = item.favoriteType || item.type
          return `${favoriteType}:${item.dataId}`
        })
      )

      const normalizedRecords = serverRecords.map((record) => ({
        ...record,
        isFavorite: favoriteKeys.has(`${record.recordType}:${record.id}`),
      }))

      setRecords((prev) =>
        append ? [...prev, ...normalizedRecords] : normalizedRecords
      )
      setPage(targetPage)
      setHasMore(serverRecords.length === PAGE_SIZE)
    } catch (error) {
      logger.error('Load calculation records failed', error)
      if (!append) setRecords([])
      setHasMore(false)
    } finally {
      setBusy(false)
    }
  }

  const handleRefresh = () => {
    loadRecords(1, false)
  }

  const handleLoadMore = () => {
    if (!loadingMore && hasMore) {
      loadRecords(page + 1, true)
    }
  }

  const handleDownload = (record) => {
    try {
      const rawData = record.data ?? record.resultData ?? record.inputData
      const payload =
        typeof rawData === 'string'
          ? (() => {
              try {
                return JSON.parse(rawData)
              } catch {
                return rawData
              }
            })()
          : rawData

      const blob = new Blob(
        [
          typeof payload === 'string'
            ? payload
            : JSON.stringify(payload, null, 2),
        ],
        { type: 'application/json' }
      )
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${record.recordTitle || 'record'}_${record.id}.json`
      link.click()
      URL.revokeObjectURL(url)
      toast.success(copy.downloadSuccess)
    } catch (error) {
      logger.error('Download record failed', error)
      toast.error(copy.downloadFailed)
    }
  }

  const handleReCalculate = (record) => {
    navigate(TYPE_ROUTE_MAP[record.recordType] || '/')
  }

  const handleToggleFavorite = async (record) => {
    try {
      const favorited = await favoritesStorage.toggle({
        type: record.recordType,
        dataId: String(record.id),
        title: record.recordTitle,
        summary: record.summary || record.question,
        data: record,
      })

      setRecords((prev) =>
        prev.map((item) =>
          item.id === record.id ? { ...item, isFavorite: favorited } : item
        )
      )
      toast.success(favorited ? copy.favoriteAdded : copy.favoriteRemoved)
    } catch (error) {
      logger.error('Toggle favorite failed', error)
      toast.error(copy.favoriteFailed)
    }
  }

  const handleShare = async (record) => {
    try {
      const shareText = `${record.recordTitle}\n${
        record.summary || record.question || ''
      }`.trim()
      await navigator.clipboard.writeText(shareText)
      toast.success(copy.shareSuccess)
    } catch (error) {
      logger.error('Share record failed', error)
      toast.error(copy.shareFailed)
    }
  }

  const handleDelete = async (record) => {
    const title = safeText(record.recordTitle, copy.unknownRecord)
    const confirmed = window.confirm(copy.deleteConfirm(title))
    if (!confirmed) return

    try {
      await calculationRecordApi.delete(record.id)
      setRecords((prev) => prev.filter((item) => item.id !== record.id))
      setHasMore(false)
      toast.success(copy.deleteSuccess)
    } catch (error) {
      logger.error('Delete record failed', error)
      toast.error(error?.message || copy.deleteFailed)
    }
  }

  if (!isLoggedIn) {
    return (
      <div className="page-shell" data-theme="default">
        <div className="mx-auto max-w-3xl py-16">
          <Card className="panel">
            <CardContent className="py-16 text-center">
              <History className="mx-auto mb-4 h-16 w-16 text-[#8f7b66]" />
              <h1 className="mb-2 text-2xl font-semibold text-white">
                {copy.loginTitle}
              </h1>
              <p className="mb-6 text-[#bdaa94]">{copy.loginDesc}</p>
              <Button onClick={() => navigate('/login')}>
                <span>{copy.loginAction}</span>
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <History className="text-theme h-4 w-4" />
            <span className="text-theme text-sm">{copy.badge}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">
            {copy.title}
          </h1>
          <p className="page-subtitle">{copy.subtitle}</p>
        </div>
      </div>

      <div className="mx-auto max-w-6xl">
        <Card className="panel mb-6">
          <CardContent className="p-4">
            <div className="flex flex-col gap-4 md:flex-row">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-[#8f7b66]" />
                  <input
                    type="text"
                    placeholder={copy.searchPlaceholder}
                    value={searchKeyword}
                    onChange={(event) => setSearchKeyword(event.target.value)}
                    className="mystic-input py-2 pl-10 pr-10"
                  />
                  {searchKeyword && (
                    <button
                      onClick={() => setSearchKeyword('')}
                      className="absolute right-3 top-1/2 -translate-y-1/2"
                    >
                      <X className="h-4 w-4 text-[#8f7b66]" />
                    </button>
                  )}
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                {recordTypes.map((type) => (
                  <button
                    key={type.value}
                    onClick={() => setSelectedType(type.value)}
                    className={`mystic-tab ${
                      selectedType === type.value
                        ? 'mystic-tab-active'
                        : ''
                    }`}
                  >
                    {type.label}
                  </button>
                ))}

                <Button
                  variant="secondary"
                  onClick={handleRefresh}
                  loading={loading && records.length > 0}
                >
                  <RefreshCw className="h-4 w-4" />
                  <span>{copy.refresh}</span>
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-4">
          {loading && records.length === 0 ? (
            <Card className="panel">
              <CardContent className="py-12 text-center">
                <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-[#d0a85b] border-t-transparent" />
                <p className="mt-4 text-[#bdaa94]">{copy.loading}</p>
              </CardContent>
            </Card>
          ) : filteredRecords.length === 0 ? (
            <Card className="panel">
              <CardContent className="py-12 text-center">
                <History className="mx-auto mb-4 h-16 w-16 text-[#8f7b66]" />
                <p className="text-[#bdaa94]">
                  {searchKeyword ? copy.noMatch : copy.empty}
                </p>
              </CardContent>
            </Card>
          ) : (
            filteredRecords.map((record) => (
              <Card key={record.id} className="panel">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0 flex-1">
                      <div className="mb-2 flex items-center space-x-2">
                        <h3 className="truncate text-lg font-medium text-white">
                          {record.recordTitle}
                        </h3>
                        {record.isFavorite && (
                          <Star className="h-4 w-4 shrink-0 fill-current text-yellow-400" />
                        )}
                      </div>

                      {record.question && (
                        <p className="mb-2 break-words text-[#bdaa94]">
                          {copy.question}: {record.question}
                        </p>
                      )}

                      {record.summary && (
                        <p className="mb-3 break-words text-sm text-[#8f7b66]">
                          {record.summary}
                        </p>
                      )}

                      <div className="flex flex-wrap items-center gap-4 text-xs text-[#8f7b66]">
                        <span>
                          {copy.type}: {normalizeRecordType(record.recordType)}
                        </span>
                        <span>
                          {copy.createdAt}:{' '}
                          {formatLocaleDateTime(
                            record.createTime || record.timestamp,
                            locale
                          )}
                        </span>
                      </div>
                    </div>

                    <div className="flex flex-wrap justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleFavorite(record)}
                        title={
                          record.isFavorite
                            ? copy.actions.unfavorite
                            : copy.actions.favorite
                        }
                      >
                        <Star
                          className={`h-4 w-4 ${
                            record.isFavorite
                              ? 'fill-current text-yellow-400'
                              : ''
                          }`}
                        />
                      </Button>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleShare(record)}
                        title={copy.actions.share}
                      >
                        <Share2 className="h-4 w-4" />
                      </Button>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDownload(record)}
                        title={copy.actions.download}
                      >
                        <Download className="h-4 w-4" />
                      </Button>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleReCalculate(record)}
                        title={copy.actions.recalculate}
                      >
                        <RefreshCw className="h-4 w-4" />
                      </Button>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(record)}
                        title={copy.actions.delete}
                      >
                        <Trash2 className="h-4 w-4 text-red-400" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>

        {hasMore && !searchKeyword && (
          <div className="mt-6 text-center">
            <Button
              variant="secondary"
              onClick={handleLoadMore}
              loading={loadingMore}
            >
              <span>{loadingMore ? copy.loading : copy.loadMore}</span>
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
