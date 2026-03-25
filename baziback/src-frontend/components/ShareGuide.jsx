import { useEffect, useMemo, useState } from 'react'
import {
  Check,
  Copy,
  Gift,
  Link2,
  MessageCircle,
  Share2,
  Users,
  X,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from './Card'
import Button from './Button'
import { toast } from './Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeText } from '../utils/displayText'
import { getTarotCardName } from '../utils/tarotText'

const SHARE_GUIDE_COPY = {
  'zh-CN': {
    close: '关闭',
    firstReward: '首次分享可获得 10 积分奖励',
    title: '分享占卜结果',
    subtitle: '把结果发给好友，一起看看这份卦意和指引。',
    moreWays: '更多分享方式',
    rewardHint: '每次分享可获得 10 积分，邀请好友注册还能解锁更多奖励。',
    copySuccess: '分享内容已复制到剪贴板',
    copyFailed: '复制失败',
    wechatHint: '请打开微信，把复制的内容发送给好友。',
    momentsHint: '请打开朋友圈，把复制的内容粘贴后发布。',
    qqHint: '请打开 QQ，把复制的内容发送给好友。',
    firstShareReward: '首次分享成功，已获得 10 积分奖励。',
    milestoneReward: (count) =>
      `分享达人，已经累计分享 ${count} 次，继续保持。`,
    fallbackText: '我刚完成了一次占卜，快来看看结果吧。',
    channels: {
      wechat: '微信好友',
      moments: '朋友圈',
      qq: 'QQ 好友',
      copy: '复制链接',
    },
    shareText: {
      yijing: (hexagram, question, url) =>
        `我刚完成了一次易经占卜：${hexagram}${question ? `\n问题：${question}` : ''}\n${url}`,
      tarot: (cards, question, url) =>
        `我刚抽到了这些塔罗牌：${cards}${question ? `\n问题：${question}` : ''}\n${url}`,
      bazi: (value, url) => `这是我的八字排盘：${value}\n${url}`,
    },
  },
  'en-US': {
    close: 'Close',
    firstReward: 'Your first share earns 10 credits',
    title: 'Share Your Reading',
    subtitle: 'Share the result with friends and explore the meaning together.',
    moreWays: 'More share options',
    rewardHint:
      'Every share earns 10 credits, and inviting friends unlocks more rewards.',
    copySuccess: 'Share content copied to clipboard',
    copyFailed: 'Copy failed',
    wechatHint: 'Open WeChat and send the copied text to your friends.',
    momentsHint: 'Open Moments and paste the copied text to share it.',
    qqHint: 'Open QQ and send the copied text to your friends.',
    firstShareReward: 'First share complete. You earned 10 credits.',
    milestoneReward: (count) =>
      `Share streak unlocked. You have shared ${count} times so far.`,
    fallbackText: 'I just completed a reading. Take a look at the result.',
    channels: {
      wechat: 'WeChat',
      moments: 'Moments',
      qq: 'QQ',
      copy: 'Copy link',
    },
    shareText: {
      yijing: (hexagram, question, url) =>
        `I just completed a Yijing reading: ${hexagram}${question ? `\nQuestion: ${question}` : ''}\n${url}`,
      tarot: (cards, question, url) =>
        `I just drew tarot cards: ${cards}${question ? `\nQuestion: ${question}` : ''}\n${url}`,
      bazi: (value, url) => `Here is my Bazi chart: ${value}\n${url}`,
    },
  },
}

export default function ShareGuide({
  isOpen,
  onClose,
  shareData,
  shareType,
  onShareSuccess,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SHARE_GUIDE_COPY[locale]
  const [copied, setCopied] = useState(false)
  const [showReward, setShowReward] = useState(false)

  useEffect(() => {
    if (!isOpen) return
    const shareCount = Number.parseInt(
      localStorage.getItem('share_count') || '0',
      10
    )
    setShowReward(shareCount === 0)
  }, [isOpen])

  const shareText = useMemo(() => {
    const url = window.location.href

    if (shareType === 'yijing' && shareData?.original?.chinese) {
      return copy.shareText.yijing(
        shareData.original.chinese,
        safeText(shareData.question),
        url
      )
    }

    if (shareType === 'tarot' && Array.isArray(shareData?.cards)) {
      const cards = shareData.cards
        .map((card) => getTarotCardName(card, locale))
        .filter(Boolean)
        .join(locale === 'en-US' ? ', ' : '、')
      return copy.shareText.tarot(cards, safeText(shareData.question), url)
    }

    const baziText = safeText(shareData?.['八字'] ?? shareData?.bazi)
    if (shareType === 'bazi' && baziText) {
      return copy.shareText.bazi(baziText, url)
    }

    return `${copy.fallbackText}\n${url}`
  }, [copy, locale, shareData, shareType])

  const trackShare = (channel) => {
    const shareCount =
      Number.parseInt(localStorage.getItem('share_count') || '0', 10) + 1
    localStorage.setItem('share_count', String(shareCount))

    const channelStats = JSON.parse(
      localStorage.getItem('share_channel_stats') || '{}'
    )
    channelStats[channel] = (channelStats[channel] || 0) + 1
    localStorage.setItem('share_channel_stats', JSON.stringify(channelStats))

    onShareSuccess?.(channel, shareCount)

    if (shareCount === 1) {
      toast.success(copy.firstShareReward)
    } else if (shareCount % 5 === 0) {
      toast.success(copy.milestoneReward(shareCount))
    }
  }

  const copyShareText = async (channel) => {
    try {
      await navigator.clipboard.writeText(shareText)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 2000)
      trackShare(channel)
      return true
    } catch (error) {
      logger.error('Copy share text failed:', error)
      toast.error(copy.copyFailed)
      return false
    }
  }

  const handleCopyLink = async () => {
    const ok = await copyShareText('copy')
    if (ok) {
      toast.success(copy.copySuccess)
    }
  }

  const handleWeChatShare = async () => {
    const ok = await copyShareText('wechat')
    if (ok) {
      toast.info(copy.wechatHint)
    }
  }

  const handleWeChatMoments = async () => {
    const ok = await copyShareText('moments')
    if (ok) {
      toast.info(copy.momentsHint)
    }
  }

  const handleQQShare = async () => {
    const ok = await copyShareText('qq')
    if (ok) {
      toast.info(copy.qqHint)
    }
  }

  const handleNativeShare = async () => {
    if (!navigator.share) {
      await handleCopyLink()
      return
    }

    try {
      await navigator.share({
        title: copy.title,
        text: shareText,
        url: window.location.href,
      })
      trackShare('native')
    } catch (error) {
      if (error?.name !== 'AbortError') {
        logger.error('Native share failed:', error)
      }
    }
  }

  if (!isOpen) return null

  const shareChannels = [
    {
      name: copy.channels.wechat,
      icon: MessageCircle,
      toneClass:
        'border-[#d0a85b]/20 bg-[#1c1614]/88 text-[#f4ece1] hover:border-[#d0a85b]/36 hover:bg-[#231b18]',
      iconClass: 'bg-[#6a4a1e]/24 text-[#dcb86f]',
      action: handleWeChatShare,
    },
    {
      name: copy.channels.moments,
      icon: Users,
      toneClass:
        'border-[#a34224]/20 bg-[#1f1716]/88 text-[#f4ece1] hover:border-[#cd7840]/36 hover:bg-[#261c1b]',
      iconClass: 'bg-[#7a3218]/24 text-[#e19a84]',
      action: handleWeChatMoments,
    },
    {
      name: copy.channels.qq,
      icon: MessageCircle,
      toneClass:
        'border-[#b88a3d]/20 bg-[#1d1714]/88 text-[#f4ece1] hover:border-[#dcb86f]/36 hover:bg-[#241d19]',
      iconClass: 'bg-[#8f5c1f]/24 text-[#f0d9a5]',
      action: handleQQShare,
    },
    {
      name: copy.channels.copy,
      icon: Link2,
      toneClass:
        'border-white/10 bg-white/[0.04] text-[#f4ece1] hover:border-[#d0a85b]/24 hover:bg-white/[0.07]',
      iconClass: 'bg-white/[0.06] text-[#bdaa94]',
      action: handleCopyLink,
    },
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel relative w-full max-w-md overflow-hidden border-white/10 bg-[linear-gradient(180deg,rgba(26,19,18,0.96),rgba(14,11,10,0.88))]">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>

        {showReward && (
          <div className="absolute left-0 right-0 top-0 bg-[linear-gradient(135deg,rgba(143,92,31,0.22),rgba(208,168,91,0.16))] p-3 text-center">
            <div className="flex items-center justify-center gap-2 text-[#f0d9a5]">
              <Gift size={18} />
              <span className="text-sm font-medium">{copy.firstReward}</span>
            </div>
          </div>
        )}

        <div className="p-6 pt-8">
          <div className="mb-6 text-center">
            <div className="mystic-icon-badge mb-4 inline-flex h-16 w-16 rounded-full">
              <Share2 size={32} className="text-white" />
            </div>
            <h2 className="mb-2 text-2xl font-bold text-[#f4ece1]">
              {copy.title}
            </h2>
            <p className="text-sm text-[#bdaa94]">{copy.subtitle}</p>
          </div>

          <div className="mb-6 grid grid-cols-2 gap-3">
            {shareChannels.map((channel) => {
              const Icon = channel.icon
              return (
                <button
                  key={channel.name}
                  onClick={channel.action}
                  className={`rounded-[22px] border p-4 text-left transition-all duration-300 hover:-translate-y-0.5 ${channel.toneClass}`}
                  title={channel.name}
                  aria-label={channel.name}
                >
                  <div
                    className={`mb-3 flex h-11 w-11 items-center justify-center rounded-2xl ${channel.iconClass}`}
                  >
                    <Icon size={20} />
                  </div>
                  <span className="text-sm font-medium">{channel.name}</span>
                </button>
              )
            })}
          </div>

          {navigator.share && (
            <Button
              onClick={handleNativeShare}
              className="mb-4 w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
              variant="secondary"
            >
              <Share2 size={18} />
              <span>{copy.moreWays}</span>
            </Button>
          )}

          <div className="rounded-[22px] border border-white/10 bg-white/[0.03] p-3">
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={window.location.href}
                readOnly
                aria-label={copy.channels.copy}
                className="flex-1 truncate bg-transparent text-sm text-[#e4d6c8] outline-none"
              />
              <button
                onClick={handleCopyLink}
                className="rounded-xl p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
                title={copy.channels.copy}
                aria-label={copy.channels.copy}
              >
                {copied ? (
                  <Check size={18} className="text-[#dcb86f]" />
                ) : (
                  <Copy size={18} className="text-[#8f7b66]" />
                )}
              </button>
            </div>
          </div>

          <div className="mt-4 rounded-[22px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 p-3">
            <div className="text-sm text-[#dcb86f]">{copy.rewardHint}</div>
          </div>
        </div>
      </Card>
    </div>
  )
}
