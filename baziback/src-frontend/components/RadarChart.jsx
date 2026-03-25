import { useEffect, useMemo, useRef, useState } from 'react'
import { Info } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

const RADAR_COPY = {
  'zh-CN': {
    defaultTitle: '雷达分析',
    hoverHint: '悬停查看详情',
    strength: '强度',
    category: '维度',
    noData: '暂无可展示的数据',
    elements: {
      wood: '木',
      fire: '火',
      earth: '土',
      metal: '金',
      water: '水',
    },
  },
  'en-US': {
    defaultTitle: 'Radar Analysis',
    hoverHint: 'Hover for details',
    strength: 'Strength',
    category: 'Category',
    noData: 'No data available',
    elements: {
      wood: 'Wood',
      fire: 'Fire',
      earth: 'Earth',
      metal: 'Metal',
      water: 'Water',
    },
  },
}

const ELEMENT_META = [
  { key: 'wood', color: '#dcb86f', angle: -90 },
  { key: 'fire', color: '#e19a84', angle: -18 },
  { key: 'earth', color: '#c78734', angle: 54 },
  { key: 'metal', color: '#f0d9a5', angle: 126 },
  { key: 'water', color: '#bdaa94', angle: 198 },
]

function normalizeRadarData(data, copy) {
  if (Array.isArray(data)) {
    return data.slice(0, 5).map((item, index) => ({
      key: item.key || `item-${index}`,
      name: item.name || item.label || `${copy.category} ${index + 1}`,
      value: Number(item.value) || 0,
      color: item.color || ELEMENT_META[index % ELEMENT_META.length].color,
      description: item.description || item.desc || '',
      angle:
        item.angle !== undefined
          ? item.angle
          : ELEMENT_META[index % ELEMENT_META.length].angle,
    }))
  }

  if (data && typeof data === 'object') {
    return ELEMENT_META.map((element) => ({
      ...element,
      name: copy.elements[element.key],
      value: Number(data[element.key]) || 0,
      description: data[`${element.key}_desc`] || '',
    }))
  }

  return []
}

export default function RadarChart({
  data,
  title,
  size = 300,
  showTooltip = true,
  onHover,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = RADAR_COPY[locale]
  const canvasRef = useRef(null)
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [tooltip, setTooltip] = useState(null)

  const chartItems = useMemo(() => normalizeRadarData(data, copy), [data, copy])

  useEffect(() => {
    if (!canvasRef.current || chartItems.length === 0) return

    const canvas = canvasRef.current
    const context = canvas.getContext('2d')
    const centerX = size / 2
    const centerY = size / 2
    const radius = size * 0.3

    context.clearRect(0, 0, size, size)

    for (let ring = 1; ring <= 5; ring += 1) {
      context.beginPath()
      chartItems.forEach((item, index) => {
        const angle = (item.angle * Math.PI) / 180
        const x = centerX + Math.cos(angle) * ((radius * ring) / 5)
        const y = centerY + Math.sin(angle) * ((radius * ring) / 5)
        if (index === 0) {
          context.moveTo(x, y)
        } else {
          context.lineTo(x, y)
        }
      })
      context.closePath()
      context.strokeStyle = 'rgba(255, 255, 255, 0.1)'
      context.lineWidth = 1
      context.stroke()
    }

    chartItems.forEach((item) => {
      const angle = (item.angle * Math.PI) / 180
      const x = centerX + Math.cos(angle) * radius
      const y = centerY + Math.sin(angle) * radius
      context.beginPath()
      context.moveTo(centerX, centerY)
      context.lineTo(x, y)
      context.strokeStyle = 'rgba(255, 255, 255, 0.14)'
      context.stroke()
    })

    context.beginPath()
    chartItems.forEach((item, index) => {
      const angle = (item.angle * Math.PI) / 180
      const normalizedValue = Math.min(Math.max(item.value / 100, 0), 1)
      const x = centerX + Math.cos(angle) * radius * normalizedValue
      const y = centerY + Math.sin(angle) * radius * normalizedValue

      if (index === 0) {
        context.moveTo(x, y)
      } else {
        context.lineTo(x, y)
      }
    })
    context.closePath()
    context.fillStyle = 'rgba(208, 168, 91, 0.14)'
    context.strokeStyle = '#dcb86f'
    context.lineWidth = 2
    context.fill()
    context.stroke()

    chartItems.forEach((item, index) => {
      const angle = (item.angle * Math.PI) / 180
      const normalizedValue = Math.min(Math.max(item.value / 100, 0), 1)
      const x = centerX + Math.cos(angle) * radius * normalizedValue
      const y = centerY + Math.sin(angle) * radius * normalizedValue

      context.beginPath()
      context.arc(x, y, 4, 0, Math.PI * 2)
      context.fillStyle = item.color
      context.fill()

      if (hoveredIndex === index) {
        context.beginPath()
        context.arc(x, y, 8, 0, Math.PI * 2)
        context.strokeStyle = item.color
        context.lineWidth = 2
        context.stroke()
      }
    })

    context.font = '14px Arial'
    context.textAlign = 'center'
    context.textBaseline = 'middle'

    chartItems.forEach((item) => {
      const angle = (item.angle * Math.PI) / 180
      const labelRadius = radius + 28
      const x = centerX + Math.cos(angle) * labelRadius
      const y = centerY + Math.sin(angle) * labelRadius
      context.fillStyle = item.color
      context.fillText(item.name, x, y)
    })
  }, [chartItems, hoveredIndex, size])

  const handleMouseMove = (event) => {
    if (!canvasRef.current || !showTooltip || chartItems.length === 0) return

    const canvas = canvasRef.current
    const rect = canvas.getBoundingClientRect()
    const centerX = size / 2
    const centerY = size / 2
    const radius = size * 0.3
    const x = event.clientX - rect.left
    const y = event.clientY - rect.top
    const dx = x - centerX
    const dy = y - centerY
    const distance = Math.sqrt(dx * dx + dy * dy)
    const angle = (Math.atan2(dy, dx) * 180) / Math.PI

    if (distance > radius + 28) {
      setHoveredIndex(null)
      setTooltip(null)
      return
    }

    let nearestIndex = -1
    let minDifference = Number.POSITIVE_INFINITY

    chartItems.forEach((item, index) => {
      let diff = Math.abs(angle - item.angle)
      if (diff > 180) diff = 360 - diff
      if (diff < minDifference) {
        minDifference = diff
        nearestIndex = index
      }
    })

    if (nearestIndex >= 0 && minDifference < 30) {
      const item = chartItems[nearestIndex]
      setHoveredIndex(nearestIndex)
      setTooltip({
        x: event.clientX,
        y: event.clientY,
        item,
      })
      onHover?.(item.key, item.value)
      return
    }

    setHoveredIndex(null)
    setTooltip(null)
  }

  const handleMouseLeave = () => {
    setHoveredIndex(null)
    setTooltip(null)
  }

  if (chartItems.length === 0) {
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

      <div className="relative inline-block">
        <canvas
          ref={canvasRef}
          width={size}
          height={size}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
          className="cursor-pointer"
        />

        <div className="mt-4 flex flex-wrap justify-center gap-3 text-sm">
          {chartItems.map((item) => (
            <div
              key={item.key}
              className="flex items-center space-x-1"
              style={{ color: item.color }}
            >
              <div
                className="h-3 w-3 rounded-full"
                style={{ backgroundColor: item.color }}
              />
              <span>
                {item.name}: {item.value}%
              </span>
            </div>
          ))}
        </div>

        {tooltip && showTooltip ? (
          <div
            className="pointer-events-none fixed z-50 rounded-[20px] border border-white/10 bg-[#120d0c]/96 px-4 py-3 shadow-[0_18px_36px_rgba(0,0,0,0.34)]"
            style={{
              left: `${tooltip.x + 10}px`,
              top: `${tooltip.y - 10}px`,
              transform: 'translateY(-100%)',
            }}
          >
            <div
              className="mb-1 text-sm font-medium"
              style={{ color: tooltip.item.color }}
            >
              {tooltip.item.name}
            </div>
            <div className="text-xs text-[#e4d6c8]">
              {copy.strength}: {tooltip.item.value}%
            </div>
            {tooltip.item.description ? (
              <div className="mt-1 max-w-xs text-xs leading-6 text-[#8f7b66]">
                {tooltip.item.description}
              </div>
            ) : null}
          </div>
        ) : null}
      </div>
    </div>
  )
}
