import { useState } from 'react'
import { History, Star, Share2, Settings, X } from 'lucide-react'
import HistoryModal from './HistoryModal'
import ShareModal from './ShareModal'
import { logger } from '../utils/logger'

/**
 * 浮动操作按钮组
 */
export default function FloatingActionButton({ 
  currentData, 
  currentType,
  onHistorySelect 
}) {
  const [isOpen, setIsOpen] = useState(false)
  const [showHistory, setShowHistory] = useState(false)
  const [showShare, setShowShare] = useState(false)

  const actions = [
    {
      icon: History,
      label: '历史记录',
      onClick: () => setShowHistory(true),
      color: 'bg-blue-500'
    },
    {
      icon: Star,
      label: '收藏',
      onClick: () => {
        // 收藏功能
        logger.debug('收藏', currentData)
      },
      color: 'bg-yellow-500'
    },
    {
      icon: Share2,
      label: '分享',
      onClick: () => setShowShare(true),
      color: 'bg-green-500',
      disabled: !currentData
    },
    {
      icon: Settings,
      label: '设置',
      onClick: () => {
        // 设置功能
        logger.debug('设置')
      },
      color: 'bg-gray-500'
    }
  ]

  return (
    <>
      <div className="fixed bottom-6 right-6 z-40 safe-area-bottom" style={{
        marginBottom: 'max(1.5rem, env(safe-area-inset-bottom))',
        marginRight: 'max(1.5rem, env(safe-area-inset-right))'
      }}>
        <div className="relative">
          {/* 操作按钮 */}
          <div className={`absolute bottom-16 right-0 space-y-3 transition-all duration-300 ${
            isOpen ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4 pointer-events-none'
          }`}>
            {actions.map((action, index) => (
              <button
                key={index}
                onClick={() => {
                  action.onClick()
                  setIsOpen(false)
                }}
                disabled={action.disabled}
                className={`flex items-center space-x-3 ${action.color} text-white px-4 py-3 rounded-full shadow-lg hover:scale-110 active:scale-95 transition-transform disabled:opacity-50 disabled:cursor-not-allowed tap-highlight min-h-[44px]`}
                style={{
                  animationDelay: `${index * 50}ms`
                }}
              >
                <action.icon size={20} />
                <span className="text-sm font-medium hidden sm:inline">{action.label}</span>
              </button>
            ))}
          </div>

          {/* 主按钮 */}
          <button
            onClick={() => setIsOpen(!isOpen)}
            className="w-14 h-14 rounded-full bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg hover:scale-110 active:scale-95 transition-transform flex items-center justify-center tap-highlight"
            style={{
              minWidth: '56px',
              minHeight: '56px'
            }}
          >
            {isOpen ? <X size={24} /> : <Settings size={24} />}
          </button>
        </div>
      </div>

      {/* 历史记录弹窗 */}
      <HistoryModal
        isOpen={showHistory}
        onClose={() => setShowHistory(false)}
        type={currentType}
        onSelect={onHistorySelect}
      />

      {/* 分享弹窗 */}
      <ShareModal
        isOpen={showShare}
        onClose={() => setShowShare(false)}
        data={currentData}
        type={currentType}
      />
    </>
  )
}
