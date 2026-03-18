import { useState, useEffect } from 'react'
import { Calendar, Brain, Star, TrendingUp, Heart, Briefcase, Wallet, Activity } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardHeader, CardTitle, CardDescription, CardContent } from '../components/Card'
import Button from '../components/Button'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'

/**
 * 每日测试页面
 * 提供每日运势测试、心情记录等功能
 */
export default function DailyTestPage() {
  const { t } = useTranslation()
  const { isLoggedIn, user } = useAuth()
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [moodScore, setMoodScore] = useState(5)
  const [moodType, setMoodType] = useState('calm')
  const [moodDesc, setMoodDesc] = useState('')
  const [testResult, setTestResult] = useState(null)
  const [loading, setLoading] = useState(false)

  const moodTypes = [
    { value: 'happy', label: '开心', icon: '😊', color: 'text-yellow-400' },
    { value: 'sad', label: '难过', icon: '😢', color: 'text-blue-400' },
    { value: 'anxious', label: '焦虑', icon: '😰', color: 'text-orange-400' },
    { value: 'calm', label: '平静', icon: '😌', color: 'text-green-400' },
    { value: 'excited', label: '兴奋', icon: '🤩', color: 'text-pink-400' },
  ]

  const testTypes = [
    { value: 'bazi_fortune', label: '今日八字运势', icon: Calendar, desc: '查看今日运势' },
    { value: 'yijing_divination', label: '每日一卦', icon: Brain, desc: '每日卦象指引' },
    { value: 'tarot_draw', label: '今日塔罗', icon: Star, desc: '抽取今日塔罗牌' },
  ]

  const handleMoodRecord = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录')
      return
    }

    setLoading(true)
    try {
      // TODO: 调用API保存心情记录
      toast.success('心情记录已保存')
      setMoodDesc('')
    } catch (error) {
      toast.error('保存失败，请重试')
    } finally {
      setLoading(false)
    }
  }

  const handleDailyTest = async (testType) => {
    if (!isLoggedIn) {
      toast.warning('请先登录')
      return
    }

    setLoading(true)
    try {
      // TODO: 调用API进行每日测试
      setTestResult({
        type: testType,
        summary: '今日运势良好，宜积极行动',
        score: 85
      })
      toast.success('测试完成')
    } catch (error) {
      toast.error('测试失败，请重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Calendar className="w-4 h-4 text-theme" />
            <span className="text-sm text-theme">每日互动</span>
          </div>
          <h1 className="page-title font-serif-title text-white">每日测试与心情记录</h1>
          <p className="page-subtitle">记录每日心情，测试今日运势</p>
        </div>
      </div>

      <div className="max-w-4xl mx-auto space-y-6">
        {/* 心情记录卡片 */}
        <Card className="panel">
          <CardHeader>
            <CardTitle className="section-title text-theme">今日心情</CardTitle>
            <CardDescription>记录您今天的心情状态</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">心情类型</label>
              <div className="grid grid-cols-5 gap-2">
                {moodTypes.map((mood) => (
                  <button
                    key={mood.value}
                    onClick={() => setMoodType(mood.value)}
                    className={`p-3 rounded-lg border transition-all ${
                      moodType === mood.value
                        ? 'border-purple-500 bg-purple-500/20'
                        : 'border-white/10 hover:border-purple-500/50'
                    }`}
                  >
                    <div className={`text-2xl mb-1 ${mood.color}`}>{mood.icon}</div>
                    <div className="text-xs text-gray-300">{mood.label}</div>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                心情评分：{moodScore}/10
              </label>
              <input
                type="range"
                min="1"
                max="10"
                value={moodScore}
                onChange={(e) => setMoodScore(parseInt(e.target.value))}
                className="w-full"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">心情描述（可选）</label>
              <textarea
                value={moodDesc}
                onChange={(e) => setMoodDesc(e.target.value)}
                placeholder="记录今天的心情..."
                rows={3}
                className="w-full px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-purple-500"
              />
            </div>

            <Button
              onClick={handleMoodRecord}
              loading={loading}
              disabled={!isLoggedIn}
              className="w-full btn-primary-theme"
            >
              <Heart size={18} />
              <span>保存心情记录</span>
            </Button>
          </CardContent>
        </Card>

        {/* 每日测试卡片 */}
        <Card className="panel">
          <CardHeader>
            <CardTitle className="section-title text-theme">每日测试</CardTitle>
            <CardDescription>选择测试类型，查看今日运势</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              {testTypes.map((test) => {
                const Icon = test.icon
                return (
                  <button
                    key={test.value}
                    onClick={() => handleDailyTest(test.value)}
                    disabled={!isLoggedIn || loading}
                    className="p-6 rounded-xl border border-white/10 hover:border-purple-500/50 hover:bg-white/5 transition-all text-left disabled:opacity-50"
                  >
                    <Icon className="w-8 h-8 text-purple-400 mb-3" />
                    <div className="font-medium text-white mb-1">{test.label}</div>
                    <div className="text-sm text-gray-400">{test.desc}</div>
                  </button>
                )
              })}
            </div>

            {testResult && (
              <div className="mt-6 p-4 bg-purple-500/10 border border-purple-500/30 rounded-lg">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-medium">测试结果</span>
                  <span className="text-purple-400">得分：{testResult.score}/100</span>
                </div>
                <p className="text-gray-300">{testResult.summary}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 未登录提示 */}
        {!isLoggedIn && (
          <Card className="panel border-yellow-500/30 bg-yellow-500/10">
            <CardContent className="text-center py-6">
              <p className="text-gray-300 mb-4">登录后可使用完整功能</p>
              <Button variant="secondary" onClick={() => window.location.href = '/login'}>
                去登录
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
