import { useState } from 'react'
import { Info, TrendingUp, TrendingDown, Minus } from 'lucide-react'
import { useTranslation } from 'react-i18next'

/**
 * 八字强弱仪表盘组件
 */
export default function BaziStrengthMeter({ 
  data, 
  title = '八字强弱分析',
  showTooltip = true,
  onHover 
}) {
  const { t } = useTranslation()
  const [hoveredKey, setHoveredKey] = useState(null)

  // 五行配置
  const elements = [
    { key: 'wood', name: '木', color: '#4ade80', icon: '🌳' },
    { key: 'fire', name: '火', color: '#f87171', icon: '🔥' },
    { key: 'earth', name: '土', color: '#fbbf24', icon: '🌍' },
    { key: 'metal', name: '金', color: '#60a5fa', icon: '⚔️' },
    { key: 'water', name: '水', color: '#34d399', icon: '💧' }
  ]

  const getStrengthLevel = (value) => {
    if (value >= 80) return { level: '极强', color: '#10b981', trend: 'up' }
    if (value >= 60) return { level: '强', color: '#3b82f6', trend: 'up' }
    if (value >= 40) return { level: '中', color: '#fbbf24', trend: 'minus' }
    if (value >= 20) return { level: '弱', color: '#f87171', trend: 'down' }
    return { level: '极弱', color: '#ef4444', trend: 'down' }
  }

  const handleMouseEnter = (key, value) => {
    setHoveredKey(key)
    onHover?.(key, value)
  }

  const handleMouseLeave = () => {
    setHoveredKey(null)
  }

  if (!data) {
    return (
      <div className="flex items-center justify-center h-64 text-gray-400">
        暂无数据
      </div>
    )
  }

  return (
    <div className="relative">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-bold">{title}</h3>
        {showTooltip && (
          <div className="flex items-center space-x-2 text-sm text-gray-400">
            <Info size={16} />
            <span>悬停查看详情</span>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        {elements.map((element) => {
          const value = data[element.key] || 0
          const strength = getStrengthLevel(value)
          const isHovered = hoveredKey === element.key

          return (
            <div
              key={element.key}
              className="relative p-4 glass rounded-xl transition-all duration-300 cursor-pointer"
              style={{
                transform: isHovered ? 'scale(1.05)' : 'scale(1)',
                border: isHovered ? `2px solid ${element.color}` : '1px solid rgba(255,255,255,0.1)'
              }}
              onMouseEnter={() => handleMouseEnter(element.key, value)}
              onMouseLeave={handleMouseLeave}
            >
              {/* 元素图标和名称 */}
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-2">
                  <span className="text-2xl">{element.icon}</span>
                  <span className="font-medium" style={{ color: element.color }}>
                    {element.name}行
                  </span>
                </div>
                {strength.trend === 'up' && <TrendingUp size={16} className="text-green-400" />}
                {strength.trend === 'down' && <TrendingDown size={16} className="text-red-400" />}
                {strength.trend === 'minus' && <Minus size={16} className="text-yellow-400" />}
              </div>

              {/* 进度条 */}
              <div className="mb-2">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs text-gray-400">{strength.level}</span>
                  <span className="text-sm font-bold" style={{ color: element.color }}>
                    {value}%
                  </span>
                </div>
                <div className="w-full h-3 bg-white/10 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${value}%`,
                      backgroundColor: element.color,
                      boxShadow: `0 0 10px ${element.color}40`
                    }}
                  />
                </div>
              </div>

              {/* 描述 */}
              {data[`${element.key}_desc`] && (
                <div className="text-xs text-gray-400 mt-2 line-clamp-2">
                  {data[`${element.key}_desc`]}
                </div>
              )}

              {/* 悬停提示 */}
              {isHovered && showTooltip && data[`${element.key}_ai`] && (
                <div className="absolute top-full left-0 right-0 mt-2 p-3 bg-gray-900 border border-white/20 rounded-lg shadow-lg z-10">
                  <div className="text-xs text-gray-300">
                    <div className="font-medium mb-1">AI 解读：</div>
                    <div className="text-gray-400">{data[`${element.key}_ai`]}</div>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </div>

      {/* 综合评分 */}
      {data.overall && (
        <div className="mt-6 p-4 glass rounded-xl">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-400">综合评分</span>
            <div className="flex items-center space-x-2">
              <span className="text-2xl font-bold text-skin-primary">{data.overall}</span>
              <span className="text-sm text-gray-400">/ 100</span>
            </div>
          </div>
          <div className="mt-2 w-full h-2 bg-white/10 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-500"
              style={{ width: `${data.overall}%` }}
            />
          </div>
        </div>
      )}
    </div>
  )
}
