import { useMemo, useState } from 'react'
import { Info, Minus, TrendingDown, TrendingUp } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale, safeText } from '../utils/displayText'

const STRENGTH_COPY = {
  'zh-CN': {
    defaultTitle: '八字强弱分析',
    noData: '暂无数据',
    hoverHint: '悬停查看详情',
    aiReading: 'AI 解读',
    overallScore: '综合评分',
    elements: {
      wood: '木',
      fire: '火',
      earth: '土',
      metal: '金',
      water: '水',
    },
    levels: {
      veryStrong: '极强',
      strong: '偏强',
      balanced: '中和',
      weak: '偏弱',
      veryWeak: '极弱',
    },
  },
  'en-US': {
    defaultTitle: 'Bazi Strength Analysis',
    noData: 'No data available',
    hoverHint: 'Hover for details',
    aiReading: 'AI reading',
    overallScore: 'Overall score',
    elements: {
      wood: 'Wood',
      fire: 'Fire',
      earth: 'Earth',
      metal: 'Metal',
      water: 'Water',
    },
    levels: {
      veryStrong: 'Very strong',
      strong: 'Strong',
      balanced: 'Balanced',
      weak: 'Weak',
      veryWeak: 'Very weak',
    },
  },
}

const ELEMENT_META = [
  { key: 'wood', color: '#dcb86f', symbol: '木' },
  { key: 'fire', color: '#e19a84', symbol: '火' },
  { key: 'earth', color: '#c78734', symbol: '土' },
  { key: 'metal', color: '#f0d9a5', symbol: '金' },
  { key: 'water', color: '#bdaa94', symbol: '水' },
]

export default function BaziStrengthMeter({
  data,
  title,
  showTooltip = true,
  onHover,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = STRENGTH_COPY[locale]
  const [hoveredKey, setHoveredKey] = useState(null)

  const elements = useMemo(
    () =>
      ELEMENT_META.map((item) => ({
        ...item,
        name: copy.elements[item.key],
      })),
    [copy.elements]
  )

  const getStrengthLevel = (value) => {
    if (value >= 80) {
      return { label: copy.levels.veryStrong, tone: 'up' }
    }
    if (value >= 60) {
      return { label: copy.levels.strong, tone: 'up' }
    }
    if (value >= 40) {
      return { label: copy.levels.balanced, tone: 'minus' }
    }
    if (value >= 20) {
      return { label: copy.levels.weak, tone: 'down' }
    }
    return { label: copy.levels.veryWeak, tone: 'down' }
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
      <div className="panel-soft flex h-64 items-center justify-center rounded-[28px] text-[#8f7b66]">
        {copy.noData}
      </div>
    )
  }

  return (
    <div className="relative">
      <div className="mb-6 flex items-center justify-between">
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

      <div className="grid grid-cols-1 gap-4 md:grid-cols-5">
        {elements.map((element) => {
          const value = Number(data[element.key]) || 0
          const strength = getStrengthLevel(value)
          const isHovered = hoveredKey === element.key
          const description = safeText(data[`${element.key}_desc`])
          const aiText = safeText(data[`${element.key}_ai`])

          return (
            <div
              key={element.key}
              className="panel-soft relative cursor-pointer rounded-[24px] p-4 transition-all duration-300"
              style={{
                transform: isHovered ? 'scale(1.03)' : 'scale(1)',
                border: isHovered
                  ? `1px solid ${element.color}66`
                  : '1px solid rgba(255,255,255,0.08)',
                boxShadow: isHovered
                  ? `0 18px 36px ${element.color}1c`
                  : 'none',
              }}
              onMouseEnter={() => handleMouseEnter(element.key, value)}
              onMouseLeave={handleMouseLeave}
            >
              <div className="mb-3 flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <div
                    className="flex h-10 w-10 items-center justify-center rounded-2xl border"
                    style={{
                      borderColor: `${element.color}40`,
                      background: `${element.color}14`,
                      color: element.color,
                    }}
                  >
                    <span className="text-lg font-bold">{element.symbol}</span>
                  </div>
                  <span className="font-medium" style={{ color: element.color }}>
                    {element.name}
                  </span>
                </div>
                {strength.tone === 'up' ? (
                  <TrendingUp size={16} className="text-[#dcb86f]" />
                ) : null}
                {strength.tone === 'down' ? (
                  <TrendingDown size={16} className="text-[#e19a84]" />
                ) : null}
                {strength.tone === 'minus' ? (
                  <Minus size={16} className="text-[#bdaa94]" />
                ) : null}
              </div>

              <div className="mb-2">
                <div className="mb-1 flex items-center justify-between">
                  <span className="text-xs text-[#8f7b66]">
                    {strength.label}
                  </span>
                  <span
                    className="text-sm font-bold"
                    style={{ color: element.color }}
                  >
                    {value}%
                  </span>
                </div>
                <div className="h-3 w-full overflow-hidden rounded-full bg-white/[0.06]">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${value}%`,
                      background: `linear-gradient(90deg, ${element.color}aa 0%, ${element.color} 100%)`,
                      boxShadow: `0 0 10px ${element.color}40`,
                    }}
                  />
                </div>
              </div>

              {description ? (
                <div className="mt-2 line-clamp-2 text-xs text-[#8f7b66]">
                  {description}
                </div>
              ) : null}

              {isHovered && showTooltip && aiText ? (
                <div className="absolute left-0 right-0 top-full z-10 mt-2 rounded-[20px] border border-white/10 bg-[#120d0c]/96 p-3 shadow-[0_16px_36px_rgba(0,0,0,0.34)]">
                  <div className="text-xs text-[#e4d6c8]">
                    <div className="mb-1 font-medium text-[#dcb86f]">
                      {copy.aiReading}
                    </div>
                    <div className="leading-6 text-[#bdaa94]">{aiText}</div>
                  </div>
                </div>
              ) : null}
            </div>
          )
        })}
      </div>

      {data.overall ? (
        <div className="panel-soft mt-6 rounded-[24px] p-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-[#8f7b66]">{copy.overallScore}</span>
            <div className="flex items-center space-x-2">
              <span className="text-2xl font-bold text-[#f0d9a5]">
                {data.overall}
              </span>
              <span className="text-sm text-[#8f7b66]">/ 100</span>
            </div>
          </div>
          <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-white/[0.06]">
            <div
              className="h-full rounded-full transition-all duration-500"
              style={{
                width: `${data.overall}%`,
                background:
                  'linear-gradient(90deg, rgba(163,66,36,0.95) 0%, rgba(208,168,91,0.95) 100%)',
              }}
            />
          </div>
        </div>
      ) : null}
    </div>
  )
}
