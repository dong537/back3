import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Image, Hash, MapPin, AtSign, Smile, X, Send, Loader2, Sparkles } from 'lucide-react'
import { toast } from '../components/Toast'
import { communityApi } from '../api'
import { useAuth } from '../context/AuthContext'

const categories = [
  { id: 'share', label: '分享', emoji: '✨', gradient: 'from-amber-400 to-orange-500' },
  { id: 'question', label: '提问', emoji: '❓', gradient: 'from-blue-400 to-indigo-500' },
  { id: 'discuss', label: '讨论', emoji: '💬', gradient: 'from-emerald-400 to-teal-500' },
  { id: 'tree_hole', label: '树洞', emoji: '🌳', gradient: 'from-violet-400 to-purple-500' },
]

const suggestedTags = [
  '塔罗分享', '八字命理', '易经占卜', '每日运势', 
  '感情问题', '事业发展', '求解读', '新手求助'
]

export default function PublishPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const [content, setContent] = useState('')
  const [title, setTitle] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('share')
  const [selectedTags, setSelectedTags] = useState([])
  const [isAnonymous, setIsAnonymous] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleTagToggle = (tag) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags(selectedTags.filter(t => t !== tag))
    } else if (selectedTags.length < 3) {
      setSelectedTags([...selectedTags, tag])
    } else {
      toast.warning('最多选择3个标签')
    }
  }

  const handleSubmit = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录')
      navigate('/login')
      return
    }
    if (!content.trim()) {
      toast.error('请输入内容')
      return
    }
    if (content.length < 10) {
      toast.error('内容至少10个字')
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
        anonymous: isAnonymous
      })
      toast.success('发布成功！')
      navigate('/')
    } catch (err) {
      console.error('发布失败:', err)
      toast.error(err.message || '发布失败，请重试')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      {/* 顶部导航 - 玻璃态 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-white/50">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <h1 className="text-lg font-bold text-gray-800">发布动态</h1>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || !content.trim()}
            className={`px-5 py-2 rounded-xl text-sm font-semibold flex items-center space-x-1.5 transition-all duration-300 ${
              content.trim() 
                ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg shadow-indigo-200 hover:shadow-xl hover:scale-105' 
                : 'bg-gray-200 text-gray-400'
            }`}
          >
            {isSubmitting ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
            <span>{isSubmitting ? '发布中...' : '发布'}</span>
          </button>
        </div>
      </div>

      <div className="p-4 space-y-4">
        {/* 分类选择 */}
        <div className="flex space-x-3 overflow-x-auto pb-2 scrollbar-hide">
          {categories.map(cat => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`flex items-center space-x-2 px-5 py-3 rounded-2xl text-sm font-semibold whitespace-nowrap transition-all duration-300 ${
                selectedCategory === cat.id
                  ? `bg-gradient-to-r ${cat.gradient} text-white shadow-lg`
                  : 'bg-white/70 backdrop-blur text-gray-600 border border-white/50 hover:bg-white hover:shadow-md'
              }`}
            >
              <span className="text-lg">{cat.emoji}</span>
              <span>{cat.label}</span>
            </button>
          ))}
        </div>

        {/* 标题输入 */}
        <div className="bg-white/70 backdrop-blur rounded-2xl border border-white/50 p-4">
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="添加标题（可选）"
            className="w-full text-lg font-semibold text-gray-800 placeholder-gray-400 bg-transparent focus:outline-none"
            maxLength={50}
          />
        </div>

        {/* 内容输入 */}
        <div className="bg-white/70 backdrop-blur rounded-2xl border border-white/50 p-4">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder={
              selectedCategory === 'question' 
                ? '描述你的问题，让大家帮你解答...' 
                : selectedCategory === 'tree_hole'
                ? '在这里倾诉你的心事，我们都在听...'
                : '分享你的占卜心得、感悟或发现...'
            }
            className="w-full h-48 resize-none text-gray-800 placeholder-gray-400 bg-transparent focus:outline-none leading-relaxed"
            maxLength={1000}
          />
          <div className="flex items-center justify-between pt-3 border-t border-gray-100/50">
            <div className="flex items-center space-x-1 text-xs text-gray-400">
              <Sparkles size={14} />
              <span>支持换行和表情</span>
            </div>
            <span className={`text-xs font-medium ${content.length > 900 ? 'text-rose-500' : 'text-gray-400'}`}>
              {content.length}/1000
            </span>
          </div>
        </div>

        {/* 标签选择 */}
        <div className="bg-white/70 backdrop-blur rounded-2xl border border-white/50 p-4">
          <div className="flex items-center space-x-2 mb-4">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center">
              <Hash size={16} className="text-white" />
            </div>
            <span className="font-semibold text-gray-800">添加话题标签</span>
            <span className="text-xs text-gray-400 px-2 py-0.5 bg-gray-100 rounded-lg">最多3个</span>
          </div>
          <div className="flex flex-wrap gap-2">
            {suggestedTags.map(tag => (
              <button
                key={tag}
                onClick={() => handleTagToggle(tag)}
                className={`px-4 py-2 rounded-xl text-sm font-medium transition-all duration-300 ${
                  selectedTags.includes(tag)
                    ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-md'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                #{tag}
              </button>
            ))}
          </div>
          {selectedTags.length > 0 && (
            <div className="mt-4 pt-4 border-t border-gray-100/50">
              <div className="flex flex-wrap gap-2">
                {selectedTags.map(tag => (
                  <span key={tag} className="flex items-center space-x-1.5 px-4 py-2 bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-700 rounded-xl text-sm font-medium">
                    <span>#{tag}</span>
                    <button onClick={() => handleTagToggle(tag)} className="hover:text-rose-500 transition-colors">
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* 匿名选项 */}
        {selectedCategory === 'tree_hole' && (
          <div className="bg-white/70 backdrop-blur rounded-2xl border border-white/50 p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <span className="text-3xl">🎭</span>
                <div>
                  <div className="font-semibold text-gray-800">匿名发布</div>
                  <div className="text-xs text-gray-500">其他人将看不到你的身份</div>
                </div>
              </div>
              <button
                onClick={() => setIsAnonymous(!isAnonymous)}
                className={`w-14 h-8 rounded-full transition-all duration-300 ${isAnonymous ? 'bg-gradient-to-r from-indigo-500 to-purple-500' : 'bg-gray-300'}`}
              >
                <div className={`w-6 h-6 bg-white rounded-full shadow-md transform transition-transform duration-300 ${isAnonymous ? 'translate-x-7' : 'translate-x-1'}`} />
              </button>
            </div>
          </div>
        )}

        {/* 工具栏 */}
        <div className="bg-white/70 backdrop-blur rounded-2xl border border-white/50 p-4">
          <div className="flex items-center justify-around">
            <button className="flex flex-col items-center space-y-1 text-gray-500 hover:text-indigo-500 transition-colors p-2">
              <div className="w-12 h-12 rounded-xl bg-gray-100 hover:bg-indigo-100 flex items-center justify-center transition-colors">
                <Image size={22} />
              </div>
              <span className="text-xs">图片</span>
            </button>
            <button className="flex flex-col items-center space-y-1 text-gray-500 hover:text-indigo-500 transition-colors p-2">
              <div className="w-12 h-12 rounded-xl bg-gray-100 hover:bg-indigo-100 flex items-center justify-center transition-colors">
                <AtSign size={22} />
              </div>
              <span className="text-xs">@好友</span>
            </button>
            <button className="flex flex-col items-center space-y-1 text-gray-500 hover:text-indigo-500 transition-colors p-2">
              <div className="w-12 h-12 rounded-xl bg-gray-100 hover:bg-indigo-100 flex items-center justify-center transition-colors">
                <MapPin size={22} />
              </div>
              <span className="text-xs">位置</span>
            </button>
            <button className="flex flex-col items-center space-y-1 text-gray-500 hover:text-indigo-500 transition-colors p-2">
              <div className="w-12 h-12 rounded-xl bg-gray-100 hover:bg-indigo-100 flex items-center justify-center transition-colors">
                <Smile size={22} />
              </div>
              <span className="text-xs">表情</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
