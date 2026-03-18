import { useState } from 'react'
import { Users, Heart, TrendingUp, Share2, Download } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardHeader, CardTitle, CardDescription, CardContent } from '../components/Card'
import Button from '../components/Button'
import Input, { Select } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import RadarChart from '../components/RadarChart'

/**
 * 八字合盘页面
 * 支持好友、伴侣合盘，生成可视化契合度报告
 */
export default function BaziCompatibilityPage() {
  const { t } = useTranslation()
  const { isLoggedIn } = useAuth()
  const [partnerType, setPartnerType] = useState('friend')
  const [partnerName, setPartnerName] = useState('')
  const [userBazi, setUserBazi] = useState('')
  const [partnerBazi, setPartnerBazi] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const partnerTypes = [
    { value: 'friend', label: '好友' },
    { value: 'lover', label: '恋人' },
    { value: 'spouse', label: '伴侣' },
  ]

  const handleCalculate = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录')
      return
    }

    if (!userBazi || !partnerBazi) {
      toast.warning('请填写完整的八字信息')
      return
    }

    setLoading(true)
    try {
      // TODO: 调用API进行合盘分析
      // const response = await api.post('/bazi/compatibility', {
      //   userBazi,
      //   partnerBazi,
      //   partnerType,
      //   partnerName,
      // })
      // setResult(response.data)

      // 示例数据
      setTimeout(() => {
        setResult({
          compatibilityScore: 85,
          compatibilityData: {
            personality: 90,
            career: 75,
            finance: 80,
            health: 85,
            relationship: 90,
          },
          aiAnalysis: '两人性格互补，相处融洽，适合长期发展。',
          suggestion: '建议多沟通，共同规划未来。',
        })
        setLoading(false)
        toast.success('合盘分析完成')
      }, 1500)
    } catch (error) {
      toast.error('分析失败，请重试')
      setLoading(false)
    }
  }

  const handleShare = async () => {
    if (!result) return

    try {
      const shareUrl = `${window.location.origin}/compatibility/${result.id}`
      await navigator.clipboard.writeText(shareUrl)
      toast.success('分享链接已复制')
    } catch (error) {
      toast.error('分享失败')
    }
  }

  const handleDownload = async () => {
    if (!result) return

    try {
      // TODO: 生成PDF或图片下载
      toast.success('下载成功')
    } catch (error) {
      toast.error('下载失败')
    }
  }

  return (
    <div className="page-shell" data-theme="bazi">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Users className="w-4 h-4 text-theme" />
            <span className="text-sm text-theme">八字合盘</span>
          </div>
          <h1 className="page-title font-serif-title text-white">八字合盘分析</h1>
          <p className="page-subtitle">分析两人八字契合度，查看相处建议</p>
        </div>
      </div>

      <div className="max-w-4xl mx-auto">
        {/* 输入表单 */}
        <Card className="panel mb-6">
          <CardHeader>
            <CardTitle className="section-title text-theme">合盘信息</CardTitle>
            <CardDescription>填写双方八字信息进行合盘分析</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <Select
                label="合盘对象类型"
                value={partnerType}
                onChange={(e) => setPartnerType(e.target.value)}
                options={partnerTypes.map(t => ({ value: t.value, label: t.label }))}
              />
              <Input
                label="对方姓名（可选）"
                value={partnerName}
                onChange={(e) => setPartnerName(e.target.value)}
                placeholder="输入对方姓名"
              />
            </div>

            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">我的八字</label>
                <input
                  type="text"
                  value={userBazi}
                  onChange={(e) => setUserBazi(e.target.value)}
                  placeholder="例如：甲子 乙丑 丙寅 丁卯"
                  className="w-full px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-purple-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">对方八字</label>
                <input
                  type="text"
                  value={partnerBazi}
                  onChange={(e) => setPartnerBazi(e.target.value)}
                  placeholder="例如：戊午 己未 庚申 辛酉"
                  className="w-full px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-purple-500"
                />
              </div>
            </div>

            <Button
              onClick={handleCalculate}
              loading={loading}
              disabled={!isLoggedIn || !userBazi || !partnerBazi}
              className="w-full btn-primary-theme"
            >
              <Heart size={18} />
              <span>开始合盘分析</span>
            </Button>
          </CardContent>
        </Card>

        {/* 合盘结果 */}
        {result && (
          <div className="space-y-6">
            {/* 契合度评分 */}
            <Card className="panel">
              <CardContent className="p-6">
                <div className="text-center mb-6">
                  <div className="text-6xl font-bold text-purple-400 mb-2">
                    {result.compatibilityScore}
                  </div>
                  <div className="text-gray-400">契合度评分</div>
                </div>

                {/* 雷达图 */}
                {result.compatibilityData && (
                  <div className="max-w-md mx-auto">
                    <RadarChart
                      data={[
                        { name: '性格', value: result.compatibilityData.personality },
                        { name: '事业', value: result.compatibilityData.career },
                        { name: '财运', value: result.compatibilityData.finance },
                        { name: '健康', value: result.compatibilityData.health },
                        { name: '感情', value: result.compatibilityData.relationship },
                      ]}
                    />
                  </div>
                )}
              </CardContent>
            </Card>

            {/* AI分析 */}
            <Card className="panel">
              <CardHeader>
                <CardTitle className="section-title text-theme">AI分析</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-300 mb-4">{result.aiAnalysis}</p>
                {result.suggestion && (
                  <div className="mt-4 p-4 bg-purple-500/10 border border-purple-500/30 rounded-lg">
                    <p className="text-sm font-medium text-purple-400 mb-2">相处建议：</p>
                    <p className="text-sm text-gray-300">{result.suggestion}</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* 操作按钮 */}
            <div className="flex gap-4">
              <Button
                onClick={handleShare}
                variant="secondary"
                className="flex-1 bg-green-500/20 hover:bg-green-500/30 text-green-400 border-green-500/30"
              >
                <Share2 size={18} />
                <span>分享结果</span>
              </Button>
              <Button
                onClick={handleDownload}
                variant="secondary"
                className="flex-1 bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 border-blue-500/30"
              >
                <Download size={18} />
                <span>下载报告</span>
              </Button>
            </div>
          </div>
        )}

        {/* 未登录提示 */}
        {!isLoggedIn && (
          <Card className="panel border-yellow-500/30 bg-yellow-500/10">
            <CardContent className="text-center py-6">
              <p className="text-gray-300 mb-4">登录后可使用合盘功能</p>
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
