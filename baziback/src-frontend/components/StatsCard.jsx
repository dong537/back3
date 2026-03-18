import { TrendingUp, TrendingDown, Minus } from 'lucide-react'
import Card from './Card'

/**
 * 统计数据卡片组件
 */
export default function StatsCard({ title, value, trend, subtitle, icon: Icon, onClick, className = '' }) {
  const getTrendIcon = () => {
    if (trend > 0) return <TrendingUp size={16} className="text-green-400" />
    if (trend < 0) return <TrendingDown size={16} className="text-red-400" />
    return <Minus size={16} className="text-gray-400" />
  }

  const getTrendColor = () => {
    if (trend > 0) return 'text-green-400'
    if (trend < 0) return 'text-red-400'
    return 'text-gray-400'
  }

  return (
    <Card className={`p-6 ${onClick ? 'cursor-pointer hover:scale-105 transition' : ''} ${className}`} onClick={onClick}>
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-2">
            {Icon && <Icon size={20} className="text-skin-primary" />}
            <h3 className="text-sm font-medium text-gray-400">{title}</h3>
          </div>
          <div className="text-3xl font-bold mb-1">{value}</div>
          {subtitle && (
            <p className="text-sm text-gray-500">{subtitle}</p>
          )}
        </div>
        {trend !== undefined && (
          <div className={`flex items-center space-x-1 ${getTrendColor()}`}>
            {getTrendIcon()}
            <span className="text-sm font-medium">
              {trend > 0 ? '+' : ''}{trend}%
            </span>
          </div>
        )}
      </div>
    </Card>
  )
}
