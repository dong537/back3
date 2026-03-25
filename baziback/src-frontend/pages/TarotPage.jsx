import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  Sparkles,
  Shuffle,
  Star,
  ArrowLeft,
  Coins,
  X,
  Share2,
  ChevronRight,
  Search,
  History,
} from 'lucide-react'
import ThinkingChain from '../components/ThinkingChain'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import {
  tarotApi,
  deepseekApi,
  calculationRecordApi,
  unwrapApiData,
} from '../api'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import {
  formatLocaleDate,
  resolvePageLocale,
  safeArray,
  safeNumber,
  safeText,
} from '../utils/displayText'
import { buildTarotCardsText, getTarotCardName } from '../utils/tarotText'

const MODAL_STATE_KEY = 'tarot_divination_modal_state'

const TAROT_PAGE_COPY_OVERRIDE = {
  'zh-CN': {
    locale: 'zh-CN',
    back: '返回',
    close: '关闭',
    search: '搜索',
    tabs: [
      { key: 'card', label: '牌阵' },
      { key: 'astro', label: '星图' },
      { key: 'fate', label: '缘分' },
      { key: 'birth', label: '生命' },
      { key: 'chat', label: '聊天' },
    ],
    quickConsults: [
      {
        title: '爱情提问',
        icon: '💞',
        desc: '恋人关系 / 复合 / 婚姻',
        spreadCode: 'LOVE_TRIAD',
        spreadTitle: '爱情三角阵',
      },
      {
        title: '事业学业',
        icon: '🎯',
        desc: '工作发展 / 晋升 / 方向',
        spreadCode: 'PAST_PRESENT_FUTURE',
        spreadTitle: '时间之流',
      },
      {
        title: '财富启示',
        icon: '💰',
        desc: '财务 / 投资 / 规划',
        spreadCode: 'SINGLE',
        spreadTitle: '单牌占卜',
      },
    ],
    beginnerSpreads: [
      {
        title: '单牌',
        desc: '快速指引',
        code: 'SINGLE',
        badge: '免费',
        badgeTone: 'free',
        icon: '🃏',
        cost: 0,
      },
      {
        title: '三牌阵',
        desc: '过去 / 现在 / 未来',
        code: 'PAST_PRESENT_FUTURE',
        badge: '免费',
        badgeTone: 'free',
        icon: '🔮',
        cost: 0,
      },
      {
        title: '恋人阵',
        desc: '感情分析',
        code: 'LOVE_TRIAD',
        badge: '热门',
        badgeTone: 'hot',
        icon: '❤️',
        cost: 0,
      },
      {
        title: '权杖二',
        desc: '关系观望',
        code: 'WANDS_TWO',
        badge: '免费',
        badgeTone: 'free',
        icon: '🪄',
        cost: 0,
      },
      {
        title: '圣杯骑士',
        desc: '情感消息',
        code: 'CUPS_KNIGHT',
        badge: '免费',
        badgeTone: 'free',
        icon: '🥂',
        cost: 0,
      },
      {
        title: '宝剑三',
        desc: '情绪疗愈',
        code: 'SWORDS_THREE',
        badge: '免费',
        badgeTone: 'free',
        icon: '⚔️',
        cost: 0,
      },
    ],
    advancedSpreads: [
      {
        title: '凯尔特十字',
        desc: '深度解读',
        code: 'CELTIC_CROSS',
        badge: '经典',
        badgeTone: 'classic',
        icon: '✴️',
        cost: 20,
      },
      {
        title: '马赛十字',
        desc: '多角度分析',
        code: 'MARSEILLE_CROSS',
        badge: '专业',
        badgeTone: 'pro',
        icon: '✝️',
        cost: 30,
      },
      {
        title: '命运之轮',
        desc: '周期趋势',
        code: 'WHEEL_OF_FORTUNE',
        badge: '专业',
        badgeTone: 'pro',
        icon: '🎡',
        cost: 30,
      },
      {
        title: '双柱六星',
        desc: '年度规划',
        code: 'TWO_PILLARS_SIX_STARS',
        badge: '专业',
        badgeTone: 'pro',
        icon: '✨',
        cost: 50,
      },
      {
        title: '星盘全息',
        desc: '全局洞察',
        code: 'ASTRO_HOLOGRAM',
        badge: '专业',
        badgeTone: 'pro',
        icon: '🌌',
        cost: 50,
      },
      {
        title: '神圣时间线',
        desc: '关键节点',
        code: 'SACRED_TIMELINE',
        badge: '专业',
        badgeTone: 'pro',
        icon: '⏳',
        cost: 50,
      },
    ],
    banners: [
      {
        id: 1,
        title: '每日一牌',
        subtitle: '开启今日能量指引',
        gradient: 'from-[#7f2416] via-[#b84e2b] to-[#d6b77a]',
        icon: '🃏',
      },
      {
        id: 2,
        title: '爱情塔罗',
        subtitle: '解读你的情感密码',
        gradient: 'from-[#6a1f17] via-[#9d4124] to-[#d0a85b]',
        icon: '💖',
      },
      {
        id: 3,
        title: '事业指引',
        subtitle: '为下一步行动做全局观察',
        gradient: 'from-[#3f1e17] via-[#7a3218] to-[#c79548]',
        icon: '🎯',
      },
    ],
    defaultSpreadTitle: '塔罗占卜',
    dailyFallbackTitle: '今日之牌',
    unknownCard: '未知牌',
    upright: '正位',
    reversed: '逆位',
    share: '分享',
    shareSuccess: '分享文案已复制',
    shareFailed: '分享失败，请稍后重试',
    enterQuestion: '请输入你的问题',
    insufficientPoints: (cost, credits) =>
      `积分不足，该牌阵需要 ${cost} 积分，当前余额：${credits}`,
    insufficientPointsGuest: (cost) => `积分不足，该牌阵需要 ${cost} 积分`,
    spendSuccess: (cost) => `已消耗 ${cost} 积分`,
    spendFailed: '积分扣除失败',
    noTarotData: '未获取到塔罗牌数据',
    drawSuccess: '抽牌成功',
    drawFailed: '抽取塔罗牌失败',
    aiInsufficientPoints: (cost, credits) =>
      `积分不足，AI 解读需要 ${cost} 积分，当前余额：${credits}`,
    aiInsufficientPointsGuest: (cost) => `积分不足，AI 解读需要 ${cost} 积分`,
    aiFailed: 'AI 解读失败，请稍后重试',
    aiGenerationFailed: '生成报告失败',
    needDrawFirst: '请先抽牌',
    recordSaved: '记录已保存',
    saveFailed: '保存记录失败',
    favoriteSaved: '已加入收藏',
    favoriteRemoved: '已取消收藏',
    favoriteFailed: '收藏操作失败',
    loginFirst: '请先登录',
    comingSoon: '该牌阵即将上线，敬请期待',
    modalHint: '静下心来，专注于你真正想问的问题。',
    costHint: (cost) => `需要消耗 ${cost} 积分`,
    questionPlaceholder: '请输入你想占卜的问题，例如：我的感情接下来会怎样？',
    drawLoading: '抽牌中...',
    drawButton: '开始抽牌',
    resultTitle: '抽牌结果',
    aiInterpret: 'AI 解读',
    favorite: '收藏',
    saveRecord: '保存记录',
    spreadSpendLabel: (spreadTitle) => `塔罗占卜 - ${spreadTitle}`,
    aiSpendLabel: 'AI塔罗解读',
    creditUnitCompact: '积分',
    todayButton: '查看今日牌卡',
    drawNow: '立即抽取',
    beginnerTitle: '入门牌阵',
    beginnerBadge: '免费',
    advancedTitle: '进阶牌阵',
    advancedBadge: '积分兑换',
    more: '更多',
    currentPoints: '当前积分：',
    getMorePoints: '获取更多积分 →',
    promptIntro: '你是一位专业的塔罗牌解读师，请为用户解读以下塔罗牌阵。',
    promptRulesTitle: '【格式要求】',
    promptRules: [
      '仅输出纯文本，不使用 Markdown 或富文本格式。',
      '不要使用标题、代码块、链接语法或花哨符号。',
      '使用自然段落与换行组织内容，必要时用数字序号。',
    ],
    promptInfoTitle: '【占卜信息】',
    promptQuestionLabel: '用户问题：',
    promptCardsLabel: '抽到的牌：',
    promptRequestTitle: '【解读要求】',
    promptRequests: [
      '1. 先概括整个牌阵的能量与主题。',
      '2. 逐张解释每张牌在当前位置的含义，以及正逆位的影响。',
      '3. 分析牌与牌之间的关系，以及它们共同构成的故事线。',
      '4. 直接回答用户的问题。',
      '5. 给出具体、可执行的建议。',
      '6. 如适合，可给出宽泛的时间参考。',
    ],
    promptClosing: '请用温暖、有洞察力的语气，帮助用户获得更清晰的方向。',
  },
  'en-US': {
    locale: 'en-US',
    back: 'Back',
    close: 'Close',
    search: 'Search',
    tabs: [
      { key: 'card', label: 'Cards' },
      { key: 'astro', label: 'Stars' },
      { key: 'fate', label: 'Fate' },
      { key: 'birth', label: 'Life' },
      { key: 'chat', label: 'Chat' },
    ],
    quickConsults: [
      {
        title: 'Love Reading',
        icon: '💞',
        desc: 'Relationship / reunion / marriage',
        spreadCode: 'LOVE_TRIAD',
        spreadTitle: 'Love Triangle Spread',
      },
      {
        title: 'Career & Study',
        icon: '🎯',
        desc: 'Work growth / promotion / direction',
        spreadCode: 'PAST_PRESENT_FUTURE',
        spreadTitle: 'Flow of Time',
      },
      {
        title: 'Wealth Insight',
        icon: '💰',
        desc: 'Money / investment / planning',
        spreadCode: 'SINGLE',
        spreadTitle: 'Single Card Reading',
      },
    ],
    beginnerSpreads: [
      {
        title: 'Single Card',
        desc: 'Quick guidance',
        code: 'SINGLE',
        badge: 'Free',
        badgeTone: 'free',
        icon: '🃏',
        cost: 0,
      },
      {
        title: 'Three Cards',
        desc: 'Past / Present / Future',
        code: 'PAST_PRESENT_FUTURE',
        badge: 'Free',
        badgeTone: 'free',
        icon: '🔮',
        cost: 0,
      },
      {
        title: 'Love Spread',
        desc: 'Relationship analysis',
        code: 'LOVE_TRIAD',
        badge: 'Hot',
        badgeTone: 'hot',
        icon: '❤️',
        cost: 0,
      },
      {
        title: 'Two of Wands',
        desc: 'Relationship outlook',
        code: 'WANDS_TWO',
        badge: 'Free',
        badgeTone: 'free',
        icon: '🪄',
        cost: 0,
      },
      {
        title: 'Knight of Cups',
        desc: 'Emotional message',
        code: 'CUPS_KNIGHT',
        badge: 'Free',
        badgeTone: 'free',
        icon: '🥂',
        cost: 0,
      },
      {
        title: 'Three of Swords',
        desc: 'Emotional healing',
        code: 'SWORDS_THREE',
        badge: 'Free',
        badgeTone: 'free',
        icon: '⚔️',
        cost: 0,
      },
    ],
    advancedSpreads: [
      {
        title: 'Celtic Cross',
        desc: 'Deep interpretation',
        code: 'CELTIC_CROSS',
        badge: 'Classic',
        badgeTone: 'classic',
        icon: '✴️',
        cost: 20,
      },
      {
        title: 'Marseille Cross',
        desc: 'Multi-angle analysis',
        code: 'MARSEILLE_CROSS',
        badge: 'Pro',
        badgeTone: 'pro',
        icon: '✝️',
        cost: 30,
      },
      {
        title: 'Wheel of Fortune',
        desc: 'Cycle forecast',
        code: 'WHEEL_OF_FORTUNE',
        badge: 'Pro',
        badgeTone: 'pro',
        icon: '🎡',
        cost: 30,
      },
      {
        title: 'Two Pillars Six Stars',
        desc: 'Annual planning',
        code: 'TWO_PILLARS_SIX_STARS',
        badge: 'Pro',
        badgeTone: 'pro',
        icon: '✨',
        cost: 50,
      },
      {
        title: 'Astro Hologram',
        desc: 'Big-picture insight',
        code: 'ASTRO_HOLOGRAM',
        badge: 'Pro',
        badgeTone: 'pro',
        icon: '🌌',
        cost: 50,
      },
      {
        title: 'Sacred Timeline',
        desc: 'Key moments',
        code: 'SACRED_TIMELINE',
        badge: 'Pro',
        badgeTone: 'pro',
        icon: '⏳',
        cost: 50,
      },
    ],
    banners: [
      {
        id: 1,
        title: 'Daily Card',
        subtitle: "Open today's energetic guidance",
        gradient: 'from-[#7f2416] via-[#b84e2b] to-[#d6b77a]',
        icon: '🃏',
      },
      {
        id: 2,
        title: 'Love Tarot',
        subtitle: 'Decode your relationship energy',
        gradient: 'from-[#6a1f17] via-[#9d4124] to-[#d0a85b]',
        icon: '💖',
      },
      {
        id: 3,
        title: 'Career Guidance',
        subtitle: 'A wider reading for your next move',
        gradient: 'from-[#3f1e17] via-[#7a3218] to-[#c79548]',
        icon: '🎯',
      },
    ],
    defaultSpreadTitle: 'Tarot Reading',
    dailyFallbackTitle: "Today's Card",
    unknownCard: 'Unknown Card',
    upright: 'Upright',
    reversed: 'Reversed',
    share: 'Share',
    shareSuccess: 'Share text copied',
    shareFailed: 'Sharing failed. Please try again later.',
    enterQuestion: 'Please enter your question',
    insufficientPoints: (cost, credits) =>
      `Not enough credits. This spread needs ${cost} credits, current balance: ${credits}`,
    insufficientPointsGuest: (cost) =>
      `Not enough credits. This spread needs ${cost} credits.`,
    spendSuccess: (cost) => `Spent ${cost} credits`,
    spendFailed: 'Failed to deduct credits',
    noTarotData: 'No Tarot card data was returned',
    drawSuccess: 'Cards drawn successfully!',
    drawFailed: 'Failed to draw Tarot cards',
    aiInsufficientPoints: (cost, credits) =>
      `Not enough credits. AI reading needs ${cost} credits, current balance: ${credits}`,
    aiInsufficientPointsGuest: (cost) =>
      `Not enough credits. AI reading needs ${cost} credits.`,
    aiFailed: 'AI reading failed. Please try again later.',
    aiGenerationFailed: 'Failed to generate the report',
    needDrawFirst: 'Please draw cards first',
    recordSaved: 'Record saved',
    saveFailed: 'Failed to save record',
    favoriteSaved: 'Added to favorites',
    favoriteRemoved: 'Removed from favorites',
    favoriteFailed: 'Favorite action failed',
    loginFirst: 'Please sign in first',
    comingSoon: 'This spread is coming soon',
    modalHint: 'Center yourself and focus on what you truly want to ask.',
    costHint: (cost) => `${cost} credits required`,
    questionPlaceholder:
      'Enter the question you want to explore, for example: How is my love life unfolding?',
    drawLoading: 'Drawing...',
    drawButton: 'Draw Cards',
    resultTitle: 'Draw Result',
    aiInterpret: 'AI Reading',
    favorite: 'Favorite',
    saveRecord: 'Save Record',
    spreadSpendLabel: (spreadTitle) => `Tarot spread - ${spreadTitle}`,
    aiSpendLabel: 'AI Tarot reading',
    creditUnitCompact: ' cr',
    todayButton: "View today's card",
    drawNow: 'Draw now',
    beginnerTitle: 'Starter Spreads',
    beginnerBadge: 'Free',
    advancedTitle: 'Advanced Spreads',
    advancedBadge: 'Credits',
    more: 'More',
    currentPoints: 'Current credits: ',
    getMorePoints: 'Get more credits →',
    promptIntro:
      'You are a professional Tarot reader. Please interpret the following Tarot spread for the user.',
    promptRulesTitle: '[Formatting Requirements]',
    promptRules: [
      'Reply in plain text only. Do not use Markdown or rich-text formatting.',
      'Do not use headings, code fences, link syntax, or decorative symbols in the answer.',
      'Use natural paragraphs and line breaks. Use numbered sections if needed.',
    ],
    promptInfoTitle: '[Reading Information]',
    promptQuestionLabel: 'User question:',
    promptCardsLabel: 'Drawn cards:',
    promptRequestTitle: '[Interpretation Requests]',
    promptRequests: [
      '1. Give an overview of the spread energy and theme.',
      '2. Explain each card in its current position, including upright or reversed meaning.',
      '3. Describe the relationship between the cards and the larger story they form.',
      '4. Answer the user question directly.',
      '5. Offer practical and specific advice.',
      '6. If appropriate, include a broad timing reference.',
    ],
    promptClosing:
      'Use a warm and insightful tone that helps the user find clarity.',
  },
}
function getTarotPageCopyOverride(locale) {
  return TAROT_PAGE_COPY_OVERRIDE[locale] || TAROT_PAGE_COPY_OVERRIDE['zh-CN']
}

function buildTarotPrompt(question, cardsDesc, copy) {
  return [
    copy.promptIntro,
    '',
    copy.promptRulesTitle,
    ...copy.promptRules,
    '',
    copy.promptInfoTitle,
    `${copy.promptQuestionLabel} ${question}`,
    `${copy.promptCardsLabel} ${cardsDesc}`,
    '',
    copy.promptRequestTitle,
    ...copy.promptRequests,
    '',
    copy.promptClosing,
  ].join('\n')
}

function getBadgeClass(tone) {
  if (tone === 'hot') return 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]'
  if (tone === 'free') return 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'
  if (tone === 'classic') return 'border border-[#d0a85b]/30 bg-[#d0a85b]/12 text-[#dcb86f]'
  if (tone === 'pro')
    return 'border border-[#f3d8a8]/10 bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb]'
  return 'border border-white/10 bg-white/[0.05] text-[#bdaa94]'
}

function normalizeDrawnCard(card, locale, copy, index) {
  const fallbackName = `${copy.unknownCard} ${index + 1}`
  return {
    id: safeText(card?.cardId ?? card?.id ?? card?.index, ''),
    cardId: safeText(card?.cardId ?? card?.id ?? card?.index, ''),
    cardNameCn: safeText(card?.cardNameCn ?? card?.card_name_cn),
    cardNameEn: safeText(card?.cardNameEn ?? card?.card_name_en),
    name: getTarotCardName(card, locale, fallbackName),
    reversed:
      card?.reversed === true ||
      card?.reversed === 'true' ||
      card?.orientation === 'REVERSED' ||
      card?.isReversed === true,
    symbol: safeText(card?.symbol, '🃏'),
    position: safeText(card?.position),
    positionMeaning: safeText(card?.positionMeaning),
  }
}

function buildCardsSummary(cards, locale, fallback, copy) {
  return buildTarotCardsText(cards, locale, {
    fallback,
    includeOrientation: false,
    uprightLabel: copy?.upright,
    reversedLabel: copy?.reversed,
  })
}

function formatCredits(value, copy) {
  return `${safeNumber(value, 0)}${copy.creditUnitCompact}`
}

function DailyDrawModal({ isOpen, onClose, dailyResult, locale, copy }) {
  if (!isOpen || !dailyResult) return null

  const cardName = getTarotCardName(
    dailyResult,
    locale,
    copy.dailyFallbackTitle
  )
  const shareText =
    `${cardName}\n${safeText(dailyResult?.interpretation) || safeText(dailyResult?.meaning) || ''}`.trim()

  const handleShare = async () => {
    try {
      if (navigator.share) {
        await navigator.share({ title: cardName, text: shareText })
      } else {
        await navigator.clipboard.writeText(shareText)
      }
      toast.success(copy.shareSuccess)
    } catch (error) {
      if (error?.name !== 'AbortError') {
        logger.error('Daily draw share failed:', error)
        toast.error(copy.shareFailed)
      }
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-md overflow-y-auto rounded-[32px] border border-white/10 bg-[#120d0c]/95 shadow-[0_24px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center text-[#8f7b66] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>
        <div className="p-6 pt-8">
          <div className="mb-6 flex justify-center">
            <div
              className={`relative ${dailyResult?.isReversed ? 'rotate-180' : ''}`}
            >
              {safeText(dailyResult?.imageUrl) ? (
                <img
                  src={dailyResult.imageUrl}
                  alt={cardName}
                  className="h-72 w-48 rounded-xl object-cover shadow-lg"
                />
              ) : (
                <div className="flex h-72 w-48 items-center justify-center rounded-xl bg-[linear-gradient(135deg,#7f2416_0%,#b84e2b_55%,#d6b77a_100%)] shadow-lg">
                  <span className="text-6xl">
                    {safeText(dailyResult?.symbol, '🃏')}
                  </span>
                </div>
              )}
            </div>
          </div>
          <div className="mb-6 text-center">
            <h2 className="inline-flex items-center gap-2 text-2xl font-bold text-[#f4ece1]">
              {cardName}
              <span
                className={`rounded-full px-3 py-1 text-sm ${dailyResult?.isReversed ? 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]' : 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'}`}
              >
                {dailyResult?.isReversed ? copy.reversed : copy.upright}
              </span>
            </h2>
            <p className="mt-2 text-[#8f7b66]">
              {formatLocaleDate(dailyResult?.date, locale)}
            </p>
          </div>
          <div className="space-y-4 text-sm leading-relaxed text-[#e4d6c8]">
            {safeText(dailyResult?.interpretation) && (
              <p>{safeText(dailyResult?.interpretation)}</p>
            )}
            {safeText(dailyResult?.meaning) && (
              <p>{safeText(dailyResult?.meaning)}</p>
            )}
            {safeText(dailyResult?.advice) && (
              <p className="italic text-[#bdaa94]">
                {safeText(dailyResult?.advice)}
              </p>
            )}
          </div>
          <button
            onClick={handleShare}
            className="mt-6 flex w-full items-center justify-center gap-2 rounded-full border border-white/10 bg-white/[0.04] py-3 text-[#f4ece1] hover:bg-white/[0.08]"
          >
            <Share2 size={18} />
            <span>{copy.share}</span>
          </button>
        </div>
      </div>
    </div>
  )
}

function DivinationModal({
  isOpen,
  onClose,
  spreadCode,
  spreadTitle,
  spreadCost,
  onPointsUpdate,
  authContext,
  initialState,
  onStateChange,
  locale,
  copy,
}) {
  const navigate = useNavigate()
  const [question, setQuestion] = useState(initialState?.question || '')
  const [loading, setLoading] = useState(false)
  const [drawnCards, setDrawnCards] = useState(initialState?.drawnCards || [])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState(initialState?.aiResult || '')
  const [pointsPaid, setPointsPaid] = useState(
    initialState?.pointsPaid || false
  )

  const { isLoggedIn, credits, spendCredits, canSpendCredits } =
    authContext || {}

  useEffect(() => {
    if (isOpen && (drawnCards.length > 0 || question)) {
      onStateChange?.({
        question,
        drawnCards,
        aiResult,
        pointsPaid,
        spreadCode,
        spreadTitle,
        spreadCost,
      })
    }
  }, [
    question,
    drawnCards,
    aiResult,
    pointsPaid,
    isOpen,
    spreadCode,
    spreadTitle,
    spreadCost,
    onStateChange,
  ])

  useEffect(() => {
    if (isOpen && initialState) {
      setQuestion(safeText(initialState.question))
      setDrawnCards(safeArray(initialState.drawnCards))
      setAiResult(safeText(initialState.aiResult))
      setPointsPaid(initialState.pointsPaid === true)
    }
  }, [isOpen, initialState])

  useEffect(() => {
    if (isOpen && !initialState) {
      setQuestion('')
      setDrawnCards([])
      setAiResult('')
      setPointsPaid(false)
    }
  }, [isOpen, spreadCode, initialState])

  if (!isOpen) return null

  const handleDrawCards = async () => {
    const trimmedQuestion = safeText(question)
    if (!trimmedQuestion) {
      toast.error(copy.enterQuestion)
      return
    }

    if (spreadCost > 0 && !pointsPaid) {
      if (isLoggedIn) {
        if (!canSpendCredits(spreadCost)) {
          toast.error(
            copy.insufficientPoints(spreadCost, safeNumber(credits, 0))
          )
          return
        }
        const result = await spendCredits(
          spreadCost,
          copy.spreadSpendLabel(spreadTitle)
        )
        if (!result.success) {
          toast.error(safeText(result.message, copy.spendFailed))
          return
        }
        setPointsPaid(true)
        toast.success(copy.spendSuccess(spreadCost))
      } else {
        if (!points.canSpend(spreadCost)) {
          toast.error(copy.insufficientPointsGuest(spreadCost))
          return
        }
        const result = points.spend(
          spreadCost,
          copy.spreadSpendLabel(spreadTitle)
        )
        if (!result.success) {
          toast.error(copy.spendFailed)
          return
        }
        setPointsPaid(true)
        onPointsUpdate?.(result.newTotal)
        toast.success(copy.spendSuccess(spreadCost))
      }
    }

    setLoading(true)
    setDrawnCards([])
    setAiResult('')
    try {
      const response = await tarotApi.drawCards(spreadCode, trimmedQuestion)
      const resultData = unwrapApiData(response)
      const cards = safeArray(resultData?.cards)
      if (cards.length === 0) {
        throw new Error(copy.noTarotData)
      }

      const formattedCards = cards.map((card, index) =>
        normalizeDrawnCard(card, locale, copy, index)
      )
      const summary = buildCardsSummary(
        formattedCards,
        locale,
        copy.unknownCard,
        copy
      )

      setDrawnCards(formattedCards)
      historyStorage.add({
        type: 'tarot',
        question: trimmedQuestion,
        dataId:
          formattedCards
            .map((card) => getTarotCardName(card, locale, ''))
            .filter(Boolean)
            .join('-') ||
          spreadCode ||
          copy.defaultSpreadTitle,
        summary: `${spreadTitle} - ${summary}`,
        data: {
          question: trimmedQuestion,
          spreadType: spreadCode,
          cards: formattedCards,
        },
      })
      toast.success(copy.drawSuccess)
    } catch (error) {
      logger.error('Draw cards error:', error)
      toast.error(
        safeText(
          error?.response?.data?.message || error?.message,
          copy.drawFailed
        )
      )
    } finally {
      setLoading(false)
    }
  }

  const handleAIInterpret = async () => {
    if (!drawnCards.length) return

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
      const cardsDesc = drawnCards
        .map(
          (card, index) =>
            `${index + 1}. ${getTarotCardName(card, locale, copy.unknownCard)} (${card?.reversed ? copy.reversed : copy.upright})`
        )
        .join(locale === 'en-US' ? '; ' : '、')
      const prompt = buildTarotPrompt(safeText(question), cardsDesc, copy)
      const response = await deepseekApi.interpretHexagram(prompt)

      let aiContent = ''
      if (response.data?.code === 200 && response.data?.data)
        aiContent = safeText(response.data.data)
      else if (typeof response.data === 'string')
        aiContent = safeText(response.data)
      else if (response.data?.content)
        aiContent = safeText(response.data.content)
      aiContent = safeText(aiContent, copy.aiGenerationFailed)

      if (isLoggedIn) {
        const result = await spendCredits(
          cost,
          copy.aiSpendLabel
        )
        if (result.success) toast.success(copy.spendSuccess(cost))
        else toast.error(safeText(result.message, copy.spendFailed))
      } else {
        const result = points.spend(
          cost,
          copy.aiSpendLabel
        )
        if (result.success) {
          onPointsUpdate?.(result.newTotal)
          toast.success(copy.spendSuccess(cost))
        }
      }

      setAiResult(aiContent)
    } catch (error) {
      logger.error('AI interpret error:', error)
      const message = safeText(error?.message, copy.aiFailed)
      toast.error(message)
      setAiResult(message)
    } finally {
      setAiLoading(false)
    }
  }

  const handleCardClick = (cardId) => {
    const target = safeText(cardId)
    if (!target) return

    sessionStorage.setItem(
      MODAL_STATE_KEY,
      JSON.stringify({
        question,
        drawnCards,
        aiResult,
        pointsPaid,
        spreadCode,
        spreadTitle,
        spreadCost,
        isOpen: true,
      })
    )
    navigate(`/tarot/card/${encodeURIComponent(target)}`)
  }

  const handleToggleFavorite = async () => {
    if (!drawnCards.length) {
      toast.warn(copy.needDrawFirst)
      return
    }

    const summary = buildCardsSummary(
      drawnCards,
      locale,
      copy.unknownCard,
      copy
    )
    const item = {
      type: 'tarot',
      question: safeText(question),
      dataId:
        drawnCards
          .map((card) => getTarotCardName(card, locale, ''))
          .filter(Boolean)
          .join('-') ||
        spreadCode ||
        copy.defaultSpreadTitle,
      title: spreadTitle,
      summary: safeText(aiResult, summary),
      data: { question, spreadType: spreadCode, drawnCards, aiResult },
    }

    try {
      const favorited = await favoritesStorage.toggle(item)
      toast.success(favorited ? copy.favoriteSaved : copy.favoriteRemoved)
    } catch (error) {
      logger.error('Toggle favorite failed:', error)
      toast.error(copy.favoriteFailed)
    }
  }

  const handleSaveRecord = async () => {
    if (!drawnCards.length) {
      toast.warn(copy.needDrawFirst)
      return
    }

    try {
      const record = {
        recordType: 'tarot',
        recordTitle: spreadTitle,
        question: safeText(question),
        summary: safeText(
          aiResult,
          buildCardsSummary(drawnCards, locale, copy.unknownCard, copy)
        ),
        data: JSON.stringify({
          question,
          spreadType: spreadCode,
          spreadTitle,
          drawnCards,
          aiResult,
        }),
      }
      await calculationRecordApi.save(record)
      toast.success(copy.recordSaved)
    } catch (error) {
      logger.error('Save record failed:', error)
      toast.error(copy.saveFailed)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-[32px] border border-white/10 bg-[#120d0c]/95 shadow-[0_24px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center text-[#8f7b66] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>
        <div className="p-6">
          <div className="mb-6 text-center">
            <h2 className="font-serif-title text-xl font-bold text-[#f4ece1]">{spreadTitle}</h2>
            <p className="mt-1 text-sm text-[#8f7b66]">{copy.modalHint}</p>
            {spreadCost > 0 && (
              <div className="mt-2 inline-flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1">
                <Coins size={14} className="text-[#d0a85b]" />
                <span className="text-sm text-[#dcb86f]">
                  {copy.costHint(spreadCost)}
                </span>
              </div>
            )}
          </div>

          <div className="mb-5">
            <textarea
              placeholder={copy.questionPlaceholder}
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              rows={3}
              className="w-full resize-none rounded-[20px] border border-white/10 bg-white/[0.04] p-4 text-[#f4ece1] placeholder-[#8f7b66] focus:border-[#a34224]/40 focus:outline-none focus:ring-2 focus:ring-[#a34224]/30"
            />
          </div>

          <button
            onClick={handleDrawCards}
            disabled={loading || !safeText(question)}
            className="btn-primary-theme mb-5 flex w-full items-center justify-center space-x-2 py-3 font-bold transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Shuffle size={18} className={loading ? 'animate-spin' : ''} />
            <span>
              {loading
                ? copy.drawLoading
                : spreadCost > 0
                  ? `${copy.drawButton} (${formatCredits(spreadCost, copy)})`
                  : copy.drawButton}
            </span>
          </button>

          {drawnCards.length > 0 && (
            <div className="mb-5 rounded-[28px] border border-white/10 bg-white/[0.04] p-4">
              <h3 className="mb-3 text-base font-bold text-[#f4ece1]">
                {copy.resultTitle}
              </h3>
              <div
                className={`mb-4 grid gap-3 ${drawnCards.length === 1 ? 'mx-auto max-w-[150px] grid-cols-1' : drawnCards.length <= 3 ? 'grid-cols-3' : 'grid-cols-2 sm:grid-cols-3'}`}
              >
                {drawnCards.map((card, index) => (
                  <div
                    key={`${card.id || card.name}-${index}`}
                    onClick={() =>
                      handleCardClick(card.id || card.cardId || card.name)
                    }
                    className="cursor-pointer rounded-[20px] border border-white/10 bg-white/[0.04] p-3 text-center transition-all hover:border-[#d0a85b]/24 hover:bg-white/[0.08]"
                  >
                    <div
                      className={`mb-2 text-4xl ${card.reversed ? 'rotate-180' : ''}`}
                    >
                      {safeText(card.symbol, '🃏')}
                    </div>
                    <div className="mb-1 text-sm font-medium text-[#f4ece1]">
                      {getTarotCardName(card, locale, copy.unknownCard)}
                    </div>
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs ${card.reversed ? 'border border-[#a34224]/30 bg-[#a34224]/18 text-[#f0b48d]' : 'border border-[#d0a85b]/24 bg-[#7a3218]/14 text-[#f0d9a5]'}`}
                    >
                      {card.reversed ? copy.reversed : copy.upright}
                    </span>
                  </div>
                ))}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={handleAIInterpret}
                  disabled={aiLoading}
                  className="btn-primary-theme flex flex-1 items-center justify-center space-x-2 py-2.5 text-sm font-medium transition-all hover:opacity-90"
                >
                  <Sparkles
                    size={16}
                    className={aiLoading ? 'animate-spin' : ''}
                  />
                  <span>{copy.aiInterpret}</span>
                  <span className="rounded-full bg-white/20 px-1.5 py-0.5 text-xs">
                    {formatCredits(POINTS_COST.AI_INTERPRET, copy)}
                  </span>
                </button>
                <button
                  onClick={handleToggleFavorite}
                  className="rounded-[18px] border border-[#d0a85b]/24 bg-[#7a3218]/14 p-2.5 text-[#dcb86f] transition-all hover:bg-[#7a3218]/24"
                  title={copy.favorite}
                  aria-label={copy.favorite}
                >
                  <Star size={16} />
                </button>
                <button
                  onClick={handleSaveRecord}
                  className="rounded-[18px] border border-white/10 bg-white/[0.04] p-2.5 text-[#f4ece1] transition-all hover:bg-white/[0.08]"
                  title={copy.saveRecord}
                  aria-label={copy.saveRecord}
                >
                  <History size={16} />
                </button>
              </div>
            </div>
          )}

          {safeText(aiResult) && (
            <div className="rounded-[28px] border border-[#d0a85b]/20 bg-[#7a3218]/10 p-4">
              <ThinkingChain
                isThinking={aiLoading}
                finalContent={aiResult}
                lightMode={false}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default function TarotPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = getTarotPageCopyOverride(locale)
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } =
    useAuth()
  const [activeTab, setActiveTab] = useState('card')
  const [userPoints, setUserPoints] = useState(points.get())
  const [dailyDrawLoading, setDailyDrawLoading] = useState(false)
  const [dailyResult, setDailyResult] = useState(null)
  const [showDailyModal, setShowDailyModal] = useState(false)
  const [hasDrawnToday, setHasDrawnToday] = useState(false)
  const [showDivinationModal, setShowDivinationModal] = useState(false)
  const [selectedSpread, setSelectedSpread] = useState(null)
  const [modalInitialState, setModalInitialState] = useState(null)
  const [bannerIndex, setBannerIndex] = useState(0)

  const banners = copy.banners
  const pointsDisplay = isLoggedIn
    ? safeNumber(credits, 0)
    : safeNumber(userPoints, 0)

  useEffect(() => {
    if (isLoggedIn) refreshCredits()
  }, [isLoggedIn, refreshCredits])

  useEffect(() => {
    const savedState = sessionStorage.getItem(MODAL_STATE_KEY)
    if (!savedState) return

    try {
      const state = JSON.parse(savedState)
      if (state?.isOpen && safeArray(state?.drawnCards).length > 0) {
        setSelectedSpread({
          code: state.spreadCode,
          title: safeText(state.spreadTitle, copy.defaultSpreadTitle),
          cost: safeNumber(state.spreadCost, 0),
        })
        setModalInitialState({
          ...state,
          drawnCards: safeArray(state.drawnCards),
          question: safeText(state.question),
          aiResult: safeText(state.aiResult),
        })
        setShowDivinationModal(true)
      }
      sessionStorage.removeItem(MODAL_STATE_KEY)
    } catch (error) {
      logger.error('Failed to restore Tarot modal state:', error)
      sessionStorage.removeItem(MODAL_STATE_KEY)
    }
  }, [location, copy.defaultSpreadTitle])

  useEffect(() => {
    const timer = setInterval(() => {
      setBannerIndex((prev) => (prev + 1) % banners.length)
    }, 4000)
    return () => clearInterval(timer)
  }, [banners.length])

  useEffect(() => {
    let cancelled = false

    const loadTodayDraw = async () => {
      try {
        const token = sessionStorage.getItem('token')
        if (!token) {
          if (!cancelled) {
            setDailyResult(null)
            setHasDrawnToday(false)
          }
          return
        }

        const response = await fetch('/api/tarot/daily-draw', {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-Language': locale,
            'Accept-Language': locale,
          },
        })
        const data = await response.json()
        if (cancelled) return

        if (data.code === 200 && data.data) {
          setDailyResult(data.data)
          setHasDrawnToday(true)
        } else {
          setDailyResult(null)
          setHasDrawnToday(false)
        }
      } catch (error) {
        logger.error('Check today draw error:', error)
      }
    }

    loadTodayDraw()

    return () => {
      cancelled = true
    }
  }, [isLoggedIn, locale])

  const handleModalStateChange = (state) => {
    if (state && safeArray(state.drawnCards).length > 0) {
      sessionStorage.setItem(
        MODAL_STATE_KEY,
        JSON.stringify({ ...state, isOpen: true })
      )
    }
  }

  const handleCloseModal = () => {
    setShowDivinationModal(false)
    setModalInitialState(null)
    sessionStorage.removeItem(MODAL_STATE_KEY)
  }

  const handleDailyDraw = async () => {
    const token = sessionStorage.getItem('token')
    if (!token) {
      toast.error(copy.loginFirst)
      navigate('/login')
      return
    }

    if (hasDrawnToday && dailyResult) {
      setShowDailyModal(true)
      return
    }

    setDailyDrawLoading(true)
    try {
      const response = await fetch('/api/tarot/daily-draw', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Language': locale,
          'Accept-Language': locale,
        },
      })
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setDailyResult(data.data)
        setHasDrawnToday(true)
        setShowDailyModal(true)
        toast.success(copy.drawSuccess)
      } else {
        toast.error(safeText(data?.message, copy.drawFailed))
      }
    } catch (error) {
      logger.error('Daily draw error:', error)
      toast.error(copy.drawFailed)
    } finally {
      setDailyDrawLoading(false)
    }
  }

  const handleSpreadClick = (spread, isAdvanced = false) => {
    if (!safeText(spread?.code)) {
      toast.info(copy.comingSoon)
      return
    }

    if (isAdvanced && safeNumber(spread?.cost, 0) > 0) {
      if (isLoggedIn) {
        if (!canSpendCredits(spread.cost)) {
          toast.error(
            copy.insufficientPoints(spread.cost, safeNumber(credits, 0))
          )
          return
        }
      } else if (!points.canSpend(spread.cost)) {
        toast.error(copy.insufficientPointsGuest(spread.cost))
        return
      }
    }

    setSelectedSpread({ ...spread, isAdvanced })
    setModalInitialState(null)
    setShowDivinationModal(true)
  }

  const handleQuickConsult = (consult) => {
    setSelectedSpread({
      code: consult.spreadCode,
      title: consult.spreadTitle,
      cost: 0,
      icon: consult.icon,
    })
    setModalInitialState(null)
    setShowDivinationModal(true)
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-40 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between gap-3 py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-all hover:bg-white/10"
            title={copy.back}
            aria-label={copy.back}
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <div className="scrollbar-hide mx-2 flex flex-1 items-center justify-center space-x-5 overflow-x-auto whitespace-nowrap">
            {copy.tabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`mystic-tab ${activeTab === tab.key ? 'mystic-tab-active' : ''}`}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <div className="flex items-center space-x-2">
            <button
              className="rounded-xl p-2 transition-all hover:bg-white/10"
              title={copy.search}
              aria-label={copy.search}
            >
              <Search size={20} className="text-[#bdaa94]" />
            </button>
            <div className="flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1.5">
              <Coins size={14} className="text-[#d0a85b]" />
              <span className="text-sm font-bold text-[#dcb86f]">
                {pointsDisplay}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="app-page-shell pb-24 pt-4">
        <div className="page-hero mb-5">
          <div className="page-hero-inner !max-w-none !px-5 !py-5 sm:!p-6">
          <div
            className="flex transition-transform duration-500 ease-out"
            style={{ transform: `translateX(-${bannerIndex * 100}%)` }}
          >
            {banners.map((banner) => (
              <div
                key={banner.id}
                className={`flex min-h-[140px] w-full flex-shrink-0 cursor-pointer flex-col items-start justify-between gap-4 bg-gradient-to-r p-5 sm:min-h-[140px] sm:flex-row sm:items-center sm:p-6 ${banner.gradient}`}
                onClick={handleDailyDraw}
              >
                <div>
                  <h3 className="mb-1 text-xl font-bold text-white">
                    {banner.title}
                  </h3>
                  <p className="text-sm text-white/80">{banner.subtitle}</p>
                  <button
                    disabled={dailyDrawLoading}
                    className="mt-3 rounded-full bg-white/20 px-4 py-1.5 text-sm font-medium text-white backdrop-blur transition-all hover:bg-white/30 disabled:opacity-70"
                  >
                    {hasDrawnToday ? copy.todayButton : copy.drawNow}
                  </button>
                </div>
                <span className="text-6xl opacity-80">{banner.icon}</span>
              </div>
            ))}
          </div>
          <div className="absolute bottom-3 left-1/2 flex -translate-x-1/2 space-x-1.5">
            {banners.map((banner, idx) => (
              <button
                key={banner.id}
                onClick={() => setBannerIndex(idx)}
                className={`h-2 w-2 rounded-full transition-all ${idx === bannerIndex ? 'w-4 bg-white' : 'bg-white/50'}`}
              />
            ))}
          </div>
        </div>
        </div>

        <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          {copy.quickConsults.map((item) => (
            <button
              key={item.title}
              onClick={() => handleQuickConsult(item)}
              className="group rounded-[24px] border border-white/10 bg-white/[0.04] p-4 text-left transition-all hover:border-[#d0a85b]/24 hover:bg-white/[0.08]"
            >
              <span className="mb-2 block text-3xl transition-transform group-hover:scale-110">
                {item.icon}
              </span>
              <h4 className="mb-0.5 text-sm font-bold text-[#f4ece1]">
                {item.title}
              </h4>
              <p className="text-xs leading-tight text-[#bdaa94]">{item.desc}</p>
            </button>
          ))}
        </div>

        <div className="mb-6">
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <div className="h-5 w-1 rounded-full bg-[linear-gradient(180deg,#a34224_0%,#e3bf73_100%)]"></div>
              <h3 className="font-bold text-[#f4ece1]">{copy.beginnerTitle}</h3>
              <span className="mystic-chip normal-case tracking-normal">
                {copy.beginnerBadge}
              </span>
            </div>
            <button className="flex items-center text-sm text-[#bdaa94] transition-colors hover:text-[#dcb86f]">
              {copy.more} <ChevronRight size={16} />
            </button>
          </div>
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
            {copy.beginnerSpreads.map((spread) => (
              <button
                key={spread.title}
                onClick={() => handleSpreadClick(spread, false)}
                className={`group relative overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.04] p-3 text-left transition-all ${!spread.code ? 'opacity-60' : 'hover:border-[#d0a85b]/24 hover:bg-white/[0.08]'}`}
              >
                {safeText(spread.badge) && (
                  <span
                    className={`absolute right-2 top-2 rounded-md px-1.5 py-0.5 text-xs font-medium ${getBadgeClass(spread.badgeTone)}`}
                  >
                    {spread.badge}
                  </span>
                )}
                <span className="mb-2 block text-2xl transition-transform group-hover:scale-110">
                  {spread.icon}
                </span>
                <h4 className="text-sm font-bold text-[#f4ece1]">
                  {spread.title}
                </h4>
                <p className="mt-0.5 text-xs text-[#bdaa94]">{spread.desc}</p>
              </button>
            ))}
          </div>
        </div>

        <div className="mb-6">
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <div className="h-5 w-1 rounded-full bg-[linear-gradient(180deg,#7a3218_0%,#d0a85b_100%)]"></div>
              <h3 className="font-bold text-[#f4ece1]">{copy.advancedTitle}</h3>
              <span className="flex items-center rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-2 py-0.5 text-xs text-[#f0d9a5]">
                <Coins size={10} className="mr-1" />
                {copy.advancedBadge}
              </span>
            </div>
            <button className="flex items-center text-sm text-[#bdaa94] transition-colors hover:text-[#dcb86f]">
              {copy.more} <ChevronRight size={16} />
            </button>
          </div>
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
            {copy.advancedSpreads.map((spread) => (
              <button
                key={spread.title}
                onClick={() => handleSpreadClick(spread, true)}
                className={`group relative overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.04] p-3 text-left transition-all ${!spread.code ? 'opacity-60' : 'hover:border-[#d0a85b]/24 hover:bg-white/[0.08]'}`}
              >
                {safeText(spread.badge) && (
                  <span
                    className={`absolute right-2 top-2 rounded-md px-1.5 py-0.5 text-xs font-medium ${getBadgeClass(spread.badgeTone)}`}
                  >
                    {spread.badge}
                  </span>
                )}
                <span className="mb-2 block text-2xl transition-transform group-hover:scale-110">
                  {spread.icon}
                </span>
                <h4 className="text-sm font-bold text-[#f4ece1]">
                  {spread.title}
                </h4>
                <p className="mt-0.5 text-xs text-[#bdaa94]">{spread.desc}</p>
                {safeNumber(spread.cost, 0) > 0 && (
                  <div className="mt-1 flex items-center text-xs text-[#dcb86f]">
                    <Coins size={10} className="mr-1" />
                    <span>{formatCredits(spread.cost, copy)}</span>
                  </div>
                )}
              </button>
            ))}
          </div>
        </div>

        <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Coins size={18} className="text-[#d0a85b]" />
              <span className="text-sm text-[#f0d9a5]">
                {copy.currentPoints}
                {pointsDisplay}
              </span>
            </div>
            <button
              onClick={() => navigate('/dashboard')}
              className="text-sm font-medium text-[#dcb86f] transition-colors hover:text-[#f0d9a5]"
            >
              {copy.getMorePoints}
            </button>
          </div>
        </div>
      </div>

      <DailyDrawModal
        isOpen={showDailyModal}
        onClose={() => setShowDailyModal(false)}
        dailyResult={dailyResult}
        locale={locale}
        copy={copy}
      />

      <DivinationModal
        isOpen={showDivinationModal}
        onClose={handleCloseModal}
        spreadCode={selectedSpread?.code}
        spreadTitle={safeText(selectedSpread?.title, copy.defaultSpreadTitle)}
        spreadCost={safeNumber(selectedSpread?.cost, 0)}
        onPointsUpdate={setUserPoints}
        authContext={{ isLoggedIn, credits, spendCredits, canSpendCredits }}
        initialState={modalInitialState}
        onStateChange={handleModalStateChange}
        locale={locale}
        copy={copy}
      />
    </div>
  )
}

