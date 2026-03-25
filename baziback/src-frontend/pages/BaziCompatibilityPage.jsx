import { useMemo, useState } from 'react'
import { Download, Heart, Share2, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../components/Card'
import Button from '../components/Button'
import Input, { Select } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import RadarChart from '../components/RadarChart'
import { resolvePageLocale } from '../utils/displayText'

const BAZI_COMPATIBILITY_COPY = {
  'zh-CN': {
    badge: '八字合盘',
    title: '八字合盘分析',
    subtitle: '分析双方八字契合度，查看相处建议与关系走势。',
    formTitle: '合盘信息',
    formDescription: '填写双方八字信息，生成当前关系的结构化参考。',
    partnerTypeLabel: '关系类型',
    partnerNameLabel: '对方姓名',
    partnerNamePlaceholder: '输入对方姓名，可留空',
    yourBaziLabel: '我的八字',
    yourBaziPlaceholder: '例如：甲子 乙丑 丙寅 丁卯',
    partnerBaziLabel: '对方八字',
    partnerBaziPlaceholder: '例如：戊午 己未 庚申 辛酉',
    startAnalysis: '开始合盘分析',
    scoreLabel: '契合度评分',
    aiAnalysisTitle: 'AI 分析',
    suggestionTitle: '相处建议',
    shareResult: '分享结果',
    downloadReport: '下载报告',
    loginHint: '登录后可使用完整的八字合盘功能。',
    goLogin: '去登录',
    loginFirst: '请先登录',
    missingInput: '请填写完整的双方八字信息',
    analysisSuccess: '合盘分析完成',
    analysisFailed: '分析失败，请稍后重试',
    shareSuccess: '分享链接已复制',
    shareFailed: '分享失败，请稍后重试',
    downloadSuccess: '报告已准备完成',
    downloadFailed: '下载失败，请稍后重试',
    partnerTypes: {
      friend: '好友',
      lover: '恋人',
      spouse: '伴侣',
    },
    radarTitle: '关系维度雷达图',
    radarDimensions: {
      personality: '性格',
      career: '事业',
      finance: '财务',
      health: '健康',
      relationship: '感情',
    },
    sample: {
      aiAnalysis:
        '双方的相处节奏比较协调，价值观上也有不少可互相支持的部分，适合通过稳定沟通继续深化关系。',
      suggestion:
        '建议把重要安排提前说清楚，遇到分歧时多确认彼此真正的需求，再决定推进方式。',
    },
  },
  'en-US': {
    badge: 'Bazi Match',
    title: 'Bazi Compatibility Analysis',
    subtitle:
      'Review mutual compatibility, relationship dynamics, and practical guidance.',
    formTitle: 'Compatibility Details',
    formDescription:
      'Fill in both Bazi profiles to generate a structured relationship reference.',
    partnerTypeLabel: 'Relationship type',
    partnerNameLabel: 'Partner name',
    partnerNamePlaceholder: 'Enter the other person’s name, optional',
    yourBaziLabel: 'My Bazi',
    yourBaziPlaceholder: 'Example: Jia Zi Yi Chou Bing Yin Ding Mao',
    partnerBaziLabel: 'Partner Bazi',
    partnerBaziPlaceholder: 'Example: Wu Wu Ji Wei Geng Shen Xin You',
    startAnalysis: 'Start compatibility reading',
    scoreLabel: 'Compatibility Score',
    aiAnalysisTitle: 'AI Analysis',
    suggestionTitle: 'Practical Guidance',
    shareResult: 'Share result',
    downloadReport: 'Download report',
    loginHint: 'Sign in to unlock the full compatibility feature set.',
    goLogin: 'Go to sign in',
    loginFirst: 'Please sign in first',
    missingInput: 'Please fill in both Bazi profiles',
    analysisSuccess: 'Compatibility analysis is ready',
    analysisFailed: 'Analysis failed. Please try again later',
    shareSuccess: 'Share link copied',
    shareFailed: 'Unable to share right now',
    downloadSuccess: 'Report is ready to download',
    downloadFailed: 'Download failed. Please try again later',
    partnerTypes: {
      friend: 'Friend',
      lover: 'Lover',
      spouse: 'Partner',
    },
    radarTitle: 'Relationship Radar',
    radarDimensions: {
      personality: 'Personality',
      career: 'Career',
      finance: 'Finance',
      health: 'Health',
      relationship: 'Relationship',
    },
    sample: {
      aiAnalysis:
        'Your interaction styles are fairly well aligned, and there are several areas where you can support each other long term.',
      suggestion:
        'Clarify important plans early, and when disagreements appear, confirm the real need behind each viewpoint before deciding next steps.',
    },
  },
}

export default function BaziCompatibilityPage() {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = BAZI_COMPATIBILITY_COPY[locale]
  const { isLoggedIn } = useAuth()
  const [partnerType, setPartnerType] = useState('friend')
  const [partnerName, setPartnerName] = useState('')
  const [userBazi, setUserBazi] = useState('')
  const [partnerBazi, setPartnerBazi] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const partnerTypeOptions = useMemo(
    () => [
      { value: 'friend', label: copy.partnerTypes.friend },
      { value: 'lover', label: copy.partnerTypes.lover },
      { value: 'spouse', label: copy.partnerTypes.spouse },
    ],
    [copy.partnerTypes]
  )

  const handleCalculate = async () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      return
    }

    if (!userBazi.trim() || !partnerBazi.trim()) {
      toast.warning(copy.missingInput)
      return
    }

    setLoading(true)
    try {
      setTimeout(() => {
        setResult({
          id: 'preview',
          compatibilityScore: 85,
          compatibilityData: {
            personality: 90,
            career: 75,
            finance: 80,
            health: 85,
            relationship: 90,
          },
          aiAnalysis: copy.sample.aiAnalysis,
          suggestion: copy.sample.suggestion,
        })
        setLoading(false)
        toast.success(copy.analysisSuccess)
      }, 1500)
    } catch (error) {
      toast.error(copy.analysisFailed)
      setLoading(false)
    }
  }

  const handleShare = async () => {
    if (!result) return

    try {
      const shareUrl = `${window.location.origin}/compatibility/${result.id || 'preview'}`
      await navigator.clipboard.writeText(shareUrl)
      toast.success(copy.shareSuccess)
    } catch (error) {
      toast.error(copy.shareFailed)
    }
  }

  const handleDownload = async () => {
    if (!result) return

    try {
      toast.success(copy.downloadSuccess)
    } catch (error) {
      toast.error(copy.downloadFailed)
    }
  }

  const radarData = result?.compatibilityData
    ? [
        {
          name: copy.radarDimensions.personality,
          value: result.compatibilityData.personality,
        },
        {
          name: copy.radarDimensions.career,
          value: result.compatibilityData.career,
        },
        {
          name: copy.radarDimensions.finance,
          value: result.compatibilityData.finance,
        },
        {
          name: copy.radarDimensions.health,
          value: result.compatibilityData.health,
        },
        {
          name: copy.radarDimensions.relationship,
          value: result.compatibilityData.relationship,
        },
      ]
    : []

  return (
    <div className="page-shell" data-theme="bazi">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Users className="text-theme h-4 w-4" />
            <span className="text-theme text-sm">{copy.badge}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">
            {copy.title}
          </h1>
          <p className="page-subtitle">{copy.subtitle}</p>
        </div>
      </div>

      <div className="mx-auto max-w-4xl">
        <Card className="panel mb-6">
          <CardHeader>
            <CardTitle className="section-title text-theme">
              {copy.formTitle}
            </CardTitle>
            <CardDescription>{copy.formDescription}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Select
                label={copy.partnerTypeLabel}
                value={partnerType}
                onChange={(event) => setPartnerType(event.target.value)}
                options={partnerTypeOptions}
              />
              <Input
                label={copy.partnerNameLabel}
                value={partnerName}
                onChange={(event) => setPartnerName(event.target.value)}
                placeholder={copy.partnerNamePlaceholder}
              />
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <label className="mb-2 block text-sm font-medium">
                  {copy.yourBaziLabel}
                </label>
                <input
                  type="text"
                  value={userBazi}
                  onChange={(event) => setUserBazi(event.target.value)}
                  placeholder={copy.yourBaziPlaceholder}
                  className="mystic-input w-full px-4 py-2 text-[#f4ece1] placeholder-[#8f7b66]"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">
                  {copy.partnerBaziLabel}
                </label>
                <input
                  type="text"
                  value={partnerBazi}
                  onChange={(event) => setPartnerBazi(event.target.value)}
                  placeholder={copy.partnerBaziPlaceholder}
                  className="mystic-input w-full px-4 py-2 text-[#f4ece1] placeholder-[#8f7b66]"
                />
              </div>
            </div>

            <Button
              onClick={handleCalculate}
              loading={loading}
              disabled={!isLoggedIn || !userBazi.trim() || !partnerBazi.trim()}
              className="btn-primary-theme w-full"
            >
              <Heart size={18} />
              <span>{copy.startAnalysis}</span>
            </Button>
          </CardContent>
        </Card>

        {result ? (
          <div className="space-y-6">
            <Card className="panel">
              <CardContent className="p-6">
                <div className="mb-6 text-center">
                  <div className="mb-2 text-6xl font-bold text-[#f0d9a5]">
                    {result.compatibilityScore}
                  </div>
                  <div className="text-[#8f7b66]">{copy.scoreLabel}</div>
                </div>

                {radarData.length > 0 ? (
                  <div className="mx-auto max-w-md">
                    <RadarChart data={radarData} title={copy.radarTitle} />
                  </div>
                ) : null}
              </CardContent>
            </Card>

            <Card className="panel">
              <CardHeader>
                <CardTitle className="section-title text-theme">
                  {copy.aiAnalysisTitle}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="mb-4 text-[#e4d6c8]">{result.aiAnalysis}</p>
                {result.suggestion ? (
                  <div className="mt-4 rounded-lg border border-[#d0a85b]/20 bg-[#7a3218]/14 p-4">
                    <p className="mb-2 text-sm font-medium text-[#dcb86f]">
                      {copy.suggestionTitle}
                    </p>
                    <p className="text-sm text-[#e4d6c8]">{result.suggestion}</p>
                  </div>
                ) : null}
              </CardContent>
            </Card>

            <div className="flex gap-4">
              <Button
                onClick={handleShare}
                variant="secondary"
                className="flex-1 border-[#d0a85b]/24 bg-[#6a4a1e]/18 text-[#f0d9a5] hover:bg-[#6a4a1e]/28"
              >
                <Share2 size={18} />
                <span>{copy.shareResult}</span>
              </Button>
              <Button
                onClick={handleDownload}
                variant="secondary"
                className="flex-1 border-[#a34224]/24 bg-[#7a3218]/18 text-[#e19a84] hover:bg-[#7a3218]/28"
              >
                <Download size={18} />
                <span>{copy.downloadReport}</span>
              </Button>
            </div>
          </div>
        ) : null}

        {!isLoggedIn ? (
          <Card className="panel border-yellow-500/30 bg-yellow-500/10">
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
