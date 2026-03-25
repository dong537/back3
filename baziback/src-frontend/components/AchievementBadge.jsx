import { Sparkles, Trophy } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

const ACHIEVEMENT_BADGE_COPY = {
  'zh-CN': {
    title: '成就解锁',
    reward: '奖励',
    points: '积分',
    action: '收下这枚勋章',
  },
  'en-US': {
    title: 'Achievement Unlocked!',
    reward: 'Reward',
    points: 'credits',
    action: 'Claim it',
  },
}

export default function AchievementBadge({ achievement, onClose }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = ACHIEVEMENT_BADGE_COPY[locale]

  if (!achievement) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
      <div className="glass-dark animate-bounce-in w-full max-w-md rounded-[32px] border border-white/10 p-8 text-center">
        <div className="mb-6 inline-flex h-24 w-24 items-center justify-center rounded-full bg-[linear-gradient(135deg,#8f5c1f_0%,#c78734_52%,#e3bf73_100%)] shadow-[0_18px_40px_rgba(143,92,31,0.22)]">
          <Trophy size={48} className="text-white" />
        </div>

        <div className="mb-4">
          <Sparkles className="mx-auto mb-2 h-8 w-8 animate-pulse text-[#dcb86f]" />
          <h2 className="mb-2 text-2xl font-bold text-[#f4ece1]">
            {copy.title}
          </h2>
          <h3 className="mb-2 text-xl font-semibold text-[#f0d9a5]">
            {achievement.name}
          </h3>
          <p className="text-[#8f7b66]">{achievement.description}</p>
        </div>

        {achievement.reward > 0 && (
          <div className="mb-6 rounded-[22px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 p-4">
            <p className="mb-1 text-sm text-[#8f7b66]">{copy.reward}</p>
            <p className="text-2xl font-bold text-[#f0d9a5]">
              +{achievement.reward} {copy.points}
            </p>
          </div>
        )}

        <button
          onClick={onClose}
          className="btn-primary-theme px-6 py-2 text-white"
        >
          {copy.action}
        </button>
      </div>
    </div>
  )
}
