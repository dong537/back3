import { useState, useEffect } from 'react'
import { History, Download, RefreshCw, Star, Share2, X, Search } from 'lucide-react'
import Card, { CardContent } from '../components/Card'
import Button from '../components/Button'
import { toast } from '../components/Toast'
import { favoritesStorage } from '../utils/storage'
import { calculationRecordApi } from '../api'

/**
 * 测算记录管理页面
 * 支持查看、编辑、下载、重新测算等功能
 */
export default function CalculationRecordPage() {
  const [records, setRecords] = useState([])
  const [filteredRecords, setFilteredRecords] = useState([])
  const [selectedType, setSelectedType] = useState('all')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [loading, setLoading] = useState(false)

  const recordTypes = [
    { value: 'all', label: '全部' },
    { value: 'bazi', label: '八字' },
    { value: 'yijing', label: '易经' },
    { value: 'tarot', label: '塔罗' },
    { value: 'compatibility', label: '合盘' },
  ]

  useEffect(() => {
    loadRecords()
  }, [])

  useEffect(() => {
    filterRecords()
  }, [records, selectedType, searchKeyword])

  const loadRecords = async () => {
    setLoading(true)
    try {
      const response = await calculationRecordApi.getAll(selectedType === 'all' ? null : selectedType)
      setRecords(response.data || [])
    } catch (error) {
      // 如果用户未登录或API调用失败，显示空列表
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  const filterRecords = () => {
    let filtered = records

    if (selectedType !== 'all') {
      filtered = filtered.filter(r => r.recordType === selectedType)
    }

    if (searchKeyword) {
      filtered = filtered.filter(r =>
        r.recordTitle.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        r.question?.toLowerCase().includes(searchKeyword.toLowerCase())
      )
    }

    setFilteredRecords(filtered)
  }

  const handleDownload = async (record) => {
    try {
      const dataStr = JSON.stringify(record.data, null, 2)
      const dataBlob = new Blob([dataStr], { type: 'application/json' })
      const url = URL.createObjectURL(dataBlob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${record.recordTitle}_${record.id}.json`
      link.click()
      URL.revokeObjectURL(url)
      toast.success('下载成功')
    } catch (error) {
      toast.error('下载失败')
    }
  }

  const handleReCalculate = async (record) => {
    try {
      const typeMap = {
        'bazi': '/bazi',
        'tarot': '/tarot',
        'yijing': '/yijing',
        'zodiac': '/zodiac',
        'compatibility': '/compatibility'
      }
      const path = typeMap[record.recordType] || '/'
      window.location.href = path
    } catch (error) {
      toast.error('操作失败')
    }
  }

  const handleToggleFavorite = async (record) => {
    try {
      const item = {
        type: record.recordType,
        dataId: String(record.id),
        title: record.recordTitle,
        summary: record.summary || record.question,
        data: record
      }
      const favorited = await favoritesStorage.toggle(item)
      setRecords(records.map(r =>
        r.id === record.id ? { ...r, isFavorite: favorited } : r
      ))
      toast.success(favorited ? '已收藏' : '已取消收藏')
    } catch (error) {
      toast.error('收藏操作失败')
    }
  }

  const handleShare = async (record) => {
    try {
      const shareUrl = `${window.location.origin}/share/record/${record.id}`
      await navigator.clipboard.writeText(shareUrl)
      toast.success('分享链接已复制')
    } catch (error) {
      toast.error('分享失败')
    }
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <History className="w-4 h-4 text-theme" />
            <span className="text-sm text-theme">记录管理</span>
          </div>
          <h1 className="page-title font-serif-title text-white">我的测算记录</h1>
          <p className="page-subtitle">查看、管理您的所有测算记录</p>
        </div>
      </div>

      <div className="max-w-6xl mx-auto">
        {/* 筛选和搜索 */}
        <Card className="panel mb-6">
          <CardContent className="p-4">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="text"
                    placeholder="搜索记录..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-purple-500"
                  />
                  {searchKeyword && (
                    <button
                      onClick={() => setSearchKeyword('')}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2"
                    >
                      <X className="w-4 h-4 text-gray-400" />
                    </button>
                  )}
                </div>
              </div>
              <div className="flex gap-2">
                {recordTypes.map((type) => (
                  <button
                    key={type.value}
                    onClick={() => setSelectedType(type.value)}
                    className={`px-4 py-2 rounded-lg border transition-all ${
                      selectedType === type.value
                        ? 'border-purple-500 bg-purple-500/20 text-white'
                        : 'border-white/10 text-gray-300 hover:border-purple-500/50'
                    }`}
                  >
                    {type.label}
                  </button>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 记录列表 */}
        <div className="space-y-4">
          {loading ? (
            <Card className="panel">
              <CardContent className="text-center py-12">
                <div className="animate-spin w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full mx-auto"></div>
                <p className="mt-4 text-gray-400">加载中...</p>
              </CardContent>
            </Card>
          ) : filteredRecords.length === 0 ? (
            <Card className="panel">
              <CardContent className="text-center py-12">
                <History className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                <p className="text-gray-400">暂无记录</p>
              </CardContent>
            </Card>
          ) : (
            filteredRecords.map((record) => (
              <Card key={record.id} className="panel">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <h3 className="text-lg font-medium text-white">{record.recordTitle}</h3>
                        {record.isFavorite && (
                          <Star className="w-4 h-4 text-yellow-400 fill-current" />
                        )}
                      </div>
                      {record.question && (
                        <p className="text-gray-400 mb-2">问题：{record.question}</p>
                      )}
                      <p className="text-sm text-gray-500 mb-3">{record.summary}</p>
                      <div className="flex items-center space-x-4 text-xs text-gray-500">
                        <span>创建时间：{new Date(record.createTime).toLocaleString('zh-CN')}</span>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2 ml-4">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleFavorite(record)}
                        title={record.isFavorite ? '取消收藏' : '收藏'}
                      >
                        <Star className={`w-4 h-4 ${record.isFavorite ? 'text-yellow-400 fill-current' : ''}`} />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleShare(record)}
                        title="分享"
                      >
                        <Share2 className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDownload(record)}
                        title="下载"
                      >
                        <Download className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleReCalculate(record)}
                        title="重新测算"
                      >
                        <RefreshCw className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
