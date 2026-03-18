import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, FileText } from 'lucide-react'
import Card from '../components/Card'

export default function PrivacyPolicyPage() {
  const navigate = useNavigate()

  useEffect(() => {
    // 可以在这里加载隐私政策内容
    // 或者直接显示静态内容
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
          <h1 className="text-lg font-bold text-white">隐私政策</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="px-4 pb-20 pt-4">
        <Card className="bg-white/5 border-white/10">
          <div className="p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="w-12 h-12 rounded-xl bg-purple-500/20 flex items-center justify-center">
                <FileText size={24} className="text-purple-400" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">隐私政策</h2>
                <p className="text-sm text-gray-400">最后更新：2026-01-17</p>
              </div>
            </div>

            <div className="prose prose-invert max-w-none text-gray-300 space-y-4">
              <p className="text-sm leading-relaxed">
                欢迎使用"易经占卜"应用。我们深知个人信息对您的重要性，并会尽全力保护您的个人信息安全可靠。
              </p>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">一、我们收集的信息</h3>
                <ul className="list-disc list-inside space-y-1 text-sm text-gray-300 ml-4">
                  <li>账户信息：手机号码、用户名等</li>
                  <li>设备信息：设备型号、操作系统版本等</li>
                  <li>使用信息：访问记录、操作日志等</li>
                </ul>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">二、我们如何使用信息</h3>
                <p className="text-sm text-gray-300">
                  我们使用收集的信息来提供、维护和改进我们的服务，包括占卜分析、用户支持等。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">三、信息存储</h3>
                <p className="text-sm text-gray-300">
                  您的个人信息将存储在中华人民共和国境内，我们采取合理的安全措施保护您的信息。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">四、您的权利</h3>
                <p className="text-sm text-gray-300">
                  您有权访问、更正、删除您的个人信息，也可以随时撤回您的授权同意。
                </p>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-white mb-2">五、联系我们</h3>
                <p className="text-sm text-gray-300">
                  如果您对本隐私政策有任何疑问，请通过应用内反馈功能联系我们。
                </p>
              </div>

              <div className="mt-6 pt-6 border-t border-white/10">
                <p className="text-xs text-gray-400">
                  完整的隐私政策文档请参考：docs/隐私政策.md
                </p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}
