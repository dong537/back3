import { useMemo, useState } from 'react'
import { Calendar, Info, Minus, TrendingDown, TrendingUp } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale, safeText } from '../utils/displayText'

const DAYUN_COPY = {
  'zh-CN': {
    noData: '暂无大运数据',
    scoreHint: '综合运势评分采用 0-100 分，分数越高表示阶段越顺。',
    excellent: '极佳',
    good: '良好',
    stable: '平稳',
    mixed: '起伏',
    caution: '留意',
    age: '岁',
    majorLuck: '大运',
    stemBranch: '干支',
    naYin: '纳音',
    wealth: '财运',
    career: '事业',
    love: '感情',
    health: '健康',
    rising: '运势上升',
    falling: '运势下降',
    flat: '运势平稳',
    clickHint: '点击柱状卡片可查看更细的说明',
  },
  'en-US': {
    noData: 'No major-luck data available',
    scoreHint:
      'Overall fortune is scored from 0 to 100. Higher scores indicate smoother momentum.',
    excellent: 'Excellent',
    good: 'Good',
    stable: 'Stable',
    mixed: 'Mixed',
    caution: 'Caution',
    age: 'yrs',
    majorLuck: 'Major Luck',
    stemBranch: 'Stem-Branch',
    naYin: 'Na Yin',
    wealth: 'Wealth',
    career: 'Career',
    love: 'Love',
    health: 'Health',
    rising: 'Trending up',
    falling: 'Trending down',
    flat: 'Stable',
    clickHint: 'Click a bar card to inspect more detail',
  },
}

function parseDayunItem(item, index) {
  const wealth = Number(item.wealth)
  const career = Number(item.career)
  const love = Number(item.love)
  const health = Number(item.health)
  const hasAllMetrics = [wealth, career, love, health].every(
    (value) => !Number.isNaN(value)
  )

  let overallScore = 50
  if (hasAllMetrics) {
    overallScore = Math.round((wealth + career + love + health) / 4)
  } else {
    const ganZhi = safeText(item['干支'] || item['大运'])
    const hash = ganZhi
      .split('')
      .reduce((sum, char) => sum + char.charCodeAt(0), 0)
    overallScore = 50 + (hash % 30)
  }

  return {
    ...item,
    index,
    overallScore,
    wealth: Number.isNaN(wealth) ? 0 : wealth,
    career: Number.isNaN(career) ? 0 : career,
    love: Number.isNaN(love) ? 0 : love,
    health: Number.isNaN(health) ? 0 : health,
    startAge: item['起始年龄'] || index * 10 + 1,
    endAge: item['结束年龄'] || (index + 1) * 10,
    ganZhi: safeText(item['干支'] || item['大运']),
    naYin: safeText(item['纳音']),
    yearRange: safeText(item['年份范围'] || item['开始']),
    desc: safeText(item.desc || item.description),
  }
}

export default function DayunTrendChart({ data, onNodeClick }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = DAYUN_COPY[locale]
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })

  const chartData = useMemo(() => {
    if (!Array.isArray(data) || data.length === 0) return []
    return data.map(parseDayunItem)
  }, [data])

  const getTrendIcon = (current, previous) => {
    if (previous === undefined) {
      return <Minus className="h-4 w-4 text-[#8f7b66]" />
    }
    if (current > previous) {
      return <TrendingUp className="h-4 w-4 text-[#dcb86f]" />
    }
    if (current < previous) {
      return <TrendingDown className="h-4 w-4 text-[#e19a84]" />
    }
    return <Minus className="h-4 w-4 text-[#8f7b66]" />
  }

  const getFortuneLevel = (score) => {
    if (score >= 80) {
      return {
        label: copy.excellent,
        color: 'text-[#f0d9a5]',
        bg: 'bg-[#6a4a1e]/18',
        border: 'border-[#d0a85b]/26',
      }
    }
    if (score >= 65) {
      return {
        label: copy.good,
        color: 'text-[#dcb86f]',
        bg: 'bg-[#8f5c1f]/16',
        border: 'border-[#b88a3d]/26',
      }
    }
    if (score >= 50) {
      return {
        label: copy.stable,
        color: 'text-[#bdaa94]',
        bg: 'bg-white/[0.04]',
        border: 'border-white/10',
      }
    }
    if (score >= 35) {
      return {
        label: copy.mixed,
        color: 'text-[#e19a84]',
        bg: 'bg-[#7a3218]/14',
        border: 'border-[#a34224]/24',
      }
    }
    return {
      label: copy.caution,
      color: 'text-[#e19a84]',
      bg: 'bg-[#5a2318]/16',
      border: 'border-[#9a4e34]/26',
    }
  }

  if (chartData.length === 0) {
    return (
      <div className="panel-soft flex h-64 items-center justify-center rounded-[28px] text-[#8f7b66]">
        <div className="text-center">
          <Calendar className="mx-auto mb-2 h-12 w-12 opacity-50" />
          <p>{copy.noData}</p>
        </div>
      </div>
    )
  }

  const maxScore = Math.max(...chartData.map((item) => item.overallScore), 100)
  const minScore = Math.min(...chartData.map((item) => item.overallScore), 0)
  const scoreRange = maxScore - minScore || 1

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center space-x-2 text-sm text-[#8f7b66]">
          <Info size={16} />
          <span>{copy.scoreHint}</span>
        </div>
        <div className="flex flex-wrap items-center gap-4 text-xs text-[#8f7b66]">
          <LegendDot color="bg-[#d0a85b]/60" label={`${copy.excellent} (80+)`} />
          <LegendDot color="bg-[#b88a3d]/60" label={`${copy.good} (65-79)`} />
          <LegendDot color="bg-white/20" label={`${copy.stable} (50-64)`} />
        </div>
      </div>

      <div className="relative">
        <div className="absolute inset-0 flex flex-col justify-between py-8">
          {[0, 25, 50, 75, 100].map((line) => (
            <div key={line} className="border-t border-white/[0.05]">
              <span className="absolute left-0 -mt-2 text-xs text-[#6f6257]">
                {line}
              </span>
            </div>
          ))}
        </div>

        <div className="relative flex h-64 items-end justify-between px-4 pb-8">
          {chartData.map((item, index) => {
            const previousScore =
              index > 0 ? chartData[index - 1].overallScore : undefined
            const height = ((item.overallScore - minScore) / scoreRange) * 100
            const level = getFortuneLevel(item.overallScore)
            const isHovered = hoveredIndex === index

            return (
              <div
                key={`${item.ganZhi || index}-${index}`}
                className="group relative flex flex-1 cursor-pointer flex-col items-center justify-end"
                onMouseEnter={(event) => {
                  setHoveredIndex(index)
                  const rect = event.currentTarget.getBoundingClientRect()
                  setMousePosition({
                    x: event.clientX - rect.left,
                    y: event.clientY - rect.top,
                  })
                }}
                onMouseMove={(event) => {
                  if (hoveredIndex !== index) return
                  const rect = event.currentTarget.getBoundingClientRect()
                  setMousePosition({
                    x: event.clientX - rect.left,
                    y: event.clientY - rect.top,
                  })
                }}
                onMouseLeave={() => setHoveredIndex(null)}
                onClick={() => onNodeClick?.(item)}
              >
                {index < chartData.length - 1 ? (
                  <div
                    className="absolute left-1/2 top-1/2 z-0 h-0.5 w-full opacity-40"
                    style={{
                      background:
                        'linear-gradient(to right, rgba(163,66,36,0.38), rgba(208,168,91,0.42))',
                      transform: 'translateY(-50%)',
                    }}
                  />
                ) : null}

                <div className="relative z-10 flex w-full flex-col items-center">
                  <div
                    className={`w-12 rounded-t-[18px] border transition-all duration-300 ${
                      isHovered ? 'w-16 shadow-[0_16px_32px_rgba(0,0,0,0.22)]' : ''
                    } ${level.bg} ${level.border}`}
                    style={{
                      height: `${Math.max(height, 5)}%`,
                      minHeight: '20px',
                    }}
                  />

                  <div
                    className={`mt-2 w-full text-center transition-all duration-300 ${
                      isHovered ? 'scale-105' : ''
                    }`}
                  >
                    <div className="mb-1 text-xs font-medium text-[#f4ece1]">
                      {item.startAge}-{item.endAge}
                      {copy.age}
                    </div>
                    <div className="mb-1 text-sm font-bold text-[#f4ece1]">
                      {item.ganZhi || '-'}
                    </div>
                    {item.naYin ? (
                      <div className="mb-1 text-xs text-[#8f7b66]">
                        {item.naYin}
                      </div>
                    ) : null}
                    {item.yearRange ? (
                      <div className="text-xs text-[#6f6257]">{item.yearRange}</div>
                    ) : null}
                    {previousScore !== undefined ? (
                      <div className="mt-1 flex justify-center">
                        {getTrendIcon(item.overallScore, previousScore)}
                      </div>
                    ) : null}
                    <div
                      className={`mt-1 inline-block rounded-full border px-2 py-0.5 text-xs ${level.bg} ${level.color} ${level.border}`}
                    >
                      {level.label}
                    </div>
                  </div>
                </div>

                {isHovered ? (
                  <div
                    className="pointer-events-none absolute z-50"
                    style={{
                      left: `${mousePosition.x}px`,
                      top: `${mousePosition.y - 10}px`,
                      transform: 'translate(-50%, -100%)',
                    }}
                  >
                    <div className="glass min-w-[240px] rounded-[22px] border border-white/10 p-3 shadow-xl">
                      <div className="mb-2 text-center text-sm font-bold text-[#f4ece1]">
                        {item.startAge}-{item.endAge}
                        {copy.age} {copy.majorLuck}
                      </div>

                      <div className="space-y-1 text-xs">
                        <TooltipRow
                          label={copy.stemBranch}
                          value={item.ganZhi}
                          valueClassName="text-[#f4ece1]"
                        />
                        {item.naYin ? (
                          <TooltipRow
                            label={copy.naYin}
                            value={item.naYin}
                            valueClassName="text-[#e4d6c8]"
                          />
                        ) : null}
                        {(item.wealth || item.career || item.love || item.health) ? (
                          <>
                            <div className="my-1 border-t border-white/10" />
                            <TooltipRow
                              label={copy.wealth}
                              value={`${item.wealth || 0}%`}
                              valueClassName="text-[#f0d9a5]"
                            />
                            <TooltipRow
                              label={copy.career}
                              value={`${item.career || 0}%`}
                              valueClassName="text-[#dcb86f]"
                            />
                            <TooltipRow
                              label={copy.love}
                              value={`${item.love || 0}%`}
                              valueClassName="text-[#e19a84]"
                            />
                            <TooltipRow
                              label={copy.health}
                              value={`${item.health || 0}%`}
                              valueClassName="text-[#bdaa94]"
                            />
                          </>
                        ) : null}
                        {item.desc ? (
                          <>
                            <div className="my-1 border-t border-white/10" />
                            <div className="text-xs leading-6 text-[#bdaa94]">
                              {item.desc}
                            </div>
                          </>
                        ) : null}
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            )
          })}
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-center gap-4 border-t border-white/10 pt-4 text-xs text-[#8f7b66]">
        <div className="flex items-center space-x-1">
          <TrendingUp className="h-3 w-3 text-[#dcb86f]" />
          <span>{copy.rising}</span>
        </div>
        <div className="flex items-center space-x-1">
          <TrendingDown className="h-3 w-3 text-[#e19a84]" />
          <span>{copy.falling}</span>
        </div>
        <div className="flex items-center space-x-1">
          <Minus className="h-3 w-3 text-[#8f7b66]" />
          <span>{copy.flat}</span>
        </div>
        <div className="ml-4 text-[#6f6257]">{copy.clickHint}</div>
      </div>
    </div>
  )
}

function LegendDot({ color, label }) {
  return (
    <div className="flex items-center space-x-1">
      <div className={`h-3 w-3 rounded ${color}`} />
      <span>{label}</span>
    </div>
  )
}

function TooltipRow({ label, value, valueClassName }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-[#8f7b66]">{label}</span>
      <span className={valueClassName}>{value}</span>
    </div>
  )
}
