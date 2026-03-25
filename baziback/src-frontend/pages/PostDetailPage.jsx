import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  ArrowLeft,
  Bookmark,
  Heart,
  Loader2,
  MessageSquare,
  MoreHorizontal,
  Send,
  Share2,
  Sparkles,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { communityApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeText } from '../utils/displayText'

const POST_DETAIL_COPY = {
  'zh-CN': {
    back: '返回',
    more: '更多操作',
    like: '点赞',
    share: '分享',
    save: '收藏',
    sendComment: '发送评论',
    title: '动态详情',
    loading: '加载中...',
    postMissing: '帖子不存在或已删除',
    loadCommentsFailed: '加载评论失败',
    loginFirst: '请先登录',
    commentRequired: '请输入评论内容',
    commentSuccess: '评论成功',
    commentFailed: '评论失败',
    saveFailed: '收藏失败，请稍后重试',
    likeFailed: '点赞失败，请稍后重试',
    shareSuccess: '链接已复制',
    shareFailed: '分享失败',
    commentsTitle: '评论',
    commentsEmpty: '暂无评论，快来抢沙发吧',
    commentPlaceholder: '写下你的评论...',
    unknownUser: '用户',
    question: '问题',
    justNow: '刚刚',
    minutesAgo: (count) => `${count} 分钟前`,
    hoursAgo: (count) => `${count} 小时前`,
    monthDay: (month, day) => `${month}月${day}日`,
  },
  'en-US': {
    back: 'Back',
    more: 'More actions',
    like: 'Like',
    share: 'Share',
    save: 'Save',
    sendComment: 'Post comment',
    title: 'Post Detail',
    loading: 'Loading...',
    postMissing: 'This post does not exist or has been removed',
    loadCommentsFailed: 'Failed to load comments',
    loginFirst: 'Please sign in first',
    commentRequired: 'Please enter a comment',
    commentSuccess: 'Comment posted',
    commentFailed: 'Failed to post comment',
    saveFailed: 'Failed to save. Please try again later.',
    likeFailed: 'Failed to like. Please try again later.',
    shareSuccess: 'Link copied',
    shareFailed: 'Share failed',
    commentsTitle: 'Comments',
    commentsEmpty: 'No comments yet. Be the first to reply.',
    commentPlaceholder: 'Write your comment...',
    unknownUser: 'User',
    question: 'Question',
    justNow: 'Just now',
    minutesAgo: (count) => `${count} min ago`,
    hoursAgo: (count) => `${count} hr ago`,
    monthDay: (month, day) => `${month}/${day}`,
  },
}

export default function PostDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = POST_DETAIL_COPY[locale]

  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [commentText, setCommentText] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const loadPost = useCallback(async () => {
    try {
      setLoading(true)
      const response = await communityApi.getPostDetail(id)
      setPost(response.data?.data || response.data)
    } catch (error) {
      logger.error('Load post failed:', error)
      toast.error(copy.postMissing)
      navigate('/')
    } finally {
      setLoading(false)
    }
  }, [copy.postMissing, id, navigate])

  const loadComments = useCallback(async () => {
    try {
      const response = await communityApi.getComments(id, 1, 50)
      const payload = response.data?.data || response.data || {}
      setComments(Array.isArray(payload.list) ? payload.list : [])
    } catch (error) {
      logger.error('Load comments failed:', error)
      toast.error(copy.loadCommentsFailed)
    }
  }, [copy.loadCommentsFailed, id])

  useEffect(() => {
    loadPost()
    loadComments()
  }, [loadComments, loadPost])

  const ensureLogin = () => {
    if (isLoggedIn) return true
    toast.warning(copy.loginFirst)
    navigate('/login')
    return false
  }

  const handleLike = async () => {
    if (!ensureLogin()) return

    setPost((prev) => ({
      ...prev,
      liked: !prev.liked,
      likesCount: prev.liked ? prev.likesCount - 1 : prev.likesCount + 1,
    }))

    try {
      await communityApi.toggleLike('post', id)
    } catch (error) {
      logger.error('Toggle post like failed:', error)
      setPost((prev) => ({
        ...prev,
        liked: !prev.liked,
        likesCount: prev.liked ? prev.likesCount + 1 : prev.likesCount - 1,
      }))
      toast.error(copy.likeFailed)
    }
  }

  const handleSave = async () => {
    if (!ensureLogin()) return

    setPost((prev) => ({ ...prev, saved: !prev.saved }))
    try {
      await communityApi.toggleFavorite(id)
    } catch (error) {
      logger.error('Toggle post favorite failed:', error)
      setPost((prev) => ({ ...prev, saved: !prev.saved }))
      toast.error(copy.saveFailed)
    }
  }

  const handleShare = async () => {
    const shareText = `${safeText(post?.title) || copy.title}\n${window.location.href}`
    try {
      await navigator.clipboard.writeText(shareText)
      toast.success(copy.shareSuccess)
    } catch (error) {
      logger.error('Share post failed:', error)
      toast.error(copy.shareFailed)
    }
  }

  const handleSubmitComment = async () => {
    if (!ensureLogin()) return
    if (!commentText.trim()) {
      toast.warning(copy.commentRequired)
      return
    }

    setSubmitting(true)
    try {
      const response = await communityApi.createComment({
        postId: Number.parseInt(id, 10),
        content: commentText.trim(),
        anonymous: false,
      })
      const newComment = response.data?.data || response.data
      setComments((prev) => [newComment, ...prev])
      setCommentText('')
      setPost((prev) => ({
        ...prev,
        commentsCount: (prev.commentsCount || 0) + 1,
      }))
      toast.success(copy.commentSuccess)
    } catch (error) {
      logger.error('Create comment failed:', error)
      toast.error(copy.commentFailed)
    } finally {
      setSubmitting(false)
    }
  }

  const formatTime = (value) => {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    const diff = Date.now() - date.getTime()
    if (diff < 60_000) return copy.justNow
    if (diff < 3_600_000) return copy.minutesAgo(Math.floor(diff / 60_000))
    if (diff < 86_400_000) return copy.hoursAgo(Math.floor(diff / 3_600_000))
    return copy.monthDay(date.getMonth() + 1, date.getDate())
  }

  if (loading) {
    return (
      <div className="page-shell flex min-h-screen items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="relative">
            <div className="h-16 w-16 animate-pulse rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]" />
            <Sparkles
              className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 animate-spin text-white"
              size={24}
            />
          </div>
          <span className="animate-pulse text-sm text-[#dcb86f]">
            {copy.loading}
          </span>
        </div>
      </div>
    )
  }

  if (!post) return null

  const user = post.user || {}

  return (
    <div className="page-shell pb-32" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="group rounded-xl p-2 transition-all duration-300 hover:bg-white/10"
            title={copy.back}
            aria-label={copy.back}
          >
            <ArrowLeft
              size={20}
              className="text-[#f4ece1] transition-colors group-hover:text-[#fff7eb]"
            />
          </button>
          <h1 className="bg-[linear-gradient(135deg,#f6e7cf_0%,#dcb86f_52%,#e19a84_100%)] bg-clip-text text-lg font-bold text-transparent">
            {copy.title}
          </h1>
          <button
            className="group rounded-xl p-2 transition-all duration-300 hover:bg-white/10"
            title={copy.more}
            aria-label={copy.more}
          >
            <MoreHorizontal
              size={20}
              className="text-[#bdaa94] transition-colors group-hover:text-[#f4ece1]"
            />
          </button>
        </div>
      </div>

      <div className="group relative m-4">
        <div className="absolute -inset-1 rounded-2xl bg-[linear-gradient(135deg,rgba(163,66,36,0.28),rgba(208,168,91,0.22),rgba(122,50,24,0.24))] opacity-60 blur-lg transition-opacity duration-500 group-hover:opacity-80" />

        <div className="relative overflow-hidden rounded-2xl border border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.9),rgba(14,11,10,0.82))] backdrop-blur-xl">
          <div className="h-1 bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]" />

          <div className="p-5">
            <div className="mb-5 flex items-center space-x-3">
              <div className="relative">
                <div className="h-12 w-12 rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] p-0.5">
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-[#100b0a] text-xl">
                    {user.avatar || '👤'}
                  </div>
                </div>
                <div className="absolute -bottom-1 -right-1 h-4 w-4 rounded-full border-2 border-[#100b0a] bg-[#d0a85b]" />
              </div>
              <div className="flex-1">
                <span className="font-semibold text-[#f4ece1]">
                  {user.nickname || copy.unknownUser}
                </span>
                <div className="text-xs text-[#8f7b66]">
                  {formatTime(post.createdAt)}
                </div>
              </div>
            </div>

            {post.title && (
              <h2 className="mb-3 text-xl font-bold leading-tight text-white">
                {post.title}
              </h2>
            )}
            <p className="mb-5 whitespace-pre-line text-[15px] leading-relaxed text-[#e4d6c8]">
              {post.content}
            </p>

            <div className="flex items-center justify-between border-t border-white/10 pt-4">
              <button
                onClick={handleLike}
                className={`flex items-center space-x-2 rounded-xl px-4 py-2 transition-all duration-300 ${
                  post.liked
                    ? 'bg-[#7a3218]/20 text-[#e19a84] shadow-lg shadow-[rgba(163,66,36,0.18)]'
                    : 'text-[#bdaa94] hover:bg-white/[0.05]'
                }`}
                title={copy.like}
                aria-label={copy.like}
              >
                <Heart
                  size={20}
                  fill={post.liked ? 'currentColor' : 'none'}
                  className={post.liked ? 'animate-pulse' : ''}
                />
                <span className="text-sm font-medium">
                  {post.likesCount || 0}
                </span>
              </button>

              <div className="flex items-center space-x-2 px-4 py-2 text-[#bdaa94]">
                <MessageSquare size={20} />
                <span className="text-sm font-medium">
                  {post.commentsCount || 0}
                </span>
              </div>

              <button
                onClick={handleShare}
                className="flex items-center space-x-2 rounded-xl px-4 py-2 text-[#bdaa94] transition-all duration-300 hover:bg-white/[0.05]"
                title={copy.share}
                aria-label={copy.share}
              >
                <Share2 size={20} />
              </button>

              <button
                onClick={handleSave}
                className={`flex items-center space-x-2 rounded-xl px-4 py-2 transition-all duration-300 ${
                  post.saved
                    ? 'bg-[#6a4a1e]/20 text-[#f0d9a5] shadow-lg shadow-[rgba(208,168,91,0.16)]'
                    : 'text-[#bdaa94] hover:bg-white/[0.05]'
                }`}
                title={copy.save}
                aria-label={copy.save}
              >
                <Bookmark
                  size={20}
                  fill={post.saved ? 'currentColor' : 'none'}
                  className={post.saved ? 'animate-pulse' : ''}
                />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="px-4">
        <div className="mb-4 flex items-center space-x-2">
          <Sparkles size={18} className="text-[#d0a85b]" />
          <h3 className="font-bold text-[#f4ece1]">{copy.commentsTitle}</h3>
          <span className="text-sm text-[#8f7b66]">
            ({comments.length})
          </span>
        </div>

        {comments.length === 0 ? (
          <div className="py-12 text-center">
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full border border-white/10 bg-white/[0.04]">
              <MessageSquare size={28} className="text-[#8f7b66]" />
            </div>
            <p className="text-[#8f7b66]">{copy.commentsEmpty}</p>
          </div>
        ) : (
          <div className="space-y-3">
            {comments.map((comment, index) => {
              const commentUser = comment.user || {}

              return (
                <div
                  key={comment.id}
                  className="group relative"
                  style={{ animationDelay: `${index * 50}ms` }}
                >
                  <div className="absolute -inset-0.5 rounded-xl bg-[linear-gradient(135deg,rgba(163,66,36,0.18),rgba(208,168,91,0.14))] opacity-0 blur transition-opacity duration-300 group-hover:opacity-100" />

                  <div className="relative rounded-xl border border-white/10 bg-white/[0.04] p-4 backdrop-blur-sm transition-all duration-300 hover:border-[#d0a85b]/24 hover:bg-white/[0.06]">
                    <div className="flex items-start space-x-3">
                      <div className="h-9 w-9 flex-shrink-0 rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] p-0.5">
                        <div className="flex h-full w-full items-center justify-center rounded-full bg-[#100b0a] text-sm">
                          {commentUser.avatar || '👤'}
                        </div>
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="mb-1.5 flex items-center justify-between">
                          <span className="text-sm font-semibold text-[#f4ece1]">
                            {commentUser.nickname || copy.unknownUser}
                          </span>
                          <span className="text-xs text-[#8f7b66]">
                            {formatTime(comment.createdAt)}
                          </span>
                        </div>
                        <p className="text-sm leading-relaxed text-[#e4d6c8]">
                          {comment.content}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      <div className="fixed bottom-0 left-0 right-0 border-t border-white/10 bg-[#0f0a09]/90 p-4 backdrop-blur-xl">
        <div className="flex items-center space-x-3">
          <div className="relative flex-1">
            <input
              type="text"
              value={commentText}
              onChange={(event) => setCommentText(event.target.value)}
              placeholder={copy.commentPlaceholder}
              className="mystic-input w-full rounded-2xl px-5 py-3 text-sm"
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !submitting) {
                  handleSubmitComment()
                }
              }}
            />
          </div>
          <button
            onClick={handleSubmitComment}
            disabled={submitting || !commentText.trim()}
            className={`rounded-2xl p-3 transition-all duration-300 ${
              commentText.trim()
                ? 'btn-primary-theme text-white hover:scale-105 active:scale-95'
                : 'border border-white/10 bg-white/[0.04] text-[#8f7b66]'
            }`}
            title={copy.sendComment}
            aria-label={copy.sendComment}
          >
            {submitting ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              <Send size={20} />
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
