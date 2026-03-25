import { useEffect, useMemo, useState } from 'react'
import { Check, Copy, Download, Share2, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from './Card'
import Button from './Button'
import { toast } from './Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeText } from '../utils/displayText'
import { getTarotCardName } from '../utils/tarotText'

const SHARE_MODAL_COPY = {
  'zh-CN': {
    close: '关闭',
    title: '分享结果',
    linkLabel: '分享链接',
    copyLink: '复制链接',
    share: '分享',
    download: '下载图片',
    copySuccess: '链接已复制',
    copyFailed: '复制失败，请手动复制',
    shareFailed: '分享失败，请稍后重试',
    defaultTitle: '占卜结果',
    fallbackText: '我刚完成了一次占卜，来看看结果吧。',
    titles: {
      yijing: '易经占卜结果',
      tarot: '塔罗占卜结果',
      bazi: '八字排盘结果',
      zodiac: '星座运势',
    },
    shareText: {
      yijing: (hexagram, question) =>
        `我刚完成了一次易经占卜：${hexagram}${question ? ` - ${question}` : ''}`,
      tarot: (cards) => `我刚抽到的塔罗牌是：${cards}`,
      bazi: (value) => `这是我的八字排盘：${value}`,
    },
  },
  'en-US': {
    close: 'Close',
    title: 'Share Result',
    linkLabel: 'Share link',
    copyLink: 'Copy link',
    share: 'Share',
    download: 'Download image',
    copySuccess: 'Link copied',
    copyFailed: 'Copy failed. Please copy it manually.',
    shareFailed: 'Share failed. Please try again later.',
    defaultTitle: 'Reading Result',
    fallbackText: 'I just completed a reading. Come see the result.',
    titles: {
      yijing: 'Yijing Reading',
      tarot: 'Tarot Reading',
      bazi: 'Bazi Chart',
      zodiac: 'Zodiac Forecast',
    },
    shareText: {
      yijing: (hexagram, question) =>
        `I just completed a Yijing reading: ${hexagram}${question ? ` - ${question}` : ''}`,
      tarot: (cards) => `I just drew tarot cards: ${cards}`,
      bazi: (value) => `Here is my Bazi chart: ${value}`,
    },
  },
}

export default function ShareModal({ isOpen, onClose, data, type }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SHARE_MODAL_COPY[locale]
  const [copied, setCopied] = useState(false)
  const [shareUrl, setShareUrl] = useState('')

  const title = useMemo(
    () => copy.titles[type] || copy.defaultTitle,
    [copy, type]
  )

  useEffect(() => {
    if (!isOpen || !data) {
      setShareUrl('')
      return
    }

    try {
      const payload = encodeURIComponent(
        JSON.stringify({
          type,
          timestamp: new Date().toISOString(),
          data,
        })
      )
      setShareUrl(`${window.location.origin}/share?payload=${payload}`)
    } catch (error) {
      logger.error('Build share url failed:', error)
      setShareUrl(window.location.href)
    }
  }, [data, isOpen, type])

  const getShareText = () => {
    if (type === 'yijing' && data?.original?.chinese) {
      return copy.shareText.yijing(
        data.original.chinese,
        safeText(data.question)
      )
    }

    if (type === 'tarot' && Array.isArray(data?.cards) && data.cards.length > 0) {
      const cards = data.cards
        .map((card) => getTarotCardName(card, locale))
        .filter(Boolean)
        .join(locale === 'en-US' ? ', ' : '、')
      return copy.shareText.tarot(cards)
    }

    const baziText = safeText(data?.['八字'] ?? data?.bazi)
    if (type === 'bazi' && baziText) {
      return copy.shareText.bazi(baziText)
    }

    return copy.fallbackText
  }

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl)
      setCopied(true)
      toast.success(copy.copySuccess)
      window.setTimeout(() => setCopied(false), 2000)
    } catch (error) {
      logger.error('Copy share url failed:', error)
      toast.error(copy.copyFailed)
    }
  }

  const handleDownload = () => {
    const canvas = document.createElement('canvas')
    canvas.width = 800
    canvas.height = 1200
    const context = canvas.getContext('2d')
    if (!context) return

    context.fillStyle = '#100c0b'
    context.fillRect(0, 0, canvas.width, canvas.height)

    const gradient = context.createLinearGradient(0, 0, canvas.width, 0)
    gradient.addColorStop(0, '#f6e7cf')
    gradient.addColorStop(0.5, '#dcb86f')
    gradient.addColorStop(1, '#e19a84')

    context.fillStyle = gradient
    context.font = 'bold 38px serif'
    context.fillText(title, 48, 90)

    context.fillStyle = '#bdaa94'
    context.font = '24px serif'
    const text = getShareText()
    const lines = text.match(/.{1,22}/g) || [text]
    lines.forEach((line, index) => {
      context.fillText(line, 48, 160 + index * 40)
    })

    canvas.toBlob((blob) => {
      if (!blob) return
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${title}_${Date.now()}.png`
      link.click()
      URL.revokeObjectURL(url)
    })
  }

  const handleNativeShare = async () => {
    if (!navigator.share) {
      await handleCopy()
      return
    }

    try {
      await navigator.share({
        title,
        text: getShareText(),
        url: shareUrl,
      })
    } catch (error) {
      if (error?.name !== 'AbortError') {
        logger.error('Native share failed:', error)
        toast.error(copy.shareFailed)
      }
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <Card className="panel w-full max-w-md border-white/10 bg-[linear-gradient(180deg,rgba(26,19,18,0.96),rgba(14,11,10,0.88))]">
        <div className="flex items-center justify-between border-b border-white/10 p-6">
          <h2 className="text-xl font-bold text-[#f4ece1]">{copy.title}</h2>
          <button
            onClick={onClose}
            className="rounded-full p-2 text-[#8f7b66] transition hover:bg-white/[0.05] hover:text-[#f4ece1]"
            title={copy.close}
            aria-label={copy.close}
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-4 p-6">
          <div>
            <label className="mb-2 block text-sm text-[#8f7b66]">
              {copy.linkLabel}
            </label>
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={shareUrl}
                readOnly
                aria-label={copy.linkLabel}
                className="mystic-input flex-1 text-sm"
              />
              <Button
                onClick={handleCopy}
                variant="secondary"
                size="sm"
                className="border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
                title={copy.copyLink}
                aria-label={copy.copyLink}
              >
                {copied ? <Check size={16} /> : <Copy size={16} />}
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Button
              onClick={handleNativeShare}
              className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
              variant="secondary"
              title={copy.share}
              aria-label={copy.share}
            >
              <Share2 size={18} />
              <span>{copy.share}</span>
            </Button>
            <Button
              onClick={handleDownload}
              className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
              variant="secondary"
              title={copy.download}
              aria-label={copy.download}
            >
              <Download size={18} />
              <span>{copy.download}</span>
            </Button>
          </div>

          <div className="rounded-[22px] border border-white/10 bg-white/[0.03] px-4 py-4">
            <p className="text-center text-xs leading-6 text-[#bdaa94]">
              {getShareText()}
            </p>
          </div>
        </div>
      </Card>
    </div>
  )
}
