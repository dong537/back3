/**
 * 骨架屏加载组件
 */
export default function SkeletonLoader({ type = 'default', count = 1 }) {
  const skeletons = Array(count).fill(0)

  if (type === 'card') {
    return (
      <div className="space-y-4">
        {skeletons.map((_, i) => (
          <div key={i} className="glass rounded-xl p-6 animate-pulse">
            <div className="h-4 bg-white/10 rounded w-3/4 mb-4"></div>
            <div className="h-4 bg-white/10 rounded w-1/2 mb-2"></div>
            <div className="h-4 bg-white/10 rounded w-5/6"></div>
          </div>
        ))}
      </div>
    )
  }

  if (type === 'table') {
    return (
      <div className="space-y-3">
        {skeletons.map((_, i) => (
          <div key={i} className="flex space-x-4 animate-pulse">
            <div className="h-4 bg-white/10 rounded flex-1"></div>
            <div className="h-4 bg-white/10 rounded flex-1"></div>
            <div className="h-4 bg-white/10 rounded flex-1"></div>
            <div className="h-4 bg-white/10 rounded flex-1"></div>
          </div>
        ))}
      </div>
    )
  }

  if (type === 'hexagram') {
    return (
      <div className="glass rounded-xl p-6 animate-pulse">
        <div className="flex items-center space-x-4 mb-6">
          <div className="w-16 h-16 bg-white/10 rounded-xl"></div>
          <div className="flex-1">
            <div className="h-6 bg-white/10 rounded w-24 mb-2"></div>
            <div className="h-4 bg-white/10 rounded w-32"></div>
          </div>
        </div>
        <div className="space-y-2">
          <div className="h-4 bg-white/10 rounded w-full"></div>
          <div className="h-4 bg-white/10 rounded w-5/6"></div>
          <div className="h-4 bg-white/10 rounded w-4/6"></div>
        </div>
      </div>
    )
  }

  // 默认骨架屏
  return (
    <div className="space-y-3">
      {skeletons.map((_, i) => (
        <div key={i} className="h-4 bg-white/10 rounded animate-pulse" style={{ width: `${80 + (i * 10)}%` }}></div>
      ))}
    </div>
  )
}
