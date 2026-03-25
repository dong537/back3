import { useMemo, useState } from 'react'
import { Brain, Calendar, Heart, Star } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../components/Card'
import Button from '../components/Button'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import { resolvePageLocale } from '../utils/displayText'

const DAILY_TEST_COPY = {
  'zh-CN': {
    badge: '每日互动',
    title: '每日测试与心情记录',
    subtitle: '记录今天的状态，并快速查看今日指引。',
    moodTitle: '今日心情',
    moodDescription: '选择心情类型并记录今天的主观感受。',
    moodTypeLabel: '心情类型',
    moodScoreLabel: (score) => `心情评分：${score}/10`,
    moodTextLabel: '心情描述',
    moodTextPlaceholder: '记录一下今天发生了什么，或者你最在意的事情。',
    dateLabel: '记录日期',
    saveMood: '保存心情记录',
    testTitle: '每日测试',
    testDescription: '选择一种方式，快速看一眼今天的重点提醒。',
    resultTitle: '测试结果',
    resultScore: (score) => `得分：${score}/100`,
    loginHint: '登录后可使用完整的每日记录与测试功能。',
    goLogin: '去登录',
    loginFirst: '请先登录',
    moodSaved: '心情记录已保存',
    moodSaveFailed: '保存失败，请稍后重试',
    testSuccess: '测试完成',
    testFailed: '测试失败，请稍后重试',
    moods: {
      happy: '开心',
      sad: '难过',
      anxious: '焦虑',
      calm: '平静',
      excited: '兴奋',
    },
    tests: {
      bazi_fortune: {
        label: '今日八字运势',
        desc: '快速查看今日整体状态与节奏。',
      },
      yijing_divination: {
        label: '每日一卦',
        desc: '用一条简洁卦象提示今天的关键方向。',
      },
      tarot_draw: {
        label: '今日塔罗',
        desc: '抽一张今日牌，看看今天更适合如何行动。',
      },
    },
    summaries: {
      bazi_fortune: '今天整体运势较稳，适合把重点放在已有计划的推进上。',
      yijing_divination: '今天更适合先观察再行动，保持节奏比一味求快更重要。',
      tarot_draw: '今天的重点是信任直觉，但也别忽略现实中的细节与边界。',
    },
  },
  'en-US': {
    badge: 'Daily Check-in',
    title: 'Daily Tests and Mood Journal',
    subtitle: 'Capture how today feels and get a quick read on today’s focus.',
    moodTitle: 'Today’s mood',
    moodDescription:
      'Choose a mood type and write down how the day feels from your side.',
    moodTypeLabel: 'Mood type',
    moodScoreLabel: (score) => `Mood score: ${score}/10`,
    moodTextLabel: 'Mood notes',
    moodTextPlaceholder:
      'Write down what happened today or what feels most important right now.',
    dateLabel: 'Entry date',
    saveMood: 'Save mood entry',
    testTitle: 'Daily tests',
    testDescription:
      'Pick one format and get a quick signal about today’s direction.',
    resultTitle: 'Result',
    resultScore: (score) => `Score: ${score}/100`,
    loginHint: 'Sign in to use the full daily journal and testing features.',
    goLogin: 'Go to sign in',
    loginFirst: 'Please sign in first',
    moodSaved: 'Mood entry saved',
    moodSaveFailed: 'Unable to save right now',
    testSuccess: 'Test completed',
    testFailed: 'Test failed. Please try again later',
    moods: {
      happy: 'Happy',
      sad: 'Sad',
      anxious: 'Anxious',
      calm: 'Calm',
      excited: 'Excited',
    },
    tests: {
      bazi_fortune: {
        label: 'Today’s Bazi outlook',
        desc: 'Check today’s general rhythm and momentum at a glance.',
      },
      yijing_divination: {
        label: 'Daily hexagram',
        desc: 'Get one short Yijing prompt to frame today’s focus.',
      },
      tarot_draw: {
        label: 'Today’s tarot',
        desc: 'Draw one card and see what kind of action suits today best.',
      },
    },
    summaries: {
      bazi_fortune:
        'Today feels steady overall, so it is a good time to keep moving existing plans forward.',
      yijing_divination:
        'Observation matters more than speed today. Hold your rhythm before making a hard push.',
      tarot_draw:
        'Trust your intuition today, but keep one eye on practical details and boundaries.',
    },
  },
}

export default function DailyTestPage() {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = DAILY_TEST_COPY[locale]
  const { isLoggedIn } = useAuth()
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  )
  const [moodScore, setMoodScore] = useState(5)
  const [moodType, setMoodType] = useState('calm')
  const [moodDesc, setMoodDesc] = useState('')
  const [testResult, setTestResult] = useState(null)
  const [loading, setLoading] = useState(false)

  const moodTypes = useMemo(
    () => [
      {
        value: 'happy',
        label: copy.moods.happy,
        icon: '😊',
        color: 'text-yellow-400',
      },
      {
        value: 'sad',
        label: copy.moods.sad,
        icon: '😢',
        color: 'text-blue-400',
      },
      {
        value: 'anxious',
        label: copy.moods.anxious,
        icon: '😰',
        color: 'text-orange-400',
      },
      {
        value: 'calm',
        label: copy.moods.calm,
        icon: '😌',
        color: 'text-green-400',
      },
      {
        value: 'excited',
        label: copy.moods.excited,
        icon: '🤩',
        color: 'text-pink-400',
      },
    ],
    [copy.moods]
  )

  const testTypes = useMemo(
    () => [
      {
        value: 'bazi_fortune',
        label: copy.tests.bazi_fortune.label,
        icon: Calendar,
        desc: copy.tests.bazi_fortune.desc,
      },
      {
        value: 'yijing_divination',
        label: copy.tests.yijing_divination.label,
        icon: Brain,
        desc: copy.tests.yijing_divination.desc,
      },
      {
        value: 'tarot_draw',
        label: copy.tests.tarot_draw.label,
        icon: Star,
        desc: copy.tests.tarot_draw.desc,
      },
    ],
    [copy.tests]
  )

  const handleMoodRecord = async () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      return
    }

    setLoading(true)
    try {
      toast.success(copy.moodSaved)
      setMoodDesc('')
    } catch (error) {
      toast.error(copy.moodSaveFailed)
    } finally {
      setLoading(false)
    }
  }

  const handleDailyTest = async (testType) => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      return
    }

    setLoading(true)
    try {
      setTestResult({
        type: testType,
        summary: copy.summaries[testType],
        score: 85,
      })
      toast.success(copy.testSuccess)
    } catch (error) {
      toast.error(copy.testFailed)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Calendar className="text-theme h-4 w-4" />
            <span className="text-theme text-sm">{copy.badge}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">
            {copy.title}
          </h1>
          <p className="page-subtitle">{copy.subtitle}</p>
        </div>
      </div>

      <div className="mx-auto max-w-4xl space-y-6">
        <Card className="panel">
          <CardHeader>
            <CardTitle className="section-title text-theme">
              {copy.moodTitle}
            </CardTitle>
            <CardDescription>{copy.moodDescription}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="mb-2 block text-sm font-medium">
                {copy.dateLabel}
              </label>
              <input
                type="date"
                value={selectedDate}
                onChange={(event) => setSelectedDate(event.target.value)}
                className="mystic-input py-2"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium">
                {copy.moodTypeLabel}
              </label>
              <div className="grid grid-cols-5 gap-2">
                {moodTypes.map((mood) => (
                  <button
                    key={mood.value}
                    onClick={() => setMoodType(mood.value)}
                    className={`rounded-[20px] border p-3 transition-all ${
                      moodType === mood.value
                        ? 'border-[#d0a85b]/35 bg-[#7a3218]/20'
                        : 'border-white/10 bg-white/[0.02] hover:border-[#d0a85b]/25'
                    }`}
                  >
                    <div className={`mb-1 text-2xl ${mood.color}`}>
                      {mood.icon}
                    </div>
                    <div className="text-xs text-gray-300">{mood.label}</div>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium">
                {copy.moodScoreLabel(moodScore)}
              </label>
              <input
                type="range"
                min="1"
                max="10"
                value={moodScore}
                onChange={(event) => setMoodScore(Number(event.target.value))}
                className="w-full"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium">
                {copy.moodTextLabel}
              </label>
              <textarea
                value={moodDesc}
                onChange={(event) => setMoodDesc(event.target.value)}
                placeholder={copy.moodTextPlaceholder}
                rows={3}
                className="mystic-input"
              />
            </div>

            <Button
              onClick={handleMoodRecord}
              loading={loading}
              disabled={!isLoggedIn}
              className="btn-primary-theme w-full"
            >
              <Heart size={18} />
              <span>{copy.saveMood}</span>
            </Button>
          </CardContent>
        </Card>

        <Card className="panel">
          <CardHeader>
            <CardTitle className="section-title text-theme">
              {copy.testTitle}
            </CardTitle>
            <CardDescription>{copy.testDescription}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-3">
              {testTypes.map((test) => {
                const Icon = test.icon
                return (
                  <button
                    key={test.value}
                    onClick={() => handleDailyTest(test.value)}
                    disabled={!isLoggedIn || loading}
                    className="rounded-[24px] border border-white/10 bg-white/[0.02] p-6 text-left transition-all hover:border-[#d0a85b]/25 hover:bg-white/[0.05] disabled:opacity-50"
                  >
                    <Icon className="mb-3 h-8 w-8 text-[#d0a85b]" />
                    <div className="mb-1 font-medium text-white">
                      {test.label}
                    </div>
                    <div className="text-sm text-[#bdaa94]">{test.desc}</div>
                  </button>
                )
              })}
            </div>

            {testResult ? (
              <div className="mt-6 rounded-[24px] border border-[#d0a85b]/25 bg-[#7a3218]/15 p-4">
                <div className="mb-2 flex items-center justify-between">
                  <span className="font-medium">{copy.resultTitle}</span>
                  <span className="text-[#d0a85b]">
                    {copy.resultScore(testResult.score)}
                  </span>
                </div>
                <p className="text-gray-300">{testResult.summary}</p>
              </div>
            ) : null}
          </CardContent>
        </Card>

        {!isLoggedIn ? (
          <Card className="panel border-[#d0a85b]/25 bg-[#7a3218]/10">
            <CardContent className="py-6 text-center">
              <p className="mb-4 text-gray-300">{copy.loginHint}</p>
              <Button
                variant="secondary"
                onClick={() => {
                  window.location.href = '/login'
                }}
              >
                {copy.goLogin}
              </Button>
            </CardContent>
          </Card>
        ) : null}
      </div>
    </div>
  )
}
