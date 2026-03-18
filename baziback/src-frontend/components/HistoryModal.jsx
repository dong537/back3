import { useState, useEffect } from 'react'
import { X, Trash2, Clock, Star, Heart } from 'lucide-react'
import { historyStorage, favoritesStorage } from '../utils/storage'
import Card from './Card'

export default function HistoryModal({ isOpen, onClose, type, onSelect }) {
  const [history, setHistory] = useState([])
  const [activeTab, setActiveTab] = useState('history') // history or favorites

  useEffect(() => {
    if (isOpen) {
      loadData()
    }
  }, [isOpen, type])

  const loadData = () => {
    if (activeTab === 'history') {
      const allHistory = type 
        ? historyStorage.getByType(type)
        : historyStorage.getAll()
      setHistory(allHistory)
    } else {
      const favorites = favoritesStorage.getAll()
      const filtered = type 
        ? favorites.filter(fav => fav.type === type)
        : favorites
      setHistory(filtered)
    }
  }

  const handleDelete = (id) => {
    if (activeTab === 'history') {
      historyStorage.remove(id)
    } else {
      favoritesStorage.remove(id)
    }
    loadData()
  }

  const handleClear = () => {
    if (confirm('确定要清空所有历史记录吗？')) {
      historyStorage.clear()
      loadData()
    }
  }

  const formatDate = (timestamp) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getTypeLabel = (type) => {
    const labels = {
      yijing: '易经占卜',
      tarot: '塔罗牌',
      bazi: '八字排盘',
      zodiac: '星座运势'
    }
    return labels[type] || type
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <Card className="w-full max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-white/10">
          <div className="flex items-center space-x-4">
            <h2 className="text-xl font-bold">历史记录</h2>
            <div className="flex space-x-2">
              <button
                onClick={() => {
                  setActiveTab('history')
                  loadData()
                }}
                className={`px-4 py-2 rounded-lg text-sm transition ${
                  activeTab === 'history'
                    ? 'bg-skin-primary/20 text-skin-primary'
                    : 'bg-white/5 text-gray-400 hover:bg-white/10'
                }`}
              >
                <Clock size={16} className="inline mr-1" />
                历史
              </button>
              <button
                onClick={() => {
                  setActiveTab('favorites')
                  loadData()
                }}
                className={`px-4 py-2 rounded-lg text-sm transition ${
                  activeTab === 'favorites'
                    ? 'bg-skin-primary/20 text-skin-primary'
                    : 'bg-white/5 text-gray-400 hover:bg-white/10'
                }`}
              >
                <Star size={16} className="inline mr-1" />
                收藏
              </button>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            {activeTab === 'history' && history.length > 0 && (
              <button
                onClick={handleClear}
                className="px-3 py-1.5 text-sm text-red-400 hover:bg-red-400/10 rounded-lg transition"
              >
                清空
              </button>
            )}
            <button
              onClick={onClose}
              className="p-2 hover:bg-white/10 rounded-lg transition"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {history.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-gray-400">
              {activeTab === 'history' ? (
                <>
                  <Clock size={48} className="mb-4 opacity-50" />
                  <p>暂无历史记录</p>
                </>
              ) : (
                <>
                  <Star size={48} className="mb-4 opacity-50" />
                  <p>暂无收藏</p>
                </>
              )}
            </div>
          ) : (
            <div className="space-y-3">
              {history.map((item) => (
                <div
                  key={item.id}
                  className="glass rounded-xl p-4 hover:bg-white/5 transition cursor-pointer group"
                  onClick={() => {
                    if (onSelect) {
                      onSelect(item)
                      onClose()
                    }
                  }}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <span className="px-2 py-0.5 text-xs rounded bg-skin-primary/20 text-skin-primary">
                          {getTypeLabel(item.type)}
                        </span>
                        <span className="text-xs text-gray-400">
                          {formatDate(item.timestamp)}
                        </span>
                      </div>
                      <p className="text-sm font-medium mb-1">
                        {item.question || item.title || '无标题'}
                      </p>
                      {item.summary && (
                        <p className="text-xs text-gray-400 line-clamp-2">
                          {item.summary}
                        </p>
                      )}
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation()
                        handleDelete(item.id)
                      }}
                      className="p-2 opacity-0 group-hover:opacity-100 hover:bg-red-400/10 text-red-400 rounded-lg transition"
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
