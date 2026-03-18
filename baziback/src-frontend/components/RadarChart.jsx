import { useState, useRef, useEffect } from 'react'
import { Info } from 'lucide-react'
import { useTranslation } from 'react-i18next'

/**
 * 雷达图组件 - 展示八字五行强弱
 */
export default function RadarChart({ 
  data, 
  title = '五行强弱分析',
  size = 300,
  showTooltip = true,
  onHover 
}) {
  const { t } = useTranslation()
  const canvasRef = useRef(null)
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [tooltip, setTooltip] = useState(null)

  // 五行数据
  const elements = [
    { name: '木', key: 'wood', color: '#4ade80', angle: -90 },
    { name: '火', key: 'fire', color: '#f87171', angle: -18 },
    { name: '土', key: 'earth', color: '#fbbf24', angle: 54 },
    { name: '金', key: 'metal', color: '#60a5fa', angle: 126 },
    { name: '水', key: 'water', color: '#34d399', angle: 198 }
  ]

  useEffect(() => {
    if (!canvasRef.current || !data) return

    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    const centerX = size / 2
    const centerY = size / 2
    const radius = size * 0.35

    // 清空画布
    ctx.clearRect(0, 0, size, size)

    // 绘制背景网格
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)'
    ctx.lineWidth = 1
    for (let i = 1; i <= 5; i++) {
      ctx.beginPath()
      ctx.arc(centerX, centerY, (radius * i) / 5, 0, Math.PI * 2)
      ctx.stroke()
    }

    // 绘制轴线
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)'
    ctx.lineWidth = 1
    elements.forEach((element, index) => {
      const angle = (element.angle * Math.PI) / 180
      const x = centerX + Math.cos(angle) * radius
      const y = centerY + Math.sin(angle) * radius
      ctx.beginPath()
      ctx.moveTo(centerX, centerY)
      ctx.lineTo(x, y)
      ctx.stroke()
    })

    // 绘制数据区域
    if (data && Object.keys(data).length > 0) {
      ctx.fillStyle = 'rgba(139, 92, 246, 0.3)'
      ctx.strokeStyle = '#8b5cf6'
      ctx.lineWidth = 2
      ctx.beginPath()

      elements.forEach((element, index) => {
        const value = data[element.key] || 0
        const normalizedValue = Math.min(Math.max(value / 100, 0), 1)
        const angle = (element.angle * Math.PI) / 180
        const x = centerX + Math.cos(angle) * radius * normalizedValue
        const y = centerY + Math.sin(angle) * radius * normalizedValue

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })

      ctx.closePath()
      ctx.fill()
      ctx.stroke()

      // 绘制数据点
      elements.forEach((element, index) => {
        const value = data[element.key] || 0
        const normalizedValue = Math.min(Math.max(value / 100, 0), 1)
        const angle = (element.angle * Math.PI) / 180
        const x = centerX + Math.cos(angle) * radius * normalizedValue
        const y = centerY + Math.sin(angle) * radius * normalizedValue

        ctx.fillStyle = element.color
        ctx.beginPath()
        ctx.arc(x, y, 4, 0, Math.PI * 2)
        ctx.fill()

        // 悬停效果
        if (hoveredIndex === index) {
          ctx.strokeStyle = element.color
          ctx.lineWidth = 2
          ctx.beginPath()
          ctx.arc(x, y, 8, 0, Math.PI * 2)
          ctx.stroke()
        }
      })
    }

    // 绘制标签
    ctx.fillStyle = '#ffffff'
    ctx.font = '14px Arial'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'

    elements.forEach((element) => {
      const angle = (element.angle * Math.PI) / 180
      const labelRadius = radius + 30
      const x = centerX + Math.cos(angle) * labelRadius
      const y = centerY + Math.sin(angle) * labelRadius

      ctx.fillStyle = element.color
      ctx.fillText(element.name, x, y)
    })
  }, [data, size, hoveredIndex])

  const handleMouseMove = (e) => {
    if (!canvasRef.current || !showTooltip) return

    const canvas = canvasRef.current
    const rect = canvas.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    const centerX = size / 2
    const centerY = size / 2
    const radius = size * 0.35

    // 计算鼠标位置对应的元素
    const dx = x - centerX
    const dy = y - centerY
    const distance = Math.sqrt(dx * dx + dy * dy)
    const angle = Math.atan2(dy, dx) * (180 / Math.PI)

    if (distance <= radius + 20) {
      // 找到最近的元素
      let minDiff = Infinity
      let nearestIndex = -1

      elements.forEach((element, index) => {
        const elementAngle = element.angle
        let diff = Math.abs(angle - elementAngle)
        if (diff > 180) diff = 360 - diff

        if (diff < minDiff) {
          minDiff = diff
          nearestIndex = index
        }
      })

      if (nearestIndex >= 0 && minDiff < 30) {
        const element = elements[nearestIndex]
        const value = data?.[element.key] || 0
        setHoveredIndex(nearestIndex)
        setTooltip({
          x: e.clientX,
          y: e.clientY,
          element: element.name,
          value: value,
          key: element.key
        })
        onHover?.(element.key, value)
      } else {
        setHoveredIndex(null)
        setTooltip(null)
      }
    } else {
      setHoveredIndex(null)
      setTooltip(null)
    }
  }

  const handleMouseLeave = () => {
    setHoveredIndex(null)
    setTooltip(null)
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

      <div className="relative inline-block">
        <canvas
          ref={canvasRef}
          width={size}
          height={size}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
          className="cursor-pointer"
        />

        {/* 数据标签 */}
        {data && (
          <div className="mt-4 flex justify-center space-x-4 text-sm">
            {elements.map((element) => {
              const value = data[element.key] || 0
              return (
                <div
                  key={element.key}
                  className="flex items-center space-x-1"
                  style={{ color: element.color }}
                >
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: element.color }}
                  />
                  <span>{element.name}: {value}%</span>
                </div>
              )
            })}
          </div>
        )}

        {/* 悬停提示 */}
        {tooltip && showTooltip && (
          <div
            className="fixed z-50 px-4 py-2 bg-gray-900 border border-white/20 rounded-lg shadow-lg pointer-events-none"
            style={{
              left: `${tooltip.x + 10}px`,
              top: `${tooltip.y - 10}px`,
              transform: 'translateY(-100%)'
            }}
          >
            <div className="text-sm font-medium mb-1" style={{ color: elements[hoveredIndex]?.color }}>
              {tooltip.element}行
            </div>
            <div className="text-xs text-gray-300">
              强度: {tooltip.value}%
            </div>
            {data?.[`${tooltip.key}_desc`] && (
              <div className="text-xs text-gray-400 mt-1 max-w-xs">
                {data[`${tooltip.key}_desc`]}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
