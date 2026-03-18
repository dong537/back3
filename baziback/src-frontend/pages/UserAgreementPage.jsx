import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ScrollText } from 'lucide-react'
import Card from '../components/Card'

export default function UserAgreementPage() {
  const navigate = useNavigate()

  useEffect(() => {
    // 可以在这里加载用户协议内容
  }, [])

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-slate-950/80 backdrop-blur-xl border-b border-white/10 safe-area-top">
        <div className="px-4 py-3 flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-white/10 rounded-xl transition-colors"
          >
            <ArrowLeft size={20} className="text-white" />
          </button>
          <h1 className="text-lg font-bold text-white">用户协议</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="px-4 pb-20 pt-4">
        <Card className="bg-white/5 border-white/10">
          <div className="p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="w-12 h-12 rounded-xl bg-blue-500/20 flex items-center justify-center">
                <ScrollText size={24} className="text-blue-400" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">用户服务协议</h2>
                <p className="text-sm text-gray-400">最后更新：2026-01-17</p>
              </div>
            </div>

            <div className="prose prose-invert max-w-none text-gray-300 space-y-4">
              <p className="text-sm leading-relaxed">
                欢迎使用"易经占卜"应用。在使用本应用之前，请您仔细阅读本《用户服务协议》。
              </p>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">一、协议的接受</h3>
                <p className="text-sm text-gray-300">
                  当您点击"同意"或开始使用本应用时，即表示您已充分理解并同意接受本协议的全部内容。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">二、服务说明</h3>
                <p className="text-sm text-gray-300">
                  本应用提供占卜、咨询服务，包括易经占卜、塔罗牌、星座运势、八字分析等功能。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">三、服务性质</h3>
                <p className="text-sm text-gray-300">
                  本应用提供的服务仅供娱乐和参考，不构成任何形式的建议、指导或承诺。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">四、用户行为规范</h3>
                <p className="text-sm text-gray-300">
                  您在使用本应用时，不得发布违法、违规信息，不得干扰应用正常运行。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">五、免责声明</h3>
                <p className="text-sm text-gray-300">
                  因不可抗力导致的服务中断，我们不承担责任。本应用提供的服务仅供参考。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">六、联系我们</h3>
                <p className="text-sm text-gray-300">
                  如果您对本协议有任何疑问，请通过应用内反馈功能联系我们。
                </p>
              </div>

              <div className="mt-6 pt-6 border-t border-white/10">
                <p className="text-xs text-gray-400">
                  完整的用户协议文档请参考：docs/用户协议.md
                </p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}
