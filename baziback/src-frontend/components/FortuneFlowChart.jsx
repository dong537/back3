import { useEffect, useMemo, useRef, useState } from 'react'
import { Info } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

const FLOW_COPY = {
  'zh-CN': {
    defaultTitle: '运势走势',
    noData: '暂无数据',
    hoverHint: '悬停查看详情',
    period: (index) => `阶段 ${index}`,
    wealth: '财运',
    career: '事业',
    love: '感情',
    health: '健康',
  },
  'en-US': {
    defaultTitle: 'Fortune Trend',
    noData: 'No data available',
    hoverHint: 'Hover for details',
    period: (index) => `Period ${index}`,
    wealth: 'Wealth',
    career: 'Career',
    love: 'Love',
    health: 'Health',
  },
}

export default function FortuneFlowChart({
  data,
  title,
  height = 300,
  showTooltip = true,
  onHover,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = FLOW_COPY[locale]
  const canvasRef = useRef(null)
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [tooltip, setTooltip] = useState(null)

  const categories = useMemo(
    () => [
      { key: 'wealth', name: copy.wealth, color: '#dcb86f' },
      { key: 'career', name: copy.career, color: '#c78734' },
      { key: 'love', name: copy.love, color: '#e19a84' },
      { key: 'health', name: copy.health, color: '#bdaa94' },
    ],
    [copy]
  )

  useEffect(() => {
    if (!canvasRef.current || !Array.isArray(data) || data.length === 0) return

    const canvas = canvasRef.current
    const context = canvas.getContext('2d')
    const width = canvas.width
    const chartHeight = height - 60
    const padding = { top: 20, right: 40, bottom: 40, left: 60 }
    const allValues = data.flatMap((item) =>
      categories.map((category) => Number(item[category.key]) || 0)
    )
    const minValue = Math.min(...allValues, 0)
    const maxValue = Math.max(...allValues, 100)
    const range = maxValue - minValue || 1
    const chartWidth = width - padding.left - padding.right
    const stepX = chartWidth / (data.length - 1 || 1)

    context.clearRect(0, 0, width, height)

    context.strokeStyle = 'rgba(255, 255, 255, 0.08)'
    context.lineWidth = 1
    const gridLines = 5
    for (let line = 0; line <= gridLines; line += 1) {
      const y = padding.top + (chartHeight / gridLines) * line
      const value = maxValue - (range / gridLines) * line
      context.beginPath()
      context.moveTo(padding.left, y)
      context.lineTo(width - padding.right, y)
      context.stroke()

      context.fillStyle = '#8f7b66'
      context.font = '12px Arial'
      context.textAlign = 'right'
      context.textBaseline = 'middle'
      context.fillText(Math.round(value).toString(), padding.left - 10, y)
    }

    categories.forEach((category) => {
      context.strokeStyle = category.color
      context.lineWidth = 2
      context.beginPath()

      data.forEach((item, index) => {
        const value = Number(item[category.key]) || 0
        const normalizedValue = (value - minValue) / range
        const x = padding.left + index * stepX
        const y = padding.top + chartHeight - normalizedValue * chartHeight

        if (index === 0) {
          context.moveTo(x, y)
        } else {
          context.lineTo(x, y)
        }
      })

      context.stroke()

      data.forEach((item, index) => {
        const value = Number(item[category.key]) || 0
        const normalizedValue = (value - minValue) / range
        const x = padding.left + index * stepX
        const y = padding.top + chartHeight - normalizedValue * chartHeight

        context.fillStyle = category.color
        context.beginPath()
        context.arc(x, y, hoveredIndex === index ? 5 : 3, 0, Math.PI * 2)
        context.fill()
      })
    })

    context.fillStyle = '#8f7b66'
    context.font = '12px Arial'
    context.textAlign = 'center'
    context.textBaseline = 'top'
    data.forEach((item, index) => {
      const x = padding.left + index * stepX
      const label = item.label || copy.period(index + 1)
      context.fillText(label, x, height - padding.bottom + 10)
    })

    const legendX = width - padding.right - 110
    const legendY = padding.top + 12
    categories.forEach((category, index) => {
      const y = legendY + index * 20
      context.fillStyle = category.color
      context.fillRect(legendX, y - 6, 12, 12)
      context.fillStyle = '#f4ece1'
      context.textAlign = 'left'
      context.fillText(category.name, legendX + 18, y)
    })
  }, [categories, copy, data, height, hoveredIndex])

  const handleMouseMove = (event) => {
    if (
      !canvasRef.current ||
      !showTooltip ||
      !Array.isArray(data) ||
      data.length === 0
    ) {
      return
    }

    const canvas = canvasRef.current
    const rect = canvas.getBoundingClientRect()
    const x = event.clientX - rect.left
    const padding = { left: 60, right: 40 }
    const chartWidth = canvas.width - padding.left - padding.right
    const stepX = chartWidth / (data.length - 1 || 1)
    const index = Math.round((x - padding.left) / stepX)

    if (index >= 0 && index < data.length) {
      setHoveredIndex(index)
      setTooltip({
        x: event.clientX,
        y: event.clientY,
        item: data[index],
        index,
      })
      onHover?.(data[index], index)
      return
    }

    setHoveredIndex(null)
    setTooltip(null)
  }

  const handleMouseLeave = () => {
    setHoveredIndex(null)
    setTooltip(null)
  }

  if (!Array.isArray(data) || data.length === 0) {
    return (
      <div className="panel-soft flex h-64 items-center justify-center rounded-[28px] text-[#8f7b66]">
        {copy.noData}
      </div>
    )
  }

  return (
    <div className="relative">
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-lg font-bold text-[#f4ece1]">
          {title || copy.defaultTitle}
        </h3>
        {showTooltip ? (
          <div className="flex items-center space-x-2 text-sm text-[#8f7b66]">
            <Info size={16} />
            <span>{copy.hoverHint}</span>
          </div>
        ) : null}
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

        {tooltip && showTooltip ? (
          <div
            className="pointer-events-none fixed z-50 min-w-[220px] rounded-[20px] border border-white/10 bg-[#120d0c]/96 px-4 py-3 shadow-[0_18px_36px_rgba(0,0,0,0.34)]"
            style={{
              left: `${tooltip.x + 10}px`,
              top: `${tooltip.y - 10}px`,
              transform: 'translateY(-100%)',
            }}
          >
            <div className="mb-2 text-sm font-medium text-[#f4ece1]">
              {tooltip.item.label || copy.period(tooltip.index + 1)}
            </div>
            <div className="space-y-1 text-xs">
              {categories.map((category) => (
                <div
                  key={category.key}
                  className="flex items-center justify-between"
                >
                  <span className="text-[#8f7b66]">{category.name}</span>
                  <span style={{ color: category.color }}>
                    {Number(tooltip.item[category.key]) || 0}%
                  </span>
                </div>
              ))}
            </div>
            {tooltip.item.desc ? (
              <div className="mt-2 border-t border-white/10 pt-2 text-xs leading-6 text-[#bdaa94]">
                {tooltip.item.desc}
              </div>
            ) : null}
          </div>
        ) : null}
      </div>
    </div>
  )
}
