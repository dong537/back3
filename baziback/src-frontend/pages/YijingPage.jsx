import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  ArrowLeft,
  Clock,
  Coins,
  Compass,
  Hash,
  History,
  Image as ImageIcon,
  Share2,
  Shuffle,
  Sparkles,
  Star,
} from 'lucide-react'
import Input, { Textarea } from '../components/Input'
import ThinkingChain from '../components/ThinkingChain'
import HexagramAnimation from '../components/HexagramAnimation'
import WeChatContact from '../components/WeChatContact'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import {
  yijingApi,
  deepseekApi,
  calculationRecordApi,
  unwrapApiData,
} from '../api'
import { logger } from '../utils/logger'
import { useAuth } from '../context/AuthContext'
import { resolvePageLocale, safeArray, safeNumber, safeText } from '../utils/displayText'

const YIJING_COPY = {
  'zh-CN': {
    back: '返回',
    pageTitle: '天机明理',
    pageSubtitle: '64 卦象 · 4 种起卦方式 · AI 智能解读',
    methods: [
      { value: 'time', label: '时间起卦', desc: '以当前时间起卦' },
      { value: 'random', label: '随机起卦', desc: '系统随机生成' },
      { value: 'number', label: '数字起卦', desc: '输入数字起卦' },
      { value: 'coin', label: '铜钱起卦', desc: '模拟摇卦' },
    ],
    chooseMethod: '选择起卦方式',
    chooseMethodDesc: '不同方式适合不同场景，选一个你最顺手的入口即可。',
    questionTitle: '输入你的问题',
    questionDesc: '心诚则灵，尽量把问题写得具体一点。',
    questionPlaceholder: '请输入你想占问的内容，例如：今年的事业运势如何？',
    seedLabel: '输入数字',
    seedPlaceholder: '请输入一个数字',
    start: '开始占卜',
    askQuestion: '请输入占问问题',
    resultTitle: '卦象结果',
    original: '本卦',
    changed: '变卦',
    hexagram: '卦',
    unknown: '未知',
    judgment: '卦辞',
    noJudgment: '暂无卦辞',
    image: '象辞',
    noImage: '暂无象辞',
    sixLineCasting: '六爻装卦',
    originalLines: '本卦六爻',
    changedLines: '变卦六爻',
    lineTexts: '爻辞',
    noLineText: '暂无爻辞',
    sixLineAnalysis: '六爻分析',
    usefulSpirit: '用神信息',
    primaryReference: '首选用神',
    auxReferences: '辅助参考',
    keyPoints: '核心判断要点',
    changeAnalysis: '动变分析',
    aiReading: 'AI 解读',
    aiReason: 'AI易经解读',
    aiFailed: 'AI 解读失败，请稍后重试',
    sceneImage: '卦象场景图',
    sceneImageAction: '生成场景图',
    sceneImageLoading: '场景图生成中...',
    sceneImageFailed: '场景图生成失败，请稍后重试',
    sceneImageEmpty: '当前未生成场景图',
    sceneImagePreview: '场景预览',
    sceneImageSummary: '画面说明',
    sceneImagePrompt: '绘图提示词',
    sceneImageFallback: '当前通道未直接返回图片，已生成可继续绘图的场景方案。',
    sceneImageUnsupported: '当前通道暂不支持直接返回图片',
    favoriteAdded: '已收藏',
    favoriteRemoved: '已取消收藏',
    favoriteFailed: '收藏操作失败',
    copied: '链接已复制',
    copyFailed: '复制失败',
    saved: '记录已保存',
    saveFailed: '保存记录失败',
    shareText: (hexagram, question, url) =>
      `我刚刚完成了一次易经占卜：${hexagram}${question ? `\n问题：${question}` : ''}\n${url}`,
    divinationFailed: '占卜失败，请重试',
    divinationReason: '卦占卜',
    currentPoints: '当前积分',
    getMorePoints: '获取更多积分',
    aiInsufficientPoints: (cost, credits) =>
      `积分不足，AI 解读需要 ${cost} 积分，当前余额：${credits}`,
    aiInsufficientPointsGuest: (cost) => `积分不足，AI 解读需要 ${cost} 积分`,
    spentPoints: (cost) => `已消耗 ${cost} 积分`,
    spendFailed: '积分扣除失败',
    lineLabel: (position) => `第${position}爻`,
    shiLabel: '世',
    yingLabel: '应',
    movingLabel: '动',
    favorite: '收藏',
    share: '分享',
    saveRecord: '保存记录',
    yes: '是',
    no: '否',
    unknownStatus: '未明确',
    pointsSuffix: '积分',
  },
  'en-US': {
    back: 'Back',
    pageTitle: 'I Ching Oracle',
    pageSubtitle: '64 hexagrams · 4 casting methods · AI interpretation',
    methods: [
      { value: 'time', label: 'Time', desc: 'Cast from the current time' },
      { value: 'random', label: 'Random', desc: 'Generated randomly' },
      { value: 'number', label: 'Number', desc: 'Cast from your number' },
      { value: 'coin', label: 'Coins', desc: 'Simulate coin casting' },
    ],
    chooseMethod: 'Choose a casting method',
    chooseMethodDesc: 'Different methods fit different situations. Pick the one that feels most natural to you.',
    questionTitle: 'Enter your question',
    questionDesc: 'Be specific. A clearer question usually leads to a better reading.',
    questionPlaceholder: 'Enter your question, for example: How will my career go this year?',
    seedLabel: 'Enter a number',
    seedPlaceholder: 'Please enter a number',
    start: 'Start Divination',
    askQuestion: 'Please enter your question',
    resultTitle: 'Hexagram Result',
    original: 'Original',
    changed: 'Changed',
    hexagram: ' Hexagram',
    unknown: 'Unknown',
    judgment: 'Judgment',
    noJudgment: 'No judgment yet',
    image: 'Image',
    noImage: 'No image text yet',
    sixLineCasting: 'Six-line Casting',
    originalLines: 'Original lines',
    changedLines: 'Changed lines',
    lineTexts: 'Line Texts',
    noLineText: 'No line text yet',
    sixLineAnalysis: 'Six-line Analysis',
    usefulSpirit: 'Useful Spirit',
    primaryReference: 'Primary reference',
    auxReferences: 'Auxiliary references',
    keyPoints: 'Key points',
    changeAnalysis: 'Change analysis',
    aiReading: 'AI Reading',
    aiReason: 'AI I Ching interpretation',
    aiFailed: 'AI interpretation failed, please try again later',
    sceneImage: 'Scene Image',
    sceneImageAction: 'Generate Scene',
    sceneImageLoading: 'Generating scene image...',
    sceneImageFailed: 'Scene image generation failed. Please try again later.',
    sceneImageEmpty: 'No scene image has been generated yet',
    sceneImagePreview: 'Scene Preview',
    sceneImageSummary: 'Visual Summary',
    sceneImagePrompt: 'Image Prompt',
    sceneImageFallback: 'This channel returned a reusable scene plan instead of a direct image.',
    sceneImageUnsupported: 'This channel does not currently return a direct image',
    favoriteAdded: 'Favorited',
    favoriteRemoved: 'Removed from favorites',
    favoriteFailed: 'Favorite action failed',
    copied: 'Link copied',
    copyFailed: 'Copy failed',
    saved: 'Record saved',
    saveFailed: 'Failed to save record',
    shareText: (hexagram, question, url) =>
      `I just completed a Yijing reading: ${hexagram}${question ? `\nQuestion: ${question}` : ''}\n${url}`,
    divinationFailed: 'Divination failed, please try again',
    divinationReason: 'Yijing reading',
    currentPoints: 'Current credits',
    getMorePoints: 'Get more credits',
    aiInsufficientPoints: (cost, credits) =>
      `Not enough credits. AI reading needs ${cost} credits, current balance: ${credits}`,
    aiInsufficientPointsGuest: (cost) =>
      `Not enough credits. AI reading needs ${cost} credits.`,
    spentPoints: (cost) => `Spent ${cost} credits`,
    spendFailed: 'Failed to deduct credits',
    lineLabel: (position) => `Line ${position}`,
    shiLabel: 'Host',
    yingLabel: 'Guest',
    movingLabel: 'Moving',
    favorite: 'Favorite',
    share: 'Share',
    saveRecord: 'Save Record',
    yes: 'Yes',
    no: 'No',
    unknownStatus: 'Unknown',
    pointsSuffix: ' cr',
  },
}

function extractResultData(result) {
  return {
    original: result?.original || result?.data?.original || null,
    changed: result?.changed || result?.data?.changed || null,
    originalYaos: result?.original_yaos || result?.data?.original_yaos || [],
    changedYaos: result?.changed_yaos || result?.data?.changed_yaos || [],
    changingLines: result?.changing_lines || result?.data?.changing_lines || [],
    liuYaoAnalysis:
      result?.liu_yao_analysis || result?.data?.liu_yao_analysis || null,
  }
}

function readLiuYaoSection(section, keys, separator = '、') {
  if (!section) return ''
  for (const key of keys) {
    const value = section?.[key]
    if (Array.isArray(value)) {
      const text = value.filter(Boolean).join(separator)
      if (text) return text
    } else if (value !== undefined && value !== null && String(value).trim()) {
      return String(value)
    }
  }
  return ''
}

function normalizeHexagramSnapshot(hexagram) {
  if (!hexagram) return null
  return {
    id: hexagram?.id ?? null,
    name: safeText(hexagram?.name),
    chinese: safeText(hexagram?.chinese),
    symbol: safeText(hexagram?.symbol),
    judgment: safeText(hexagram?.judgment),
    image: safeText(hexagram?.image),
    meaning: safeText(hexagram?.meaning),
    keywords: safeArray(hexagram?.keywords).filter(Boolean),
    element: safeText(hexagram?.element),
    season: safeText(hexagram?.season),
    direction: safeText(hexagram?.direction),
    applications:
      hexagram?.applications && typeof hexagram.applications === 'object'
        ? hexagram.applications
        : {},
  }
}

function resolveSceneImageSource(sceneImage) {
  const imageUrl = safeText(sceneImage?.imageUrl, sceneImage?.image_url)
  if (imageUrl) {
    return imageUrl
  }
  const imageBase64 = safeText(sceneImage?.imageBase64, sceneImage?.image_base64)
  if (imageBase64) {
    return imageBase64.startsWith('data:')
      ? imageBase64
      : `data:image/png;base64,${imageBase64}`
  }
  return ''
}

function normalizeSceneImageResult(payload) {
  if (!payload || typeof payload !== 'object') {
    return null
  }

  return {
    ...payload,
    sceneCategory: safeText(payload.sceneCategory, payload.scene_category),
    revisedPrompt: safeText(payload.revisedPrompt, payload.revised_prompt),
    imageBase64: safeText(payload.imageBase64, payload.image_base64),
    imageUrl: safeText(payload.imageUrl, payload.image_url),
    generationMode: safeText(payload.generationMode, payload.generation_mode),
    imageSupported:
      typeof payload.imageSupported === 'boolean'
        ? payload.imageSupported
        : typeof payload.image_supported === 'boolean'
          ? payload.image_supported
          : null,
    visualSummary: safeText(payload.visualSummary, payload.visual_summary),
    negativePrompt: safeText(payload.negativePrompt, payload.negative_prompt),
    displayText: safeText(payload.displayText, payload.display_text),
  }
}

export default function YijingPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const copy = YIJING_COPY[locale] || YIJING_COPY['zh-CN']
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()

  const [question, setQuestion] = useState('')
  const [method, setMethod] = useState('time')
  const [seed, setSeed] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState('')
  const [sceneImageLoading, setSceneImageLoading] = useState(false)
  const [sceneImageResult, setSceneImageResult] = useState(null)
  const [isAnimating, setIsAnimating] = useState(false)
  const [userPoints, setUserPoints] = useState(points.get())

  const methods = useMemo(
    () => [
      { value: 'time', icon: Clock, label: copy.methods[0].label, desc: copy.methods[0].desc },
      { value: 'random', icon: Shuffle, label: copy.methods[1].label, desc: copy.methods[1].desc },
      { value: 'number', icon: Hash, label: copy.methods[2].label, desc: copy.methods[2].desc },
      { value: 'coin', icon: Coins, label: copy.methods[3].label, desc: copy.methods[3].desc },
    ],
    [copy]
  )

  useEffect(() => {
    if (isLoggedIn) refreshCredits()
  }, [isLoggedIn, refreshCredits])

  useEffect(() => {
    const state = location?.state
    if (!state?.fromFavorite) return
    if (typeof state.question === 'string') setQuestion(state.question)
    if (state.result) {
      setResult(state.result)
      setIsAnimating(false)
      setLoading(false)
    }
  }, [location?.state])

  const handleDivination = async () => {
    if (!question.trim()) {
      toast.error(copy.askQuestion)
      return
    }

    setLoading(true)
    setIsAnimating(true)
    setResult(null)
    setAiResult('')
    setSceneImageResult(null)

    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      const response = await yijingApi.quickDivination(question, method, seed)
      const resultData = unwrapApiData(response)

      setTimeout(() => {
        setResult(resultData)
        setIsAnimating(false)
        const original = resultData?.original
        if (!original) return

        historyStorage.add({
          type: 'yijing',
          question,
          method,
          dataId: original.id?.toString() || '',
          summary: `${original.chinese}${copy.hexagram} - ${original.judgment?.substring(0, 30) || ''}...`,
          data: response.data,
        })

        if (isLoggedIn) {
          calculationRecordApi.save({
            recordType: 'yijing',
            recordTitle: `${original.chinese} ${copy.divinationReason}`,
            question,
            summary: original.judgment?.substring(0, 100) || '',
            data: JSON.stringify(resultData?.data || resultData),
          }).catch((error) => logger.warn('auto save yijing record failed', error))
        }
      }, 1500)
    } catch (error) {
      logger.error('Divination error:', error)
      toast.error(error?.response?.data?.error || copy.divinationFailed)
      setIsAnimating(false)
    } finally {
      setLoading(false)
    }
  }

  const handleFavorite = async () => {
    const { original } = extractResultData(result)
    if (!original) return
    try {
      const favorited = await favoritesStorage.toggle({
        type: 'yijing',
        question,
        dataId: original.id?.toString() || '',
        title: `${original.chinese}${copy.hexagram}`,
        summary: original.judgment?.substring(0, 50),
        data: result?.data || result,
      })
      toast.success(favorited ? copy.favoriteAdded : copy.favoriteRemoved)
    } catch (error) {
      logger.error('favorite yijing failed', error)
      toast.error(copy.favoriteFailed)
    }
  }

  const handleShare = () => {
    const { original } = extractResultData(result)
    if (!original) return
    const shareText = copy.shareText(
      safeText(original?.chinese, copy.unknown),
      safeText(question),
      window.location.href
    )
    navigator.clipboard.writeText(shareText)
      .then(() => toast.success(copy.copied))
      .catch(() => toast.error(copy.copyFailed))
  }

  const handleSaveRecord = async () => {
    const { original } = extractResultData(result)
    if (!original) return
    try {
      await calculationRecordApi.save({
        recordType: 'yijing',
        recordTitle: `${original.chinese} ${copy.divinationReason}`,
        question,
        summary: original.judgment?.substring(0, 100) || '',
        data: JSON.stringify(result?.data || result),
      })
      toast.success(copy.saved)
    } catch (error) {
      logger.error('save yijing record failed', error)
      toast.error(copy.saveFailed)
    }
  }

  const handleAIInterpret = async () => {
    if (!result) return
    const cost = POINTS_COST.AI_INTERPRET
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(copy.aiInsufficientPoints(cost, safeNumber(credits, 0)))
        return
      }
    } else if (!points.canSpend(cost)) {
      toast.error(copy.aiInsufficientPointsGuest(cost))
      return
    }

    setAiLoading(true)
    setAiResult('')
    try {
      const { original, changed } = extractResultData(result)
      let prompt = isEn
        ? `You are an expert I Ching interpreter. Please explain this divination in plain text only.\n\nQuestion: ${question}\nOriginal hexagram: ${original?.chinese || ''}\nJudgment: ${original?.judgment || ''}`
        : `你是一位精通易经的传统文化解读顾问，请仅用纯文本说明以下卦象。\n\n占问问题：${question}\n本卦：${original?.chinese || ''}\n卦辞：${original?.judgment || ''}`
      if (changed) {
        prompt += isEn ? `\nChanged hexagram: ${changed.chinese}` : `\n变卦：${changed.chinese}`
      }
      prompt += isEn
        ? `\n\nPlease explain:\n1. The core meaning of the hexagram.\n2. The analysis for the user's question.\n3. Auspicious or inauspicious trend.\n4. Practical suggestions.\n5. Key cautions.`
        : `\n\n请从以下几个方面解读：\n1. 卦象总论\n2. 针对问题的分析\n3. 吉凶趋势\n4. 行动建议\n5. 注意事项`

      const response = await deepseekApi.interpretHexagram(prompt)
      const content = (response.data?.code === 200 && response.data?.data) || response.data?.content || (typeof response.data === 'string' ? response.data : '') || copy.aiFailed

      if (isLoggedIn) {
        const spendResult = await spendCredits(cost, copy.aiReason)
        if (spendResult.success) toast.success(copy.spentPoints(cost))
        else toast.error(safeText(spendResult.message, copy.spendFailed))
      } else {
        const spendResult = points.spend(cost, copy.aiReason)
        if (spendResult.success) {
          setUserPoints(spendResult.newTotal)
          toast.success(copy.spentPoints(cost))
        }
      }

      setAiResult(content)
    } catch (error) {
      logger.error('AI interpret error:', error)
      const message = safeText(error?.message, copy.aiFailed)
      toast.error(message)
      setAiResult(message)
    } finally {
      setAiLoading(false)
    }
  }

  const handleGenerateSceneImage = async () => {
    if (!result) return

    const { original, changed, changingLines } = extractResultData(result)
    if (!original) return

    const interpretation = safeText(
      aiResult,
      [
        safeText(result?.interpretation_hint),
        safeText(original?.judgment),
        safeText(original?.judgmentExplanation),
        safeText(original?.image),
        safeText(original?.imageExplanation),
      ]
        .filter(Boolean)
        .join(isEn ? '. ' : '；')
    )

    setSceneImageLoading(true)
    try {
      const response = await yijingApi.generateSceneImage({
        question,
        method,
        interpretation,
        interpretation_hint: safeText(result?.interpretation_hint),
        changing_lines: safeArray(changingLines),
        original: normalizeHexagramSnapshot(original),
        changed: normalizeHexagramSnapshot(changed),
      })

      const data = normalizeSceneImageResult(unwrapApiData(response))
      logger.info('Yijing scene image payload normalized', {
        generationMode: data?.generationMode,
        hasImageUrl: Boolean(data?.imageUrl),
        hasImageBase64: Boolean(data?.imageBase64),
        imageSupported: data?.imageSupported,
        visualSummaryLength: data?.visualSummary?.length || 0,
        displayTextLength: data?.displayText?.length || 0,
      })
      setSceneImageResult(data)
    } catch (error) {
      logger.error('Generate yijing scene image failed:', error)
      toast.error(safeText(error?.message, copy.sceneImageFailed))
      setSceneImageResult(null)
    } finally {
      setSceneImageLoading(false)
    }
  }

  const needsSeed = method === 'number'
  const { original, changed, originalYaos, changedYaos, changingLines, liuYaoAnalysis } = extractResultData(result)
  const usefulSpirit = liuYaoAnalysis?.['用神信息'] || null
  const changeAnalysis = liuYaoAnalysis?.['动变分析'] || null
  const sceneImageSrc = resolveSceneImageSource(sceneImageResult)

  const renderYaoRow = (yao, idx, isChanged = false) => {
    const pos = yao?.yao_position ?? idx + 1
    const isChanging = Array.isArray(changingLines) && changingLines.includes(pos)
    const isShi = yao?.is_shi === 1 || yao?.is_shi === true
    const isYing = yao?.is_ying === 1 || yao?.is_ying === true

    return (
      <div key={`${isChanged ? 'c' : 'o'}-${pos}`} className={`flex items-center justify-between gap-3 rounded-[18px] border px-3 py-2 ${isChanged ? 'border-[#d0a85b]/24 bg-[#7a3218]/14' : 'border-white/10 bg-white/[0.03]'}`}>
        <div className="w-16 text-xs text-[#bdaa94]">{copy.lineLabel(pos)}</div>
        <div className="flex-1 text-sm text-white">
          <span className="mr-2 font-semibold">{yao?.yao_type || ''}</span>
          <span className="mr-2 text-[#f0d9a5]">{yao?.stem || ''}{yao?.branch || ''}</span>
          <span className="text-[#bdaa94]">{yao?.liu_qin || ''}</span>
          {isShi && <span className="ml-2 rounded-full border border-[#d0a85b]/30 bg-[#7a3218]/20 px-2 py-0.5 text-[10px] text-[#dcb86f]">{copy.shiLabel}</span>}
          {isYing && <span className="ml-2 rounded-full border border-white/10 bg-white/[0.05] px-2 py-0.5 text-[10px] text-[#f4ece1]">{copy.yingLabel}</span>}
          {isChanging && <span className="ml-2 rounded-full border border-[#a34224]/30 bg-[#a34224]/18 px-2 py-0.5 text-[10px] text-[#f0b48d]">{copy.movingLabel}</span>}
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button onClick={() => navigate(-1)} className="rounded-xl p-2 transition-all hover:bg-white/10" aria-label={copy.back}>
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div className="flex items-center space-x-2">
            <Compass className="h-5 w-5 text-[#d0a85b]" />
            <h1 className="font-serif-title text-lg font-bold text-white">{copy.pageTitle}</h1>
          </div>
          <div className="flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1.5">
            <Coins size={14} className="text-[#d0a85b]" />
            <span className="text-sm font-bold text-[#dcb86f]">{isLoggedIn ? credits ?? 0 : userPoints}</span>
          </div>
        </div>
      </div>

      <div className="app-page-shell-narrow pb-20 pt-6">
        <div className="page-hero mb-8">
          <div className="page-hero-inner">
            <div className="page-badge">
              <Compass className="text-theme h-4 w-4" />
              <span>{copy.chooseMethod}</span>
            </div>
            <h2 className="page-title font-serif-title text-white">{copy.pageTitle}</h2>
            <p className="page-subtitle">{copy.pageSubtitle}</p>
          </div>
        </div>

        <div className="panel mb-5 p-5">
          <h3 className="mb-2 text-base font-bold text-white">{copy.chooseMethod}</h3>
          <p className="mb-4 text-xs text-[#bdaa94]">{copy.chooseMethodDesc}</p>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {methods.map(({ value, icon: Icon, label, desc }) => (
              <button key={value} onClick={() => setMethod(value)} className={`group rounded-[24px] border p-4 text-left transition-all hover:scale-[1.02] ${method === value ? 'border-[#d0a85b]/28 bg-[#7a3218]/18 shadow-[0_18px_50px_rgba(0,0,0,0.18)]' : 'border-white/10 bg-white/[0.03] hover:border-[#d0a85b]/22 hover:bg-white/[0.05]'}`}>
                <div className={`mb-3 flex h-10 w-10 items-center justify-center rounded-xl ${method === value ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]' : 'bg-white/[0.06] group-hover:bg-white/[0.1]'}`}>
                  <Icon className={`h-5 w-5 ${method === value ? 'text-white' : 'text-[#bdaa94]'}`} />
                </div>
                <div className={`mb-1 text-sm font-bold ${method === value ? 'text-white' : 'text-[#f4ece1]'}`}>{label}</div>
                <div className="text-xs text-[#8f7b66]">{desc}</div>
              </button>
            ))}
          </div>
        </div>

        <div className="panel mb-5 p-5">
          <h3 className="mb-2 text-base font-bold text-white">{copy.questionTitle}</h3>
          <p className="mb-4 text-xs text-[#bdaa94]">{copy.questionDesc}</p>
          <Textarea placeholder={copy.questionPlaceholder} value={question} onChange={(event) => setQuestion(event.target.value)} rows={3} className="min-h-[110px]" />
          {needsSeed && (
            <Input label={copy.seedLabel} placeholder={copy.seedPlaceholder} value={seed} onChange={(event) => setSeed(event.target.value)} type="number" className="mt-3" />
          )}
        </div>

        <button onClick={handleDivination} disabled={loading || !question.trim()} className="btn-primary-theme mb-5 flex w-full items-center justify-center space-x-2 py-4 text-lg font-bold disabled:cursor-not-allowed disabled:opacity-50">
          <Compass size={20} className={loading ? 'animate-spin' : ''} />
          <span>{copy.start}</span>
        </button>

        {(isAnimating || result) && (
          <HexagramAnimation isGenerating={isAnimating} onComplete={() => {}}>
            {result && !isAnimating && (
              <div className="panel mb-5 p-5">
                <h3 className="mb-4 text-base font-bold text-white">{copy.resultTitle}</h3>
                <div className="mb-5 grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4 text-center">
                    <div className="mb-3 text-5xl">{original?.symbol || '☯'}</div>
                    <div className="mb-1 text-xl font-bold text-white">{safeText(original?.chinese, copy.unknown)}{copy.hexagram}</div>
                    <div className="text-xs font-medium text-[#dcb86f]">{copy.original}</div>
                  </div>
                  {changed && (
                    <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4 text-center">
                      <div className="mb-3 text-5xl">{changed.symbol || '☯'}</div>
                      <div className="mb-1 text-xl font-bold text-white">{safeText(changed.chinese, copy.unknown)}{copy.hexagram}</div>
                      <div className="text-xs font-medium text-[#f0d9a5]">{copy.changed}</div>
                    </div>
                  )}
                </div>

                <div className="mb-4 rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
                  <h4 className="mb-2 text-sm font-bold text-[#dcb86f]">{copy.judgment}</h4>
                  <p className="text-sm leading-relaxed text-white">{safeText(original?.judgment, copy.noJudgment)}</p>
                  {safeText(original?.judgmentExplanation) && <p className="mt-2 text-sm leading-relaxed text-[#bdaa94]">{original.judgmentExplanation}</p>}
                </div>

                <div className="mb-4 rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
                  <h4 className="mb-2 text-sm font-bold text-[#f0d9a5]">{copy.image}</h4>
                  <p className="text-sm leading-relaxed text-white">{safeText(original?.image, copy.noImage)}</p>
                  {safeText(original?.imageExplanation) && <p className="mt-2 text-sm leading-relaxed text-[#bdaa94]">{original.imageExplanation}</p>}
                </div>

                {(originalYaos.length > 0 || changedYaos.length > 0) && (
                  <div className="mb-4">
                    <h4 className="mb-2 text-sm font-bold text-[#dcb86f]">{copy.sixLineCasting}</h4>
                    {originalYaos.length > 0 && (
                      <div className="mb-3">
                        <div className="mb-2 text-xs text-[#8f7b66]">{copy.originalLines}</div>
                        <div className="space-y-2">{originalYaos.map((yao, idx) => renderYaoRow(yao, idx, false))}</div>
                      </div>
                    )}
                    {changedYaos.length > 0 && (
                      <div>
                        <div className="mb-2 text-xs text-[#8f7b66]">{copy.changedLines}</div>
                        <div className="space-y-2">{changedYaos.map((yao, idx) => renderYaoRow(yao, idx, true))}</div>
                      </div>
                    )}
                  </div>
                )}

                {safeArray(original?.lines).length > 0 && (
                  <div className="mb-5">
                    <h4 className="mb-2 text-sm font-bold text-[#f0d9a5]">{copy.lineTexts}</h4>
                    <div className="space-y-3">
                      {safeArray(original?.lines).map((line) => (
                        <div key={line.position} className="rounded-[22px] border border-white/10 bg-white/[0.03] p-4">
                          <div className="mb-2 text-xs text-[#8f7b66]">{copy.lineLabel(line.position)}</div>
                          <div className="text-sm leading-relaxed text-white">{safeText(line.text, copy.noLineText)}</div>
                          {safeText(line.textExplanation) && <div className="mt-2 text-sm leading-relaxed text-[#bdaa94]">{line.textExplanation}</div>}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {liuYaoAnalysis && (
                  <div className="mb-5 space-y-3">
                    <h4 className="text-sm font-bold text-[#dcb86f]">{copy.sixLineAnalysis}</h4>
                    {usefulSpirit && (
                      <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
                        <div className="mb-2 text-xs font-semibold text-[#dcb86f]">{copy.usefulSpirit}</div>
                        <div className="space-y-1 text-sm text-white">
                          {readLiuYaoSection(usefulSpirit, ['首选用神']) && <div>{copy.primaryReference}: {readLiuYaoSection(usefulSpirit, ['首选用神'])}</div>}
                          {readLiuYaoSection(usefulSpirit, ['辅助参考']) && <div>{copy.auxReferences}: {readLiuYaoSection(usefulSpirit, ['辅助参考'], isEn ? '; ' : '、')}</div>}
                          {readLiuYaoSection(usefulSpirit, ['核心判断要点']) && <div>{copy.keyPoints}: {readLiuYaoSection(usefulSpirit, ['核心判断要点'], isEn ? '; ' : '；')}</div>}
                        </div>
                      </div>
                    )}
                    {changeAnalysis && (
                      <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
                        <div className="mb-2 text-xs font-semibold text-[#f0d9a5]">{copy.changeAnalysis}</div>
                        <pre className="whitespace-pre-wrap break-words text-xs text-white">{JSON.stringify(changeAnalysis, null, 2)}</pre>
                      </div>
                    )}
                  </div>
                )}

                <div className="flex flex-wrap gap-3">
                  <button onClick={handleAIInterpret} disabled={aiLoading} className="btn-primary-theme flex flex-1 items-center justify-center space-x-2 py-3 font-bold transition-all hover:opacity-90 disabled:opacity-60">
                    <Sparkles size={18} className={aiLoading ? 'animate-spin' : ''} />
                    <span>{copy.aiReading}</span>
                    <span className="rounded-full bg-white/20 px-2 py-0.5 text-xs">{POINTS_COST.AI_INTERPRET}{copy.pointsSuffix}</span>
                  </button>
                  <button onClick={handleGenerateSceneImage} disabled={sceneImageLoading} className="rounded-[18px] border border-[#d0a85b]/24 bg-[#7a3218]/14 px-4 py-3 text-[#f0d9a5] transition-all hover:bg-[#7a3218]/24 disabled:cursor-not-allowed disabled:opacity-60">
                    <span className="flex items-center justify-center space-x-2 text-sm font-semibold">
                      <ImageIcon size={18} className={sceneImageLoading ? 'animate-pulse' : ''} />
                      <span>{sceneImageLoading ? copy.sceneImageLoading : copy.sceneImageAction}</span>
                    </span>
                  </button>
                  <button onClick={handleFavorite} className="rounded-[18px] border border-[#d0a85b]/24 bg-[#7a3218]/14 p-3 text-[#dcb86f] transition-all hover:bg-[#7a3218]/24" title={copy.favorite} aria-label={copy.favorite}><Star size={18} /></button>
                  <button onClick={handleShare} className="rounded-[18px] border border-white/10 bg-white/[0.04] p-3 text-[#f4ece1] transition-all hover:bg-white/[0.08]" title={copy.share} aria-label={copy.share}><Share2 size={18} /></button>
                  <button onClick={handleSaveRecord} className="rounded-[18px] border border-white/10 bg-white/[0.04] p-3 text-[#f4ece1] transition-all hover:bg-white/[0.08]" title={copy.saveRecord} aria-label={copy.saveRecord}><History size={18} /></button>
                </div>
              </div>
            )}
          </HexagramAnimation>
        )}

        {(aiLoading || aiResult) && (
          <div className="panel mb-5 p-5">
            <h3 className="mb-3 flex items-center text-base font-bold text-white">
              <Sparkles className="mr-2 h-5 w-5 text-[#d0a85b]" />
              {copy.aiReading}
            </h3>
            <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/10 p-4">
              <ThinkingChain isThinking={aiLoading} finalContent={aiResult} />
            </div>
          </div>
        )}

        {(sceneImageLoading || sceneImageResult) && (
          <div className="panel mb-5 p-5">
            <h3 className="mb-4 flex items-center text-base font-bold text-white">
              <ImageIcon className="mr-2 h-5 w-5 text-[#d0a85b]" />
              {copy.sceneImage}
            </h3>

            {sceneImageLoading && !sceneImageResult ? (
              <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/10 p-5 text-sm text-[#f0d9a5]">
                {copy.sceneImageLoading}
              </div>
            ) : (
              <div className="space-y-4">
                <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
                  <div className="mb-3 text-sm font-semibold text-[#f0d9a5]">
                    {copy.sceneImagePreview}
                  </div>
                  {sceneImageSrc ? (
                    <img
                      src={sceneImageSrc}
                      alt={copy.sceneImage}
                      className="w-full rounded-[20px] border border-white/10 object-cover shadow-[0_20px_60px_rgba(0,0,0,0.25)]"
                    />
                  ) : (
                    <div className="rounded-[20px] border border-dashed border-[#d0a85b]/24 bg-[#7a3218]/10 p-5 text-sm leading-6 text-[#bdaa94]">
                      {safeText(
                        sceneImageResult?.displayText,
                        sceneImageResult?.imageSupported === false
                          ? copy.sceneImageUnsupported
                          : copy.sceneImageFallback
                      )}
                    </div>
                  )}
                </div>

                {(safeText(sceneImageResult?.visualSummary) ||
                  safeText(sceneImageResult?.displayText)) && (
                  <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
                    <div className="mb-2 text-sm font-semibold text-[#dcb86f]">
                      {copy.sceneImageSummary}
                    </div>
                    <div className="space-y-2 text-sm leading-6 text-white">
                      {safeText(sceneImageResult?.visualSummary) && (
                        <p>{sceneImageResult.visualSummary}</p>
                      )}
                      {safeText(sceneImageResult?.displayText) && (
                        <p className="text-[#bdaa94]">{sceneImageResult.displayText}</p>
                      )}
                    </div>
                  </div>
                )}

                {safeText(sceneImageResult?.revisedPrompt || sceneImageResult?.prompt) && (
                  <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
                    <div className="mb-2 text-sm font-semibold text-[#f0d9a5]">
                      {copy.sceneImagePrompt}
                    </div>
                    <pre className="whitespace-pre-wrap break-words text-xs leading-6 text-[#f4ece1]">
                      {safeText(sceneImageResult?.revisedPrompt, sceneImageResult?.prompt)}
                    </pre>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {result && <WeChatContact className="mb-5" relatedRecordId={original?.id || null} />}

        <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center space-x-2">
              <Coins size={18} className="text-[#d0a85b]" />
              <span className="text-sm text-[#f0d9a5]">{`${copy.currentPoints}${isEn ? ':' : '：'} ${isLoggedIn ? credits ?? 0 : userPoints}`}</span>
            </div>
            <button onClick={() => navigate('/dashboard')} className="text-sm font-medium text-[#dcb86f] transition-colors hover:text-[#f0d9a5]">{copy.getMorePoints} →</button>
          </div>
        </div>
      </div>
    </div>
  )
}
