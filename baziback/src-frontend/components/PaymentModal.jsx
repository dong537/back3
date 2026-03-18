import { X, Crown, Sparkles, LogIn, CreditCard } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import Button from './Button'

export default function PaymentModal({ isOpen, onClose, reason, remainingCount = 0 }) {
  const location = useLocation()
  
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* 背景遮罩 */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* 弹窗内容 */}
      <div className="relative glass rounded-2xl p-6 max-w-md w-full animate-fadeIn">
        {/* 关闭按钮 */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-white transition-colors"
        >
          <X size={20} />
        </button>

        {reason === 'not_logged_in' ? (
          <>
            {/* 未登录提示 */}
            <div className="text-center mb-6">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center mx-auto mb-4">
                <LogIn className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-xl font-bold mb-2">登录解锁 AI 解读</h3>
              <p className="text-gray-400">
                登录后每天可免费使用 <span className="text-purple-400 font-medium">2次</span> AI深度解读
              </p>
            </div>

            <div className="space-y-3">
              <Link to="/login" state={{ from: location }} className="block" onClick={onClose}>
                <Button className="w-full" size="lg">
                  <LogIn size={18} />
                  <span>立即登录</span>
                </Button>
              </Link>
              <Button variant="outline" className="w-full" onClick={onClose}>
                <span>稍后再说</span>
              </Button>
            </div>
          </>
        ) : (
          <>
            {/* 次数用完提示 */}
            <div className="text-center mb-6">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-amber-500 to-orange-500 flex items-center justify-center mx-auto mb-4">
                <Crown className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-xl font-bold mb-2">今日免费次数已用完</h3>
              <p className="text-gray-400 mb-4">
                您今天的 2 次免费 AI 解读已使用完毕
              </p>
              
              {/* 会员特权 */}
              <div className="glass rounded-xl p-4 text-left mb-4">
                <h4 className="font-medium text-purple-300 mb-3 flex items-center">
                  <Sparkles size={16} className="mr-2" />
                  升级会员享受更多权益
                </h4>
                <ul className="space-y-2 text-sm text-gray-300">
                  <li className="flex items-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-400 mr-2"></span>
                    无限次 AI 深度解读
                  </li>
                  <li className="flex items-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-400 mr-2"></span>
                    专属高级牌阵/卦象分析
                  </li>
                  <li className="flex items-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-400 mr-2"></span>
                    历史记录永久保存
                  </li>
                  <li className="flex items-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-400 mr-2"></span>
                    专属客服支持
                  </li>
                </ul>
              </div>
            </div>

            <div className="space-y-3">
              <Button className="w-full bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-400 hover:to-orange-400" size="lg">
                <CreditCard size={18} />
                <span>开通会员 ¥9.9/月</span>
              </Button>
              <Button variant="outline" className="w-full" onClick={onClose}>
                <span>明天再来</span>
              </Button>
            </div>

            <p className="text-center text-gray-500 text-xs mt-4">
              免费次数将于每日 0:00 重置
            </p>
          </>
        )}
      </div>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }
        .animate-fadeIn {
          animation: fadeIn 0.2s ease-out;
        }
      `}</style>
    </div>
  )
}
