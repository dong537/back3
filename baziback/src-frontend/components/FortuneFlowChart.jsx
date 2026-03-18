import { useState, useEffect, useRef } from 'react'
import { TrendingUp, TrendingDown, Minus, Info } from 'lucide-react'
import { useTranslation } from 'react-i18next'

/**
 * 运势流图组件 - 展示运势趋势
 */
export default function FortuneFlowChart({ 
  data, 
  title = '运势趋势',
  height = 300,
  showTooltip = true,
  onHover 
}) {
  const { t } = useTranslation()
  const canvasRef = useRef(null)
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [tooltip, setTooltip] = useState(null)

  useEffect(() => {
    if (!canvasRef.current || !data || data.length === 0) return

    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    const width = canvas.width
    const chartHeight = height - 60
    const padding = { top: 20, right: 40, bottom: 40, left: 60 }

    // 清空画布
    ctx.clearRect(0, 0, width, height)

    // 计算数据范围
    const allValues = data.flatMap(d => [
      d.wealth || 0,
      d.career || 0,
      d.love || 0,
      d.health || 0
    ])
    const minValue = Math.min(...allValues, 0)
    const maxValue = Math.max(...allValues, 100)
    const range = maxValue - minValue || 1

    // 绘制背景网格
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)'
    ctx.lineWidth = 1
    const gridLines = 5
    for (let i = 0; i <= gridLines; i++) {
      const y = padding.top + (chartHeight / gridLines) * i
      ctx.beginPath()
      ctx.moveTo(padding.left, y)
      ctx.lineTo(width - padding.right, y)
      ctx.stroke()

      // Y轴标签
      const value = maxValue - (range / gridLines) * i
      ctx.fillStyle = '#9ca3af'
      ctx.font = '12px Arial'
      ctx.textAlign = 'right'
      ctx.textBaseline = 'middle'
      ctx.fillText(Math.round(value).toString(), padding.left - 10, y)
    }

    // 绘制数据线
    const colors = {
      wealth: '#fbbf24',
      career: '#3b82f6',
      love: '#ef4444',
      health: '#10b981'
    }

    const categories = [
      { key: 'wealth', name: '财运', color: colors.wealth },
      { key: 'career', name: '事业', color: colors.career },
      { key: 'love', name: '感情', color: colors.love },
      { key: 'health', name: '健康', color: colors.health }
    ]

    const chartWidth = width - padding.left - padding.right
    const stepX = chartWidth / (data.length - 1 || 1)

    categories.forEach((category) => {
      ctx.strokeStyle = category.color
      ctx.fillStyle = category.color
      ctx.lineWidth = 2
      ctx.beginPath()

      data.forEach((item, index) => {
        const value = item[category.key] || 0
        const normalizedValue = (value - minValue) / range
        const x = padding.left + index * stepX
        const y = padding.top + chartHeight - (normalizedValue * chartHeight)

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }

        // 绘制数据点
        ctx.beginPath()
        ctx.arc(x, y, 3, 0, Math.PI * 2)
        ctx.fill()

        // 悬停效果
        if (hoveredIndex === index) {
          ctx.beginPath()
          ctx.arc(x, y, 6, 0, Math.PI * 2)
          ctx.stroke()
        }
      })

      ctx.stroke()
    })

    // 绘制X轴标签
    ctx.fillStyle = '#9ca3af'
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'top'

    data.forEach((item, index) => {
      const x = padding.left + index * stepX
      const y = height - padding.bottom + 10
      ctx.fillText(item.label || `第${index + 1}天`, x, y)
    })

    // 绘制图例
    const legendX = width - padding.right - 100
    const legendY = padding.top + 10
    categories.forEach((category, index) => {
      const y = legendY + index * 20
      ctx.fillStyle = category.color
      ctx.fillRect(legendX, y - 6, 12, 12)
      ctx.fillStyle = '#ffffff'
      ctx.font = '12px Arial'
      ctx.textAlign = 'left'
      ctx.fillText(category.name, legendX + 16, y)
    })
  }, [data, height, hoveredIndex])

  const handleMouseMove = (e) => {
    if (!canvasRef.current || !showTooltip || !data || data.length === 0) return

    const canvas = canvasRef.current
    const rect = canvas.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top

    const padding = { left: 60, right: 40 }
    const chartWidth = canvas.width - padding.left - padding.right
    const stepX = chartWidth / (data.length - 1 || 1)

    // 计算鼠标位置对应的数据点
    const index = Math.round((x - padding.left) / stepX)
    if (index >= 0 && index < data.length) {
      setHoveredIndex(index)
      setTooltip({
        x: e.clientX,
        y: e.clientY,
        data: data[index]
      })
      onHover?.(data[index], index)
    } else {
      setHoveredIndex(null)
      setTooltip(null)
    }
  }

  const handleMouseLeave = () => {
    setHoveredIndex(null)
    setTooltip(null)
  }

  if (!data || data.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-gray-400">
        暂无数据
      </div>
    )
  }

  return (
    <div className="relative">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold">{title}</h3>
        {showTooltip && (
          <div className="flex items-center space-x-2 text-sm text-gray-400">
            <Info size={16} />
            <span>悬停查看详情</span>
          </div>
        )}
      </div>

      <div className="relative">
        <canvas
          ref={canvasRef}
          width={800}
          height={height}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
          className="w-full cursor-pointer"
        />

        {/* 悬停提示 */}
        {tooltip && showTooltip && (
          <div
            className="fixed z-50 px-4 py-3 bg-gray-900 border border-white/20 rounded-lg shadow-lg pointer-events-none min-w-[200px]"
            style={{
              left: `${tooltip.x + 10}px`,
              top: `${tooltip.y - 10}px`,
              transform: 'translateY(-100%)'
            }}
          >
            <div className="text-sm font-medium mb-2">{tooltip.data.label || `第${hoveredIndex + 1}天`}</div>
            <div className="space-y-1 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-gray-400">财运</span>
                <span className="text-yellow-400 font-medium">{tooltip.data.wealth || 0}%</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">事业</span>
                <span className="text-blue-400 font-medium">{tooltip.data.career || 0}%</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">感情</span>
                <span className="text-red-400 font-medium">{tooltip.data.love || 0}%</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-400">健康</span>
                <span className="text-green-400 font-medium">{tooltip.data.health || 0}%</span>
              </div>
            </div>
            {tooltip.data.desc && (
              <div className="mt-2 pt-2 border-t border-white/10 text-xs text-gray-400">
                {tooltip.data.desc}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
