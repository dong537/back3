import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ScrollText } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from '../components/Card'
import { resolvePageLocale } from '../utils/displayText'

const AGREEMENT_COPY = {
  'zh-CN': {
    title: '用户协议',
    subtitle: '最后更新：2026-01-17',
    intro:
      '欢迎使用“天机命理”应用。在使用本应用前，请认真阅读本协议。您访问、注册或继续使用本应用，即视为已经理解并同意接受本协议全部内容。',
    sections: [
      {
        title: '一、协议的接受',
        paragraphs: [
          '当您点击同意、完成注册或开始使用本应用时，即表示您同意遵守本协议及平台公布的相关规则。',
        ],
      },
      {
        title: '二、服务说明',
        paragraphs: [
          '本应用提供易经占卜、塔罗分析、命理解读、AI 问答等内容服务，具体功能以页面展示和实际开放能力为准。',
        ],
      },
      {
        title: '三、服务性质',
        paragraphs: [
          '本应用提供的内容主要用于文化体验、娱乐参考与信息交流，不构成医疗、法律、投资或其他专业建议。',
        ],
      },
      {
        title: '四、用户行为规范',
        paragraphs: [
          '您在使用过程中不得发布违法违规内容，不得干扰平台正常运行，也不得利用本服务从事侵害他人权益的行为。',
        ],
      },
      {
        title: '五、责任限制',
        paragraphs: [
          '在法律允许范围内，对于因不可抗力、网络故障、第三方服务异常等原因导致的服务中断或结果偏差，我们将尽力恢复，但不承担超出法律规定之外的责任。',
        ],
      },
      {
        title: '六、联系我们',
        paragraphs: [
          '如果您对本协议有疑问或建议，请通过应用内反馈渠道与我们联系。',
        ],
      },
    ],
    footer: '如需查看完整版本，请参考：docs/用户协议.md',
  },
  'en-US': {
    title: 'User Agreement',
    subtitle: 'Last updated: January 17, 2026',
    intro:
      'Welcome to Mystic Insight. Please read this agreement carefully before using the app. By accessing, registering for, or continuing to use the app, you acknowledge that you understand and accept all terms in this agreement.',
    sections: [
      {
        title: '1. Acceptance of Terms',
        paragraphs: [
          'By clicking agree, completing registration, or using the app, you agree to comply with this agreement and any related platform rules that are published from time to time.',
        ],
      },
      {
        title: '2. Service Description',
        paragraphs: [
          'The app may provide services such as Yijing divination, Tarot analysis, destiny interpretation, and AI-assisted Q&A. The exact scope depends on the features currently shown and enabled in the product.',
        ],
      },
      {
        title: '3. Nature of the Service',
        paragraphs: [
          'The content provided in this app is mainly for cultural experience, entertainment reference, and informational exchange. It does not constitute medical, legal, investment, or other professional advice.',
        ],
      },
      {
        title: '4. User Conduct',
        paragraphs: [
          'You must not publish illegal or inappropriate content, interfere with the normal operation of the platform, or use the service to infringe upon the rights and interests of others.',
        ],
      },
      {
        title: '5. Limitation of Liability',
        paragraphs: [
          'To the extent permitted by law, we will make reasonable efforts to restore service when interruptions or inaccurate results are caused by force majeure, network failures, or third-party service issues, but we are not liable beyond applicable legal requirements.',
        ],
      },
      {
        title: '6. Contact Us',
        paragraphs: [
          'If you have any questions or suggestions regarding this agreement, please contact us through the in-app feedback channel.',
        ],
      },
    ],
    footer: 'For the full version, please refer to: docs/UserAgreement.md',
  },
}

export default function UserAgreementPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = AGREEMENT_COPY[locale]

  return (
    <div className="page-shell">
      <div className="safe-area-top sticky top-0 z-50 mb-6 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 text-[#f4ece1] transition-colors hover:bg-white/[0.06]"
          >
            <ArrowLeft size={20} />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          <div className="w-10" />
        </div>
      </div>

      <div className="app-page-shell pb-20">
        <Card className="panel border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.94),rgba(14,11,10,0.84))]">
          <div className="p-6 md:p-8">
            <div className="mb-8 flex items-center gap-4">
              <div className="mystic-icon-badge h-12 w-12">
                <ScrollText size={24} />
              </div>
              <div>
                <h2 className="text-xl font-bold text-[#f4ece1]">
                  {copy.title}
                </h2>
                <p className="text-sm text-[#8f7b66]">{copy.subtitle}</p>
              </div>
            </div>

            <div className="space-y-6 text-[#e4d6c8]">
              <p className="text-sm leading-7">{copy.intro}</p>

              {copy.sections.map((section) => (
                <section
                  key={section.title}
                  className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5"
                >
                  <h3 className="mb-3 text-lg font-semibold text-[#f4ece1]">
                    {section.title}
                  </h3>
                  <div className="space-y-3">
                    {section.paragraphs.map((paragraph) => (
                      <p key={paragraph} className="text-sm leading-7">
                        {paragraph}
                      </p>
                    ))}
                  </div>
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
