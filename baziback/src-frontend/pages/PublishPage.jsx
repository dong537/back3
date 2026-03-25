import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  AtSign,
  Hash,
  Image,
  Loader2,
  MapPin,
  Send,
  Smile,
  Sparkles,
  X,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toast } from '../components/Toast'
import { communityApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'

const PUBLISH_COPY = {
  'zh-CN': {
    title: '发布动态',
    submit: '发布',
    submitting: '发布中...',
    loginFirst: '请先登录',
    missingContent: '请输入内容',
    contentTooShort: '内容至少需要 10 个字',
    tagLimit: '最多选择 3 个标签',
    publishSuccess: '发布成功',
    publishFailed: '发布失败，请稍后重试',
    addTitle: '添加标题（可选）',
    supportHint: '支持换行和表情',
    tagTitle: '添加话题标签',
    maxThree: '最多 3 个',
    anonymousTitle: '匿名发布',
    anonymousDesc: '其他用户将不会看到你的身份信息',
    toolsTitle: '快捷工具',
    tools: {
      image: '图片',
      mention: '@好友',
      location: '位置',
      emoji: '表情',
    },
    placeholders: {
      share: '分享你的占卜心得、感悟或最新发现...',
      question: '描述你的问题，让大家帮你一起分析...',
      discuss: '发起一个你想认真讨论的话题...',
      tree_hole: '在这里倾诉你的心事，我们都会认真听你说...',
    },
    categories: {
      share: '分享',
      question: '提问',
      discuss: '讨论',
      tree_hole: '树洞',
    },
    tags: [
      '塔罗分享',
      '八字命理',
      '易经占卜',
      '每日运势',
      '感情问题',
      '事业发展',
      '求解惑',
      '新手求助',
    ],
  },
  'en-US': {
    title: 'Create Post',
    submit: 'Post',
    submitting: 'Posting...',
    loginFirst: 'Please sign in first',
    missingContent: 'Please enter some content',
    contentTooShort: 'Content must be at least 10 characters long',
    tagLimit: 'You can select up to 3 tags',
    publishSuccess: 'Post published successfully',
    publishFailed: 'Failed to publish. Please try again later.',
    addTitle: 'Add a title (optional)',
    supportHint: 'Supports line breaks and emoji',
    tagTitle: 'Add topic tags',
    maxThree: 'Up to 3',
    anonymousTitle: 'Post anonymously',
    anonymousDesc: 'Other users will not see your identity',
    toolsTitle: 'Quick tools',
    tools: {
      image: 'Image',
      mention: '@Friend',
      location: 'Location',
      emoji: 'Emoji',
    },
    placeholders: {
      share: 'Share your divination takeaways, reflections, or discoveries...',
      question: 'Describe your question so others can help analyze it...',
      discuss: 'Start a topic you want to discuss in depth...',
      tree_hole: 'Share what is on your mind. We are here to listen...',
    },
    categories: {
      share: 'Share',
      question: 'Question',
      discuss: 'Discuss',
      tree_hole: 'Anonymous',
    },
    tags: [
      'Tarot Sharing',
      'Bazi Reading',
      'Yijing Divination',
      'Daily Fortune',
      'Relationship',
      'Career Growth',
      'Need Guidance',
      'Beginner Help',
    ],
  },
}

const CATEGORY_META = {
  share: { emoji: '✨', gradient: 'from-[#a34224] to-[#e3bf73]' },
  question: { emoji: '❓', gradient: 'from-[#7a3218] to-[#d0a85b]' },
  discuss: { emoji: '💬', gradient: 'from-[#6a4a1e] to-[#b88a3d]' },
  tree_hole: { emoji: '🌙', gradient: 'from-[#5c3320] to-[#a35a34]' },
}

export default function PublishPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = PUBLISH_COPY[locale]
  const [content, setContent] = useState('')
  const [title, setTitle] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('share')
  const [selectedTags, setSelectedTags] = useState([])
  const [isAnonymous, setIsAnonymous] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const categories = useMemo(
    () =>
      Object.entries(copy.categories).map(([id, label]) => ({
        id,
        label,
        ...CATEGORY_META[id],
      })),
    [copy.categories]
  )

  const handleTagToggle = (tag) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags((prev) => prev.filter((item) => item !== tag))
      return
    }

    if (selectedTags.length >= 3) {
      toast.warning(copy.tagLimit)
      return
    }

    setSelectedTags((prev) => [...prev, tag])
  }

  const handleSubmit = async () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      navigate('/login')
      return
    }

    if (!content.trim()) {
      toast.error(copy.missingContent)
      return
    }

    if (content.trim().length < 10) {
      toast.error(copy.contentTooShort)
      return
    }

    setIsSubmitting(true)

    try {
      await communityApi.createPost({
        content: content.trim(),
        title: title.trim() || null,
        category: selectedCategory,
        tags: selectedTags.length > 0 ? selectedTags : null,
        images: null,
        anonymous: isAnonymous,
      })

      toast.success(copy.publishSuccess)
      navigate('/')
    } catch (error) {
      logger.error('Create post failed:', error)
      toast.error(error.message || copy.publishFailed)
    } finally {
      setIsSubmitting(false)
    }
  }

  const contentPlaceholder =
    copy.placeholders[selectedCategory] || copy.placeholders.share

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-white/10"
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || !content.trim()}
            className={`flex items-center space-x-1.5 rounded-xl px-5 py-2 text-sm font-semibold transition-all duration-300 ${
              content.trim()
                ? 'btn-primary-theme text-white hover:opacity-95'
                : 'cursor-not-allowed border border-white/10 bg-white/[0.05] text-[#8f7b66]'
            }`}
          >
            {isSubmitting ? (
              <Loader2 size={16} className="animate-spin" />
            ) : (
              <Send size={16} />
            )}
            <span>{isSubmitting ? copy.submitting : copy.submit}</span>
          </button>
        </div>
      </div>

      <div className="app-page-shell-narrow space-y-4 py-4">
        <div className="scrollbar-hide flex space-x-3 overflow-x-auto pb-2">
          {categories.map((category) => (
            <button
              key={category.id}
              onClick={() => setSelectedCategory(category.id)}
              className={`flex items-center space-x-2 whitespace-nowrap rounded-2xl px-5 py-3 text-sm font-semibold transition-all duration-300 ${
                selectedCategory === category.id
                  ? `bg-gradient-to-r ${category.gradient} text-white shadow-lg`
                  : 'border border-white/10 bg-white/[0.04] text-[#bdaa94] backdrop-blur hover:bg-white/[0.08]'
              }`}
            >
              <span className="text-lg">{category.emoji}</span>
              <span>{category.label}</span>
            </button>
          ))}
        </div>

        <div className="panel p-4">
          <input
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder={copy.addTitle}
            maxLength={50}
            className="mystic-input w-full border-0 bg-transparent px-0 py-0 text-lg font-semibold text-[#f4ece1] placeholder-[#8f7b66] focus:ring-0"
          />
        </div>

        <div className="panel p-4">
          <textarea
            value={content}
            onChange={(event) => setContent(event.target.value)}
            placeholder={contentPlaceholder}
            maxLength={1000}
            className="mystic-input h-48 w-full resize-none border-0 bg-transparent px-0 py-0 leading-relaxed text-[#f4ece1] placeholder-[#8f7b66] focus:ring-0"
          />
          <div className="flex items-center justify-between border-t border-white/10 pt-3">
            <div className="flex items-center space-x-1 text-xs text-[#8f7b66]">
              <Sparkles size={14} />
              <span>{copy.supportHint}</span>
            </div>
            <span
              className={`text-xs font-medium ${
                content.length > 900 ? 'text-[#f08a7b]' : 'text-[#8f7b66]'
              }`}
            >
              {content.length}/1000
            </span>
          </div>
        </div>

        <div className="panel p-4">
          <div className="mb-4 flex items-center space-x-2">
            <div className="mystic-icon-badge h-8 w-8 rounded-xl">
              <Hash size={16} className="text-white" />
            </div>
            <span className="font-semibold text-[#f4ece1]">{copy.tagTitle}</span>
            <span className="rounded-lg border border-white/10 bg-white/[0.05] px-2 py-0.5 text-xs text-[#8f7b66]">
              {copy.maxThree}
            </span>
          </div>

          <div className="flex flex-wrap gap-2">
            {copy.tags.map((tag) => (
              <button
                key={tag}
                onClick={() => handleTagToggle(tag)}
                className={`rounded-xl px-4 py-2 text-sm font-medium transition-all duration-300 ${
                  selectedTags.includes(tag)
                    ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-[0_16px_32px_rgba(163,66,36,0.2)]'
                    : 'border border-white/10 bg-white/[0.04] text-[#bdaa94] hover:bg-white/[0.08]'
                }`}
              >
                #{tag}
              </button>
            ))}
          </div>

          {selectedTags.length > 0 && (
            <div className="mt-4 border-t border-white/10 pt-4">
              <div className="flex flex-wrap gap-2">
                {selectedTags.map((tag) => (
                  <span
                    key={tag}
                    className="flex items-center space-x-1.5 rounded-xl border border-[#d0a85b]/20 bg-[#7a3218]/16 px-4 py-2 text-sm font-medium text-[#f0d9a5]"
                  >
                    <span>#{tag}</span>
                    <button
                      onClick={() => handleTagToggle(tag)}
                      className="transition-colors hover:text-[#fff7eb]"
                    >
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>

        {selectedCategory === 'tree_hole' && (
          <div className="panel p-4">
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center space-x-3">
                <span className="text-3xl">🫥</span>
                <div>
                  <div className="font-semibold text-[#f4ece1]">
                    {copy.anonymousTitle}
                  </div>
                  <div className="text-xs text-[#8f7b66]">
                    {copy.anonymousDesc}
                  </div>
                </div>
              </div>
              <button
                onClick={() => setIsAnonymous((prev) => !prev)}
                className={`h-8 w-14 rounded-full transition-all duration-300 ${
                  isAnonymous
                    ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]'
                    : 'bg-white/[0.10]'
                }`}
              >
                <div
                  className={`h-6 w-6 rounded-full bg-[#fff7eb] shadow-md transition-transform duration-300 ${
                    isAnonymous ? 'translate-x-7' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>
          </div>
        )}

        <div className="panel p-4">
          <div className="mb-4 font-semibold text-[#f4ece1]">
            {copy.toolsTitle}
          </div>
          <div className="flex items-center justify-around">
            <ToolButton icon={Image} label={copy.tools.image} />
            <ToolButton icon={AtSign} label={copy.tools.mention} />
            <ToolButton icon={MapPin} label={copy.tools.location} />
            <ToolButton icon={Smile} label={copy.tools.emoji} />
          </div>
        </div>
      </div>
    </div>
  )
}

function ToolButton({ icon: Icon, label }) {
  return (
    <button className="flex flex-col items-center space-y-1 p-2 text-[#8f7b66] transition-colors hover:text-[#dcb86f]">
      <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-white/10 bg-white/[0.04] transition-colors hover:bg-white/[0.08]">
        <Icon size={22} />
      </div>
      <span className="text-xs">{label}</span>
    </button>
  )
}
