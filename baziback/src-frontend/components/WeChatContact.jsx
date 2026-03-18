import { useState, useEffect } from 'react'
import { MessageCircle, QrCode, X, ZoomIn } from 'lucide-react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { contactRecordApi } from '../api'
import { logger } from '../utils/logger'

/**
 * 微信联系方式组件
 * 在占卜完成后展示，供用户添加微信获取更多详细资讯
 */
export default function WeChatContact({ className = '', relatedRecordId = null }) {
  const location = useLocation()
  const { isLoggedIn } = useAuth()
  const [hasRecorded, setHasRecorded] = useState(false)
  const [showImageModal, setShowImageModal] = useState(false) // 控制图片放大模态框
  
  // 微信信息配置（可以从配置文件或环境变量读取）
  const wechatInfo = {
    name: '李钧泽',
    location: '江西 赣州',
    qrCodeImage: '/images/wechat-qrcode.png', // 二维码图片路径，请将二维码图片放在 public/images/ 目录下
    description: '想要获取更多详细资讯，可以添加微信咨询专业解读'
  }

  // 记录联系方式查看/点击
  const recordContact = async (actionType = 'view') => {
    if (hasRecorded && actionType === 'view') {
      return // 避免重复记录查看
    }
    
    try {
      await contactRecordApi.record({
        contactType: 'wechat',
        contactName: wechatInfo.name,
        contactInfo: wechatInfo.name,
        sourcePage: location.pathname,
        sourceType: 'divination_result',
        actionType: actionType,
        relatedRecordId: relatedRecordId
      })
      
      if (actionType === 'view') {
        setHasRecorded(true)
      }
      
      logger.info(`记录联系方式: ${actionType}`)
    } catch (error) {
      logger.warn('记录联系方式失败:', error)
      // 静默失败，不影响用户体验
    }
  }

  // 组件加载时记录查看
  useEffect(() => {
    recordContact('view')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className={`bg-gradient-to-br from-green-500/10 to-emerald-500/10 rounded-2xl border border-green-500/20 p-5 ${className}`}>
      <div className="flex items-center space-x-2 mb-4">
        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-500 to-emerald-500 flex items-center justify-center">
          <MessageCircle size={20} className="text-white" />
        </div>
        <div>
          <h3 className="text-base font-bold text-white">获取更多详细资讯</h3>
          <p className="text-xs text-gray-300">添加微信咨询专业解读</p>
        </div>
      </div>

      <div className="bg-white/5 rounded-xl p-4 border border-white/10">
        <div className="flex flex-col md:flex-row items-center gap-4">
          {/* 左侧：二维码 */}
          <div className="flex-shrink-0">
            <div 
              className="w-32 h-32 bg-white rounded-lg p-2 shadow-lg cursor-pointer hover:shadow-xl transition-all relative group"
              onClick={() => {
                recordContact('click')
                setShowImageModal(true) // 显示放大图片
              }}
              title="点击放大二维码"
            >
              <img
                src={wechatInfo.qrCodeImage}
                alt="微信二维码"
                className="w-full h-full object-contain transition-transform group-hover:scale-105"
                onError={(e) => {
                  // 如果图片加载失败，显示占位符
                  e.target.style.display = 'none'
                  e.target.nextSibling.style.display = 'flex'
                }}
                onClick={(e) => {
                  e.stopPropagation()
                  recordContact('scan')
                  setShowImageModal(true) // 显示放大图片
                }}
              />
              <div className="w-full h-full bg-gray-100 rounded flex items-center justify-center" style={{ display: 'none' }}>
                <QrCode size={48} className="text-gray-400" />
              </div>
              {/* 悬停提示 */}
              <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 rounded-lg flex items-center justify-center transition-all opacity-0 group-hover:opacity-100">
                <div className="bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-lg shadow-lg text-xs font-medium text-gray-800 flex items-center space-x-1">
                  <ZoomIn size={14} />
                  <span>点击放大</span>
                </div>
              </div>
            </div>
          </div>

          {/* 右侧：联系信息 */}
          <div className="flex-1 text-center md:text-left">
            <div className="mb-3">
              <div className="flex items-center justify-center md:justify-start space-x-2 mb-2">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-green-400 to-emerald-400 flex items-center justify-center text-white font-bold text-lg">
                  {wechatInfo.name.charAt(0)}
                </div>
                <div>
                  <div className="text-white font-semibold">{wechatInfo.name}</div>
                  <div className="text-xs text-gray-400">{wechatInfo.location}</div>
                </div>
              </div>
            </div>
            
            <p className="text-sm text-gray-300 mb-3">
              {wechatInfo.description}
            </p>

            <div className="flex items-center justify-center md:justify-start space-x-2 text-xs text-green-400">
              <QrCode size={14} />
              <span>扫一扫二维码，加我为朋友</span>
            </div>
          </div>
        </div>
      </div>

      {/* 图片放大模态框 */}
      {showImageModal && (
        <div 
          className="fixed inset-0 z-[9999] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
          onClick={() => setShowImageModal(false)}
        >
          <div 
            className="relative max-w-md w-full bg-white rounded-2xl p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            {/* 关闭按钮 */}
            <button
              onClick={() => setShowImageModal(false)}
              className="absolute top-4 right-4 w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors z-10"
              aria-label="关闭"
            >
              <X size={20} className="text-gray-600" />
            </button>

            {/* 标题 */}
            <div className="text-center mb-4">
              <h3 className="text-lg font-bold text-gray-800">微信二维码</h3>
              <p className="text-sm text-gray-500 mt-1">长按保存或扫描添加好友</p>
            </div>

            {/* 放大的二维码图片 */}
            <div className="flex justify-center mb-4">
              <div className="bg-white p-4 rounded-lg shadow-lg">
                <img
                  src={wechatInfo.qrCodeImage}
                  alt="微信二维码"
                  className="w-64 h-64 object-contain"
                  onError={(e) => {
                    e.target.style.display = 'none'
                    e.target.nextSibling.style.display = 'flex'
                  }}
                />
                <div className="w-64 h-64 bg-gray-100 rounded flex items-center justify-center" style={{ display: 'none' }}>
                  <QrCode size={64} className="text-gray-400" />
                </div>
              </div>
            </div>

            {/* 联系信息 */}
            <div className="text-center">
              <div className="flex items-center justify-center space-x-2 mb-2">
                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-400 to-emerald-400 flex items-center justify-center text-white font-bold">
                  {wechatInfo.name.charAt(0)}
                </div>
                <div>
                  <div className="text-gray-800 font-semibold">{wechatInfo.name}</div>
                  <div className="text-xs text-gray-500">{wechatInfo.location}</div>
                </div>
              </div>
              <p className="text-sm text-gray-600 mt-2">{wechatInfo.description}</p>
            </div>

            {/* 提示文字 */}
            <div className="mt-4 text-center">
              <div className="inline-flex items-center space-x-2 text-xs text-gray-500 bg-gray-50 px-3 py-2 rounded-lg">
                <ZoomIn size={14} />
                <span>点击外部区域或关闭按钮退出</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
