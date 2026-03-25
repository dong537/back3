import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  ArrowLeft,
  ChevronRight,
  Sparkles,
  Heart,
  Briefcase,
  GraduationCap,
  Users,
  Wallet,
  Clock,
  Share2,
  Gift,
} from 'lucide-react'
import { getDailyOverallScore } from '../utils/dailyRandom'
import { resolvePageLocale } from '../utils/displayText'
import { toast } from '../components/Toast'

const TAB_KEYS = ['day', 'week', 'month', 'year']

const ZODIAC_COPY = {
  'zh-CN': {
    title: '星运',
    share: '分享',
    shareSuccess: '分享链接已复制',
    shareFailed: '暂时无法分享，请稍后重试',
    chart: '查看星盘',
    tabs: {
      day: '日',
      week: '周',
      month: '月',
      year: '年',
    },
    today: '今天',
    scoreLabel: '综合分数',
    scoreUnit: '分',
    boostTitle: '今天的能量状态不错',
    boostDesc: '抽一张启发卡，给自己一点好运加成',
    freeDraw: '免费抽卡',
    loveReminder: '感情提醒',
    fromAstro: '来自星运指引',
    fullText: '全文',
    suggestions: '建议',
    avoidances: '避免',
    askEntry: '今日提问',
    luckyElements: '幸运元素',
    luckyNumbers: '幸运数字',
    dimensionSuffix: '运势',
    mood: '心情',
    sports: '行动',
    surprise: '惊喜',
    romance: '桃花',
    dimensionLabels: {
      love: '感情',
      wealth: '财运',
      career: '事业',
      study: '学习',
      social: '人缘',
    },
    luckyLabels: {
      color: '幸运色',
      accessory: '幸运物',
      time: '幸运时段',
      direction: '幸运方位',
    },
    luckyValues: {
      color: '宝石蓝',
      accessory: '白水晶',
      time: '05:00 - 07:00',
      direction: '正东',
    },
    question: '我今天最值得投入精力的方向是什么？',
    moodCards: [
      { icon: '😊', label: '心情' },
      { icon: '🏃', label: '行动' },
      { icon: '🎁', label: '惊喜' },
      { icon: '🌸', label: '桃花' },
    ],
    narratives: {
      day: {
        advice:
          '今天适合把注意力收回来，先完成最关键的一件事。节奏不必快，但要稳，越是把手头的小事处理干净，越容易迎来后续的顺风局。',
        love:
          '沟通时别急着给结论，先把对方真正想表达的情绪听完整。温柔回应，比强行给建议更能拉近距离。',
        suggestions: ['先做最重要的一件事', '把决定控制在两到三个选项内'],
        avoidances: ['临时起意大改计划', '被外界节奏带着走'],
      },
      week: {
        advice:
          '这周整体呈现稳中有升的状态，适合整理资源、建立边界，并主动推进已经准备成熟的计划。你会发现越早开口，事情反而越顺。',
        love:
          '本周感情里的重点是“确认彼此期待”。如果有暧昧或误会，坦诚反而能帮你节省很多内耗。',
        suggestions: ['提早安排关键会议', '主动确认合作细节'],
        avoidances: ['拖到最后一刻才回复', '把压力憋在心里'],
      },
      month: {
        advice:
          '这个月更适合长期布局，而不是只盯着眼前反馈。把时间分给学习、复盘和人脉维护，会让你在下半月明显提速。',
        love:
          '情感上别只看短期热度，更要关注相处时是否稳定、轻松。让关系慢一点，反而更容易看清真正的价值。',
        suggestions: ['为自己留固定复盘时间', '主动维护一段重要关系'],
        avoidances: ['只凭一时情绪做决定', '透支精力去迎合所有人'],
      },
      year: {
        advice:
          '今年的关键词是“建立自己的主场”。只要愿意长期投入，你会逐渐摆脱被动应对，转向更主动、更有掌控感的状态。',
        love:
          '今年适合筛选真正值得同行的人。关系里的安全感，不来自频繁试探，而来自持续一致的行动。',
        suggestions: ['给长期目标设阶段节点', '把精力投向真正重要的人和事'],
        avoidances: ['被短期起伏轻易击退', '为了讨好别人放弃原则'],
      },
    },
  },
  'en-US': {
    title: 'Zodiac',
    share: 'Share',
    shareSuccess: 'Share link copied',
    shareFailed: 'Sharing is unavailable right now',
    chart: 'View Chart',
    tabs: {
      day: 'Day',
      week: 'Week',
      month: 'Month',
      year: 'Year',
    },
    today: 'Today',
    scoreLabel: 'Overall Score',
    scoreUnit: 'pts',
    boostTitle: 'Your energy looks strong today',
    boostDesc: 'Draw an inspiration card and add a little extra luck',
    freeDraw: 'Free Draw',
    loveReminder: 'Love Reminder',
    fromAstro: 'From your zodiac guide',
    fullText: 'Full text',
    suggestions: 'Do',
    avoidances: 'Avoid',
    askEntry: 'Today\'s Question',
    luckyElements: 'Lucky Elements',
    luckyNumbers: 'Lucky Numbers',
    dimensionSuffix: '',
    mood: 'Mood',
    sports: 'Action',
    surprise: 'Surprise',
    romance: 'Romance',
    dimensionLabels: {
      love: 'Love',
      wealth: 'Wealth',
      career: 'Career',
      study: 'Study',
      social: 'Social',
    },
    luckyLabels: {
      color: 'Color',
      accessory: 'Charm',
      time: 'Best Time',
      direction: 'Direction',
    },
    luckyValues: {
      color: 'Sapphire Blue',
      accessory: 'Clear Quartz',
      time: '05:00 - 07:00',
      direction: 'East',
    },
    question: 'Where should I focus my energy most today?',
    moodCards: [
      { icon: '😊', label: 'Mood' },
      { icon: '🏃', label: 'Action' },
      { icon: '🎁', label: 'Surprise' },
      { icon: '🌸', label: 'Romance' },
    ],
    narratives: {
      day: {
        advice:
          'Today favors focus over speed. Finish the single most important thing first, and the rest of the day will feel much more cooperative.',
        love:
          'Listen before you solve. A calm and attentive response will do more for intimacy than an instant opinion.',
        suggestions: ['Finish your top priority first', 'Keep your decisions down to two or three options'],
        avoidances: ['Changing plans on impulse', 'Letting outside noise dictate your pace'],
      },
      week: {
        advice:
          'This week trends upward if you organize your resources early and speak up before things become urgent. Clarity will create momentum.',
        love:
          'The emotional theme this week is alignment. Honest conversations about expectations will prevent unnecessary doubt.',
        suggestions: ['Schedule important talks early', 'Confirm details before moving forward'],
        avoidances: ['Replying at the last minute', 'Bottling up pressure'],
      },
      month: {
        advice:
          'This month rewards long-term positioning. Time spent on learning, review, and relationship upkeep will accelerate results later on.',
        love:
          'Do not chase intensity alone. Notice whether a relationship feels steady, easy, and sustainable in real life.',
        suggestions: ['Reserve time for review every week', 'Protect one meaningful relationship on purpose'],
        avoidances: ['Making decisions from temporary emotions', 'Draining yourself to please everyone'],
      },
      year: {
        advice:
          'Your yearly theme is building your own center of gravity. Consistency will move you from reacting to leading.',
        love:
          'This year is better for choosing depth over noise. Emotional safety grows from steady action, not repeated testing.',
        suggestions: ['Break big goals into stages', 'Invest in what matters most'],
        avoidances: ['Getting defeated by short-term swings', 'Giving up your standards to gain approval'],
      },
    },
  },
}

function getDailyValue(seed, min, max) {
  const today = new Date().toDateString()
  const hash = (today + seed)
    .split('')
    .reduce((acc, char) => ((acc << 5) - acc) + char.charCodeAt(0), 0)
  return min + (Math.abs(hash) % (max - min + 1))
}

function formatSelectedDate(date, locale, isToday, copy) {
  if (isToday) return copy.today
  return new Intl.DateTimeFormat(locale, {
    month: 'numeric',
    day: 'numeric',
  }).format(date)
}

function formatDayName(date, locale) {
  return new Intl.DateTimeFormat(locale, {
    weekday: 'short',
  }).format(date)
}

function buildDates(locale, copy) {
  const dates = []
  const today = new Date()

  for (let i = -2; i <= 4; i += 1) {
    const current = new Date(today)
    current.setDate(today.getDate() + i)
    const isToday = i === 0

    dates.push({
      key: current.toISOString().slice(0, 10),
      day: formatDayName(current, locale),
      dateNumber: current.getDate(),
      isToday,
      label: formatSelectedDate(current, locale, isToday, copy),
    })
  }

  return dates
}

function buildFortuneData(activeTab, copy) {
  const narrative = copy.narratives[activeTab]

  return {
    overallScore: getDailyOverallScore(70, 92),
    dimensions: [
      {
        key: 'love',
        label: copy.dimensionLabels.love,
        value: getDailyValue(`${activeTab}-love`, 60, 95),
        color: 'from-rose-300 to-red-500',
        icon: Heart,
      },
      {
        key: 'wealth',
        label: copy.dimensionLabels.wealth,
        value: getDailyValue(`${activeTab}-wealth`, 65, 95),
        color: 'from-amber-300 to-yellow-400',
        icon: Wallet,
      },
      {
        key: 'career',
        label: copy.dimensionLabels.career,
        value: getDailyValue(`${activeTab}-career`, 55, 90),
        color: 'from-orange-300 to-orange-500',
        icon: Briefcase,
      },
      {
        key: 'study',
        label: copy.dimensionLabels.study,
        value: getDailyValue(`${activeTab}-study`, 70, 99),
        color: 'from-amber-200 to-orange-400',
        icon: GraduationCap,
      },
      {
        key: 'social',
        label: copy.dimensionLabels.social,
        value: getDailyValue(`${activeTab}-social`, 65, 95),
        color: 'from-yellow-200 to-amber-500',
        icon: Users,
      },
    ],
    luckyItems: [
      { key: 'color', label: copy.luckyLabels.color, value: copy.luckyValues.color, icon: '🔷' },
      { key: 'accessory', label: copy.luckyLabels.accessory, value: copy.luckyValues.accessory, icon: '💎' },
      { key: 'time', label: copy.luckyLabels.time, value: copy.luckyValues.time, icon: '🌅' },
      { key: 'direction', label: copy.luckyLabels.direction, value: copy.luckyValues.direction, icon: '➡️' },
    ],
    luckyNumbers: [
      getDailyValue(`${activeTab}-number-a`, 3, 9),
      getDailyValue(`${activeTab}-number-b`, 10, 19),
    ],
    suggestions: narrative.suggestions,
    avoidances: narrative.avoidances,
    mainAdvice: narrative.advice,
    loveAdvice: narrative.love,
    question: copy.question,
  }
}

export default function ZodiacPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = ZODIAC_COPY[locale] || ZODIAC_COPY['zh-CN']
  const [activeTab, setActiveTab] = useState('day')
  const dates = buildDates(locale, copy)
  const [selectedDate, setSelectedDate] = useState(dates.find((item) => item.isToday)?.label || dates[0]?.label)
  const fortuneData = buildFortuneData(activeTab, copy)

  const handleShare = async () => {
    const shareData = {
      title: copy.title,
      text: `${copy.scoreLabel}: ${fortuneData.overallScore}${copy.scoreUnit}`,
      url: window.location.href,
    }

    try {
      if (navigator.share) {
        await navigator.share(shareData)
      } else if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(shareData.url)
        toast.success(copy.shareSuccess)
        return
      } else {
        throw new Error('share-not-supported')
      }
      toast.success(copy.shareSuccess)
    } catch (error) {
      if (error?.name === 'AbortError') return
      toast.error(copy.shareFailed)
    }
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-colors hover:bg-white/10"
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>

          <div className="flex items-center space-x-2">
            <span className="text-lg font-bold text-[#f4ece1]">{copy.title}</span>
            <span className="text-[#8f7b66]">✦</span>
          </div>

          <div className="flex items-center space-x-2">
            <button className="rounded-xl p-2 transition-colors hover:bg-white/10">
              <Clock size={20} className="text-[#bdaa94]" />
            </button>
            <button
              onClick={handleShare}
              className="rounded-xl p-2 transition-colors hover:bg-white/10"
              aria-label={copy.share}
            >
              <Share2 size={20} className="text-[#bdaa94]" />
            </button>
          </div>
        </div>

        <div className="app-sticky-inner flex items-center justify-between gap-3 pb-3">
          <div className="scrollbar-hide flex flex-1 space-x-6 overflow-x-auto whitespace-nowrap">
            {TAB_KEYS.map((tabKey) => (
              <button
                key={tabKey}
                onClick={() => setActiveTab(tabKey)}
                className={`mystic-tab ${activeTab === tabKey ? 'mystic-tab-active' : ''}`}
              >
                {copy.tabs[tabKey]}
              </button>
            ))}
          </div>
          <button className="flex items-center text-sm text-[#dcb86f] transition-colors hover:text-[#f0d9a5]">
            {copy.chart} <ChevronRight size={16} />
          </button>
        </div>

        <div className="app-sticky-inner scrollbar-hide flex space-x-2 overflow-x-auto pb-3">
          {dates.map((item) => (
            <button
              key={item.key}
              onClick={() => setSelectedDate(item.label)}
              className={`flex min-w-[56px] flex-col items-center rounded-[18px] px-3 py-2 transition-all ${
                selectedDate === item.label
                  ? 'border border-[#d0a85b]/30 bg-[#7a3218]/18 text-[#fff7eb]'
                  : 'text-[#bdaa94] hover:bg-white/[0.05]'
              }`}
            >
              <span className="text-xs">{item.day}</span>
              <span className={`text-sm font-semibold ${item.isToday ? '' : 'mt-0.5'}`}>
                {item.isToday ? copy.today : item.dateNumber}
              </span>
            </button>
          ))}
        </div>
      </div>

      <div className="app-page-shell-narrow pb-24">
        <div className="panel mt-4 p-5">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h3 className="mb-2 text-sm uppercase tracking-[0.18em] text-[#bdaa94]">{copy.scoreLabel}</h3>
              <div className="flex items-baseline">
                <span className="text-5xl font-bold text-[#f4ece1]">{fortuneData.overallScore}</span>
                <span className="ml-1 text-2xl text-[#8f7b66]">{copy.scoreUnit}</span>
              </div>
            </div>

            <div className="flex items-end justify-between gap-3 overflow-x-auto pb-1 lg:justify-start">
              {fortuneData.dimensions.map((dimension) => (
                <div key={dimension.key} className="flex flex-col items-center">
                  <div className="flex h-28 w-6 flex-col-reverse overflow-hidden rounded-full bg-white/10">
                    <div
                      className={`w-full rounded-full bg-gradient-to-t ${dimension.color} transition-all duration-700`}
                      style={{ height: `${dimension.value}%` }}
                    />
                  </div>
                  <span className="mt-2 text-sm font-bold text-[#f4ece1]">{dimension.value}</span>
                  <span className="text-xs text-[#8f7b66]">{dimension.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-4 flex flex-col gap-4 rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center space-x-3">
            <div className="mystic-icon-badge h-10 w-10 rounded-full">
              <Sparkles size={20} className="text-white" />
            </div>
            <div>
              <p className="font-semibold text-[#f4ece1]">{copy.boostTitle}</p>
              <p className="text-sm text-[#bdaa94]">{copy.boostDesc}</p>
            </div>
          </div>
          <button className="btn-primary-theme rounded-full px-4 py-2 text-sm font-medium text-white transition-all hover:shadow-xl">
            <span className="flex items-center space-x-1">
              <Gift size={16} />
              <span>{copy.freeDraw}</span>
            </span>
          </button>
        </div>

        <div className="panel mt-4 p-5">
          <p className="text-[15px] leading-relaxed text-[#f4ece1]">{fortuneData.mainAdvice}</p>
        </div>

        <div className="panel mt-4 p-5">
          <div className="mb-3 flex items-center space-x-2">
            <span className="font-semibold text-[#f4ece1]">{copy.loveReminder}</span>
            <span className="text-xs text-[#8f7b66]">- {copy.fromAstro}</span>
          </div>
          <p className="text-[15px] leading-relaxed text-[#e4d6c8]">
            {fortuneData.loveAdvice}
            <button className="ml-1 text-[#dcb86f]">{copy.fullText}</button>
          </p>
        </div>

        <div className="mt-4 flex flex-col space-y-3 md:flex-row md:space-x-3 md:space-y-0">
          <div className="panel-soft flex-1 rounded-[24px] p-4">
            <div className="mb-2 flex items-center space-x-2">
              <span className="font-semibold text-[#f4ece1]">{copy.suggestions}</span>
              <span className="text-green-500">💡</span>
            </div>
            <p className="text-sm text-[#bdaa94]">{fortuneData.suggestions.join(locale === 'en-US' ? ' • ' : '、')}</p>
          </div>
          <div className="panel-soft flex-1 rounded-[24px] p-4">
            <div className="mb-2 flex items-center space-x-2">
              <span className="font-semibold text-[#f4ece1]">{copy.avoidances}</span>
              <span className="text-orange-500">⚠️</span>
            </div>
            <p className="text-sm text-[#bdaa94]">{fortuneData.avoidances.join(locale === 'en-US' ? ' • ' : '、')}</p>
          </div>
        </div>

        <div className="mt-4 flex items-center justify-between rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
          <div className="flex items-center space-x-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-sm font-bold text-white">
              Q
            </div>
            <div>
              <p className="text-xs uppercase tracking-wide text-[#8f7b66]">{copy.askEntry}</p>
              <span className="text-[#e4d6c8]">{fortuneData.question}</span>
            </div>
          </div>
          <ChevronRight size={20} className="text-[#8f7b66]" />
        </div>

        <div className="panel mt-4 p-5">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-semibold text-[#f4ece1]">{copy.luckyElements}</h3>
            <span className="text-sm text-[#8f7b66]">{selectedDate}</span>
          </div>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            {fortuneData.luckyItems.map((item) => (
              <div key={item.key} className="flex flex-col items-center">
                <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-2xl border border-white/10 bg-white/[0.04] text-2xl">
                  {item.icon}
                </div>
                <span className="text-center text-sm font-medium text-[#f4ece1]">{item.value}</span>
                <span className="text-xs text-[#8f7b66]">{item.label}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="panel mt-4 p-5">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-semibold text-[#f4ece1]">{copy.luckyNumbers}</h3>
            <span className="text-sm text-[#8f7b66]">{copy.tabs[activeTab]}</span>
          </div>

          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:space-x-4">
            <div className="flex items-baseline justify-center space-x-2 sm:justify-start">
              {fortuneData.luckyNumbers.map((number) => (
                <span key={number} className="text-4xl font-bold text-[#f4ece1]">
                  {number}
                </span>
              ))}
            </div>

            <div className="grid flex-1 grid-cols-2 gap-3 sm:grid-cols-4">
              {copy.moodCards.map((item) => (
                <div key={item.label} className="flex flex-col items-center">
                  <span className="mb-1 text-2xl">{item.icon}</span>
                  <span className="text-xs text-[#8f7b66]">{item.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-4 space-y-3">
          {fortuneData.dimensions.map((dimension) => {
            const Icon = dimension.icon
            return (
              <div key={dimension.key} className="panel-soft rounded-[24px] p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div
                      className={`flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br ${dimension.color}`}
                    >
                      <Icon size={20} className="text-white" />
                    </div>
                    <div>
                      <span className="font-semibold text-[#f4ece1]">
                        {dimension.label}
                        {copy.dimensionSuffix}
                      </span>
                      <div className="mt-1 flex items-center space-x-2">
                        <div className="h-2 w-24 overflow-hidden rounded-full bg-white/10">
                          <div
                            className={`h-full rounded-full bg-gradient-to-r ${dimension.color}`}
                            style={{ width: `${dimension.value}%` }}
                          />
                        </div>
                        <span className="text-sm font-bold text-[#e4d6c8]">
                          {dimension.value}
                          {copy.scoreUnit}
                        </span>
                      </div>
                    </div>
                  </div>
                  <ChevronRight size={20} className="text-[#8f7b66]" />
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
