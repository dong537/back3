import { useNavigate } from 'react-router-dom'
import { ArrowLeft, FileText } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from '../components/Card'
import { resolvePageLocale } from '../utils/displayText'

const PRIVACY_COPY = {
  'zh-CN': {
    title: '隐私政策',
    subtitle: '最后更新：2026-01-17',
    intro:
      '欢迎使用“易经占卜”应用。我们重视并尽力保护您的个人信息安全。本政策说明我们会收集哪些信息、如何使用这些信息，以及您享有的相关权利。',
    sections: [
      {
        title: '一、我们收集的信息',
        paragraphs: [
          '为了提供账号、分析和安全保障服务，我们可能收集账号信息、设备信息、使用日志以及您主动提交的内容。',
        ],
        bullets: [
          '账号信息：手机号、昵称、头像等。',
          '设备信息：设备型号、操作系统版本、网络环境等。',
          '使用信息：访问记录、操作日志、功能使用情况等。',
        ],
      },
      {
        title: '二、我们如何使用信息',
        paragraphs: [
          '我们会在提供、维护和改进服务所必需的范围内使用这些信息，包括身份验证、问题诊断、模型分析、客服支持和安全风控。',
        ],
      },
      {
        title: '三、信息存储与保护',
        paragraphs: [
          '我们会采取合理的技术和管理措施保护您的信息，尽量避免未经授权的访问、披露、篡改或丢失。',
          '相关数据可能存储在中国境内或依法合规的服务设施中，并按照业务需要保留必要期限。',
        ],
      },
      {
        title: '四、您的权利',
        paragraphs: [
          '您有权访问、更正、删除您的个人信息，也可以申请注销账号或撤回部分授权。',
          '若您对隐私处理有疑问，可通过应用内反馈与我们联系。',
        ],
      },
      {
        title: '五、联系我们',
        paragraphs: [
          '如果您对本隐私政策有任何问题、意见或建议，请通过应用内反馈渠道联系我们。',
        ],
      },
    ],
    footer: '如需查看更完整版本，请参考：docs/隐私政策.md',
  },
  'en-US': {
    title: 'Privacy Policy',
    subtitle: 'Last updated: January 17, 2026',
    intro:
      'Welcome to Yijing Divination. We value your privacy and work to protect your personal information. This policy explains what information we collect, how we use it, and what rights you have.',
    sections: [
      {
        title: '1. Information We Collect',
        paragraphs: [
          'To provide account, analysis, and security services, we may collect account details, device information, usage logs, and content you actively submit.',
        ],
        bullets: [
          'Account information: phone number, nickname, avatar, and related profile details.',
          'Device information: device model, OS version, network environment, and similar technical data.',
          'Usage information: access records, operation logs, and feature usage activity.',
        ],
      },
      {
        title: '2. How We Use Information',
        paragraphs: [
          'We use the information only as needed to provide, maintain, and improve the service, including identity verification, issue diagnosis, model analysis, customer support, and security risk control.',
        ],
      },
      {
        title: '3. Storage and Protection',
        paragraphs: [
          'We apply reasonable technical and organizational safeguards to protect your information from unauthorized access, disclosure, tampering, or loss.',
          'Relevant data may be stored in mainland China or other compliant service facilities and retained for the period necessary for business operations and legal obligations.',
        ],
      },
      {
        title: '4. Your Rights',
        paragraphs: [
          'You may request access to, correction of, or deletion of your personal information. You may also request account closure or withdraw certain permissions where applicable.',
          'If you have questions about how your data is handled, please contact us through the in-app feedback channel.',
        ],
      },
      {
        title: '5. Contact Us',
        paragraphs: [
          'If you have any questions, comments, or suggestions regarding this privacy policy, please contact us through the in-app feedback channel.',
        ],
      },
    ],
    footer: 'For the full version, please refer to: docs/PrivacyPolicy.md',
  },
}

export default function PrivacyPolicyPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = PRIVACY_COPY[locale]

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="safe-area-top sticky top-0 z-50 -mx-4 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-colors hover:bg-white/10"
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="app-page-shell-narrow pb-20 pt-4">
        <Card className="panel">
          <div className="p-6">
            <div className="mb-6 flex items-center space-x-3">
              <div className="mystic-icon-badge h-12 w-12 rounded-xl">
                <FileText size={24} className="text-white" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-[#f4ece1]">{copy.title}</h2>
                <p className="text-sm text-[#8f7b66]">{copy.subtitle}</p>
              </div>
            </div>

            <div className="space-y-6 text-[#e4d6c8]">
              <p className="text-sm leading-relaxed">{copy.intro}</p>

              {copy.sections.map((section) => (
                <section key={section.title}>
                  <h3 className="mb-2 text-lg font-semibold text-[#f4ece1]">
                    {section.title}
                  </h3>
                  {section.paragraphs.map((paragraph) => (
                    <p key={paragraph} className="mb-3 text-sm leading-relaxed">
                      {paragraph}
                    </p>
                  ))}
                  {section.bullets ? (
                    <ul className="ml-5 list-disc space-y-2 text-sm text-[#e4d6c8]">
                      {section.bullets.map((bullet) => (
                        <li key={bullet}>{bullet}</li>
                      ))}
                    </ul>
                  ) : null}
                </section>
              ))}

              <div className="border-t border-white/10 pt-6">
                <p className="text-xs text-[#8f7b66]">{copy.footer}</p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}
