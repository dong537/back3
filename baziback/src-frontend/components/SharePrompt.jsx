import { useEffect, useMemo, useState } from 'react'
import { Gift, Share2, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from './Card'
import Button from './Button'
import { getReferralCode, points } from '../utils/referral'
import { toast } from './Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeText } from '../utils/displayText'
import { getTarotCardName } from '../utils/tarotText'

const SHARE_PROMPT_COPY = {
  'zh-CN': {
    close: '关闭',
    title: '分享有礼',
    subtitle: '把结果分享给好友，双方都有机会获得积分奖励。',
    shareReward: '分享奖励',
    inviteReward: '好友注册奖励',
    firstReadingReward: '好友首次占卜',
    myCode: '我的邀请码',
    copyCode: '复制',
    copyCodeSuccess: '邀请码已复制',
    nativeShare: '立即分享',
    copyLink: '复制分享链接',
    later: '稍后再说',
    shareDialogTitle: '占卜结果分享',
    shareSuccess: '分享成功，已获得 10 积分',
    rewardReason: '分享奖励',
    linkCopied: '分享链接已复制，发给好友即可',
    shareFailed: '分享失败，请稍后重试',
    fallbackText: '我刚完成了一次占卜，快来试试看吧。',
    shareText: {
      yijing: (hexagram, question) =>
        `我刚完成了一次易经占卜：${hexagram}${question ? ` - ${question}` : ''}`,
      tarot: (cards) => `我刚抽到了这些塔罗牌：${cards}`,
      bazi: (value) => `这是我的八字排盘：${value}`,
    },
  },
  'en-US': {
    close: 'Close',
    title: 'Share and Earn',
    subtitle:
      'Share your result with friends and both of you can earn credits.',
    shareReward: 'Share reward',
    inviteReward: 'Friend signup reward',
    firstReadingReward: 'Friend first reading',
    myCode: 'My invite code',
    copyCode: 'Copy',
    copyCodeSuccess: 'Invite code copied',
    nativeShare: 'Share now',
    copyLink: 'Copy share link',
    later: 'Maybe later',
    shareDialogTitle: 'Reading Result Share',
    shareSuccess: 'Shared successfully. You earned 10 credits.',
    rewardReason: 'Share reward',
    linkCopied: 'Share link copied. Send it to your friends.',
    shareFailed: 'Share failed. Please try again later.',
    fallbackText: 'I just completed a reading. Give it a try too!',
    shareText: {
      yijing: (hexagram, question) =>
        `I just completed a Yijing reading: ${hexagram}${question ? ` - ${question}` : ''}`,
      tarot: (cards) => `I just drew these tarot cards: ${cards}`,
      bazi: (value) => `Here is my Bazi chart: ${value}`,
    },
  },
}

export default function SharePrompt({
  isOpen,
  onClose,
  shareData,
  shareType = 'yijing',
  onShareSuccess,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SHARE_PROMPT_COPY[locale]
  const [referralCode] = useState(getReferralCode())
  const [shareUrl, setShareUrl] = useState('')

  const shareText = useMemo(() => {
    if (shareType === 'yijing' && shareData?.original?.chinese) {
      return copy.shareText.yijing(
        shareData.original.chinese,
        safeText(shareData.question)
      )
    }

    if (shareType === 'tarot' && Array.isArray(shareData?.cards)) {
      const names = shareData.cards
        .map((card) => getTarotCardName(card, locale))
        .filter(Boolean)
        .join(locale === 'en-US' ? ', ' : '、')
      return copy.shareText.tarot(names)
    }

    const baziText = safeText(shareData?.['八字'] ?? shareData?.bazi)
    if (shareType === 'bazi' && baziText) {
      return copy.shareText.bazi(baziText)
    }

    return copy.fallbackText
  }, [copy, locale, shareData, shareType])

  useEffect(() => {
    if (!isOpen || !shareData) {
      setShareUrl('')
      return
    }

    const payload = encodeURIComponent(JSON.stringify(shareData))
    setShareUrl(
      `${window.location.origin}/share?code=${referralCode}&type=${shareType}&data=${payload}`
    )
  }, [isOpen, referralCode, shareData, shareType])

  const rewardShareSuccess = (message = copy.shareSuccess) => {
    onShareSuccess?.()
    points.add(10, copy.rewardReason)
    toast.success(message)
    onClose()
  }

  const handleShare = async (platform) => {
    const fullText = `${shareText}\n\n${shareUrl}\n\n${referralCode}`

    try {
      if (platform === 'native' && navigator.share) {
        await navigator.share({
          title: copy.shareDialogTitle,
          text: shareText,
          url: shareUrl,
        })
        rewardShareSuccess()
        return
      }

      if (platform === 'copy') {
        await navigator.clipboard.writeText(fullText)
        rewardShareSuccess(copy.linkCopied)
      }
    } catch (error) {
      if (error?.name !== 'AbortError') {
        logger.error('Share prompt action failed:', error)
        toast.error(copy.shareFailed)
      }
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel relative w-full max-w-md border-white/10 bg-[linear-gradient(180deg,rgba(26,19,18,0.96),rgba(14,11,10,0.88))]">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>

        <div className="p-6">
          <div className="mb-6 text-center">
            <div className="mystic-icon-badge mb-4 inline-flex h-16 w-16 rounded-full">
              <Gift size={32} className="text-white" />
            </div>
            <h2 className="mb-2 text-2xl font-bold text-[#f4ece1]">
              {copy.title}
            </h2>
            <p className="text-[#bdaa94]">{copy.subtitle}</p>
          </div>

          <div className="mystic-muted-box mb-6 space-y-2">
            <RewardRow label={copy.shareReward} value="+10" />
            <RewardRow label={copy.inviteReward} value="+20" />
            <RewardRow label={copy.firstReadingReward} value="+30" />
          </div>

          <div className="mb-6">
            <label className="mb-2 block text-sm text-[#8f7b66]">
              {copy.myCode}
            </label>
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={referralCode}
                readOnly
                aria-label={copy.myCode}
                className="mystic-input flex-1 text-center font-mono text-lg font-bold"
              />
              <Button
                onClick={() => {
                  navigator.clipboard.writeText(referralCode)
                  toast.success(copy.copyCodeSuccess)
                }}
                variant="secondary"
                size="sm"
                className="border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
                title={copy.copyCode}
                aria-label={copy.copyCode}
              >
                {copy.copyCode}
              </Button>
            </div>
          </div>

          <div className="space-y-3">
            {navigator.share && (
              <Button
                onClick={() => handleShare('native')}
                className="w-full"
                title={copy.nativeShare}
                aria-label={copy.nativeShare}
              >
                <Share2 size={18} />
                <span>{copy.nativeShare}</span>
              </Button>
            )}

            <Button
              onClick={() => handleShare('copy')}
              variant="secondary"
              className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
              title={copy.copyLink}
              aria-label={copy.copyLink}
            >
              <Share2 size={18} />
              <span>{copy.copyLink}</span>
            </Button>

            <div className="text-center">
              <button
                onClick={onClose}
                className="text-sm text-[#8f7b66] transition hover:text-[#f4ece1]"
              >
                {copy.later}
              </button>
            </div>
          </div>
        </div>
      </Card>
    </div>
  )
}

function RewardRow({ label, value }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-[#8f7b66]">{label}</span>
      <span className="font-bold text-[#dcb86f]">{value}</span>
    </div>
  )
}
