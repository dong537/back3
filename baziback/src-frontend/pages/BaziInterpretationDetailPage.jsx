import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { ArrowLeft, Share2, ThumbsDown, ThumbsUp } from 'lucide-react'
import { toast } from '../components/Toast'
import { resolvePageLocale, safeText } from '../utils/displayText'

const SHI_SHEN_ALIASES = {
  indirect_wealth: ['偏财'],
  direct_wealth: ['正财'],
  direct_officer: ['正官'],
  seven_killings: ['七杀'],
  direct_resource: ['正印'],
  indirect_resource: ['偏印'],
  peer: ['比肩'],
  rob_wealth: ['劫财'],
  eating_god: ['食神'],
  hurting_officer: ['伤官'],
}

const SHI_SHEN_CONFIG = {
  indirect_wealth: { icon: '💰', color: 'from-amber-400 to-orange-500', textColor: 'text-amber-700', bgColor: 'bg-amber-50' },
  direct_wealth: { icon: '🪙', color: 'from-yellow-300 to-amber-500', textColor: 'text-yellow-700', bgColor: 'bg-yellow-50' },
  direct_officer: { icon: '🧭', color: 'from-stone-400 to-amber-600', textColor: 'text-amber-700', bgColor: 'bg-amber-50' },
  seven_killings: { icon: '⚔️', color: 'from-red-500 to-orange-500', textColor: 'text-red-700', bgColor: 'bg-red-50' },
  direct_resource: { icon: '📘', color: 'from-amber-300 to-yellow-500', textColor: 'text-amber-700', bgColor: 'bg-amber-50' },
  indirect_resource: { icon: '✨', color: 'from-orange-400 to-amber-500', textColor: 'text-orange-700', bgColor: 'bg-orange-50' },
  peer: { icon: '🤝', color: 'from-stone-400 to-orange-500', textColor: 'text-orange-700', bgColor: 'bg-orange-50' },
  rob_wealth: { icon: '🔥', color: 'from-orange-400 to-red-500', textColor: 'text-orange-700', bgColor: 'bg-orange-50' },
  eating_god: { icon: '🌾', color: 'from-yellow-400 to-amber-500', textColor: 'text-yellow-700', bgColor: 'bg-yellow-50' },
  hurting_officer: { icon: '🌟', color: 'from-rose-400 to-orange-500', textColor: 'text-rose-700', bgColor: 'bg-rose-50' },
}

const SHI_SHEN_LABELS = {
  indirect_wealth: { zh: '偏财', en: 'Indirect Wealth' },
  direct_wealth: { zh: '正财', en: 'Direct Wealth' },
  direct_officer: { zh: '正官', en: 'Direct Officer' },
  seven_killings: { zh: '七杀', en: 'Seven Killings' },
  direct_resource: { zh: '正印', en: 'Direct Resource' },
  indirect_resource: { zh: '偏印', en: 'Indirect Resource' },
  peer: { zh: '比肩', en: 'Peer' },
  rob_wealth: { zh: '劫财', en: 'Rob Wealth' },
  eating_god: { zh: '食神', en: 'Eating God' },
  hurting_officer: { zh: '伤官', en: 'Hurting Officer' },
}

const POSITION_ALIASES = {
  year_stem: ['年干'],
  year_branch: ['年支'],
  month_stem: ['月干'],
  month_branch: ['月支'],
  day_stem: ['日干'],
  day_branch: ['日支'],
  hour_stem: ['时干'],
  hour_branch: ['时支'],
}

const POSITION_LABELS = {
  year_stem: { zh: '年干', en: 'Year Stem' },
  year_branch: { zh: '年支', en: 'Year Branch' },
  month_stem: { zh: '月干', en: 'Month Stem' },
  month_branch: { zh: '月支', en: 'Month Branch' },
  day_stem: { zh: '日干', en: 'Day Stem' },
  day_branch: { zh: '日支', en: 'Day Branch' },
  hour_stem: { zh: '时干', en: 'Hour Stem' },
  hour_branch: { zh: '时支', en: 'Hour Branch' },
}

const SCORE_LABELS = {
  overall: { zh: '综合', en: 'Overall' },
  love: { zh: '感情', en: 'Love' },
  career: { zh: '事业', en: 'Career' },
  wealth: { zh: '财富', en: 'Wealth' },
  health: { zh: '健康', en: 'Health' },
  social: { zh: '社交', en: 'Social' },
}

const ADVICE_LABELS = {
  career: { zh: '事业建议', en: 'Career Advice' },
  love: { zh: '感情建议', en: 'Relationship Advice' },
  wealth: { zh: '财富建议', en: 'Wealth Advice' },
  health: { zh: '健康建议', en: 'Health Advice' },
  social: { zh: '社交建议', en: 'Social Advice' },
  study: { zh: '学习建议', en: 'Study Advice' },
  emotion: { zh: '情绪建议', en: 'Emotion Advice' },
  family: { zh: '家庭建议', en: 'Family Advice' },
  careerAdvice: { zh: '事业建议', en: 'Career Advice' },
  loveAdvice: { zh: '感情建议', en: 'Relationship Advice' },
  wealthAdvice: { zh: '财富建议', en: 'Wealth Advice' },
  healthAdvice: { zh: '健康建议', en: 'Health Advice' },
  socialAdvice: { zh: '社交建议', en: 'Social Advice' },
}

function normalizeInterpretation(item) {
  if (!item) return null
  return {
    ...item,
    position: item.position || item.type || '',
    tags: Array.isArray(item.tags) ? item.tags : [],
    scores: item.scores || {},
    advices: item.advices || {},
  }
}

function normalizeAlias(value, aliasMap) {
  if (!value) return ''
  return Object.entries(aliasMap).find(([, aliases]) => aliases.includes(value))?.[0] || value
}

function translateMappedValue(value, map, isEn) {
  if (!value) return ''
  const label = map[value]
  if (!label) return value
  return isEn ? label.en : label.zh
}

function formatAdviceLabel(key, isEn) {
  const label = ADVICE_LABELS[key]
  if (label) return isEn ? label.en : label.zh
  return key
}

export default function BaziInterpretationDetailPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const [interpretation, setInterpretation] = useState(null)
  const [helpStatus, setHelpStatus] = useState(null)
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const text = (zh, en) => (isEn ? en : zh)

  useEffect(() => {
    if (location.state?.interpretation) {
      setInterpretation(normalizeInterpretation(location.state.interpretation))
      return
    }
    toast.error(text('未找到八字解释数据', 'Interpretation data not found'))
    navigate('/bazi')
  }, [location, navigate, isEn])

  const scoreItems = useMemo(() => {
    if (!interpretation?.scores) return []
    return Object.entries(interpretation.scores)
      .filter(([, value]) => value !== undefined && value !== null)
      .map(([key, value]) => {
        const label = SCORE_LABELS[key]
        return {
          key,
          label: label ? (isEn ? label.en : label.zh) : key,
          value,
        }
      })
  }, [interpretation, isEn])

  const shiShenKey = normalizeAlias(interpretation?.shiShen, SHI_SHEN_ALIASES)
  const positionKey = normalizeAlias(interpretation?.position, POSITION_ALIASES)
  const translatedShiShen = translateMappedValue(shiShenKey, SHI_SHEN_LABELS, isEn)
  const translatedPosition = translateMappedValue(positionKey, POSITION_LABELS, isEn)

  const handleShare = () => {
    if (!interpretation) return
    const shareTitle = interpretation.title || text('八字解释', 'Bazi Interpretation')
    const shareText = `${shareTitle}\n${interpretation.basicDef || ''}`

    if (navigator.share) {
      navigator.share({ title: shareTitle, text: shareText, url: window.location.href }).catch(() => {})
      return
    }

    navigator.clipboard
      .writeText(shareText)
      .then(() => toast.success(text('已复制分享内容', 'Copied to clipboard')))
      .catch(() => {})
  }

  if (!interpretation) {
    return <div className="page-shell flex min-h-screen items-center justify-center text-[#8f7b66]">{text('加载中...', 'Loading...')}</div>
  }

  const config = SHI_SHEN_CONFIG[shiShenKey] || SHI_SHEN_CONFIG.indirect_wealth

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button onClick={() => navigate(-1)} className="rounded-xl p-2 transition-colors hover:bg-white/10" aria-label={text('返回', 'Back')}>
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{interpretation.title}</h1>
          <button onClick={handleShare} className="rounded-xl p-2 transition-colors hover:bg-white/10" aria-label={text('分享', 'Share')}>
            <Share2 size={20} className="text-[#bdaa94]" />
          </button>
        </div>
      </div>

      <div className="app-page-shell-narrow">
        <div className="panel mt-4 p-5">
          <div className="mb-5 flex items-center space-x-3">
            <div className={`flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br ${config.color} text-2xl shadow-[0_16px_36px_rgba(163,66,36,0.18)]`}>{config.icon}</div>
            <div>
              <div className="font-bold text-[#f4ece1]">{translatedShiShen || interpretation.shiShen}</div>
              <div className="text-sm text-[#8f7b66]">{translatedPosition || interpretation.position}</div>
            </div>
          </div>

          {safeText(interpretation.basicDef) && <p className="mb-6 text-[15px] leading-relaxed text-[#bdaa94]">{interpretation.basicDef}</p>}
          {safeText(interpretation.mainContent) && <div className="whitespace-pre-line text-[15px] leading-relaxed text-[#f4ece1]">{interpretation.mainContent}</div>}
          {safeText(interpretation.supportContent) && (
            <div className="panel-soft mt-5 px-4 py-3">
              <div className="mb-1 text-sm font-medium text-[#dcb86f]">{text('扶助状态', 'Support State')}</div>
              <p className="text-[15px] leading-relaxed text-[#e4d6c8]">{interpretation.supportContent}</p>
            </div>
          )}
          {safeText(interpretation.restrictContent) && (
            <div className="panel-soft mt-4 border border-[#a34224]/18 bg-[#7a3218]/12 px-4 py-3">
              <div className="mb-1 text-sm font-medium text-[#e19a84]">{text('受制状态', 'Restricted State')}</div>
              <p className="text-[15px] leading-relaxed text-[#e4d6c8]">{interpretation.restrictContent}</p>
            </div>
          )}
          {safeText(interpretation.genderDiff) && (
            <div className="panel-soft mt-4 px-4 py-3">
              <div className="mb-1 text-sm font-medium text-[#dcb86f]">{text('男女差异', 'Gender Difference')}</div>
              <p className="text-[15px] leading-relaxed text-[#e4d6c8]">{interpretation.genderDiff}</p>
            </div>
          )}

          {scoreItems.length > 0 && (
            <div className="mt-6">
              <div className="mb-3 text-sm font-medium text-[#dcb86f]">{text('评分', 'Scores')}</div>
              <div className="grid grid-cols-2 gap-3">
                {scoreItems.map((item) => (
                  <div key={item.key} className="panel-soft px-4 py-3">
                    <div className="text-sm text-[#8f7b66]">{item.label}</div>
                    <div className="mt-1 text-xl font-bold text-[#f0d9a5]">{item.value}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {Object.keys(interpretation.advices || {}).length > 0 && (
            <div className="mt-6 space-y-3">
              {Object.entries(interpretation.advices).filter(([, value]) => value).map(([key, value]) => (
                <div key={key} className="panel-soft px-4 py-3">
                  <div className="mb-1 text-sm font-medium text-[#dcb86f]">{formatAdviceLabel(key, isEn)}</div>
                  <p className="text-[15px] leading-relaxed text-[#e4d6c8]">{value}</p>
                </div>
              ))}
            </div>
          )}

          {interpretation.tags?.length > 0 && (
            <div className="mt-6 flex flex-wrap gap-2">
              {interpretation.tags.map((tag) => (
                <span key={tag} className="mystic-chip normal-case tracking-normal">#{tag}</span>
              ))}
            </div>
          )}

          <div className="mt-8 flex items-center justify-center space-x-8 border-t border-white/10 pt-4">
            <button onClick={() => { setHelpStatus('help'); toast.success(text('感谢你的反馈', 'Thanks for the feedback')) }} className={`flex items-center space-x-2 rounded-full px-4 py-2 ${helpStatus === 'help' ? 'border border-[#d0a85b]/25 bg-[#7a3218]/18 text-[#f0d9a5]' : 'text-[#8f7b66] hover:bg-white/[0.04] hover:text-[#dcb86f]'}`}>
              <ThumbsUp size={18} />
              <span className="text-sm">{text('有帮助', 'Helpful')}</span>
            </button>
            <button onClick={() => { setHelpStatus('unhelp'); toast.success(text('已收到反馈', 'Feedback received')) }} className={`flex items-center space-x-2 rounded-full px-4 py-2 ${helpStatus === 'unhelp' ? 'border border-[#a34224]/25 bg-[#7a3218]/14 text-[#e19a84]' : 'text-[#8f7b66] hover:bg-white/[0.04] hover:text-[#e19a84]'}`}>
              <ThumbsDown size={18} />
              <span className="text-sm">{text('可改进', 'Needs work')}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
