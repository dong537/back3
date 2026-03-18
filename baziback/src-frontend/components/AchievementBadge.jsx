import { Trophy, Sparkles } from 'lucide-react'
import { toast } from './Toast'

/**
 * 成就解锁提示
 */
export default function AchievementBadge({ achievement, onClose }) {
  if (!achievement) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="glass rounded-2xl p-8 max-w-md w-full text-center animate-bounce-in">
        <div className="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-r from-yellow-400 to-orange-500 mb-6">
          <Trophy size={48} className="text-white" />
        </div>
        
        <div className="mb-4">
          <Sparkles className="w-8 h-8 text-yellow-400 mx-auto mb-2 animate-pulse" />
          <h2 className="text-2xl font-bold mb-2">成就解锁！</h2>
          <h3 className="text-xl font-semibold text-skin-primary mb-2">
            {achievement.name}
          </h3>
          <p className="text-gray-400">{achievement.description}</p>
        </div>

        {achievement.reward > 0 && (
          <div className="bg-skin-primary/20 rounded-lg p-4 mb-6">
            <p className="text-sm text-gray-400 mb-1">奖励</p>
            <p className="text-2xl font-bold text-skin-primary">
              +{achievement.reward} 积分
            </p>
          </div>
        )}

        <button
          onClick={onClose}
          className="px-6 py-2 bg-skin-primary text-white rounded-lg hover:opacity-90 transition"
        >
          太棒了！
        </button>
      </div>
    </div>
  )
}
