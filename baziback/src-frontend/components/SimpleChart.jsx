/**
 * 简单的图表组件（不依赖外部库）
 * 用于展示运势趋势等数据
 */
export default function SimpleChart({ data, title, height = 200 }) {
  if (!data || data.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-gray-400">
        暂无数据
      </div>
    )
  }

  const maxValue = Math.max(...data.map(d => d.value || 0))
  const minValue = Math.min(...data.map(d => d.value || 0))
  const range = maxValue - minValue || 1

  return (
    <div className="w-full">
      {title && (
        <h3 className="text-sm font-medium text-gray-400 mb-4">{title}</h3>
      )}
      <div className="relative" style={{ height: `${height}px` }}>
        {/* Y轴标签 */}
        <div className="absolute left-0 top-0 bottom-0 flex flex-col justify-between text-xs text-gray-500 pr-2">
          <span>{Math.round(maxValue)}</span>
          <span>{Math.round((maxValue + minValue) / 2)}</span>
          <span>{Math.round(minValue)}</span>
        </div>

        {/* 图表区域 */}
        <div className="ml-12 h-full relative">
          {/* 网格线 */}
          <div className="absolute inset-0 flex flex-col justify-between">
            {[0, 0.5, 1].map((pos) => (
              <div
                key={pos}
                className="border-t border-white/5"
                style={{ marginTop: pos === 0 ? 0 : pos === 1 ? 'auto' : '50%' }}
              />
            ))}
          </div>

          {/* 数据点连线 */}
          <svg className="absolute inset-0 w-full h-full" style={{ overflow: 'visible' }} viewBox="0 0 100 100" preserveAspectRatio="none">
            <polyline
              points={data.map((d, i) => {
                const x = (i / (data.length - 1 || 1)) * 100
                const y = 100 - ((d.value - minValue) / range) * 100
                return `${x},${y}`
              }).join(' ')}
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              className="text-skin-primary"
            />
            
            {/* 数据点 */}
            {data.map((d, i) => {
              const x = (i / (data.length - 1 || 1)) * 100
              const y = 100 - ((d.value - minValue) / range) * 100
              return (
                <circle
                  key={i}
                  cx={x}
                  cy={y}
                  r="4"
                  fill="currentColor"
                  className="text-skin-primary"
                />
              )
            })}
          </svg>

          {/* X轴标签 */}
          <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-gray-500">
            {data.map((d, i) => (
              <span key={i} className="transform -rotate-45 origin-top-left" style={{ transform: 'rotate(-45deg)' }}>
                {d.label || i + 1}
              </span>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
