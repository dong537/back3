import { useState, useEffect } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { ArrowLeft, Share2, ThumbsUp, ThumbsDown, ChevronRight, Flame, BookOpen } from 'lucide-react'
import { toast } from '../components/Toast'

// 十神图标和颜色配置
const shiShenConfig = {
  '偏财': { icon: '💰', color: 'from-amber-400 to-orange-500', bgColor: 'bg-amber-50', textColor: 'text-amber-700' },
  '正财': { icon: '💎', color: 'from-yellow-400 to-amber-500', bgColor: 'bg-yellow-50', textColor: 'text-yellow-700' },
  '正官': { icon: '👔', color: 'from-blue-400 to-indigo-500', bgColor: 'bg-blue-50', textColor: 'text-blue-700' },
  '七杀': { icon: '⚔️', color: 'from-red-400 to-rose-500', bgColor: 'bg-red-50', textColor: 'text-red-700' },
  '正印': { icon: '📚', color: 'from-emerald-400 to-teal-500', bgColor: 'bg-emerald-50', textColor: 'text-emerald-700' },
  '偏印': { icon: '🔮', color: 'from-purple-400 to-violet-500', bgColor: 'bg-purple-50', textColor: 'text-purple-700' },
  '比肩': { icon: '🤝', color: 'from-cyan-400 to-blue-500', bgColor: 'bg-cyan-50', textColor: 'text-cyan-700' },
  '劫财': { icon: '💫', color: 'from-orange-400 to-red-500', bgColor: 'bg-orange-50', textColor: 'text-orange-700' },
  '食神': { icon: '🍀', color: 'from-green-400 to-emerald-500', bgColor: 'bg-green-50', textColor: 'text-green-700' },
  '伤官': { icon: '✨', color: 'from-pink-400 to-rose-500', bgColor: 'bg-pink-50', textColor: 'text-pink-700' },
}

export default function BaziInterpretationDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [interpretation, setInterpretation] = useState(null)
  const [baziData, setBaziData] = useState(null)
  const [helpStatus, setHelpStatus] = useState(null) // 'help' | 'unhelp' | null

  useEffect(() => {
    if (location.state?.interpretation) {
      setInterpretation(location.state.interpretation)
      setBaziData(location.state.baziData)
    } else {
      toast.error('未找到解释数据')
      navigate('/bazi')
    }
  }, [location, navigate])

  const handleShare = () => {
    if (!interpretation) return
    const shareText = `${interpretation.title || '八字解读'}\n${interpretation.basicDef || ''}`
    if (navigator.share) {
      navigator.share({ title: interpretation.title, text: shareText, url: window.location.href }).catch(() => {})
    } else {
      navigator.clipboard.writeText(shareText).then(() => toast.success('已复制到剪贴板')).catch(() => {})
    }
  }

  const handleHelp = (type) => {
    setHelpStatus(type)
    toast.success(type === 'help' ? '感谢您的反馈' : '我们会继续改进')
  }

  if (!interpretation) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-white flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl mb-4">📖</div>
          <div className="text-gray-400">加载中...</div>
        </div>
      </div>
    )
  }

  const config = shiShenConfig[interpretation.shiShen] || shiShenConfig['偏财']

  // 解析mainContent中的各个部分
  const parseMainContent = (content) => {
    if (!content) return { full: '', sections: {} }
    const sections = {}
    const lines = content.split('\n')
    let currentSection = null
    let currentText = []
    
    for (const line of lines) {
      if (line.startsWith('【')) {
        if (currentSection && currentText.length > 0) {
          sections[currentSection] = currentText.join('\n').trim()
        }
        currentSection = line.replace(/【|】/g, '').trim()
        currentText = []
      } else if (line.trim()) {
        currentText.push(line.trim())
      }
    }
    if (currentSection && currentText.length > 0) {
      sections[currentSection] = currentText.join('\n').trim()
    }
    
    // 如果没有解析出分段，返回完整内容
    const full = content.replace(/【[^】]+】/g, '').trim()
    return { full, sections }
  }

  const { full: mainContentFull, sections: mainSections } = parseMainContent(interpretation.mainContent)

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 via-orange-50 to-white pb-20">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-amber-100">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-amber-100 rounded-xl transition-all">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <h1 className="text-lg font-bold text-gray-800">{interpretation.title}</h1>
          <button onClick={handleShare} className="p-2 hover:bg-amber-100 rounded-xl transition-all">
            <Share2 size={20} className="text-gray-500" />
          </button>
        </div>
      </div>

      <div className="px-4">
        {/* 主要内容卡片 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm border border-amber-100">
          {/* 基础定义 */}
          {interpretation.basicDef && (
            <p className="text-gray-600 leading-relaxed mb-6 text-[15px]">
              {interpretation.basicDef}
            </p>
          )}

          {/* 主要内容 */}
          <div className="text-gray-800 leading-relaxed text-[15px] whitespace-pre-line">
            {mainContentFull || interpretation.mainContent}
          </div>

          {/* 生扶状态 */}
          {interpretation.supportContent && (
            <div className="mt-6">
              <p className="text-gray-800 leading-relaxed text-[15px]">
                <span className="font-medium">若处于生扶状态，</span>
                {interpretation.supportContent}
              </p>
            </div>
          )}

          {/* 制约状态 */}
          {interpretation.restrictContent && (
            <div className="mt-4">
              <p className="text-gray-800 leading-relaxed text-[15px]">
                <span className="font-medium">若处于制约状态，</span>
                {interpretation.restrictContent}
              </p>
            </div>
          )}

          {/* 性别差异 */}
          {interpretation.genderDiff && (
            <div className="mt-4">
              <p className="text-gray-800 leading-relaxed text-[15px]">
                {interpretation.genderDiff}
              </p>
            </div>
          )}

          {/* 反馈按钮 */}
          <div className="mt-8 pt-4 border-t border-gray-100 flex items-center justify-center space-x-8">
            <button 
              onClick={() => handleHelp('help')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-full transition-all ${
                helpStatus === 'help' 
                  ? 'bg-green-100 text-green-600' 
                  : 'text-gray-400 hover:text-green-500 hover:bg-green-50'
              }`}
            >
              <ThumbsUp size={18} />
              <span className="text-sm">有帮助</span>
            </button>
            <button 
              onClick={() => handleHelp('unhelp')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-full transition-all ${
                helpStatus === 'unhelp' 
                  ? 'bg-red-100 text-red-600' 
                  : 'text-gray-400 hover:text-red-500 hover:bg-red-50'
              }`}
            >
              <ThumbsDown size={18} />
              <span className="text-sm">无帮助</span>
            </button>
          </div>
        </div>

        {/* 相关解读 */}
        <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm border border-amber-100">
          <h3 className="font-bold text-gray-800 mb-4">相关解读</h3>
          
          <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-2xl p-4 border border-amber-200">
            <div className="flex items-start space-x-3">
              <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-amber-100 to-orange-100 flex items-center justify-center text-xs text-amber-700 font-medium leading-tight text-center p-2 flex-shrink-0">
                2026年最顺星座前三名！！！
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="font-bold text-gray-800 mb-1">2026年最顺星座前三名！！！</h4>
                <p className="text-sm text-gray-600 leading-relaxed line-clamp-2">
                  2026年最顺的星座是天蝎座，其次是狮子座和白羊座，各有不同领...
                </p>
                <div className="flex items-center space-x-2 mt-2 text-xs text-gray-400">
                  <span>🌸 不见花海</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 标签 */}
        {interpretation.tag && (
          <div className="mt-4 flex flex-wrap gap-2">
            {interpretation.tag.split(';').filter(Boolean).map((tag, i) => (
              <span 
                key={i} 
                className="px-3 py-1.5 bg-amber-100 text-amber-700 text-sm rounded-full"
              >
                #{tag}
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
