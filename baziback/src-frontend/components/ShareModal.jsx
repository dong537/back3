import { useState, useEffect } from 'react'
import { X, Copy, Download, Share2, Check } from 'lucide-react'
import Card from './Card'
import Button from './Button'
import { logger } from '../utils/logger'

export default function ShareModal({ isOpen, onClose, data, type }) {
  const [copied, setCopied] = useState(false)
  const [shareUrl, setShareUrl] = useState('')

  useEffect(() => {
    if (isOpen && data) {
      // 生成分享链接
      const shareData = {
        type,
        timestamp: new Date().toISOString(),
        data: data
      }
      const encoded = btoa(JSON.stringify(shareData))
      const url = `${window.location.origin}/share/${encoded}`
      setShareUrl(url)
    }
  }, [isOpen, data, type])

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch (error) {
      logger.error('复制失败:', error)
      alert('复制失败，请手动复制')
    }
  }

  const handleDownload = () => {
    // 生成图片或PDF
    const canvas = document.createElement('canvas')
    canvas.width = 800
    canvas.height = 1200
    const ctx = canvas.getContext('2d')
    
    // 绘制背景
    ctx.fillStyle = '#1a1a2e'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    
    // 绘制内容
    ctx.fillStyle = '#ffffff'
    ctx.font = 'bold 32px Arial'
    ctx.fillText(getTitle(), 40, 80)
    
    // 添加更多内容...
    
    // 下载
    canvas.toBlob((blob) => {
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${getTitle()}_${Date.now()}.png`
      a.click()
      URL.revokeObjectURL(url)
    })
  }

  const handleNativeShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: getTitle(),
          text: getShareText(),
          url: shareUrl
        })
      } catch (error) {
        if (error.name !== 'AbortError') {
          logger.error('分享失败:', error)
        }
      }
    } else {
      handleCopy()
    }
  }

  const getTitle = () => {
    const titles = {
      yijing: '易经占卜结果',
      tarot: '塔罗牌占卜结果',
      bazi: '八字排盘结果',
      zodiac: '星座运势'
    }
    return titles[type] || '占卜结果'
  }

  const getShareText = () => {
    if (type === 'yijing' && data?.original) {
      return `我刚刚占卜了：${data.original.chinese}卦 - ${data.question || ''}`
    }
    if (type === 'tarot' && data?.cards) {
      return `我刚刚抽了塔罗牌：${data.cards.map(c => c.name).join('、')}`
    }
    if (type === 'bazi' && data?.八字) {
      return `我的八字排盘：${data.八字}`
    }
    return '我刚刚进行了占卜，来看看结果吧！'
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <Card className="w-full max-w-md">
        <div className="flex items-center justify-between p-6 border-b border-white/10">
          <h2 className="text-xl font-bold">分享结果</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-white/10 rounded-lg transition"
          >
            <X size={20} />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div>
            <label className="text-sm text-gray-400 mb-2 block">分享链接</label>
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={shareUrl}
                readOnly
                className="flex-1 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm"
              />
              <Button
                onClick={handleCopy}
                variant="secondary"
                size="sm"
              >
                {copied ? <Check size={16} /> : <Copy size={16} />}
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Button
              onClick={handleNativeShare}
              className="w-full"
              variant="secondary"
            >
              <Share2 size={18} />
              <span>分享</span>
            </Button>
            <Button
              onClick={handleDownload}
              className="w-full"
              variant="secondary"
            >
              <Download size={18} />
              <span>下载图片</span>
            </Button>
          </div>

          <div className="pt-4 border-t border-white/10">
            <p className="text-xs text-gray-400 text-center">
              {getShareText()}
            </p>
          </div>
        </div>
      </Card>
    </div>
  )
}
