import { useState } from 'react'
import { Calendar, TrendingUp, TrendingDown, AlertCircle, Heart, Briefcase, Wallet, Activity } from 'lucide-react'
import Card from './Card'

/**
 * 运势时间轴图表组件
 * 显示大运、流年关键节点
 */
export default function FortuneTimelineChart({ timelineData = [], onNodeClick }) {
  const [selectedNode, setSelectedNode] = useState(null)

  const nodeIcons = {
    wealth_peak: Wallet,
    wealth_low: Wallet,
    career_turn: Briefcase,
    career_peak: Briefcase,
    love_opportunity: Heart,
    love_challenge: Heart,
    health_warning: Activity,
    health_good: Activity,
  }

  const nodeColors = {
    wealth_peak: 'text-green-400',
    wealth_low: 'text-red-400',
    career_turn: 'text-blue-400',
    career_peak: 'text-purple-400',
    love_opportunity: 'text-pink-400',
    love_challenge: 'text-orange-400',
    health_warning: 'text-red-400',
    health_good: 'text-green-400',
  }

  const handleNodeClick = (node) => {
    setSelectedNode(node)
    if (onNodeClick) {
      onNodeClick(node)
    }
  }

  // 如果没有数据，显示示例数据
  const displayData = timelineData.length > 0 ? timelineData : [
    {
      id: 1,
      startDate: '2024-01-01',
      endDate: '2024-03-31',
      nodeType: 'wealth_peak',
      nodeTitle: '财运高峰期',
      nodeDesc: '此期间财运较旺，适合投资理财',
      importance: 8,
    },
    {
      id: 2,
      startDate: '2024-04-01',
      endDate: '2024-06-30',
      nodeType: 'career_turn',
      nodeTitle: '事业转折期',
      nodeDesc: '事业可能出现重要变化，需谨慎决策',
      importance: 9,
    },
    {
      id: 3,
      startDate: '2024-07-01',
      endDate: '2024-09-30',
      nodeType: 'love_opportunity',
      nodeTitle: '感情机遇期',
      nodeDesc: '感情运势较好，适合主动出击',
      importance: 7,
    },
  ]

  return (
    <Card className="panel">
      <div className="p-6">
        <h3 className="text-xl font-bold mb-6 flex items-center">
          <Calendar className="w-5 h-5 mr-2 text-purple-400" />
          年度运势时间轴
        </h3>

        <div className="relative">
          {/* 时间轴主线 */}
          <div className="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-purple-500 via-pink-500 to-purple-500"></div>

          {/* 节点 */}
          <div className="space-y-8">
            {displayData.map((node, index) => {
              const Icon = nodeIcons[node.nodeType] || AlertCircle
              const colorClass = nodeColors[node.nodeType] || 'text-gray-400'
              const isLeft = index % 2 === 0

              return (
                <div
                  key={node.id}
                  className={`relative flex items-center ${isLeft ? 'flex-row' : 'flex-row-reverse'}`}
                >
                  {/* 节点圆圈 */}
                  <div
                    className={`absolute left-8 w-4 h-4 rounded-full border-2 border-purple-500 bg-gray-900 z-10 cursor-pointer hover:scale-125 transition-transform ${
                      selectedNode?.id === node.id ? 'ring-2 ring-purple-400' : ''
                    }`}
                    onClick={() => handleNodeClick(node)}
                    style={{
                      transform: 'translateX(-50%)',
                    }}
                  />

                  {/* 内容卡片 */}
                  <div
                    className={`flex-1 ${isLeft ? 'ml-16 mr-0' : 'mr-16 ml-0'} ${
                      selectedNode?.id === node.id ? 'scale-105' : ''
                    } transition-transform`}
                  >
                    <div
                      className={`p-4 rounded-lg border cursor-pointer hover:border-purple-500/50 transition-all ${
                        selectedNode?.id === node.id
                          ? 'border-purple-500 bg-purple-500/10'
                          : 'border-white/10 bg-white/5'
                      }`}
                      onClick={() => handleNodeClick(node)}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center space-x-2">
                          <Icon className={`w-5 h-5 ${colorClass}`} />
                          <h4 className="font-medium text-white">{node.nodeTitle}</h4>
                        </div>
                        <div className="flex items-center space-x-1">
                          {Array.from({ length: node.importance || 5 }).map((_, i) => (
                            <div
                              key={i}
                              className="w-1.5 h-1.5 rounded-full bg-purple-400"
                            />
                          ))}
                        </div>
                      </div>
                      <p className="text-sm text-gray-400 mb-2">{node.nodeDesc}</p>
                      <div className="text-xs text-gray-500">
                        {node.startDate} - {node.endDate}
                      </div>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* 选中节点的详细信息 */}
        {selectedNode && (
          <div className="mt-6 p-4 bg-purple-500/10 border border-purple-500/30 rounded-lg">
            <h4 className="font-medium mb-2">{selectedNode.nodeTitle}</h4>
            {selectedNode.aiInterpretation && (
              <div className="mb-3">
                <p className="text-sm text-gray-300">{selectedNode.aiInterpretation}</p>
              </div>
            )}
            {selectedNode.actionSuggestion && (
              <div>
                <p className="text-sm font-medium text-purple-400 mb-1">行动建议：</p>
                <p className="text-sm text-gray-300">{selectedNode.actionSuggestion}</p>
              </div>
            )}
          </div>
        )}
      </div>
    </Card>
  )
}
