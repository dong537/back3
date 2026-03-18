import { useState, useMemo } from 'react'
import { TrendingUp, TrendingDown, Minus, Calendar, Info } from 'lucide-react'
import { useTranslation } from 'react-i18next'

/**
 * 大运走势图组件 - 优化版，更清晰明了
 */
export default function DayunTrendChart({ data, onNodeClick }) {
  const { t } = useTranslation()
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })

  // 计算综合运势分数
  const chartData = useMemo(() => {
    if (!data || data.length === 0) return []
    
    return data.map((yun, index) => {
      // 如果有运势数据，使用它；否则基于干支计算
      let overallScore = 50
      if (yun.wealth !== undefined && yun.career !== undefined && yun.love !== undefined && yun.health !== undefined) {
        overallScore = Math.round((yun.wealth + yun.career + yun.love + yun.health) / 4)
      } else {
        // 基于干支的简单哈希计算
        const ganZhi = yun.干支 || yun.大运 || ''
        const hash = ganZhi.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
        overallScore = 50 + (hash % 30)
      }

      return {
        ...yun,
        index,
        overallScore,
        startAge: yun.起始年龄 || (index * 10 + 1),
        endAge: yun.结束年龄 || ((index + 1) * 10),
        ganZhi: yun.干支 || yun.大运 || '',
        naYin: yun.纳音 || '',
        yearRange: yun.年份范围 || yun.开始 || ''
      }
    })
  }, [data])

  if (!chartData || chartData.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-gray-400">
        <div className="text-center">
          <Calendar className="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无大运数据</p>
        </div>
      </div>
    )
  }

  const maxScore = Math.max(...chartData.map(d => d.overallScore), 100)
  const minScore = Math.min(...chartData.map(d => d.overallScore), 0)
  const scoreRange = maxScore - minScore || 1

  // 获取趋势图标
  const getTrendIcon = (current, prev) => {
    if (prev === undefined) return <Minus className="w-4 h-4" />
    if (current > prev) return <TrendingUp className="w-4 h-4 text-green-400" />
    if (current < prev) return <TrendingDown className="w-4 h-4 text-red-400" />
    return <Minus className="w-4 h-4 text-gray-400" />
  }

  // 获取运势等级
  const getFortuneLevel = (score) => {
    if (score >= 80) return { label: '极佳', color: 'text-green-400', bg: 'bg-green-500/20', border: 'border-green-500/50' }
    if (score >= 65) return { label: '良好', color: 'text-blue-400', bg: 'bg-blue-500/20', border: 'border-blue-500/50' }
    if (score >= 50) return { label: '平稳', color: 'text-yellow-400', bg: 'bg-yellow-500/20', border: 'border-yellow-500/50' }
    if (score >= 35) return { label: '一般', color: 'text-orange-400', bg: 'bg-orange-500/20', border: 'border-orange-500/50' }
    return { label: '需注意', color: 'text-red-400', bg: 'bg-red-500/20', border: 'border-red-500/50' }
  }

  return (
    <div className="space-y-6">
      {/* 图表说明 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2 text-sm text-gray-400">
          <Info size={16} />
          <span>综合运势评分：0-100分，分数越高运势越好</span>
        </div>
        <div className="flex items-center space-x-4 text-xs text-gray-500">
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 rounded bg-green-500/50"></div>
            <span>极佳(80+)</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 rounded bg-blue-500/50"></div>
            <span>良好(65-79)</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 rounded bg-yellow-500/50"></div>
            <span>平稳(50-64)</span>
          </div>
        </div>
      </div>

      {/* 主图表区域 */}
      <div className="relative">
        {/* 背景网格 */}
        <div className="absolute inset-0 flex flex-col justify-between py-8">
          {[0, 25, 50, 75, 100].map((line) => (
            <div key={line} className="border-t border-white/5">
              <span className="absolute left-0 text-xs text-gray-600 -mt-2">{line}</span>
            </div>
          ))}
        </div>

        {/* 数据点连线 */}
        <div className="relative h-64 flex items-end justify-between px-4 pb-8">
          {chartData.map((item, index) => {
            const prevScore = index > 0 ? chartData[index - 1].overallScore : undefined
            const nextScore = index < chartData.length - 1 ? chartData[index + 1].overallScore : undefined
            const height = ((item.overallScore - minScore) / scoreRange) * 100
            const level = getFortuneLevel(item.overallScore)
            const isHovered = hoveredIndex === index

            return (
              <div
                key={index}
                className="flex-1 flex flex-col items-center justify-end relative group cursor-pointer"
                onMouseEnter={(e) => {
                  setHoveredIndex(index)
                  const rect = e.currentTarget.getBoundingClientRect()
                  setMousePosition({
                    x: e.clientX - rect.left,
                    y: e.clientY - rect.top
                  })
                }}
                onMouseMove={(e) => {
                  if (hoveredIndex === index) {
                    const rect = e.currentTarget.getBoundingClientRect()
                    setMousePosition({
                      x: e.clientX - rect.left,
                      y: e.clientY - rect.top
                    })
                  }
                }}
                onMouseLeave={() => {
                  setHoveredIndex(null)
                }}
                onClick={() => onNodeClick?.(item)}
              >
                {/* 连接线 */}
                {index < chartData.length - 1 && (
                  <div
                    className="absolute top-1/2 left-1/2 w-full h-0.5 z-0 opacity-30"
                    style={{
                      background: `linear-gradient(to right, rgba(34, 197, 94, 0.5), rgba(59, 130, 246, 0.5))`,
                      transform: 'translateY(-50%)'
                    }}
                  />
                )}

                  {/* 数据柱 */}
                  <div className="relative w-full flex flex-col items-center z-10">
                    {/* 柱状图 */}
                    <div
                      className={`w-12 rounded-t-lg transition-all duration-300 ${
                        isHovered ? 'w-16 shadow-lg' : ''
                      } ${level.bg} ${level.border} border-2`}
                      style={{
                        height: `${Math.max(height, 5)}%`,
                        minHeight: '20px'
                      }}
                    >
                    </div>

                  {/* 底部信息卡片 */}
                  <div
                    className={`mt-2 w-full text-center transition-all duration-300 ${
                      isHovered ? 'scale-110' : ''
                    }`}
                  >
                    {/* 年龄范围 */}
                    <div className="text-xs font-medium text-white mb-1">
                      {item.startAge}-{item.endAge}岁
                    </div>
                    
                    {/* 干支 */}
                    <div className="text-sm font-bold text-white mb-1">
                      {item.ganZhi}
                    </div>

                    {/* 纳音 */}
                    {item.naYin && (
                      <div className="text-xs text-gray-400 mb-1">
                        {item.naYin}
                      </div>
                    )}

                    {/* 年份范围 */}
                    {item.yearRange && (
                      <div className="text-xs text-gray-500">
                        {item.yearRange}
                      </div>
                    )}

                    {/* 趋势图标 */}
                    {prevScore !== undefined && (
                      <div className="mt-1 flex justify-center">
                        {getTrendIcon(item.overallScore, prevScore)}
                      </div>
                    )}

                    {/* 运势等级标签 */}
                    <div className={`mt-1 inline-block px-2 py-0.5 rounded text-xs ${level.bg} ${level.color} border ${level.border}`}>
                      {level.label}
                    </div>
                  </div>
                </div>

                {/* 悬停详情卡片 */}
                {isHovered && (
                  <div 
                    className="absolute z-50 pointer-events-none"
                    style={{
                      left: `${mousePosition.x}px`,
                      top: `${mousePosition.y - 10}px`,
                      transform: 'translate(-50%, -100%)'
                    }}
                  >
                    <div className="glass rounded-lg p-3 shadow-xl border border-white/20 min-w-[200px]">
                      <div className="text-sm font-bold text-white mb-2 text-center">
                        {item.startAge}-{item.endAge}岁大运
                      </div>
                      
                      <div className="space-y-1 text-xs">
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400">干支：</span>
                          <span className="text-white font-medium">{item.ganZhi}</span>
                        </div>
                        {item.naYin && (
                          <div className="flex items-center justify-between">
                            <span className="text-gray-400">纳音：</span>
                            <span className="text-white">{item.naYin}</span>
                          </div>
                        )}
                        {item.wealth !== undefined && (
                          <>
                            <div className="border-t border-white/10 my-1"></div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-400">财运：</span>
                              <span className="text-yellow-400">{item.wealth || 0}%</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-400">事业：</span>
                              <span className="text-blue-400">{item.career || 0}%</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-400">感情：</span>
                              <span className="text-red-400">{item.love || 0}%</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-gray-400">健康：</span>
                              <span className="text-green-400">{item.health || 0}%</span>
                            </div>
                          </>
                        )}
                        {item.desc && (
                          <>
                            <div className="border-t border-white/10 my-1"></div>
                            <div className="text-gray-300 text-xs">{item.desc}</div>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* 图例说明 */}
      <div className="flex flex-wrap items-center justify-center gap-4 text-xs text-gray-400 pt-4 border-t border-white/10">
        <div className="flex items-center space-x-1">
          <TrendingUp className="w-3 h-3 text-green-400" />
          <span>运势上升</span>
        </div>
        <div className="flex items-center space-x-1">
          <TrendingDown className="w-3 h-3 text-red-400" />
          <span>运势下降</span>
        </div>
        <div className="flex items-center space-x-1">
          <Minus className="w-3 h-3 text-gray-400" />
          <span>运势平稳</span>
        </div>
        <div className="text-gray-500 ml-4">
          点击柱状图可查看详细解读
        </div>
      </div>
    </div>
  )
}
