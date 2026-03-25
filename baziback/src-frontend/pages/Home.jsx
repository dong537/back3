import { useState, useEffect, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  User,
  ChevronRight,
  BookOpen,
  Calendar,
  Brain,
  Gift,
  Star,
  Heart,
  Briefcase,
  DollarSign,
  Activity,
  GraduationCap,
  Users,
  Sparkles,
  Clock,
  MapPin,
  CheckCircle2,
  XCircle,
  Trophy,
  Zap,
  ArrowRight,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import CheckinProgress from '../components/CheckinProgress'
import { checkin } from '../utils/referral'
import { historyStorage } from '../utils/storage'
import PangleCarouselAd from '../components/PangleCarouselAd'
import { pangleConfig } from '../config/pangle'
import useAppLocale from '../hooks/useAppLocale'
import LanguageToggleButton from '../components/LanguageToggleButton'
import { safeArray, safeNumber, safeText } from '../utils/displayText'

const DAILY_FORTUNE_CACHE_PREFIX = 'home_daily_fortune_detail'

const PANEL_CLASS =
  'relative overflow-hidden rounded-[30px] border border-white/10 bg-[#140f0f]/72 shadow-[0_22px_80px_rgba(0,0,0,0.35)] backdrop-blur-2xl'

const DISPLAY_FONT_ZH = {
  fontFamily: "'Noto Serif SC', 'Songti SC', 'STKaiti', 'KaiTi', serif",
}

const DISPLAY_FONT_EN = {
  fontFamily: "Georgia, 'Times New Roman', serif",
}

const CJK_TEXT_PATTERN = /[\u3400-\u9fff]/
const LATIN_TEXT_PATTERN = /[A-Za-z]{3,}/

const HOME_COPY = {
  'zh-CN': {
    greetingBack: '欢迎归位',
    greetingLogin: '登录后开启今日天机',
    heroEyebrow: '东方玄学首页',
    heroTitle: '观星象，排命局，先看今日气场',
    heroDescription:
      '把八字、易经、塔罗与 AI 解读收束进一张更有仪式感的首页，用主视觉与分栏卡片的现代版式重构内容节奏，同时保留古意与神秘感。',
    layoutBadge: '首屏叙事新版',
    sectionEyebrow: '玄序',
    entryLabel: '入口',
    enterLabel: '进入',
    heroPrimary: '进入八字排盘',
    heroSecondary: '查看易经占问',
    heroPrimaryMeta: '命盘主轴',
    heroSecondaryMeta: '卦象择势',
    scoreMetric: '今日势能',
    timeMetric: '吉时',
    directionMetric: '旺位',
    focusMetric: '主运',
    creditsLabel: '灵感值',
    checkin: '今日签到',
    checkedIn: '今日已签',
    loginFirst: '请先登录后签到',
    languageLabel: '切换 English',
    awaitingData: '待生成',
    overallFortune: '综合运势',
    fortuneTitle: '今日天机',
    fortuneMeta: '把运势、宜忌与幸运元素收成一张可快速扫读的总览卡。',
    fortuneEmptyTitle: '运势尚未显化',
    fortuneEmptyDesc:
      '当前还没有拿到今日运势数据，但首页布局已经预留好位置，等接口返回后会自动补全。',
    ritualTitle: '今日仪轨',
    ritualMeta: '借鉴热门首页的信息节奏，再换成更贴近东方命理的叙事语境。',
    luckyElements: '幸运坐标',
    color: '颜色',
    number: '数字',
    direction: '方位',
    time: '时段',
    doToday: '今日宜',
    avoidToday: '今日忌',
    overallAdvice: '一句总述',
    keywordTitle: '今日关键字',
    featureSectionTitle: '四大玄学入口',
    featureSectionMeta: '把高频功能放回第一屏，让中文环境下的浏览与选择都更顺手。',
    featureLabels: {
      bazi: '八字命盘',
      tarot: '塔罗抽牌',
      yijing: '易经起卦',
      ai: 'AI 解读',
    },
    featureDescriptions: {
      bazi: '以出生时辰为轴，拆解事业、财运、关系与流年走势。',
      tarot: '适合情绪、关系与选择题，用牌面直觉切入问题核心。',
      yijing: '以问入卦，快速判断当下局势与行动时机。',
      ai: '把零散困惑整理成结构化提问，获得即时分析。',
    },
    featureSignals: {
      bazi: '适合做更深层的人生盘点',
      tarot: '情绪议题和关系题更有代入感',
      yijing: '适合判断眼下去留与节奏',
      ai: '适合追问细节与连续追踪',
    },
    quickAccess: '常用去处',
    quickAccessMeta: '把收藏、成就、个人中心和面相报告折成辅助卡片，降低切换成本。',
    quickLabels: {
      favorites: '我的收藏',
      achievements: '我的成就',
      dashboard: '个人中心',
      face: '面相报告',
    },
    quickDescriptions: {
      favorites: '回看你收藏的卦象、牌阵与命盘记录。',
      achievements: '查看签到、任务与成长勋章。',
      dashboard: '管理积分、消息、资料与常用入口。',
      face: '用文化视角生成面相观察报告。',
    },
    recentRecords: '最近记录',
    recentRecordsMeta: '保留最近一次占问的时间线，让首页更像持续使用中的工作台。',
    viewMore: '查看全部',
    untitled: '未命名记录',
    noRecords: '还没有最近记录，去抽一张牌或起一卦吧。',
    recordTypes: {
      yijing: '易经',
      tarot: '塔罗',
      bazi: '八字',
      other: '其他',
    },
    levels: {
      excellent: '大吉',
      good: '顺势',
      fair: '平稳',
      caution: '收敛',
    },
    aspects: {
      love: '感情',
      career: '事业',
      wealth: '财运',
      health: '健康',
      study: '学业',
      relationship: '人际',
    },
    points: '分',
    today: '今日',
    defaultKeywords: ['玄象', '节律', '机势', '定心'],
  },
  'en-US': {
    greetingBack: 'Welcome back',
    greetingLogin: "Sign in to unlock today's reading",
    heroEyebrow: 'Mystic Landing Page',
    heroTitle: 'Read the signs before you choose your next move',
    heroDescription:
      'This homepage blends Bazi, Yijing, Tarot, and AI guidance into a more atmospheric editorial layout while keeping the product practical and easy to scan.',
    layoutBadge: 'Hero Layout Refresh',
    sectionEyebrow: 'Section',
    entryLabel: 'Entry',
    enterLabel: 'Enter',
    heroPrimary: 'Open Bazi Chart',
    heroSecondary: 'Start Yijing Reading',
    heroPrimaryMeta: 'Life chart',
    heroSecondaryMeta: 'Situation reading',
    scoreMetric: 'Today Score',
    timeMetric: 'Lucky Time',
    directionMetric: 'Lucky Direction',
    focusMetric: 'Top Focus',
    creditsLabel: 'Credits',
    checkin: 'Daily Check-In',
    checkedIn: 'Checked In',
    loginFirst: 'Please sign in first',
    languageLabel: 'Switch to 中文',
    awaitingData: 'Pending',
    overallFortune: 'Overall Fortune',
    fortuneTitle: "Today's Reading",
    fortuneMeta:
      'A scan-friendly board for fortune, lucky elements, and do / avoid guidance.',
    fortuneEmptyTitle: 'Reading not ready yet',
    fortuneEmptyDesc:
      "Today's fortune data has not arrived yet, but the new layout is already in place and will fill in as soon as the API responds.",
    ritualTitle: 'Daily Ritual',
    ritualMeta:
      'Popular landing-page density, translated into an Eastern metaphysics tone.',
    luckyElements: 'Lucky Coordinates',
    color: 'Color',
    number: 'Number',
    direction: 'Direction',
    time: 'Time',
    doToday: 'Do Today',
    avoidToday: 'Avoid Today',
    overallAdvice: 'Summary',
    keywordTitle: 'Keywords',
    featureSectionTitle: 'Core Paths',
    featureSectionMeta:
      'The four highest-frequency actions are brought back into the first screen for faster scanning.',
    featureLabels: {
      bazi: 'Bazi Chart',
      tarot: 'Tarot Draw',
      yijing: 'Yijing Cast',
      ai: 'AI Reading',
    },
    featureDescriptions: {
      bazi: 'Use birth time as the anchor to break down work, wealth, relationships, and long-term cycles.',
      tarot: 'Best for emotions, relationships, and difficult choices with an intuitive entry point.',
      yijing: 'Turn a question into a hexagram to judge timing and direction.',
      ai: 'Shape vague worries into structured prompts and get instant interpretation.',
    },
    featureSignals: {
      bazi: 'Best for deeper life review',
      tarot: 'Strong for emotional and relational topics',
      yijing: 'Best for near-term decisions and timing',
      ai: 'Best for fast follow-up questions',
    },
    quickAccess: 'Quick Access',
    quickAccessMeta:
      'Secondary cards for favorites, achievements, profile, and face reading.',
    quickLabels: {
      favorites: 'Favorites',
      achievements: 'Achievements',
      dashboard: 'Profile',
      face: 'Face Reading',
    },
    quickDescriptions: {
      favorites: 'Review saved readings, spreads, and chart records.',
      achievements: 'Track check-ins, quests, and growth badges.',
      dashboard: 'Manage credits, messages, profile, and shortcuts.',
      face: 'Generate a Gemini physiognomy report through a cultural lens.',
    },
    recentRecords: 'Recent Records',
    recentRecordsMeta:
      'Keeps the homepage feeling like a living workspace instead of a static menu.',
    viewMore: 'View All',
    untitled: 'Untitled Record',
    noRecords:
      'No recent records yet. Draw a card or cast a hexagram to begin.',
    recordTypes: {
      yijing: 'Yijing',
      tarot: 'Tarot',
      bazi: 'Bazi',
      other: 'Other',
    },
    levels: {
      excellent: 'Excellent',
      good: 'Aligned',
      fair: 'Stable',
      caution: 'Cautious',
    },
    aspects: {
      love: 'Love',
      career: 'Career',
      wealth: 'Wealth',
      health: 'Health',
      study: 'Study',
      relationship: 'Social',
    },
    points: 'pts',
    today: 'Today',
    defaultKeywords: ['Pattern', 'Rhythm', 'Chance', 'Focus'],
  },
}

const ASPECT_META = {
  love: {
    icon: Heart,
    barClass: 'from-[#a34224] to-[#e19a84]',
    iconWrapClass: 'bg-[#7a3218]/18 text-[#e19a84]',
    borderClass: 'border-[#a34224]/12',
  },
  career: {
    icon: Briefcase,
    barClass: 'from-[#c78734] to-[#e3bf73]',
    iconWrapClass: 'bg-[#8f5c1f]/18 text-[#f0d9a5]',
    borderClass: 'border-[#d0a85b]/12',
  },
  wealth: {
    icon: DollarSign,
    barClass: 'from-[#d0a85b] to-[#f0d9a5]',
    iconWrapClass: 'bg-[#6a4a1e]/18 text-[#f0d9a5]',
    borderClass: 'border-[#d0a85b]/12',
  },
  health: {
    icon: Activity,
    barClass: 'from-[#8f6b4c] to-[#bdaa94]',
    iconWrapClass: 'bg-[#3f2b17]/18 text-[#d9c1aa]',
    borderClass: 'border-[#8f6b4c]/12',
  },
  study: {
    icon: GraduationCap,
    barClass: 'from-[#b88a3d] to-[#dcb86f]',
    iconWrapClass: 'bg-[#5e431d]/18 text-[#dcb86f]',
    borderClass: 'border-[#b88a3d]/12',
  },
  relationship: {
    icon: Users,
    barClass: 'from-[#7a3218] to-[#c96a4c]',
    iconWrapClass: 'bg-[#5a2318]/18 text-[#e4b3a1]',
    borderClass: 'border-[#9a4e34]/12',
  },
}

function getTodayCacheDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function pickLocalizedCandidate(source, locale, key) {
  if (!source || typeof source !== 'object') return ''

  const englishCandidates = [
    source?.[`${key}En`],
    source?.[`${key}_en`],
    source?.[`${key}English`],
    source?.[`${key}_english`],
    source?.[key],
  ]

  const chineseCandidates = [
    source?.[`${key}Zh`],
    source?.[`${key}_zh`],
    source?.[`${key}Cn`],
    source?.[`${key}_cn`],
    source?.[key],
  ]

  const candidates = locale === 'en-US' ? englishCandidates : chineseCandidates
  return candidates.find((candidate) => safeText(candidate)) || ''
}

function normalizeLocalizedText(value, locale, fallback = '') {
  const sourceText = safeText(value, fallback)
  if (!sourceText) return fallback

  const hasCJKText = CJK_TEXT_PATTERN.test(sourceText)
  const hasLatinText = LATIN_TEXT_PATTERN.test(sourceText)

  if (locale === 'en-US') {
    return hasCJKText ? fallback : sourceText
  }

  if (hasLatinText && !hasCJKText) {
    return fallback
  }

  if (hasLatinText && hasCJKText) {
    return fallback
  }

  return sourceText
}

function normalizeLocalizedList(values, locale) {
  return safeArray(values)
    .map((value) => normalizeLocalizedText(value, locale, ''))
    .filter(Boolean)
}

function normalizeFortuneDetailData(data, locale) {
  if (!data || typeof data !== 'object') return null

  const aspects = Object.entries(data?.aspects || {}).reduce(
    (result, [key, aspect]) => {
      result[key] = {
        ...aspect,
        analysis: normalizeLocalizedText(
          pickLocalizedCandidate(aspect, locale, 'analysis'),
          locale,
          ''
        ),
      }
      return result
    },
    {}
  )

  return {
    ...data,
    date: normalizeLocalizedText(data?.date, locale, ''),
    overallAdvice: normalizeLocalizedText(
      pickLocalizedCandidate(data, locale, 'overallAdvice'),
      locale,
      ''
    ),
    keywords: normalizeLocalizedList(data?.keywords, locale),
    suitableActions: normalizeLocalizedList(data?.suitableActions, locale),
    unsuitableActions: normalizeLocalizedList(data?.unsuitableActions, locale),
    luckyElements: {
      color: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'color'),
        locale,
        ''
      ),
      number: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'number'),
        locale,
        ''
      ),
      direction: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'direction'),
        locale,
        ''
      ),
      time: normalizeLocalizedText(
        pickLocalizedCandidate(data?.luckyElements, locale, 'time'),
        locale,
        ''
      ),
    },
    aspects,
  }
}

function getDailyFortuneCacheKey(userId, locale) {
  return `${DAILY_FORTUNE_CACHE_PREFIX}:${getTodayCacheDate()}:${userId ?? 'guest'}:${locale ?? 'zh-CN'}`
}

function readDailyFortuneCache(userId, locale) {
  try {
    const raw = sessionStorage.getItem(getDailyFortuneCacheKey(userId, locale))
    if (!raw) return null
    const parsed = JSON.parse(raw)
    return parsed?.data || null
  } catch (error) {
    console.warn('读取首页运势缓存失败:', error)
    return null
  }
}

function writeDailyFortuneCache(userId, locale, data) {
  try {
    sessionStorage.setItem(
      getDailyFortuneCacheKey(userId, locale),
      JSON.stringify({
        cachedAt: Date.now(),
        data,
      })
    )
  } catch (error) {
    console.warn('写入首页运势缓存失败:', error)
  }
}

function getScoreLevel(score, copy) {
  if (score >= 85) {
    return {
      label: copy.levels.excellent,
      textClass: 'text-emerald-200',
      badgeClass:
        'border border-emerald-400/20 bg-emerald-500/10 text-emerald-200',
    }
  }

  if (score >= 75) {
    return {
      label: copy.levels.good,
      textClass: 'text-sky-200',
      badgeClass: 'border border-sky-400/20 bg-sky-500/10 text-sky-200',
    }
  }

  if (score >= 65) {
    return {
      label: copy.levels.fair,
      textClass: 'text-amber-100',
      badgeClass: 'border border-amber-400/20 bg-amber-500/10 text-amber-100',
    }
  }

  return {
    label: copy.levels.caution,
    textClass: 'text-orange-200',
    badgeClass: 'border border-orange-400/20 bg-orange-500/10 text-orange-200',
  }
}

function formatDisplayDate(timestamp, locale) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleDateString(locale, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function Home() {
  const navigate = useNavigate()
  const { locale } = useAppLocale()
  const copy = HOME_COPY[locale] || HOME_COPY['zh-CN']
  const isEnglish = locale === 'en-US'
  const displayFont = isEnglish ? DISPLAY_FONT_EN : DISPLAY_FONT_ZH
  const heroTitleClass = isEnglish
    ? 'max-w-5xl text-4xl font-semibold leading-[0.94] tracking-[-0.02em] text-stone-50 sm:text-5xl lg:text-[4.75rem]'
    : 'max-w-4xl text-4xl font-semibold leading-tight text-stone-50 sm:text-5xl lg:text-6xl'
  const heroDescriptionClass = isEnglish
    ? 'mt-4 max-w-3xl text-base leading-8 text-stone-300'
    : 'mt-4 max-w-2xl text-sm leading-7 text-stone-300 sm:text-base'
  const { isLoggedIn, user, credits, refreshCredits } = useAuth()

  const [showCheckinProgress, setShowCheckinProgress] = useState(false)
  const [canCheckin, setCanCheckin] = useState(checkin.canCheckin())
  const [fortuneSource, setFortuneSource] = useState(null)
  const [loading, setLoading] = useState(true)
  const [recentHistory, setRecentHistory] = useState([])
  const hasFortuneSource = Boolean(fortuneSource)

  useEffect(() => {
    setCanCheckin(checkin.canCheckin())
    setRecentHistory(historyStorage.getAll().slice(0, 5))
  }, [isLoggedIn])

  useEffect(() => {
    let cancelled = false

    const loadFortuneDetail = async () => {
      const cachedData = readDailyFortuneCache(user?.id, locale)
      if (cachedData) {
        if (!cancelled) {
          setFortuneSource(cachedData)
          setLoading(false)
        }
        return
      }

      try {
        if (!hasFortuneSource && !cancelled) {
          setLoading(true)
        }

        const token = sessionStorage.getItem('token')
        const headers = {
          'Content-Type': 'application/json',
          'X-Language': locale,
          'Accept-Language': locale,
        }

        if (token) {
          headers.Authorization = `Bearer ${token}`
        }

        const response = await fetch('/api/daily-fortune-detail/today', {
          method: 'GET',
          headers,
        })

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }

        const result = await response.json()
        if (result.success && result.data) {
          if (!cancelled) {
            setFortuneSource(result.data)
          }
          writeDailyFortuneCache(user?.id, locale, result.data)
        } else {
          console.warn('获取首页运势详情失败:', result.message || '未知错误')
          if (!cancelled && !hasFortuneSource) {
            setFortuneSource(null)
          }
        }
      } catch (error) {
        console.error('加载首页运势详情失败:', error)
        if (!cancelled && !hasFortuneSource) {
          setFortuneSource(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadFortuneDetail()

    return () => {
      cancelled = true
    }
  }, [hasFortuneSource, locale, user?.id])

  const fortuneDetail = useMemo(
    () => normalizeFortuneDetailData(fortuneSource, locale),
    [fortuneSource, locale]
  )

  const aspectEntries = useMemo(
    () =>
      Object.entries(ASPECT_META).map(([key, meta]) => {
        const aspect = fortuneDetail?.aspects?.[key] || {}
        return {
          key,
          label: copy.aspects[key],
          icon: meta.icon,
          score: safeNumber(aspect.score || 0, 0),
          analysis: normalizeLocalizedText(
            pickLocalizedCandidate(aspect, locale, 'analysis'),
            locale,
            ''
          ),
          barClass: meta.barClass,
          iconWrapClass: meta.iconWrapClass,
          borderClass: meta.borderClass,
        }
      }),
    [copy, fortuneDetail, locale]
  )

  const filledScores = useMemo(
    () => aspectEntries.map((item) => item.score).filter((score) => score > 0),
    [aspectEntries]
  )

  const averageScore = useMemo(() => {
    if (filledScores.length === 0) return null
    return Math.round(
      filledScores.reduce((current, next) => current + next, 0) /
        filledScores.length
    )
  }, [filledScores])

  const scoreTone = useMemo(
    () => getScoreLevel(averageScore ?? 0, copy),
    [averageScore, copy]
  )

  const topAspects = useMemo(() => {
    const scored = aspectEntries.filter((item) => item.score > 0)
    if (scored.length === 0) return aspectEntries.slice(0, 3)
    return [...scored].sort((left, right) => right.score - left.score).slice(0, 3)
  }, [aspectEntries])

  const luckyElements = fortuneDetail?.luckyElements || {}
  const suitableActions = safeArray(fortuneDetail?.suitableActions)
  const unsuitableActions = safeArray(fortuneDetail?.unsuitableActions)
  const keywords =
    safeArray(fortuneDetail?.keywords).length > 0
      ? safeArray(fortuneDetail?.keywords).slice(0, 4)
      : copy.defaultKeywords

  const heroMetrics = useMemo(
    () => [
      {
        label: copy.scoreMetric,
        value: averageScore === null ? '--' : averageScore,
        note: averageScore === null ? copy.awaitingData : scoreTone.label,
      },
      {
        label: copy.timeMetric,
        value: luckyElements.time || '--',
        note: copy.time,
      },
      {
        label: copy.directionMetric,
        value: luckyElements.direction || '--',
        note: copy.direction,
      },
      {
        label: copy.focusMetric,
        value: topAspects[0]?.label || '--',
        note:
          topAspects[0]?.score > 0
            ? `${topAspects[0].score}${copy.points}`
            : copy.awaitingData,
      },
    ],
    [
      averageScore,
      copy,
      luckyElements.direction,
      luckyElements.time,
      scoreTone.label,
      topAspects,
    ]
  )

  const featureCards = useMemo(
    () => [
      {
        path: '/bazi',
        icon: Calendar,
        label: copy.featureLabels.bazi,
        description: copy.featureDescriptions.bazi,
        signal: copy.featureSignals.bazi,
        accentClass:
          'bg-[radial-gradient(circle_at_top_left,rgba(226,184,98,0.18),transparent_55%),linear-gradient(135deg,rgba(91,48,24,0.6),rgba(20,15,15,0.25))]',
        badgeClass: 'border-amber-300/20 bg-amber-500/10 text-amber-100',
        iconClass: 'bg-amber-500/15 text-amber-100',
        spanClass: 'lg:col-span-2 lg:row-span-2',
      },
      {
        path: '/yijing',
        icon: BookOpen,
        label: copy.featureLabels.yijing,
        description: copy.featureDescriptions.yijing,
        signal: copy.featureSignals.yijing,
        accentClass:
          'bg-[radial-gradient(circle_at_top_right,rgba(61,156,137,0.18),transparent_58%),linear-gradient(135deg,rgba(14,59,53,0.55),rgba(20,15,15,0.25))]',
        badgeClass: 'border-emerald-300/20 bg-emerald-500/10 text-emerald-100',
        iconClass: 'bg-emerald-500/15 text-emerald-100',
        spanClass: 'lg:col-span-2',
      },
      {
        path: '/tarot',
        icon: Sparkles,
        label: copy.featureLabels.tarot,
        description: copy.featureDescriptions.tarot,
        signal: copy.featureSignals.tarot,
        accentClass:
          'bg-[radial-gradient(circle_at_top_left,rgba(160,113,255,0.18),transparent_55%),linear-gradient(135deg,rgba(54,33,83,0.55),rgba(20,15,15,0.25))]',
        badgeClass: 'border-violet-300/20 bg-violet-500/10 text-violet-100',
        iconClass: 'bg-violet-500/15 text-violet-100',
        spanClass: '',
      },
      {
        path: '/ai',
        icon: Brain,
        label: copy.featureLabels.ai,
        description: copy.featureDescriptions.ai,
        signal: copy.featureSignals.ai,
        accentClass:
          'bg-[radial-gradient(circle_at_top_right,rgba(57,138,255,0.18),transparent_58%),linear-gradient(135deg,rgba(20,38,74,0.55),rgba(20,15,15,0.25))]',
        badgeClass: 'border-sky-300/20 bg-sky-500/10 text-sky-100',
        iconClass: 'bg-sky-500/15 text-sky-100',
        spanClass: '',
      },
    ],
    [copy]
  )

  const quickAccessCards = useMemo(
    () => [
      {
        path: '/favorites',
        icon: Heart,
        label: copy.quickLabels.favorites,
        description: copy.quickDescriptions.favorites,
        iconClass: 'bg-rose-500/15 text-rose-200',
      },
      {
        path: '/achievement',
        icon: Trophy,
        label: copy.quickLabels.achievements,
        description: copy.quickDescriptions.achievements,
        iconClass: 'bg-amber-500/15 text-amber-100',
      },
      {
        path: '/dashboard',
        icon: User,
        label: copy.quickLabels.dashboard,
        description: copy.quickDescriptions.dashboard,
        iconClass: 'bg-emerald-500/15 text-emerald-100',
      },
      {
        path: '/ai/face',
        icon: Zap,
        label: copy.quickLabels.face,
        description: copy.quickDescriptions.face,
        iconClass: 'bg-sky-500/15 text-sky-100',
      },
    ],
    [copy]
  )

  const handleCheckin = () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      navigate('/login')
      return
    }

    setShowCheckinProgress(true)
  }

  const onCheckinClose = () => {
    setShowCheckinProgress(false)
    refreshCredits({ force: true })
    setCanCheckin(checkin.canCheckin())
  }

  return (
    <div
      key={locale}
      lang={locale}
      translate="no"
      className="notranslate relative min-h-screen overflow-hidden bg-[#090706] text-stone-100"
    >
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(149,42,25,0.25),transparent_28%),radial-gradient(circle_at_top_right,rgba(214,183,122,0.15),transparent_30%),radial-gradient(circle_at_bottom_left,rgba(43,102,89,0.16),transparent_28%)]" />
        <div
          className="absolute inset-0 opacity-40"
          style={{
            backgroundImage:
              'linear-gradient(rgba(214,183,122,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(214,183,122,0.06) 1px, transparent 1px)',
            backgroundSize: '72px 72px',
            maskImage:
              'radial-gradient(circle at center, black 45%, transparent 85%)',
          }}
        />
      </div>

      <div className="relative mx-auto max-w-7xl px-4 pb-24 pt-4 sm:px-6 lg:px-8">
        <section className={`${PANEL_CLASS} mb-4 p-4 sm:p-6 lg:p-8`}>
          <div className="pointer-events-none absolute -left-12 top-10 h-28 w-28 rounded-full border border-amber-200/10" />
          <div className="pointer-events-none absolute right-8 top-6 h-24 w-24 rounded-full border border-amber-200/10 [animation:spin_18s_linear_infinite]" />
          <div className="pointer-events-none absolute bottom-0 right-0 h-40 w-40 translate-x-1/3 translate-y-1/4 rounded-full bg-amber-200/10 blur-3xl" />

          <div className="mb-8 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
            <Link
              to={isLoggedIn ? '/dashboard' : '/login'}
              className="group flex items-center gap-4 rounded-[24px] border border-white/10 bg-white/[0.04] px-4 py-4 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.06]"
            >
              <div className="flex h-14 w-14 items-center justify-center rounded-[20px] bg-gradient-to-br from-[#7f2416] via-[#b84e2b] to-[#d6b77a] shadow-[0_12px_30px_rgba(130,54,30,0.35)] transition-transform duration-300 group-hover:scale-105">
                <User size={24} className="text-white" />
              </div>

              <div className="min-w-0">
                <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
                  {copy.heroEyebrow}
                </div>
                <div className="mt-1 text-lg font-semibold text-stone-100">
                  {isLoggedIn
                    ? `${copy.greetingBack}${user?.username ? ` · ${user.username}` : ''}`
                    : copy.greetingLogin}
                </div>
              </div>
            </Link>

            <div className="flex flex-wrap items-center gap-2">
              <div className="inline-flex items-center gap-2 rounded-full border border-amber-200/15 bg-amber-500/10 px-4 py-2 text-sm text-amber-100">
                <Star size={16} />
                <span>
                  {copy.creditsLabel}: {credits ?? 0}
                </span>
              </div>

              <LanguageToggleButton className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-4 py-2 text-sm text-stone-200 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.07]">
                {copy.languageLabel}
              </LanguageToggleButton>

              <button
                type="button"
                onClick={handleCheckin}
                className={`inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition-all duration-300 ${
                  canCheckin
                    ? 'bg-gradient-to-r from-[#8d301d] via-[#ba512d] to-[#d2a866] text-white shadow-[0_12px_30px_rgba(133,54,30,0.35)] hover:-translate-y-0.5'
                    : 'border border-white/10 bg-white/[0.05] text-stone-400'
                }`}
              >
                <Gift size={16} />
                <span>{canCheckin ? copy.checkin : copy.checkedIn}</span>
              </button>
            </div>
          </div>

          <div className="grid gap-4 xl:grid-cols-[1.18fr_0.82fr]">
            <div className="space-y-6">
              <div className="inline-flex items-center gap-2 rounded-full border border-amber-200/15 bg-white/[0.04] px-4 py-2 text-[11px] uppercase tracking-[0.28em] text-amber-200/80">
                <span className="h-2 w-2 rounded-full bg-amber-200" />
                {copy.layoutBadge}
              </div>

              <div>
                <h1
                  style={displayFont}
                  className={heroTitleClass}
                >
                  {copy.heroTitle}
                </h1>
                <p className={heroDescriptionClass}>
                  {copy.heroDescription}
                </p>
              </div>

              <div className="flex flex-wrap gap-3">
                <HeroActionLink
                  to="/bazi"
                  title={copy.heroPrimary}
                  meta={copy.heroPrimaryMeta}
                  primary
                  isEnglish={isEnglish}
                />
                <HeroActionLink
                  to="/yijing"
                  title={copy.heroSecondary}
                  meta={copy.heroSecondaryMeta}
                  isEnglish={isEnglish}
                />
              </div>

              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                {heroMetrics.map((metric) => (
                  <div
                    key={metric.label}
                    className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4"
                  >
                    <div className="text-[11px] uppercase tracking-[0.28em] text-stone-400">
                      {metric.label}
                    </div>
                    <div className="mt-3 text-2xl font-semibold text-stone-50">
                      {metric.value}
                    </div>
                    <div className="mt-1 text-sm text-stone-400">{metric.note}</div>
                  </div>
                ))}
              </div>

              <div className="flex flex-wrap gap-2">
                {keywords.map((keyword) => (
                  <span
                    key={keyword}
                    className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1.5 text-xs tracking-[0.16em] text-amber-100"
                  >
                    #{keyword}
                  </span>
                ))}
              </div>
            </div>

            <div>
              <div className="rounded-[28px] border border-white/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.08),rgba(255,255,255,0.02))] p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
                      {copy.overallFortune}
                    </div>
                    <div className="mt-4 flex items-end gap-3">
                      <span
                        className={`text-5xl font-semibold ${averageScore === null ? 'text-stone-300' : scoreTone.textClass}`}
                      >
                        {averageScore === null ? '--' : averageScore}
                      </span>
                      <span className="pb-1 text-sm text-stone-400">
                        {copy.points}
                      </span>
                    </div>
                    <div className="mt-3 inline-flex rounded-full px-3 py-1 text-xs tracking-[0.18em] uppercase">
                      <span className={`rounded-full px-3 py-1 ${scoreTone.badgeClass}`}>
                        {averageScore === null ? copy.awaitingData : scoreTone.label}
                      </span>
                    </div>
                  </div>

                  <div className="relative flex h-24 w-24 items-center justify-center rounded-full border border-amber-200/15 bg-white/[0.04]">
                    <div className="absolute inset-2 rounded-full border border-amber-200/10 [animation:spin_16s_linear_infinite]" />
                    <Sparkles size={28} className="text-amber-100" />
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  {topAspects.map((aspect) => {
                    const Icon = aspect.icon
                    return (
                      <div
                        key={aspect.key}
                        className={`rounded-[20px] border ${aspect.borderClass} bg-black/15 p-3`}
                      >
                        <div className="flex items-center justify-between gap-3">
                          <div className="flex items-center gap-3">
                            <div
                              className={`flex h-10 w-10 items-center justify-center rounded-2xl ${aspect.iconWrapClass}`}
                            >
                              <Icon size={18} />
                            </div>
                            <div>
                              <div className="font-medium text-stone-100">
                                {aspect.label}
                              </div>
                              <div className="text-xs text-stone-400">
                                {aspect.score > 0
                                  ? `${aspect.score}${copy.points}`
                                  : copy.awaitingData}
                              </div>
                            </div>
                          </div>
                          <div className="h-1.5 w-24 overflow-hidden rounded-full bg-white/10">
                            <div
                              className={`h-full rounded-full bg-gradient-to-r ${aspect.barClass}`}
                              style={{ width: `${Math.max(aspect.score, 12)}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </section>

        <div className={`${PANEL_CLASS} mb-4 overflow-hidden p-2`}>
          <PangleCarouselAd
            slotId={pangleConfig.carouselSlotId}
            className="w-full"
            autoPlay={true}
            interval={3000}
            showIndicators={true}
            showArrows={true}
            onAdLoad={(ads) => {
              console.log('首页轮播广告加载成功:', ads)
            }}
            onAdError={(error) => {
              console.error('首页轮播广告加载失败:', error)
            }}
          />
        </div>

        <div className="grid gap-4 xl:grid-cols-[1.14fr_0.86fr]">
          <div className="space-y-4">
            <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
              <SectionHeading
                title={copy.featureSectionTitle}
                description={copy.featureSectionMeta}
                eyebrow={copy.sectionEyebrow}
                titleStyle={displayFont}
                isEnglish={isEnglish}
              />

              <div className="mt-5 grid auto-rows-[minmax(190px,auto)] gap-4 lg:grid-cols-4">
                {featureCards.map((feature) => (
                  <FeatureCard
                    key={feature.path}
                    feature={feature}
                    entryLabel={copy.entryLabel}
                    enterLabel={copy.enterLabel}
                    titleStyle={displayFont}
                    isEnglish={isEnglish}
                  />
                ))}
              </div>
            </section>

            <div className="grid gap-4 lg:grid-cols-[0.94fr_1.06fr]">
              <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
                <SectionHeading
                  title={copy.quickAccess}
                  description={copy.quickAccessMeta}
                  eyebrow={copy.sectionEyebrow}
                  titleStyle={displayFont}
                  isEnglish={isEnglish}
                />

                <div className="mt-5 space-y-3">
                  {quickAccessCards.map((item) => (
                    <QuickAccessCard key={item.path} item={item} />
                  ))}
                </div>
              </section>

              <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
                <div className="flex items-start justify-between gap-4">
                  <SectionHeading
                    title={copy.recentRecords}
                    description={copy.recentRecordsMeta}
                    eyebrow={copy.sectionEyebrow}
                    titleStyle={displayFont}
                    isEnglish={isEnglish}
                  />
                  <Link
                    to="/records"
                    className="inline-flex items-center gap-1 text-sm text-amber-100 transition-colors hover:text-amber-50"
                  >
                    {copy.viewMore}
                    <ChevronRight size={16} />
                  </Link>
                </div>

                <div className="mt-5 space-y-3">
                  {recentHistory.length > 0 ? (
                    recentHistory.slice(0, 4).map((item) => (
                      <div
                        key={item.id}
                        className="rounded-[22px] border border-white/10 bg-white/[0.04] p-4 transition-all duration-300 hover:bg-white/[0.06]"
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div className="min-w-0">
                            <div className="text-sm font-medium text-stone-100">
                              {item.question || item.title || copy.untitled}
                            </div>
                            <div className="mt-2 text-xs text-stone-400">
                              {formatDisplayDate(item.timestamp, locale)}
                            </div>
                          </div>
                          <span className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1 text-xs text-amber-100">
                            {copy.recordTypes[item.type] || copy.recordTypes.other}
                          </span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="rounded-[22px] border border-dashed border-white/10 bg-white/[0.03] px-4 py-10 text-center text-sm leading-7 text-stone-400">
                      {copy.noRecords}
                    </div>
                  )}
                </div>
              </section>
            </div>
          </div>

          <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
            <SectionHeading
              title={copy.fortuneTitle}
              description={copy.fortuneMeta}
              eyebrow={copy.sectionEyebrow}
              titleStyle={displayFont}
              isEnglish={isEnglish}
            />

            {loading ? (
              <div className="mt-5 space-y-4">
                <div className="h-36 animate-pulse rounded-[28px] bg-white/[0.06]" />
                <div className="grid gap-3 sm:grid-cols-2">
                  {[1, 2, 3, 4].map((value) => (
                    <div
                      key={value}
                      className="h-28 animate-pulse rounded-[24px] bg-white/[0.05]"
                    />
                  ))}
                </div>
              </div>
            ) : (
              <DailyFortuneDeck
                fortuneDetail={fortuneDetail}
                copy={copy}
                averageScore={averageScore}
                scoreTone={scoreTone}
                aspectEntries={aspectEntries}
                luckyElements={luckyElements}
                suitableActions={suitableActions}
                unsuitableActions={unsuitableActions}
                keywords={keywords}
              />
            )}
          </section>
        </div>
      </div>

      <CheckinProgress isOpen={showCheckinProgress} onClose={onCheckinClose} />
    </div>
  )
}

function SectionHeading({
  title,
  description,
  eyebrow = 'Section',
  titleStyle = DISPLAY_FONT_ZH,
  isEnglish = false,
}) {
  return (
    <div>
      <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
        {eyebrow}
      </div>
      <h2
        style={titleStyle}
        className={`mt-2 font-semibold text-stone-50 ${
          isEnglish ? 'text-[2rem] leading-[1.05]' : 'text-2xl'
        }`}
      >
        {title}
      </h2>
      <p className="mt-2 max-w-2xl text-sm leading-6 text-stone-400">
        {description}
      </p>
    </div>
  )
}

function HeroActionLink({ to, title, meta, primary = false, isEnglish = false }) {
  return (
    <Link
      to={to}
      className={`group inline-flex items-center gap-3 rounded-full px-5 py-3 text-sm transition-all duration-300 ${
        primary
          ? 'bg-gradient-to-r from-[#8f331f] via-[#b9552f] to-[#d6b77a] text-white shadow-[0_14px_34px_rgba(138,59,32,0.35)] hover:-translate-y-0.5'
          : 'border border-white/10 bg-white/[0.05] text-stone-100 hover:border-amber-200/20 hover:bg-white/[0.08]'
      } ${isEnglish ? 'min-w-[220px] justify-between' : ''}`}
    >
      <div className={isEnglish ? 'text-left' : ''}>
        <div className="font-medium">{title}</div>
        <div className="text-xs text-white/70">{meta}</div>
      </div>
      <ArrowRight
        size={18}
        className="transition-transform duration-300 group-hover:translate-x-1"
      />
    </Link>
  )
}

function InfoLine({ icon: Icon, label, value }) {
  return (
    <div className="flex items-center gap-3 rounded-[20px] border border-white/10 bg-black/15 px-4 py-3">
      <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-white/[0.06] text-amber-100">
        <Icon size={18} />
      </div>
      <div>
        <div className="text-xs uppercase tracking-[0.24em] text-stone-500">
          {label}
        </div>
        <div className="mt-1 text-sm font-medium text-stone-100">{value}</div>
      </div>
    </div>
  )
}

function ActionListCard({ icon: Icon, title, items, emptyLabel, tone }) {
  const toneClass =
    tone === 'good'
      ? 'border-emerald-300/15 bg-emerald-500/10 text-emerald-100'
      : 'border-orange-300/15 bg-orange-500/10 text-orange-100'

  const chipsClass =
    tone === 'good'
      ? 'border-emerald-300/15 bg-emerald-500/10 text-emerald-100'
      : 'border-orange-300/15 bg-orange-500/10 text-orange-100'

  return (
    <div className={`rounded-[22px] border p-4 ${toneClass}`}>
      <div className="flex items-center gap-2 text-sm font-medium">
        <Icon size={16} />
        <span>{title}</span>
      </div>
      <div className="mt-3 flex flex-wrap gap-2">
        {items.length > 0 ? (
          items.slice(0, 4).map((item) => (
            <span
              key={`${title}-${item}`}
              className={`rounded-full border px-3 py-1.5 text-xs ${chipsClass}`}
            >
              {item}
            </span>
          ))
        ) : (
          <span className="text-xs opacity-80">{emptyLabel}</span>
        )}
      </div>
    </div>
  )
}

function FeatureCard({
  feature,
  entryLabel = 'Entry',
  enterLabel = 'Enter',
  titleStyle = DISPLAY_FONT_ZH,
  isEnglish = false,
}) {
  const Icon = feature.icon

  return (
    <Link
      to={feature.path}
      className={`group relative overflow-hidden rounded-[28px] border border-white/10 bg-white/[0.04] p-5 transition-all duration-300 hover:-translate-y-1 hover:border-amber-200/20 hover:bg-white/[0.06] ${feature.spanClass}`}
    >
      <div className={`pointer-events-none absolute inset-0 ${feature.accentClass}`} />
      <div className="relative flex h-full flex-col justify-between">
        <div className="flex items-start justify-between gap-3">
          <div className={`inline-flex rounded-full border px-3 py-1 text-xs ${feature.badgeClass}`}>
            {entryLabel}
          </div>
          <div
            className={`flex h-12 w-12 items-center justify-center rounded-[18px] ${feature.iconClass}`}
          >
            <Icon size={22} />
          </div>
        </div>

        <div className="mt-8">
          <h3
            style={titleStyle}
            className={`font-semibold text-stone-50 ${
              isEnglish ? 'text-[1.9rem] leading-[1.05]' : 'text-2xl'
            }`}
          >
            {feature.label}
          </h3>
          <p className="mt-3 text-sm leading-7 text-stone-300">{feature.description}</p>
        </div>

        <div className="mt-6 flex items-center justify-between gap-3">
          <span className="text-sm text-stone-300">{feature.signal}</span>
          <span className="inline-flex items-center gap-1 text-sm text-amber-100 transition-transform duration-300 group-hover:translate-x-1">
            <span>{enterLabel}</span>
            <ChevronRight size={16} />
          </span>
        </div>
      </div>
    </Link>
  )
}

function QuickAccessCard({ item }) {
  const Icon = item.icon

  return (
    <Link
      to={item.path}
      className="group flex items-center justify-between gap-3 rounded-[22px] border border-white/10 bg-white/[0.04] px-4 py-4 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.06]"
    >
      <div className="flex items-center gap-3">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-[18px] ${item.iconClass}`}
        >
          <Icon size={20} />
        </div>
        <div>
          <div className="font-medium text-stone-100">{item.label}</div>
          <div className="mt-1 text-sm text-stone-400">{item.description}</div>
        </div>
      </div>

      <ChevronRight
        size={18}
        className="text-stone-500 transition-transform duration-300 group-hover:translate-x-1"
      />
    </Link>
  )
}

function DailyFortuneDeck({
  fortuneDetail,
  copy,
  averageScore,
  scoreTone,
  aspectEntries,
  luckyElements,
  suitableActions,
  unsuitableActions,
  keywords,
}) {
  if (!fortuneDetail) {
    return (
      <div className="mt-5 rounded-[28px] border border-dashed border-white/10 bg-white/[0.03] p-6">
        <div className="text-lg font-semibold text-stone-100">
          {copy.fortuneEmptyTitle}
        </div>
        <p className="mt-3 text-sm leading-7 text-stone-400">{copy.fortuneEmptyDesc}</p>
      </div>
    )
  }

  return (
    <div className="mt-5 space-y-4">
      <div className="overflow-hidden rounded-[28px] border border-white/10 bg-[radial-gradient(circle_at_top_right,rgba(214,183,122,0.18),transparent_34%),linear-gradient(180deg,rgba(255,255,255,0.08),rgba(255,255,255,0.03))] p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
              {copy.overallFortune}
            </div>
            <div className="mt-3 flex items-end gap-3">
              <span className={`text-5xl font-semibold ${scoreTone.textClass}`}>
                {averageScore ?? '--'}
              </span>
              <span className="pb-1 text-sm text-stone-400">{copy.points}</span>
            </div>
            <div className="mt-3 inline-flex rounded-full px-3 py-1 text-xs tracking-[0.18em] uppercase">
              <span className={`rounded-full px-3 py-1 ${scoreTone.badgeClass}`}>
                {averageScore === null ? copy.awaitingData : scoreTone.label}
              </span>
            </div>
          </div>

          <div className="rounded-full border border-amber-200/15 bg-white/[0.05] px-3 py-1 text-xs tracking-[0.18em] text-amber-100">
            {fortuneDetail.date || copy.today}
          </div>
        </div>

        {fortuneDetail.overallAdvice && (
          <p className="mt-5 text-sm leading-7 text-stone-300">
            {fortuneDetail.overallAdvice}
          </p>
        )}
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        {aspectEntries.map((aspect) => {
          const Icon = aspect.icon
          return (
            <div
              key={aspect.key}
              className={`rounded-[24px] border ${aspect.borderClass} bg-white/[0.04] p-4`}
            >
              <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-2xl ${aspect.iconWrapClass}`}
                  >
                    <Icon size={18} />
                  </div>
                  <div>
                    <div className="font-medium text-stone-100">{aspect.label}</div>
                    <div className="text-xs text-stone-400">
                      {aspect.score > 0
                        ? `${aspect.score}${copy.points}`
                        : copy.awaitingData}
                    </div>
                  </div>
                </div>
                <div className="h-1.5 w-20 overflow-hidden rounded-full bg-white/10">
                  <div
                    className={`h-full rounded-full bg-gradient-to-r ${aspect.barClass}`}
                    style={{ width: `${Math.max(aspect.score, 12)}%` }}
                  />
                </div>
              </div>

              {aspect.analysis && (
                <p className="mt-3 text-sm leading-6 text-stone-400">
                  {aspect.analysis}
                </p>
              )}
            </div>
          )
        })}
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
        <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
          {copy.luckyElements}
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <InfoLine
            icon={Sparkles}
            label={copy.color}
            value={luckyElements.color || copy.awaitingData}
          />
          <InfoLine
            icon={Star}
            label={copy.number}
            value={luckyElements.number || copy.awaitingData}
          />
          <InfoLine
            icon={MapPin}
            label={copy.direction}
            value={luckyElements.direction || copy.awaitingData}
          />
          <InfoLine
            icon={Clock}
            label={copy.time}
            value={luckyElements.time || copy.awaitingData}
          />
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <ActionListCard
          icon={CheckCircle2}
          title={copy.doToday}
          items={suitableActions}
          emptyLabel={copy.awaitingData}
          tone="good"
        />
        <ActionListCard
          icon={XCircle}
          title={copy.avoidToday}
          items={unsuitableActions}
          emptyLabel={copy.awaitingData}
          tone="warn"
        />
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
        <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
          {copy.keywordTitle}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {keywords.map((keyword) => (
            <span
              key={keyword}
              className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1.5 text-xs text-amber-100"
            >
              #{keyword}
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}
