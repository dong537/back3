import { useEffect, useMemo, useState } from 'react'
import { Clock, Star, Trash2, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { favoritesStorage, historyStorage } from '../utils/storage'
import Card from './Card'
import { resolvePageLocale, safeText } from '../utils/displayText'
import { logger } from '../utils/logger'

const HISTORY_COPY = {
  'zh-CN': {
    title: '历史记录',
    tabs: {
      history: '历史',
      favorites: '收藏',
    },
    clear: '清空',
    emptyHistory: '暂无历史记录',
    emptyFavorites: '暂无收藏',
    unknownTitle: '无标题',
    clearConfirm: '确定要清空所有历史记录吗？',
    loading: '加载中...',
    justNow: '刚刚',
    minutesAgo: (count) => `${count} 分钟前`,
    hoursAgo: (count) => `${count} 小时前`,
    daysAgo: (count) => `${count} 天前`,
    types: {
      yijing: '易经占卜',
      tarot: '塔罗',
      bazi: '八字排盘',
      zodiac: '星座运势',
      compatibility: '合盘分析',
    },
  },
  'en-US': {
    title: 'History',
    tabs: {
      history: 'History',
      favorites: 'Favorites',
    },
    clear: 'Clear',
    emptyHistory: 'No history records yet',
    emptyFavorites: 'No favorites yet',
    unknownTitle: 'Untitled',
    clearConfirm: 'Clear all history records?',
    loading: 'Loading...',
    justNow: 'Just now',
    minutesAgo: (count) => `${count} min ago`,
    hoursAgo: (count) => `${count} hr ago`,
    daysAgo: (count) => `${count} day${count === 1 ? '' : 's'} ago`,
    types: {
      yijing: 'Yijing',
      tarot: 'Tarot',
      bazi: 'Bazi',
      zodiac: 'Zodiac',
      compatibility: 'Compatibility',
    },
  },
}

export default function HistoryModal({ isOpen, onClose, type, onSelect }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = HISTORY_COPY[locale]
  const [items, setItems] = useState([])
  const [activeTab, setActiveTab] = useState('history')
  const [loading, setLoading] = useState(false)

  const typeLabelMap = useMemo(
    () => ({
      ...copy.types,
    }),
    [copy.types]
  )

  useEffect(() => {
    if (isOpen) {
      void loadData(activeTab)
    }
  }, [activeTab, isOpen, type])

  const normalizeFavoriteItem = (item) => {
    const favoriteType = item.favoriteType || item.type
    let parsedData = item.data

    if (typeof parsedData === 'string') {
      try {
        parsedData = JSON.parse(parsedData)
      } catch {
        parsedData = item.data
      }
    }

    return {
      ...item,
      type: favoriteType,
      question:
        item.question ||
        item.title ||
        (typeof parsedData === 'object' ? parsedData?.question : ''),
      summary:
        item.summary ||
        (typeof parsedData === 'object' ? parsedData?.summary : '') ||
        '',
      timestamp: item.timestamp || item.createTime || item.createdAt,
      data: parsedData,
    }
  }

  const loadData = async (tab = activeTab) => {
    setLoading(true)

    try {
      if (tab === 'history') {
        const historyItems = type
          ? historyStorage.getByType(type)
          : historyStorage.getAll()
        setItems(historyItems)
      } else {
        const favorites = await favoritesStorage.getAll()
        const normalizedFavorites = favorites
          .map(normalizeFavoriteItem)
          .filter((item) => !type || item.type === type)
        setItems(normalizedFavorites)
      }
    } catch (error) {
      logger.error('Load history modal data failed', error)
      setItems([])
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (item) => {
    try {
      if (activeTab === 'history') {
        historyStorage.remove(item.id)
      } else {
        await favoritesStorage.remove(item.id)
      }
      void loadData(activeTab)
    } catch (error) {
      logger.error('Delete history modal item failed', error)
    }
  }

  const handleClear = () => {
    if (window.confirm(copy.clearConfirm)) {
      historyStorage.clear()
      void loadData('history')
    }
  }

  const formatDate = (timestamp) => {
    const date = new Date(timestamp)
    if (Number.isNaN(date.getTime())) return ''

    const diff = Date.now() - date.getTime()
    if (diff < 60_000) return copy.justNow
    if (diff < 3_600_000) return copy.minutesAgo(Math.floor(diff / 60_000))
    if (diff < 86_400_000) return copy.hoursAgo(Math.floor(diff / 3_600_000))
    if (diff < 604_800_000) return copy.daysAgo(Math.floor(diff / 86_400_000))

    return date.toLocaleDateString(locale, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const getTypeLabel = (value) => {
    const text = safeText(value)
    return typeLabelMap[text] || text
  }

  const openItem = (item) => {
    if (!onSelect) return

    onSelect({
      ...item,
      type: item.type || item.favoriteType,
    })
    onClose()
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel flex max-h-[80vh] w-full max-w-2xl flex-col overflow-hidden border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.96),rgba(14,11,10,0.88))]">
        <div className="flex items-center justify-between border-b border-white/10 p-6">
          <div className="flex items-center space-x-4">
            <h2 className="text-xl font-bold text-[#f4ece1]">{copy.title}</h2>
            <div className="flex space-x-2">
              <TabButton
                active={activeTab === 'history'}
                icon={Clock}
                label={copy.tabs.history}
                onClick={() => setActiveTab('history')}
              />
              <TabButton
                active={activeTab === 'favorites'}
                icon={Star}
                label={copy.tabs.favorites}
                onClick={() => setActiveTab('favorites')}
              />
            </div>
          </div>

          <div className="flex items-center space-x-2">
            {activeTab === 'history' && items.length > 0 && (
              <button
                onClick={handleClear}
                className="rounded-2xl px-3 py-1.5 text-sm text-[#e19a84] transition hover:bg-[#7a3218]/12"
              >
                {copy.clear}
              </button>
            )}
            <button
              onClick={onClose}
              className="rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="py-12 text-center text-[#8f7b66]">
              {copy.loading}
            </div>
          ) : items.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-[#8f7b66]">
              {activeTab === 'history' ? (
                <>
                  <Clock size={48} className="mb-4 opacity-50" />
                  <p>{copy.emptyHistory}</p>
                </>
              ) : (
                <>
                  <Star size={48} className="mb-4 opacity-50" />
                  <p>{copy.emptyFavorites}</p>
                </>
              )}
            </div>
          ) : (
            <div className="space-y-3">
              {items.map((item) => (
                <div
                  key={`${activeTab}-${item.id}`}
                  className="group cursor-pointer rounded-[22px] border border-white/10 bg-[#140f0f]/72 p-4 transition hover:border-[#d0a85b]/18 hover:bg-[#171110]/84"
                  onClick={() => openItem(item)}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1">
                      <div className="mb-2 flex items-center space-x-2">
                        <span className="rounded-full border border-[#d0a85b]/16 bg-[#6a4a1e]/16 px-2 py-0.5 text-xs text-[#dcb86f]">
                          {getTypeLabel(item.type)}
                        </span>
                        <span className="text-xs text-[#8f7b66]">
                          {formatDate(item.timestamp)}
                        </span>
                      </div>

                      <p className="mb-1 text-sm font-medium text-[#f4ece1]">
                        {item.question ||
                          item.title ||
                          item.recordTitle ||
                          copy.unknownTitle}
                      </p>

                      {item.summary && (
                        <p className="line-clamp-2 text-xs leading-6 text-[#8f7b66]">
                          {item.summary}
                        </p>
                      )}
                    </div>

                    <button
                      onClick={(event) => {
                        event.stopPropagation()
                        void handleDelete(item)
                      }}
                      className="rounded-xl p-2 text-[#e19a84] opacity-0 transition hover:bg-[#7a3218]/12 group-hover:opacity-100"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </Card>
    </div>
  )
}

function TabButton({ active, icon: Icon, label, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`rounded-2xl px-4 py-2 text-sm transition ${
        active
          ? 'border border-[#d0a85b]/20 bg-[#6a4a1e]/16 text-[#f0d9a5]'
          : 'border border-white/10 bg-white/[0.03] text-[#8f7b66] hover:bg-white/[0.06] hover:text-[#f4ece1]'
      }`}
    >
      <Icon size={16} className="mr-1 inline" />
      {label}
    </button>
  )
}
