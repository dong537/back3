import {
  User,
  BookOpen,
  Calendar,
  Brain,
  Eye,
  Heart,
  Briefcase,
  DollarSign,
  Activity,
  GraduationCap,
  Users,
  Sparkles,
  Trophy,
} from 'lucide-react'

export const PANEL_CLASS =
  'relative overflow-hidden rounded-[30px] border border-white/10 bg-[#140f0f]/72 shadow-[0_22px_80px_rgba(0,0,0,0.35)] backdrop-blur-2xl'

export const DISPLAY_FONT_ZH = {
  fontFamily: "'Noto Serif SC', 'Songti SC', 'STKaiti', 'KaiTi', serif",
}

export const DISPLAY_FONT_EN = {
  fontFamily: "Georgia, 'Times New Roman', serif",
}

export const HOME_COPY = {
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
    quickAccessMeta:
      '把面相、个人中心、收藏与成就提前到更靠上的位置，首屏附近就能快速进入。',
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
      'Bring face reading, profile, favorites, and achievements closer to the top for faster access.',
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

export const ASPECT_META = {
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

export function buildFeatureCards(copy) {
  return [
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
  ]
}

export function buildQuickAccessCards(copy) {
  return [
    {
      path: '/ai/face',
      icon: Eye,
      label: copy.quickLabels.face,
      description: copy.quickDescriptions.face,
      iconClass: 'bg-sky-500/15 text-sky-100',
    },
    {
      path: '/dashboard',
      icon: User,
      label: copy.quickLabels.dashboard,
      description: copy.quickDescriptions.dashboard,
      iconClass: 'bg-emerald-500/15 text-emerald-100',
    },
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
  ]
}
