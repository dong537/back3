import { useState, useEffect } from 'react'
import { X, Share2, Gift, Users, Sparkles } from 'lucide-react'
import Card from './Card'
import Button from './Button'
import { getReferralCode, points } from '../utils/referral'
import { toast } from './Toast'
import { logger } from '../utils/logger'

/**
 * 分享引导弹窗
 * 在用户完成占卜后弹出，引导分享
 */
export default function SharePrompt({ 
  isOpen, 
  onClose, 
  shareData, 
  shareType = 'yijing',
  onShareSuccess 
}) {
  const [referralCode] = useState(getReferralCode())
  const [shareUrl, setShareUrl] = useState('')

  useEffect(() => {
    if (isOpen && shareData) {
      const shareText = getShareText()
      const url = `${window.location.origin}/share?code=${referralCode}&type=${shareType}&data=${encodeURIComponent(JSON.stringify(shareData))}`
      setShareUrl(url)
    }
  }, [isOpen, shareData, shareType, referralCode])

  const getShareText = () => {
    if (shareType === 'yijing' && shareData?.original) {
      return `我刚刚占卜了：${shareData.original.chinese}卦 - ${shareData.question || ''}\n快来试试吧！`
    }
    if (shareType === 'tarot' && shareData?.cards) {
      return `我刚刚抽了塔罗牌：${shareData.cards.map(c => c.name).join('、')}\n快来试试吧！`
    }
    if (shareType === 'bazi' && shareData?.八字) {
      return `我的八字排盘：${shareData.八字}\n快来试试吧！`
    }
    return '我刚刚进行了占卜，来看看结果吧！'
  }

  const handleShare = async (platform) => {
    const shareText = getShareText()
    const fullText = `${shareText}\n\n使用我的邀请码 ${referralCode} 注册，我们都能获得奖励！\n${shareUrl}`

    try {
      if (platform === 'native' && navigator.share) {
        await navigator.share({
          title: '占卜结果分享',
          text: shareText,
          url: shareUrl
        })
        onShareSuccess?.()
        points.add(10, '分享奖励')
        toast.success('分享成功！获得10积分')
        onClose()
      } else if (platform === 'copy') {
        await navigator.clipboard.writeText(fullText)
        toast.success('链接已复制！分享给好友可获得积分奖励')
        onClose()
      } else if (platform === 'wechat') {
        // 微信分享（需要配置）
        toast.info('请点击右上角分享到微信')
      }
    } catch (error) {
      if (error.name !== 'AbortError') {
        logger.error('分享失败:', error)
        toast.error('分享失败，请重试')
      }
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <Card className="w-full max-w-md relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition"
        >
          <X size={20} />
        </button>

        <div className="p-6">
          {/* 奖励提示 */}
          <div className="text-center mb-6">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 mb-4">
              <Gift size={32} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold mb-2">分享有礼！</h2>
            <p className="text-gray-400">
              分享给好友，双方都能获得积分奖励
            </p>
          </div>

          {/* 奖励说明 */}
          <div className="bg-white/5 rounded-lg p-4 mb-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-400">分享奖励</span>
              <span className="text-skin-primary font-bold">+10 积分</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-400">好友注册奖励</span>
              <span className="text-skin-primary font-bold">+20 积分</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-400">好友首次占卜</span>
              <span className="text-skin-primary font-bold">+30 积分</span>
            </div>
          </div>

          {/* 邀请码 */}
          <div className="mb-6">
            <label className="text-sm text-gray-400 mb-2 block">我的邀请码</label>
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={referralCode}
                readOnly
                className="flex-1 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-center font-mono font-bold text-lg"
              />
              <Button
                onClick={() => {
                  navigator.clipboard.writeText(referralCode)
                  toast.success('邀请码已复制')
                }}
                variant="secondary"
                size="sm"
              >
                复制
              </Button>
            </div>
          </div>

          {/* 分享按钮 */}
          <div className="space-y-3">
            {navigator.share && (
              <Button
                onClick={() => handleShare('native')}
                className="w-full"
              >
                <Share2 size={18} />
                <span>分享到...</span>
              </Button>
            )}
            <Button
              onClick={() => handleShare('copy')}
              variant="secondary"
              className="w-full"
            >
              <Share2 size={18} />
              <span>复制分享链接</span>
            </Button>
            <div className="text-center">
              <button
                onClick={onClose}
                className="text-sm text-gray-400 hover:text-gray-300 transition"
              >
                稍后分享
              </button>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}
