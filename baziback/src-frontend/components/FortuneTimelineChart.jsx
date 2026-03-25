import { useMemo, useState } from 'react'
import {
  Activity,
  AlertCircle,
  Briefcase,
  Calendar,
  Heart,
  Wallet,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card from './Card'
import { formatLocaleDate, resolvePageLocale } from '../utils/displayText'

const TIMELINE_COPY = {
  'zh-CN': {
    title: '年度运势时间轴',
    actionSuggestion: '行动建议',
    sampleNodes: [
      {
        id: 1,
        startDate: '2024-01-01',
        endDate: '2024-03-31',
        nodeType: 'wealth_peak',
        nodeTitle: '财运高峰期',
        nodeDesc: '这一阶段资源与机会更集中，适合稳步布局与资源整合。',
        importance: 4,
      },
      {
        id: 2,
        startDate: '2024-04-01',
        endDate: '2024-06-30',
        nodeType: 'career_turn',
        nodeTitle: '事业转折点',
        nodeDesc: '工作节奏会出现关键变化，适合提前准备新的合作和职责。',
        importance: 5,
      },
      {
        id: 3,
        startDate: '2024-07-01',
        endDate: '2024-09-30',
        nodeType: 'love_opportunity',
        nodeTitle: '感情机遇期',
        nodeDesc: '关系互动更顺畅，适合表达态度并推动重要沟通。',
        importance: 4,
      },
    ],
  },
  'en-US': {
    title: 'Yearly Fortune Timeline',
    actionSuggestion: 'Action suggestion',
    sampleNodes: [
      {
        id: 1,
        startDate: '2024-01-01',
        endDate: '2024-03-31',
        nodeType: 'wealth_peak',
        nodeTitle: 'Wealth Upswing',
        nodeDesc:
          'Financial opportunities gather in this phase, making it a good time for steady planning.',
        importance: 4,
      },
      {
        id: 2,
        startDate: '2024-04-01',
        endDate: '2024-06-30',
        nodeType: 'career_turn',
        nodeTitle: 'Career Turning Point',
        nodeDesc:
          'Work rhythm may shift noticeably here, so prepare for role and partnership changes.',
        importance: 5,
      },
      {
        id: 3,
        startDate: '2024-07-01',
        endDate: '2024-09-30',
        nodeType: 'love_opportunity',
        nodeTitle: 'Relationship Opportunity',
        nodeDesc:
          'Connections flow more smoothly in this period and important conversations land better.',
        importance: 4,
      },
    ],
  },
}

const NODE_ICONS = {
  wealth_peak: Wallet,
  wealth_low: Wallet,
  career_turn: Briefcase,
  career_peak: Briefcase,
  love_opportunity: Heart,
  love_challenge: Heart,
  health_warning: Activity,
  health_good: Activity,
}

const NODE_COLORS = {
  wealth_peak: 'text-[#f0d9a5]',
  wealth_low: 'text-[#e19a84]',
  career_turn: 'text-[#dcb86f]',
  career_peak: 'text-[#f0d9a5]',
  love_opportunity: 'text-[#e19a84]',
  love_challenge: 'text-[#c96a4c]',
  health_warning: 'text-[#c96a4c]',
  health_good: 'text-[#dcb86f]',
}

export default function FortuneTimelineChart({
  timelineData = [],
  onNodeClick,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = TIMELINE_COPY[locale]
  const [selectedNode, setSelectedNode] = useState(null)

  const displayData = useMemo(
    () => (timelineData.length > 0 ? timelineData : copy.sampleNodes),
    [copy.sampleNodes, timelineData]
  )

  const handleNodeClick = (node) => {
    setSelectedNode(node)
    onNodeClick?.(node)
  }

  return (
    <Card className="panel">
      <div className="p-6">
        <h3 className="mb-6 flex items-center text-xl font-bold text-[#f4ece1]">
          <Calendar className="mr-2 h-5 w-5 text-[#dcb86f]" />
          {copy.title}
        </h3>

        <div className="relative">
          <div className="absolute bottom-0 left-8 top-0 w-0.5 bg-[linear-gradient(180deg,rgba(163,66,36,0.95),rgba(208,168,91,0.85),rgba(143,92,31,0.95))]" />

          <div className="space-y-8">
            {displayData.map((node, index) => {
              const Icon = NODE_ICONS[node.nodeType] || AlertCircle
              const colorClass = NODE_COLORS[node.nodeType] || 'text-[#8f7b66]'
              const isLeft = index % 2 === 0
              const isSelected = selectedNode?.id === node.id

              return (
                <div
                  key={node.id || `${node.nodeType}-${index}`}
                  className={`relative flex items-center ${
                    isLeft ? 'flex-row' : 'flex-row-reverse'
                  }`}
                >
                  <button
                    type="button"
                    className={`absolute left-8 z-10 h-4 w-4 rounded-full border-2 border-[#d0a85b] bg-[#140f0f] transition-transform hover:scale-125 ${
                      isSelected ? 'ring-2 ring-[#dcb86f]/60' : ''
                    }`}
                    onClick={() => handleNodeClick(node)}
                    style={{ transform: 'translateX(-50%)' }}
                    aria-label={node.nodeTitle}
                  />

                  <div
                    className={`flex-1 ${
                      isLeft ? 'ml-16 mr-0' : 'ml-0 mr-16'
                    } ${isSelected ? 'scale-105' : ''} transition-transform`}
                  >
                    <div
                      className={`cursor-pointer rounded-[24px] border p-4 transition-all ${
                        isSelected
                          ? 'border-[#d0a85b]/28 bg-[#6a4a1e]/12 shadow-[0_16px_36px_rgba(208,168,91,0.12)]'
                          : 'border-white/10 bg-white/[0.03] hover:border-[#d0a85b]/20 hover:bg-white/[0.05]'
                      }`}
                      onClick={() => handleNodeClick(node)}
                    >
                      <div className="mb-2 flex items-start justify-between">
                        <div className="flex items-center space-x-2">
                          <Icon className={`h-5 w-5 ${colorClass}`} />
                          <h4 className="font-medium text-[#f4ece1]">
                            {node.nodeTitle}
                          </h4>
                        </div>
                        <div className="flex items-center space-x-1">
                          {Array.from({
                            length: Math.max(1, node.importance || 3),
                          }).map((_, dotIndex) => (
                            <div
                              key={`${node.id || index}-dot-${dotIndex}`}
                              className="h-1.5 w-1.5 rounded-full bg-[#dcb86f]"
                            />
                          ))}
                        </div>
                      </div>
                      <p className="mb-2 text-sm text-[#bdaa94]">
                        {node.nodeDesc}
                      </p>
                      <div className="text-xs text-[#8f7b66]">
                        {formatLocaleDate(
                          node.startDate,
                          locale,
                          node.startDate
                        )}{' '}
                        - {formatLocaleDate(node.endDate, locale, node.endDate)}
                      </div>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {selectedNode ? (
          <div className="mt-6 rounded-[24px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 p-4">
            <h4 className="mb-2 font-medium text-[#f4ece1]">
              {selectedNode.nodeTitle}
            </h4>
            {selectedNode.aiInterpretation ? (
              <div className="mb-3">
                <p className="text-sm leading-6 text-[#e4d6c8]">
                  {selectedNode.aiInterpretation}
                </p>
              </div>
            ) : null}
            {selectedNode.actionSuggestion ? (
              <div>
                <p className="mb-1 text-sm font-medium text-[#dcb86f]">
                  {copy.actionSuggestion}
                </p>
                <p className="text-sm leading-6 text-[#e4d6c8]">
                  {selectedNode.actionSuggestion}
                </p>
              </div>
            ) : null}
          </div>
        ) : null}
      </div>
    </Card>
  )
}
