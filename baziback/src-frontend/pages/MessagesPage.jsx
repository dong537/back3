import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Bell, Heart, MessageSquare, UserPlus, AtSign, Settings, ChevronRight, Loader2, CheckCheck } from 'lucide-react'
import { communityApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'

const notificationTypes = [
  { id: 'all', label: '全部', icon: Bell, gradient: 'from-gray-500 to-slate-500' },
  { id: 'like', label: '赞', icon: Heart, gradient: 'from-rose-500 to-pink-500' },
  { id: 'comment', label: '评论', icon: MessageSquare, gradient: 'from-blue-500 to-indigo-500' },
  { id: 'follow', label: '关注', icon: UserPlus, gradient: 'from-emerald-500 to-teal-500' },
  { id: 'mention', label: '@我', icon: AtSign, gradient: 'from-violet-500 to-purple-500' },
]

export default function MessagesPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const [activeType, setActiveType] = useState('all')
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [unreadCount, setUnreadCount] = useState(0)

  const loadNotifications = useCallback(async (type = activeType) => {
    if (!isLoggedIn) {
      setLoading(false)
      return
    }
    try {
      setLoading(true)
      const typeParam = type === 'all' ? null : type
      const res = await communityApi.getNotifications(1, 50, typeParam)
      const data = res.data?.data || res.data
      setNotifications(data.list || [])
      setUnreadCount(data.unreadCount || 0)
    } catch (err) {
      console.error('加载通知失败:', err)
      setNotifications(getDefaultNotifications())
    } finally {
      setLoading(false)
    }
  }, [isLoggedIn, activeType])

  useEffect(() => { loadNotifications() }, [])
  useEffect(() => { loadNotifications(activeType) }, [activeType])

  const markAllRead = async () => {
    try {
      await communityApi.markAllNotificationsAsRead()
      setNotifications(notifications.map(n => ({ ...n, isRead: true })))
      setUnreadCount(0)
      toast.success('已全部标记为已读')
    } catch (err) {
      console.error('标记失败:', err)
      toast.error('操作失败')
    }
  }

  const getDefaultNotifications = () => [
    { id: 1, type: 'like', fromUser: { nickname: '星月占卜师', avatar: '🌙' }, content: '赞了你的动态', createdAt: new Date(Date.now() - 300000).toISOString(), isRead: false },
    { id: 2, type: 'comment', fromUser: { nickname: '命理研究者', avatar: '🎓' }, content: '分析得很到位！', createdAt: new Date(Date.now() - 1800000).toISOString(), isRead: false },
  ]

  const getTypeIcon = (type) => {
    const iconMap = {
      like: <Heart size={14} className="text-white" />,
      comment: <MessageSquare size={14} className="text-white" />,
      follow: <UserPlus size={14} className="text-white" />,
      mention: <AtSign size={14} className="text-white" />,
    }
    return iconMap[type] || <Bell size={14} className="text-white" />
  }

  const getTypeGradient = (type) => {
    const gradientMap = {
      like: 'from-rose-500 to-pink-500',
      comment: 'from-blue-500 to-indigo-500',
      follow: 'from-emerald-500 to-teal-500',
      mention: 'from-violet-500 to-purple-500',
    }
    return gradientMap[type] || 'from-gray-500 to-slate-500'
  }

  const formatTime = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    return '更早'
  }

  const getActionText = (type) => {
    const textMap = {
      like: '赞了你的动态',
      comment: '评论了你的动态',
      follow: '关注了你',
      mention: '在动态中@了你',
    }
    return textMap[type] || '发来了通知'
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      {/* 顶部导航 - 玻璃态 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-white/50">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <h1 className="text-lg font-bold text-gray-800">消息通知</h1>
          <button onClick={() => navigate('/settings')} className="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors">
            <Settings size={20} className="text-gray-700" />
          </button>
        </div>
      </div>

      {/* 分类标签 */}
      <div className="bg-white/60 backdrop-blur border-b border-white/50">
        <div className="flex items-center px-4 py-3 overflow-x-auto scrollbar-hide space-x-2">
          {notificationTypes.map(type => {
            const Icon = type.icon
            const isActive = activeType === type.id
            return (
              <button
                key={type.id}
                onClick={() => setActiveType(type.id)}
                className={`flex items-center space-x-2 px-4 py-2.5 rounded-xl text-sm font-semibold whitespace-nowrap transition-all duration-300 ${
                  isActive
                    ? `bg-gradient-to-r ${type.gradient} text-white shadow-lg`
                    : 'bg-white/70 text-gray-600 hover:bg-white hover:shadow-md border border-white/50'
                }`}
              >
                <Icon size={16} />
                <span>{type.label}</span>
              </button>
            )
          })}
        </div>
      </div>

      {/* 操作栏 */}
      {unreadCount > 0 && (
        <div className="mx-4 mt-4 p-4 rounded-2xl bg-gradient-to-r from-indigo-500 to-purple-500 shadow-lg shadow-indigo-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2 text-white">
              <Bell size={18} />
              <span className="font-medium">{unreadCount} 条未读消息</span>
            </div>
            <button onClick={markAllRead} className="flex items-center space-x-1.5 px-4 py-2 bg-white/20 hover:bg-white/30 rounded-xl text-white text-sm font-medium transition-colors">
              <CheckCheck size={16} />
              <span>全部已读</span>
            </button>
          </div>
        </div>
      )}

      {/* 通知列表 */}
      <div className="p-4 space-y-3">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center shadow-lg shadow-indigo-200 mb-4">
              <Loader2 size={28} className="animate-spin text-white" />
            </div>
            <span className="text-gray-400 text-sm">加载中...</span>
          </div>
        ) : !isLoggedIn ? (
          <div className="text-center py-16">
            <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center mx-auto mb-4">
              <Bell size={32} className="text-gray-400" />
            </div>
            <p className="text-gray-500 mb-4">登录后查看消息</p>
            <button onClick={() => navigate('/login')} className="px-8 py-3 bg-gradient-to-r from-indigo-500 to-purple-500 text-white rounded-2xl shadow-lg shadow-indigo-200 font-medium hover:shadow-xl transition-all">
              去登录
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-16">
            <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center mx-auto mb-4">
              <Bell size={32} className="text-gray-400" />
            </div>
            <p className="text-gray-500">暂无消息</p>
          </div>
        ) : (
          notifications.map(notification => {
            const user = notification.fromUser || {}
            return (
              <div 
                key={notification.id} 
                className={`bg-white/70 backdrop-blur rounded-2xl border border-white/50 overflow-hidden cursor-pointer hover:bg-white hover:shadow-lg transition-all duration-300 ${
                  !notification.isRead ? 'ring-2 ring-indigo-200' : ''
                }`}
              >
                <div className="p-4">
                  <div className="flex items-start space-x-3">
                    <div className="relative">
                      <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-100 via-purple-100 to-pink-100 flex items-center justify-center text-2xl shadow-sm">
                        {user.avatar || '👤'}
                      </div>
                      <div className={`absolute -bottom-1 -right-1 w-7 h-7 bg-gradient-to-r ${getTypeGradient(notification.type)} rounded-xl flex items-center justify-center shadow-md`}>
                        {getTypeIcon(notification.type)}
                      </div>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-semibold text-gray-800">{user.nickname || '用户'}</span>
                        <span className="text-xs text-gray-400">{formatTime(notification.createdAt)}</span>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">{getActionText(notification.type)}</p>
                      {notification.content && (
                        <p className="text-sm text-gray-500 bg-gray-50 rounded-xl p-3 line-clamp-2">
                          {notification.content}
                        </p>
                      )}
                    </div>
                    <ChevronRight size={18} className="text-gray-400 flex-shrink-0 mt-4" />
                  </div>
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
