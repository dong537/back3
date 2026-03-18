import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, Clock, Trash2, Star, Search, Filter, CheckSquare, Square, Download, AlertTriangle } from 'lucide-react'
import { favoritesStorage } from '../utils/storage'
import { toast } from '../components/Toast'
import { favoriteApi } from '../api'

function getTypeLabel(type) {
  const map = {
    tarot: '塔罗',
    yijing: '易经',
    bazi: '八字',
    zodiac: '星座',
  }
  return map[type] || type || '未知'
}

function getTypeBadgeClass(type) {
  if (type === 'tarot') return 'bg-purple-500/20 text-purple-300 border-purple-500/30'
  if (type === 'yijing') return 'bg-amber-500/20 text-amber-300 border-amber-500/30'
  if (type === 'bazi') return 'bg-orange-500/20 text-orange-300 border-orange-500/30'
  if (type === 'zodiac') return 'bg-blue-500/20 text-blue-300 border-blue-500/30'
  return 'bg-white/10 text-gray-200 border-white/10'
}

function formatDate(ts) {
  try {
    const d = new Date(ts)
    return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

function buildNavTarget(item) {
  if (!item) return null

  const type = item.favoriteType || item.type

  if (type === 'tarot') {
    const cards = item?.data?.drawnCards || item?.data?.cards || []
    const first = cards?.[0]
    const cardId = first?.id
    const cardName = first?.name
    const param = cardId != null ? String(cardId) : (cardName ? encodeURIComponent(cardName) : '')
    if (!param) return null
    return { pathname: `/tarot/card/${param}` }
  }

  if (type === 'yijing') {
    const state = {
      fromFavorite: true,
      question: item.question,
      result: typeof item.data === 'string' ? JSON.parse(item.data) : item.data,
    }
    return { pathname: '/yijing', state }
  }

  if (type === 'bazi') {
    const state = {
      fromFavorite: true,
      result: typeof item.data === 'string' ? JSON.parse(item.data) : item.data,
    }
    return { pathname: '/bazi', state }
  }

  return null
}

function normalizeText(v) {
  return (v == null ? '' : String(v)).toLowerCase()
}

export default function FavoritesPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState([])
  const [typeFilter, setTypeFilter] = useState('all')
  const [query, setQuery] = useState('')
  const [isLoading, setIsLoading] = useState(true)

  const [isSelectionMode, setIsSelectionMode] = useState(false)
  const [selectedIds, setSelectedIds] = useState(new Set())

  useEffect(() => {
    const loadItems = async () => {
      setIsLoading(true)
      const favs = await favoritesStorage.getAll()
      setItems(favs)
      setIsLoading(false)
    }
    loadItems()
  }, [])

  const filtered = useMemo(() => {
    const q = normalizeText(query).trim()

    return items
      .filter((it) => {
        if (!it) return false  // 过滤掉 null/undefined 项
        if (typeFilter === 'all') return true
        // 兼容 favoriteType 和 type 两种字段
        const itemType = it.favoriteType || it.type
        return itemType === typeFilter
      })
      .filter((it) => {
        if (!q) return true
        const hay = [
          it?.title,
          it?.summary,
          it?.question,
          it?.favoriteType,
          it?.type,
        ]
          .map(normalizeText)
          .join(' ')
        return hay.includes(q)
      })
      .sort((a, b) => {
        const ta = new Date(a.createTime || a.timestamp || 0).getTime()
        const tb = new Date(b.createTime || b.timestamp || 0).getTime()
        return tb - ta
      })
  }, [items, typeFilter, query])

  const handleRemove = async (id, e) => {
    e?.preventDefault?.()
    e?.stopPropagation?.()
    const ok = await favoritesStorage.remove(id)
    if (ok) {
      setItems(await favoritesStorage.getAll(true))
      toast.success('已取消收藏')
    } else {
      toast.error('取消收藏失败')
    }
  }

  const handleSelectionChange = (id) => {
    setSelectedIds(prev => {
      const newSet = new Set(prev)
      if (newSet.has(id)) {
        newSet.delete(id)
      } else {
        newSet.add(id)
      }
      return newSet
    })
  }

  const handleBatchDelete = async () => {
    if (selectedIds.size === 0) {
      toast.info('请选择要删除的收藏')
      return
    }
    if (window.confirm(`确定要删除选中的 ${selectedIds.size} 条收藏吗？`)) {
      try {
        const idsToDelete = Array.from(selectedIds)
        await favoriteApi.removeBatch(idsToDelete)
        setItems(await favoritesStorage.getAll(true))
        setSelectedIds(new Set())
        setIsSelectionMode(false)
        toast.success(`成功删除 ${idsToDelete.length} 条收藏`)
      } catch (error) {
        toast.error('批量删除失败')
      }
    }
  }

  const handleClearAll = async () => {
    if (window.confirm('确定要清空所有收藏吗？此操作不可恢复。')) {
      try {
        await favoriteApi.clearAll()
        setItems([])
        toast.success('已清空所有收藏')
      } catch (error) {
        toast.error('清空失败')
      }
    }
  }

  const handleExport = () => {
    const dataStr = JSON.stringify(filtered, null, 2)
    const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr)
    const exportFileDefaultName = 'favorites.json'
    const linkElement = document.createElement('a')
    linkElement.setAttribute('href', dataUri)
    linkElement.setAttribute('download', exportFileDefaultName)
    linkElement.click()
    toast.success('已开始导出')
  }

  const toggleSelectAll = () => {
    if (selectedIds.size === filtered.length) {
      setSelectedIds(new Set())
    } else {
      setSelectedIds(new Set(filtered.map(item => item.id)))
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-950 to-slate-900">
      <div className="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-xl border-b border-white/10">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-white/10 rounded-xl transition-all">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div className="flex items-center space-x-2">
            <Star size={18} className="text-yellow-400" />
            <h1 className="text-lg font-bold text-white">我的收藏</h1>
          </div>
          <button onClick={() => setIsSelectionMode(!isSelectionMode)} className="p-2 text-sm text-white hover:bg-white/10 rounded-lg">
            {isSelectionMode ? '取消' : '管理'}
          </button>
        </div>
      </div>

      <div className="px-4 pb-20 pt-6">
        <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-4 mb-4">
          <div className="flex items-center gap-2 mb-3">
            <div className="flex-1 flex items-center gap-2 bg-white/5 border border-white/10 rounded-xl px-3 py-2">
              <Search size={16} className="text-gray-400" />
              <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="搜索：标题/摘要/问题" className="w-full bg-transparent outline-none text-sm text-white placeholder-gray-500" />
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            {['all', 'tarot', 'yijing', 'bazi'].map((t) => (
              <button key={t} type="button" onClick={() => setTypeFilter(t)} className={`px-3 py-1.5 rounded-full text-xs font-medium border transition-all ${typeFilter === t ? 'bg-white/15 border-white/20 text-white' : 'bg-white/5 border-white/10 text-gray-300 hover:bg-white/10'}`}>
                {t === 'all' ? '全部' : getTypeLabel(t)}
              </button>
            ))}
          </div>
          <div className="flex items-center justify-between text-xs text-gray-400 mt-3 pt-3 border-t border-white/10">
            <span>共 {filtered.length} 条</span>
            <div className="flex gap-3">
              <button onClick={handleExport} className="hover:text-white flex items-center gap-1"><Download size={14} />导出</button>
              <button onClick={handleClearAll} className="hover:text-red-400 text-red-500 flex items-center gap-1"><AlertTriangle size={14} />清空</button>
            </div>
          </div>
        </div>

        {isLoading ? (
          <div className="text-center text-gray-400 py-10">加载中...</div>
        ) : filtered.length === 0 ? (
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-8 text-center">
            <div className="text-5xl mb-3">⭐</div>
            <div className="text-white font-semibold">暂无收藏</div>
            <div className="text-gray-400 text-sm mt-2">去抽牌/占卜后，点星星即可收藏</div>
          </div>
        ) : (
          <div className="space-y-3">
            {filtered.map((item) => {
              const target = buildNavTarget(item)
              const Wrapper = !isSelectionMode && target ? Link : 'div'
              const wrapperProps = !isSelectionMode && target ? { to: target.pathname, state: target.state } : {}
              const type = item.favoriteType || item.type

              return (
                <Wrapper key={item.id} {...wrapperProps} className={`block bg-white/5 backdrop-blur-xl rounded-2xl border p-4 transition-all ${isSelectionMode ? 'border-purple-500/30 cursor-pointer' : 'border-white/10 hover:bg-white/10'}`} onClick={() => isSelectionMode && handleSelectionChange(item.id)}>
                  <div className="flex items-center justify-between gap-3">
                    {isSelectionMode && <div className="text-purple-400">{selectedIds.has(item.id) ? <CheckSquare size={20} /> : <Square size={20} />}</div>}
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <span className={`text-[10px] px-2 py-1 rounded-full border ${getTypeBadgeClass(type)}`}>{getTypeLabel(type)}</span>
                        <span className="text-xs text-gray-400 flex items-center gap-1"><Clock size={12} />{formatDate(item.createTime || item.timestamp)}</span>
                      </div>
                      <div className="text-white font-semibold line-clamp-1">{item.title || item.question || '未命名收藏'}</div>
                      {item.summary && <div className="text-sm text-gray-300 mt-1 line-clamp-2">{item.summary}</div>}
                    </div>
                    {!isSelectionMode && <button type="button" onClick={(e) => handleRemove(item.id, e)} className="p-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-gray-300 hover:text-red-300 transition-all" title="取消收藏"><Trash2 size={16} /></button>}
                  </div>
                </Wrapper>
              )
            })}
          </div>
        )}
      </div>

      {isSelectionMode && (
        <div className="fixed bottom-0 left-0 right-0 bg-slate-800/80 backdrop-blur-lg border-t border-white/10 p-4 flex items-center justify-between z-50">
          <button onClick={toggleSelectAll} className="text-sm text-white flex items-center gap-2">
            {selectedIds.size === filtered.length ? <CheckSquare /> : <Square />} 
            全选 ({selectedIds.size})
          </button>
          <div className="flex gap-3">
            <button onClick={() => setIsSelectionMode(false)} className="px-4 py-2 text-sm rounded-lg bg-white/10 text-white">取消</button>
            <button onClick={handleBatchDelete} className="px-4 py-2 text-sm rounded-lg bg-red-500 text-white flex items-center gap-2"><Trash2 size={16} /> 删除选中</button>
          </div>
        </div>
      )}
    </div>
  )
}
