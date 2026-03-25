import { Crown, CreditCard, LogIn, Sparkles, X } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Button from './Button'
import { resolvePageLocale } from '../utils/displayText'

const PAYMENT_COPY = {
  'zh-CN': {
    close: '关闭',
    loginTitle: '登录解锁 AI 解读',
    loginDesc: (count) => `登录后每天可免费使用 ${count} 次 AI 深度解读`,
    loginNow: '立即登录',
    later: '稍后再说',
    limitTitle: '今日免费次数已用完',
    limitDesc: (count) => `你今天的 ${count} 次免费 AI 解读已经全部用完`,
    vipTitle: '升级会员，享受更多权益',
    vipBenefits: [
      '不限次 AI 深度解读',
      '高级牌阵与卦象解读',
      '历史记录长期保存',
      '专属优先支持',
    ],
    openVip: '开通会员，9.9 元/月',
    tryTomorrow: '明天再来',
    resetHint: '免费次数将在每天 00:00 重置',
  },
  'en-US': {
    close: 'Close',
    loginTitle: 'Sign in to unlock AI insights',
    loginDesc: (count) =>
      `Sign in to get ${count} free AI deep readings every day`,
    loginNow: 'Sign in now',
    later: 'Maybe later',
    limitTitle: "Today's free quota is used up",
    limitDesc: (count) =>
      `You have used all ${count} free AI readings available today`,
    vipTitle: 'Upgrade to membership for more benefits',
    vipBenefits: [
      'Unlimited AI deep readings',
      'Advanced spreads and hexagram insights',
      'Saved reading history',
      'Priority support',
    ],
    openVip: 'Upgrade membership, CNY 9.9/month',
    tryTomorrow: 'Come back tomorrow',
    resetHint: 'Your free quota resets every day at 00:00',
  },
}

export default function PaymentModal({
  isOpen,
  onClose,
  reason,
  remainingCount = 0,
}) {
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = PAYMENT_COPY[locale]
  const freeCount = remainingCount > 0 ? remainingCount : 2

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-md"
        onClick={onClose}
      />

      <div className="glass-dark animate-fadeIn relative w-full max-w-md rounded-[32px] border border-white/10 p-6 shadow-[0_24px_80px_rgba(0,0,0,0.42)]">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 rounded-full p-2 text-[#8f7b66] transition-colors hover:bg-white/[0.05] hover:text-[#f4ece1]"
          title={copy.close}
          aria-label={copy.close}
        >
          <X size={20} />
        </button>

        {reason === 'not_logged_in' ? (
          <>
            <div className="mb-6 text-center">
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb] shadow-[0_18px_40px_rgba(163,66,36,0.24)]">
                <LogIn className="h-8 w-8" />
              </div>
              <h3 className="mb-2 text-xl font-bold text-[#f4ece1]">
                {copy.loginTitle}
              </h3>
              <p className="text-[#bdaa94]">{copy.loginDesc(freeCount)}</p>
            </div>

            <div className="space-y-3">
              <Link
                to="/login"
                state={{ from: location }}
                className="block"
                onClick={onClose}
              >
                <Button className="w-full" size="lg">
                  <LogIn size={18} />
                  <span>{copy.loginNow}</span>
                </Button>
              </Link>
              <Button
                variant="secondary"
                className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
                onClick={onClose}
              >
                <span>{copy.later}</span>
              </Button>
            </div>
          </>
        ) : (
          <>
            <div className="mb-6 text-center">
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-[linear-gradient(135deg,#8f5c1f_0%,#c78734_52%,#e3bf73_100%)] text-[#fff7eb] shadow-[0_18px_40px_rgba(143,92,31,0.24)]">
                <Crown className="h-8 w-8" />
              </div>
              <h3 className="mb-2 text-xl font-bold text-[#f4ece1]">
                {copy.limitTitle}
              </h3>
              <p className="mb-4 text-[#bdaa94]">{copy.limitDesc(freeCount)}</p>

              <div className="mystic-muted-box mb-4 text-left">
                <h4 className="mb-3 flex items-center font-medium text-[#f0d9a5]">
                  <Sparkles size={16} className="mr-2 text-[#dcb86f]" />
                  {copy.vipTitle}
                </h4>
                <ul className="space-y-2 text-sm text-[#e4d6c8]">
                  {copy.vipBenefits.map((benefit) => (
                    <li key={benefit} className="flex items-center">
                      <span className="mr-2 h-1.5 w-1.5 rounded-full bg-[#dcb86f]" />
                      {benefit}
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="space-y-3">
              <Button className="w-full" size="lg">
                <CreditCard size={18} />
                <span>{copy.openVip}</span>
              </Button>
              <Button
                variant="secondary"
                className="w-full border-white/10 bg-white/[0.04] hover:bg-white/[0.08]"
                onClick={onClose}
              >
                <span>{copy.tryTomorrow}</span>
              </Button>
            </div>

            <p className="mt-4 text-center text-xs text-[#8f7b66]">
              {copy.resetHint}
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
