import { useMemo, useState } from 'react'
import { History, Settings, Share2, Star, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import HistoryModal from './HistoryModal'
import ShareModal from './ShareModal'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'

const FAB_COPY = {
  'zh-CN': {
    history: '历史记录',
    favorite: '收藏',
    share: '分享',
    settings: '设置',
  },
  'en-US': {
    history: 'History',
    favorite: 'Favorite',
    share: 'Share',
    settings: 'Settings',
  },
}

export default function FloatingActionButton({
  currentData,
  currentType,
  onHistorySelect,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = FAB_COPY[locale]
  const [isOpen, setIsOpen] = useState(false)
  const [showHistory, setShowHistory] = useState(false)
  const [showShare, setShowShare] = useState(false)

  const actions = useMemo(
    () => [
      {
        icon: History,
        label: copy.history,
        onClick: () => setShowHistory(true),
        color: 'bg-[linear-gradient(135deg,#7a3218_0%,#d0a85b_100%)]',
      },
      {
        icon: Star,
        label: copy.favorite,
        onClick: () => {
          logger.debug('Favorite action clicked', currentData)
        },
        color: 'bg-[linear-gradient(135deg,#6a4a1e_0%,#e3bf73_100%)]',
      },
      {
        icon: Share2,
        label: copy.share,
        onClick: () => setShowShare(true),
        color: 'bg-[linear-gradient(135deg,#8b4a1e_0%,#cd7840_100%)]',
        disabled: !currentData,
      },
      {
        icon: Settings,
        label: copy.settings,
        onClick: () => {
          logger.debug('Settings action clicked')
        },
        color: 'bg-[linear-gradient(135deg,#5e4a36_0%,#8d6a3d_100%)]',
      },
    ],
    [copy, currentData]
  )

  return (
    <>
      <div
        className="safe-area-bottom fixed bottom-6 right-6 z-40"
        style={{
          marginBottom: 'max(1.5rem, env(safe-area-inset-bottom))',
          marginRight: 'max(1.5rem, env(safe-area-inset-right))',
        }}
      >
        <div className="relative">
          <div
            className={`absolute bottom-16 right-0 space-y-3 transition-all duration-300 ${
              isOpen
                ? 'translate-y-0 opacity-100'
                : 'pointer-events-none translate-y-4 opacity-0'
            }`}
          >
            {actions.map((action, index) => (
              <button
                key={action.label}
                onClick={() => {
                  action.onClick()
                  setIsOpen(false)
                }}
                disabled={action.disabled}
                className={`tap-highlight flex min-h-[44px] items-center space-x-3 rounded-full px-4 py-3 text-white shadow-lg transition-transform hover:scale-110 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 ${action.color}`}
                style={{ animationDelay: `${index * 50}ms` }}
              >
                <action.icon size={20} />
                <span className="hidden text-sm font-medium sm:inline">
                  {action.label}
                </span>
              </button>
            ))}
          </div>

          <button
            onClick={() => setIsOpen((prev) => !prev)}
            className="tap-highlight flex h-14 w-14 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-lg transition-transform hover:scale-110 active:scale-95"
            style={{
              minWidth: '56px',
              minHeight: '56px',
            }}
          >
            {isOpen ? <X size={24} /> : <Settings size={24} />}
          </button>
        </div>
      </div>

      <HistoryModal
        isOpen={showHistory}
        onClose={() => setShowHistory(false)}
        type={currentType}
        onSelect={onHistorySelect}
      />

      <ShareModal
        isOpen={showShare}
        onClose={() => setShowShare(false)}
        data={currentData}
        type={currentType}
      />
    </>
  )
}
