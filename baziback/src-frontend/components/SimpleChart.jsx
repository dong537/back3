import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

const SIMPLE_CHART_COPY = {
  'zh-CN': {
    noData: '暂无数据',
  },
  'en-US': {
    noData: 'No data available',
  },
}

export default function SimpleChart({ data, title, height = 200 }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SIMPLE_CHART_COPY[locale]

  if (!Array.isArray(data) || data.length === 0) {
    return (
      <div className="flex h-48 items-center justify-center text-gray-400">
        {copy.noData}
      </div>
    )
  }

  const maxValue = Math.max(...data.map((item) => item.value || 0))
  const minValue = Math.min(...data.map((item) => item.value || 0))
  const range = maxValue - minValue || 1

  return (
    <div className="w-full">
      {title ? (
        <h3 className="mb-4 text-sm font-medium text-gray-400">{title}</h3>
      ) : null}
      <div className="relative" style={{ height: `${height}px` }}>
        <div className="absolute bottom-0 left-0 top-0 flex flex-col justify-between pr-2 text-xs text-gray-500">
          <span>{Math.round(maxValue)}</span>
          <span>{Math.round((maxValue + minValue) / 2)}</span>
          <span>{Math.round(minValue)}</span>
        </div>

        <div className="relative ml-12 h-full">
          <div className="absolute inset-0 flex flex-col justify-between">
            {[0, 0.5, 1].map((position) => (
              <div
                key={position}
                className="border-t border-white/5"
                style={{
                  marginTop:
                    position === 0 ? 0 : position === 1 ? 'auto' : '50%',
                }}
              />
            ))}
          </div>

          <svg
            className="absolute inset-0 h-full w-full"
            style={{ overflow: 'visible' }}
            viewBox="0 0 100 100"
            preserveAspectRatio="none"
          >
            <polyline
              points={data
                .map((item, index) => {
                  const x = (index / (data.length - 1 || 1)) * 100
                  const y = 100 - (((item.value || 0) - minValue) / range) * 100
                  return `${x},${y}`
                })
                .join(' ')}
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              className="text-skin-primary"
            />
            {data.map((item, index) => {
              const x = (index / (data.length - 1 || 1)) * 100
              const y = 100 - (((item.value || 0) - minValue) / range) * 100
              return (
                <circle
                  key={`${item.label || index}-${index}`}
                  cx={x}
                  cy={y}
                  r="4"
                  fill="currentColor"
                  className="text-skin-primary"
                />
              )
            })}
          </svg>

          <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-gray-500">
            {data.map((item, index) => (
              <span
                key={`${item.label || index}-label-${index}`}
                className="origin-top-left"
                style={{ transform: 'rotate(-45deg)' }}
              >
                {item.label || index + 1}
              </span>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
