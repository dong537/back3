import { useState, useEffect } from 'react'
import { X, Share2, MessageCircle, Users, Link2, Copy, Check, QrCode, Gift, Sparkles } from 'lucide-react'
import Card from './Card'
import Button from './Button'
import { toast } from './Toast'
import { logger } from '../utils/logger'

/**
 * 分享引导组件
 * 在占卜结果生成后引导用户分享
 */
export default function ShareGuide({ 
  isOpen, 
  onClose, 
  shareData, 
  shareType,
  onShareSuccess 
}) {
  const [copied, setCopied] = useState(false)
  const [showReward, setShowReward] = useState(false)

  useEffect(() => {
    if (isOpen) {
      // 检查是否首次分享，显示奖励提示
      const shareCount = parseInt(localStorage.getItem('share_count') || '0')
      if (shareCount === 0) {
        setShowReward(true)
      }
    }
  }, [isOpen])

  const shareChannels = [
    {
      name: '微信好友',
      icon: MessageCircle,
      color: 'bg-green-500',
      action: () => handleWeChatShare()
    },
    {
      name: '朋友圈',
      icon: Users,
      color: 'bg-green-600',
      action: () => handleWeChatMoments()
    },
    {
      name: 'QQ好友',
      icon: MessageCircle,
      color: 'bg-blue-500',
      action: () => handleQQShare()
    },
    {
      name: '复制链接',
      icon: Link2,
      color: 'bg-purple-500',
      action: () => handleCopyLink()
    }
  ]

  const getShareText = () => {
    if (shareType === 'yijing' && shareData?.original) {
      return `我刚刚占卜了：${shareData.original.chinese}卦\n${shareData.question || ''}\n${window.location.href}`
    }
    if (shareType === 'tarot' && shareData?.cards) {
      return `我刚刚抽了塔罗牌：${shareData.cards.map(c => c.name).join('、')}\n问题：${shareData.question || ''}\n${window.location.href}`
    }
    if (shareType === 'bazi' && shareData?.八字) {
      return `我的八字排盘：${shareData.八字}\n${window.location.href}`
    }
    return `我刚刚进行了占卜，来看看结果吧！\n${window.location.href}`
  }

  const handleWeChatShare = () => {
    // 微信分享需要特殊处理
    const shareText = getShareText()
    if (window.wx) {
      // 微信JS-SDK
      window.wx.updateTimelineShareData({
        title: '占卜结果分享',
        link: window.location.href,
        imgUrl: window.location.origin + '/logo.png'
      })
    } else {
      // 降级方案：复制文本
      handleCopyLink()
      toast.info('请打开微信，粘贴链接分享给好友')
    }
    trackShare('wechat')
  }

  const handleWeChatMoments = () => {
    if (window.wx) {
      window.wx.updateTimelineShareData({
        title: getShareText(),
        link: window.location.href
      })
    } else {
      handleCopyLink()
      toast.info('请打开微信朋友圈，粘贴链接分享')
    }
    trackShare('moments')
  }

  const handleQQShare = () => {
    const shareText = getShareText()
    if (window.mqq) {
      // QQ分享
      window.mqq.ui.shareMessage({
        title: '占卜结果',
        description: shareText,
        url: window.location.href
      })
    } else {
      handleCopyLink()
      toast.info('请打开QQ，粘贴链接分享')
    }
    trackShare('qq')
  }

  const handleCopyLink = async () => {
    const shareText = getShareText()
    try {
      await navigator.clipboard.writeText(shareText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
      toast.success('链接已复制到剪贴板')
      trackShare('copy')
    } catch (error) {
      toast.error('复制失败')
    }
  }

  const handleNativeShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: '占卜结果分享',
          text: getShareText(),
          url: window.location.href
        })
        trackShare('native')
      } catch (error) {
        if (error.name !== 'AbortError') {
          logger.error('分享失败:', error)
        }
      }
    } else {
      handleCopyLink()
    }
  }

  const trackShare = (channel) => {
    // 记录分享
    const shareCount = parseInt(localStorage.getItem('share_count') || '0') + 1
    localStorage.setItem('share_count', shareCount.toString())
    
    // 记录分享渠道
    const channelStats = JSON.parse(localStorage.getItem('share_channel_stats') || '{}')
    channelStats[channel] = (channelStats[channel] || 0) + 1
    localStorage.setItem('share_channel_stats', JSON.stringify(channelStats))

    // 触发分享成功回调
    onShareSuccess?.(channel, shareCount)

    // 显示奖励提示
    if (shareCount === 1) {
      toast.success('首次分享成功！获得 10 积分奖励 🎉')
    } else if (shareCount % 5 === 0) {
      toast.success(`分享达人！已分享 ${shareCount} 次，获得额外奖励 🎁`)
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <Card className="w-full max-w-md relative overflow-hidden">
        {/* 关闭按钮 */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition z-10"
        >
          <X size={20} />
        </button>

        {/* 奖励提示 */}
        {showReward && (
          <div className="absolute top-0 left-0 right-0 bg-gradient-to-r from-yellow-500/20 to-orange-500/20 p-3 text-center">
            <div className="flex items-center justify-center space-x-2 text-yellow-400">
              <Gift size={18} />
              <span className="text-sm font-medium">首次分享可获得 10 积分奖励！</span>
            </div>
          </div>
        )}

        <div className="p-6 pt-8">
          {/* 标题 */}
          <div className="text-center mb-6">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 mb-4">
              <Share2 size={32} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold mb-2">分享占卜结果</h2>
            <p className="text-sm text-gray-400">
              分享给好友，一起探索命运奥秘
            </p>
          </div>

          {/* 分享渠道 */}
          <div className="grid grid-cols-2 gap-3 mb-6">
            {shareChannels.map((channel, index) => {
              const Icon = channel.icon
              return (
                <button
                  key={index}
                  onClick={channel.action}
                  className={`flex flex-col items-center justify-center p-4 rounded-xl ${channel.color} text-white hover:scale-105 transition-transform`}
                >
                  <Icon size={24} className="mb-2" />
                  <span className="text-sm font-medium">{channel.name}</span>
                </button>
              )
            })}
          </div>

          {/* 原生分享按钮 */}
          {navigator.share && (
            <Button
              onClick={handleNativeShare}
              className="w-full mb-4"
              variant="secondary"
            >
              <Share2 size={18} />
              <span>更多分享方式</span>
            </Button>
          )}

          {/* 复制链接 */}
          <div className="flex items-center space-x-2 p-3 bg-white/5 rounded-lg">
            <input
              type="text"
              value={window.location.href}
              readOnly
              className="flex-1 bg-transparent text-sm text-gray-300 truncate"
            />
            <button
              onClick={handleCopyLink}
              className="p-2 hover:bg-white/10 rounded transition"
            >
              {copied ? (
                <Check size={18} className="text-green-400" />
              ) : (
                <Copy size={18} className="text-gray-400" />
              )}
            </button>
          </div>

          {/* 分享奖励提示 */}
          <div className="mt-4 p-3 bg-purple-500/10 border border-purple-500/20 rounded-lg">
            <div className="flex items-center space-x-2 text-sm text-purple-300">
              <Sparkles size={16} />
              <span>每次分享可获得 10 积分，邀请好友注册可获得更多奖励！</span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}
