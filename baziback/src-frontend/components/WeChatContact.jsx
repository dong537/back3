import { useEffect, useMemo, useState } from 'react'
import { MessageCircle, QrCode, X, ZoomIn } from 'lucide-react'
import { useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { contactRecordApi } from '../api'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'

const WECHAT_COPY = {
  'zh-CN': {
    title: '获取更详细资讯',
    subtitle: '添加微信咨询专业解读',
    name: '李钧泽',
    location: '江西 赣州',
    description: '如果你想继续深聊当前结果，可以添加微信获取更细的人工解读。',
    scanHint: '扫一扫二维码，加我为好友',
    zoomHint: '点击放大',
    modalTitle: '微信二维码',
    modalSubtitle: '长按保存或扫描添加好友',
    modalCloseHint: '点击外部区域或关闭按钮退出',
    closeLabel: '关闭',
    qrAlt: '微信二维码',
  },
  'en-US': {
    title: 'Get More Detailed Guidance',
    subtitle: 'Add WeChat for a professional reading',
    name: 'Li Junze',
    location: 'Ganzhou, Jiangxi',
    description:
      'If you want a deeper follow-up on the current result, feel free to add this WeChat contact.',
    scanHint: 'Scan the QR code to add me as a contact',
    zoomHint: 'Tap to enlarge',
    modalTitle: 'WeChat QR Code',
    modalSubtitle: 'Long press to save or scan to add the contact',
    modalCloseHint: 'Tap outside or use the close button to exit',
    closeLabel: 'Close',
    qrAlt: 'WeChat QR code',
  },
}

export default function WeChatContact({
  className = '',
  relatedRecordId = null,
}) {
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = WECHAT_COPY[locale]
  const [hasRecorded, setHasRecorded] = useState(false)
  const [showImageModal, setShowImageModal] = useState(false)

  const wechatInfo = useMemo(
    () => ({
      name: copy.name,
      location: copy.location,
      qrCodeImage: '/images/wechat-qrcode.png',
      description: copy.description,
    }),
    [copy]
  )

  const recordContact = async (actionType = 'view') => {
    if (hasRecorded && actionType === 'view') return

    try {
      await contactRecordApi.record({
        contactType: 'wechat',
        contactName: wechatInfo.name,
        contactInfo: wechatInfo.name,
        sourcePage: location.pathname,
        sourceType: 'divination_result',
        actionType,
        relatedRecordId,
      })

      if (actionType === 'view') {
        setHasRecorded(true)
      }

      logger.info(`Contact record created: ${actionType}`)
    } catch (error) {
      logger.warn('Record contact action failed:', error)
    }
  }

  useEffect(() => {
    void recordContact('view')
  }, [])

  const openModal = (actionType) => {
    void recordContact(actionType)
    setShowImageModal(true)
  }

  return (
    <div
      className={`rounded-[28px] border border-[#d0a85b]/18 bg-[linear-gradient(180deg,rgba(22,17,16,0.94),rgba(14,11,10,0.82))] p-5 ${className}`}
    >
      <div className="mb-4 flex items-center space-x-3">
        <div className="mystic-icon-badge h-10 w-10 rounded-full">
          <MessageCircle size={20} className="text-white" />
        </div>
        <div>
          <h3 className="text-base font-bold text-[#f4ece1]">{copy.title}</h3>
          <p className="text-xs text-[#8f7b66]">{copy.subtitle}</p>
        </div>
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
        <div className="flex flex-col items-center gap-4 md:flex-row">
          <div className="flex-shrink-0">
            <div
              className="group relative h-32 w-32 cursor-pointer rounded-[20px] border border-white/10 bg-[#f4ece1] p-2 shadow-lg transition-all hover:shadow-xl"
              onClick={() => openModal('click')}
              title={copy.zoomHint}
            >
              <img
                src={wechatInfo.qrCodeImage}
                alt={copy.qrAlt}
                className="h-full w-full object-contain transition-transform group-hover:scale-105"
                onError={(event) => {
                  event.currentTarget.style.display = 'none'
                  event.currentTarget.nextElementSibling.style.display = 'flex'
                }}
                onClick={(event) => {
                  event.stopPropagation()
                  openModal('scan')
                }}
              />
              <div className="hidden h-full w-full items-center justify-center rounded bg-[#ebe1d2]">
                <QrCode size={48} className="text-[#8f7b66]" />
              </div>

              <div className="absolute inset-0 flex items-center justify-center rounded-[20px] bg-black/0 opacity-0 transition-all group-hover:bg-black/10 group-hover:opacity-100">
                <div className="flex items-center space-x-1 rounded-lg bg-white/90 px-3 py-1.5 text-xs font-medium text-[#3f2b17] shadow-lg backdrop-blur-sm">
                  <ZoomIn size={14} />
                  <span>{copy.zoomHint}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="flex-1 text-center md:text-left">
            <div className="mb-3">
              <div className="mb-2 flex items-center justify-center space-x-2 md:justify-start">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[linear-gradient(135deg,#8f5c1f_0%,#c78734_52%,#e3bf73_100%)] text-lg font-bold text-white">
                  {wechatInfo.name.charAt(0)}
                </div>
                <div>
                  <div className="font-semibold text-[#f4ece1]">
                    {wechatInfo.name}
                  </div>
                  <div className="text-xs text-[#8f7b66]">
                    {wechatInfo.location}
                  </div>
                </div>
              </div>
            </div>

            <p className="mb-3 text-sm leading-6 text-[#bdaa94]">
              {wechatInfo.description}
            </p>

            <div className="flex items-center justify-center space-x-2 text-xs text-[#dcb86f] md:justify-start">
              <QrCode size={14} />
              <span>{copy.scanHint}</span>
            </div>
          </div>
        </div>
      </div>

      {showImageModal && (
        <div
          className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm"
          onClick={() => setShowImageModal(false)}
        >
          <div
            className="glass-dark relative w-full max-w-md rounded-[28px] border border-white/10 p-6 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <button
              onClick={() => setShowImageModal(false)}
              aria-label={copy.closeLabel}
              className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white/[0.05] text-[#8f7b66] transition-colors hover:bg-white/[0.08] hover:text-[#f4ece1]"
            >
              <X size={20} />
            </button>

            <div className="mb-4 text-center">
              <h3 className="text-lg font-bold text-[#f4ece1]">
                {copy.modalTitle}
              </h3>
              <p className="mt-1 text-sm text-[#8f7b66]">{copy.modalSubtitle}</p>
            </div>

            <div className="mb-4 flex justify-center">
              <div className="rounded-[22px] bg-[#f4ece1] p-4 shadow-lg">
                <img
                  src={wechatInfo.qrCodeImage}
                  alt={copy.qrAlt}
                  className="h-64 w-64 object-contain"
                  onError={(event) => {
                    event.currentTarget.style.display = 'none'
                    event.currentTarget.nextElementSibling.style.display =
                      'flex'
                  }}
                />
                <div className="hidden h-64 w-64 items-center justify-center rounded bg-[#ebe1d2]">
                  <QrCode size={64} className="text-[#8f7b66]" />
                </div>
              </div>
            </div>

            <div className="text-center">
              <div className="mb-2 flex items-center justify-center space-x-2">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[linear-gradient(135deg,#8f5c1f_0%,#c78734_52%,#e3bf73_100%)] font-bold text-white">
                  {wechatInfo.name.charAt(0)}
                </div>
                <div>
                  <div className="font-semibold text-[#f4ece1]">
                    {wechatInfo.name}
                  </div>
                  <div className="text-xs text-[#8f7b66]">
                    {wechatInfo.location}
                  </div>
                </div>
              </div>
              <p className="mt-2 text-sm leading-6 text-[#bdaa94]">
                {wechatInfo.description}
              </p>
            </div>

            <div className="mt-4 text-center">
              <div className="inline-flex items-center space-x-2 rounded-[16px] border border-white/10 bg-white/[0.03] px-3 py-2 text-xs text-[#8f7b66]">
                <ZoomIn size={14} />
                <span>{copy.modalCloseHint}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
