import { useState, useEffect } from 'react'
import { Users, Gift, Copy, Share2, TrendingUp, Award } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardHeader, CardTitle, CardContent } from '../components/Card'
import Button from '../components/Button'
import { referralApi, creditApi } from '../api'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'

export default function ReferralPage() {
  const { t } = useTranslation()
  const [referralCode, setReferralCode] = useState('')
  const [stats, setStats] = useState({ total: 0, registered: 0, divined: 0, completed: 0, pending: 0 })
  const [userPoints, setUserPoints] = useState(0)
  const [invites, setInvites] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [codeRes, statsRes, balanceRes, recordsRes] = await Promise.all([
        referralApi.getReferralCode(),
        referralApi.getStats(),
        creditApi.getBalance(),
        referralApi.getRecords()
      ])
      
      // 后端返回格式：{ code: 200, message: "...", data: ... }
      if (codeRes.data?.code === 200) {
        setReferralCode(codeRes.data.data || '')
      }
      if (statsRes.data?.code === 200) {
        setStats(statsRes.data.data || {})
      }
      if (balanceRes.data?.code === 200) {
        // 修正：后端返回结构为 { data: { balance: 123 } }
        setUserPoints(balanceRes.data.data.balance || 0)
      }
      if (recordsRes.data?.code === 200) {
        setInvites(recordsRes.data.data || [])
      }
    } catch (error) {
      logger.error('加载数据失败:', error)
      toast.error('加载数据失败')
    } finally {
      setLoading(false)
    }
  }

  const shareUrl = `${window.location.origin}?ref=${referralCode}`
  const shareText = `我发现了一个超准的占卜应用！使用我的邀请码 ${referralCode} 注册，我们都能获得积分奖励！\n${shareUrl}`

  const handleCopyCode = () => {
    navigator.clipboard.writeText(referralCode)
    toast.success('邀请码已复制')
  }

  const handleCopyLink = () => {
    navigator.clipboard.writeText(shareText)
    toast.success('分享链接已复制')
  }

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: '邀请好友一起占卜',
          text: shareText,
          url: shareUrl
        })
        toast.success('分享成功！')
      } catch (error) {
        if (error.name !== 'AbortError') {
          handleCopyLink()
        }
      }
    } else {
      handleCopyLink()
    }
  }

  const totalReward = (stats.completed || 0) * 50 // 每个成功邀请50积分

  return (
    <div className="page-shell">
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold mb-2">邀请好友</h1>
          <p className="text-gray-400">分享给好友，双方都能获得丰厚奖励</p>
        </div>

        {/* 当前积分 */}
        <Card className="mb-6">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-400 mb-1">当前积分</p>
                <p className="text-3xl font-bold text-skin-primary">{userPoints}</p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-400 mb-1">累计获得</p>
                <p className="text-2xl font-bold">{totalReward}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 邀请码卡片 */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>我的邀请码</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center space-x-3 mb-4">
              <input
                type="text"
                value={referralCode}
                readOnly
                className="flex-1 px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-center font-mono font-bold text-xl"
              />
              <Button onClick={handleCopyCode} variant="secondary">
                <Copy size={18} />
                <span>复制</span>
              </Button>
            </div>
            <Button onClick={handleShare} className="w-full">
              <Share2 size={18} />
              <span>分享邀请链接</span>
            </Button>
          </CardContent>
        </Card>

        {/* 奖励规则 */}
        <div className="grid md:grid-cols-3 gap-6 mb-6">
          <Card>
            <CardContent className="p-6 text-center">
              <div className="w-12 h-12 rounded-full bg-green-500/20 flex items-center justify-center mx-auto mb-3">
                <Users className="text-green-400" size={24} />
              </div>
              <h3 className="font-bold mb-2">好友注册</h3>
              <p className="text-2xl font-bold text-skin-primary mb-1">+20 积分</p>
              <p className="text-xs text-gray-400">好友使用你的邀请码注册</p>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 text-center">
              <div className="w-12 h-12 rounded-full bg-blue-500/20 flex items-center justify-center mx-auto mb-3">
                <Gift className="text-blue-400" size={24} />
              </div>
              <h3 className="font-bold mb-2">好友首次占卜</h3>
              <p className="text-2xl font-bold text-skin-primary mb-1">+30 积分</p>
              <p className="text-xs text-gray-400">好友完成第一次占卜</p>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 text-center">
              <div className="w-12 h-12 rounded-full bg-purple-500/20 flex items-center justify-center mx-auto mb-3">
                <Award className="text-purple-400" size={24} />
              </div>
              <h3 className="font-bold mb-2">邀请达人</h3>
              <p className="text-2xl font-bold text-skin-primary mb-1">+100 积分</p>
              <p className="text-xs text-gray-400">成功邀请3位好友</p>
            </CardContent>
          </Card>
        </div>

        {/* 邀请统计 */}
        <Card>
          <CardHeader>
            <CardTitle>邀请统计</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-4 mb-6">
              <div className="text-center">
                <p className="text-3xl font-bold text-skin-primary mb-1">
                  {stats.total || 0}
                </p>
                <p className="text-sm text-gray-400">总邀请</p>
              </div>
              <div className="text-center">
                <p className="text-3xl font-bold text-green-400 mb-1">
                  {stats.completed || 0}
                </p>
                <p className="text-sm text-gray-400">已完成</p>
              </div>
              <div className="text-center">
                <p className="text-3xl font-bold text-yellow-400 mb-1">
                  {stats.pending || 0}
                </p>
                <p className="text-sm text-gray-400">待完成</p>
              </div>
            </div>

            {invites.length > 0 ? (
              <div className="space-y-2">
                <h3 className="text-sm font-medium mb-3">邀请记录</h3>
                {invites.map((invite) => (
                  <div
                    key={invite.id}
                    className="flex items-center justify-between p-3 bg-white/5 rounded-lg"
                  >
                    <div>
                      <p className="font-medium">推荐码：{invite.referralCode}</p>
                      <p className="text-xs text-gray-400">
                        {invite.createTime ? new Date(invite.createTime).toLocaleString('zh-CN') : ''}
                      </p>
                    </div>
                    <span
                      className={`px-3 py-1 rounded-full text-xs ${
                        invite.inviteStatus >= 3
                          ? 'bg-green-500/20 text-green-400'
                          : invite.inviteStatus >= 2
                          ? 'bg-blue-500/20 text-blue-400'
                          : invite.inviteStatus >= 1
                          ? 'bg-yellow-500/20 text-yellow-400'
                          : 'bg-gray-500/20 text-gray-400'
                      }`}
                    >
                      {invite.inviteStatus >= 3 ? '已完成' : 
                       invite.inviteStatus >= 2 ? '已占卜' :
                       invite.inviteStatus >= 1 ? '已注册' : '待注册'}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-400">
                <Users size={48} className="mx-auto mb-4 opacity-50" />
                <p>还没有邀请记录</p>
                <p className="text-sm mt-2">分享邀请码给好友开始获得奖励吧！</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 使用说明 */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>使用说明</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-sm text-gray-400">
              <div className="flex items-start space-x-3">
                <span className="text-skin-primary font-bold">1.</span>
                <p>复制你的邀请码或分享邀请链接给好友</p>
              </div>
              <div className="flex items-start space-x-3">
                <span className="text-skin-primary font-bold">2.</span>
                <p>好友使用你的邀请码注册后，你立即获得 20 积分</p>
              </div>
              <div className="flex items-start space-x-3">
                <span className="text-skin-primary font-bold">3.</span>
                <p>好友完成首次占卜后，你再获得 30 积分</p>
              </div>
              <div className="flex items-start space-x-3">
                <span className="text-skin-primary font-bold">4.</span>
                <p>积分可用于兑换免费占卜次数和高级功能</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
