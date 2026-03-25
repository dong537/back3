import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  AtSign,
  Bell,
  CheckCheck,
  ChevronRight,
  Heart,
  Loader2,
  MessageSquare,
  Settings,
  UserPlus,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { communityApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'

const MESSAGES_COPY = {
  'zh-CN': {
    title: '消息通知',
    loading: '加载中...',
    loginPrompt: '登录后查看消息',
    loginAction: '去登录',
    empty: '暂无消息',
    unreadCount: (count) => `${count} 条未读消息`,
    markAllRead: '全部已读',
    markAllReadSuccess: '已全部标记为已读',
    markAllReadFailed: '操作失败',
    loadFailed: '加载通知失败，已展示示例消息',
    settingsAria: '打开设置',
    backAria: '返回上一页',
    justNow: '刚刚',
    minutesAgo: (count) => `${count} 分钟前`,
    hoursAgo: (count) => `${count} 小时前`,
    daysAgo: (count) => `${count} 天前`,
    earlier: '更早',
    unknownUser: '用户',
    tabs: {
      all: '全部',
      like: '点赞',
      comment: '评论',
      follow: '关注',
      mention: '@我',
    },
    actions: {
      like: '赞了你的动态',
      comment: '评论了你的动态',
      follow: '关注了你',
      mention: '在动态中提到了你',
      default: '给你发来一条通知',
    },
    demoUsers: {
      astro: '星月占卜师',
      master: '命理研究者',
    },
    demoContent: {
      like: '你的分享很有启发，已经收藏准备再看一遍。',
      comment: '这次分析很到位，我也有类似经历。',
    },
  },
  'en-US': {
    title: 'Notifications',
    loading: 'Loading...',
    loginPrompt: 'Sign in to view notifications',
    loginAction: 'Go to sign in',
    empty: 'No notifications yet',
    unreadCount: (count) => `${count} unread notifications`,
    markAllRead: 'Mark all read',
    markAllReadSuccess: 'All notifications marked as read',
    markAllReadFailed: 'Action failed',
    loadFailed: 'Failed to load notifications. Showing demo items instead.',
    settingsAria: 'Open settings',
    backAria: 'Go back',
    justNow: 'Just now',
    minutesAgo: (count) => `${count} min ago`,
    hoursAgo: (count) => `${count} hr ago`,
    daysAgo: (count) => `${count} day${count === 1 ? '' : 's'} ago`,
    earlier: 'Earlier',
    unknownUser: 'User',
    tabs: {
      all: 'All',
      like: 'Likes',
      comment: 'Comments',
      follow: 'Follows',
      mention: '@Mentions',
    },
    actions: {
      like: 'liked your post',
      comment: 'commented on your post',
      follow: 'followed you',
      mention: 'mentioned you in a post',
      default: 'sent you a notification',
    },
    demoUsers: {
      astro: 'Moon Oracle',
      master: 'Destiny Researcher',
    },
    demoContent: {
      like: 'Your post was inspiring, so I bookmarked it to revisit later.',
      comment: 'This reading was spot on. I had a very similar experience.',
    },
  },
}

export default function MessagesPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = MESSAGES_COPY[locale]
  const [activeType, setActiveType] = useState('all')
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [unreadCount, setUnreadCount] = useState(0)

  const notificationTypes = useMemo(
    () => [
      {
        id: 'all',
        label: copy.tabs.all,
        icon: Bell,
        gradient: 'from-[#5e4a36] to-[#8d6a3d]',
      },
      {
        id: 'like',
        label: copy.tabs.like,
        icon: Heart,
        gradient: 'from-[#8e2f21] to-[#d67b4d]',
      },
      {
        id: 'comment',
        label: copy.tabs.comment,
        icon: MessageSquare,
        gradient: 'from-[#7a3218] to-[#d0a85b]',
      },
      {
        id: 'follow',
        label: copy.tabs.follow,
        icon: UserPlus,
        gradient: 'from-[#6a4a1e] to-[#b88a3d]',
      },
      {
        id: 'mention',
        label: copy.tabs.mention,
        icon: AtSign,
        gradient: 'from-[#5c3320] to-[#a35a34]',
      },
    ],
    [copy]
  )

  const getDefaultNotifications = useCallback(
    () => [
      {
        id: 'demo-like',
        type: 'like',
        fromUser: {
          nickname: copy.demoUsers.astro,
          avatar: '🔮',
        },
        content: copy.demoContent.like,
        createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
        isRead: false,
      },
      {
        id: 'demo-comment',
        type: 'comment',
        fromUser: {
          nickname: copy.demoUsers.master,
          avatar: '📿',
        },
        content: copy.demoContent.comment,
        createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
        isRead: false,
      },
    ],
    [copy]
  )

  const loadNotifications = useCallback(
    async (type = activeType) => {
      if (!isLoggedIn) {
        setNotifications([])
        setUnreadCount(0)
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const typeParam = type === 'all' ? null : type
        const response = await communityApi.getNotifications(1, 50, typeParam)
        const payload = response.data?.data || response.data || {}

        setNotifications(Array.isArray(payload.list) ? payload.list : [])
        setUnreadCount(Number(payload.unreadCount) || 0)
      } catch (error) {
        logger.error('Load notifications failed:', error)
        toast.warning(copy.loadFailed)
        setNotifications(getDefaultNotifications())
        setUnreadCount(2)
      } finally {
        setLoading(false)
      }
    },
    [activeType, copy, getDefaultNotifications, isLoggedIn]
  )

  useEffect(() => {
    loadNotifications(activeType)
  }, [activeType, loadNotifications])

  const markAllRead = async () => {
    try {
      await communityApi.markAllNotificationsAsRead()
      setNotifications((prev) =>
        prev.map((item) => ({ ...item, isRead: true }))
      )
      setUnreadCount(0)
      toast.success(copy.markAllReadSuccess)
    } catch (error) {
      logger.error('Mark notifications as read failed:', error)
      toast.error(copy.markAllReadFailed)
    }
  }

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
      like: 'from-[#8e2f21] to-[#d67b4d]',
      comment: 'from-[#7a3218] to-[#d0a85b]',
      follow: 'from-[#6a4a1e] to-[#b88a3d]',
      mention: 'from-[#5c3320] to-[#a35a34]',
    }
    return gradientMap[type] || 'from-[#5e4a36] to-[#8d6a3d]'
  }

  const formatTime = (value) => {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''

    const diff = Date.now() - date.getTime()
    if (diff < 60_000) return copy.justNow
    if (diff < 3_600_000) return copy.minutesAgo(Math.floor(diff / 60_000))
    if (diff < 86_400_000) return copy.hoursAgo(Math.floor(diff / 3_600_000))
    if (diff < 604_800_000) return copy.daysAgo(Math.floor(diff / 86_400_000))
    return copy.earlier
  }

  const getActionText = (type) => copy.actions[type] || copy.actions.default

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            aria-label={copy.backAria}
            className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-white/10"
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          <button
            onClick={() => navigate('/settings')}
            aria-label={copy.settingsAria}
            className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-white/10"
          >
            <Settings size={20} className="text-[#bdaa94]" />
          </button>
        </div>
        <div className="app-sticky-inner border-t border-white/10 pb-3 pt-3">
          <div className="scrollbar-hide flex items-center space-x-2 overflow-x-auto">
          {notificationTypes.map((type) => {
            const Icon = type.icon
            const isActive = activeType === type.id
            return (
              <button
                key={type.id}
                onClick={() => setActiveType(type.id)}
                className={`flex items-center space-x-2 whitespace-nowrap rounded-xl px-4 py-2.5 text-sm font-semibold transition-all duration-300 ${
                  isActive
                    ? `bg-gradient-to-r ${type.gradient} text-white shadow-lg`
                    : 'border border-white/10 bg-white/[0.04] text-[#bdaa94] hover:bg-white/[0.08]'
                }`}
              >
                <Icon size={16} />
                <span>{type.label}</span>
              </button>
            )
          })}
          </div>
        </div>
      </div>

      {unreadCount > 0 && (
        <div className="app-page-shell-narrow pt-4">
          <div className="rounded-[28px] border border-[#d0a85b]/20 bg-[#7a3218]/16 p-4 shadow-[0_20px_60px_rgba(0,0,0,0.22)]">
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center space-x-2 text-white">
                <Bell size={18} className="text-[#f0d9a5]" />
              <span className="font-medium">
                {copy.unreadCount(unreadCount)}
              </span>
            </div>
            <button
              onClick={markAllRead}
              className="flex items-center space-x-1.5 rounded-xl border border-white/10 bg-white/[0.08] px-4 py-2 text-sm font-medium text-[#f4ece1] transition-colors hover:bg-white/[0.12]"
            >
              <CheckCheck size={16} />
              <span>{copy.markAllRead}</span>
            </button>
          </div>
        </div>
        </div>
      )}

      <div className="app-page-shell-narrow space-y-3 py-4">
        {loading ? (
          <div className="panel flex flex-col items-center justify-center py-16">
            <div className="mystic-icon-badge mb-4 flex h-16 w-16 items-center justify-center rounded-2xl">
              <Loader2 size={28} className="animate-spin text-white" />
            </div>
            <span className="text-sm text-[#8f7b66]">{copy.loading}</span>
          </div>
        ) : !isLoggedIn ? (
          <div className="panel py-16 text-center">
            <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-3xl border border-white/10 bg-white/[0.04]">
              <Bell size={32} className="text-[#8f7b66]" />
            </div>
            <p className="mb-4 text-[#bdaa94]">{copy.loginPrompt}</p>
            <button
              onClick={() => navigate('/login')}
              className="btn-primary-theme px-8 py-3"
            >
              {copy.loginAction}
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="panel py-16 text-center">
            <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-3xl border border-white/10 bg-white/[0.04]">
              <Bell size={32} className="text-[#8f7b66]" />
            </div>
            <p className="text-[#bdaa94]">{copy.empty}</p>
          </div>
        ) : (
          notifications.map((notification) => {
            const user = notification.fromUser || {}

            return (
              <div
                key={notification.id}
                className={`cursor-pointer overflow-hidden rounded-[26px] border border-white/10 bg-white/[0.04] backdrop-blur transition-all duration-300 hover:bg-white/[0.06] hover:shadow-[0_22px_60px_rgba(0,0,0,0.22)] ${
                  !notification.isRead ? 'ring-1 ring-[#d0a85b]/25' : ''
                }`}
              >
                <div className="p-4">
                  <div className="flex items-start space-x-3">
                    <div className="relative">
                      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-2xl shadow-[0_16px_36px_rgba(163,66,36,0.2)]">
                        {user.avatar || '👤'}
                      </div>
                      <div
                        className={`absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-xl bg-gradient-to-r ${getTypeGradient(notification.type)} shadow-[0_10px_24px_rgba(0,0,0,0.2)]`}
                      >
                        {getTypeIcon(notification.type)}
                      </div>
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="mb-1 flex items-center justify-between gap-2">
                        <span className="font-semibold text-[#f4ece1]">
                          {user.nickname || copy.unknownUser}
                        </span>
                        <span className="text-xs text-[#8f7b66]">
                          {formatTime(notification.createdAt)}
                        </span>
                      </div>

                      <p className="mb-2 text-sm text-[#bdaa94]">
                        {getActionText(notification.type)}
                      </p>

                      {notification.content && (
                        <p className="line-clamp-2 rounded-xl border border-white/10 bg-white/[0.03] p-3 text-sm text-[#8f7b66]">
                          {notification.content}
                        </p>
                      )}
                    </div>

                    <ChevronRight
                      size={18}
                      className="mt-4 flex-shrink-0 text-[#8f7b66]"
                    />
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
