import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowRight, Wallet, Users, Shield, Heart, Briefcase, DollarSign, Activity, BookOpen, Star } from 'lucide-react'
import Card from './Card'
import { baziApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from './Toast'

/**
 * 八字解释列表组件
 * 显示日支、年干、年支等十神信息的解释
 */
export default function BaziInterpretationList({ baziData }) {
  const navigate = useNavigate()
  const [interpretations, setInterpretations] = useState([])
  const [loading, setLoading] = useState(false)

  // 从后端API获取解释数据
  useEffect(() => {
    if (!baziData) {
      setInterpretations([])
      return
    }

    const loadInterpretations = async () => {
      try {
        setLoading(true)
        const response = await baziApi.getInterpretationsFromBaziData(baziData)
        
        if (response.data?.success && response.data.data) {
          const interpretationData = response.data.data
          
          // 转换为前端需要的格式
          const items = interpretationData.map((item, index) => {
            // 根据位置和十神类型选择图标和颜色
            const iconConfig = getIconAndColor(item.type, item.shiShen)
            
            return {
              id: item.id,
              type: item.type,
              shiShen: item.shiShen,
              title: item.title,
              icon: iconConfig.icon,
              color: iconConfig.color,
              borderColor: iconConfig.borderColor,
              textColor: iconConfig.textColor,
              shortDesc: item.basicDef || `${item.type}出现${item.shiShen}，对命主有一定影响。`,
              fullDesc: {
                basic: item.basicDef,
                mainContent: item.mainContent,
                supportContent: item.supportContent,
                restrictContent: item.restrictContent,
                genderDiff: item.genderDiff,
                tag: item.tag
              }
            }
          })
          
          setInterpretations(items)
        } else {
          logger.warn('获取八字解释失败:', response.data)
        }
      } catch (error) {
        logger.error('加载八字解释失败:', error)
        toast.error('加载八字解释失败')
      } finally {
        setLoading(false)
      }
    }

    loadInterpretations()
  }, [baziData])

  const handleItemClick = (item) => {
    // 导航到详情页面
    navigate(`/bazi/interpretation/${item.id}`, {
      state: { interpretation: item, baziData }
    })
  }

  // 根据位置和十神类型获取图标和颜色配置
  function getIconAndColor(position, shiShen) {
    const configs = {
      '日支': { icon: Wallet, color: 'from-amber-500/20 to-orange-500/20', borderColor: 'border-amber-500/30', textColor: 'text-amber-300' },
      '年干': { icon: Users, color: 'from-green-500/20 to-emerald-500/20', borderColor: 'border-green-500/30', textColor: 'text-green-300' },
      '年支': { icon: Shield, color: 'from-blue-500/20 to-cyan-500/20', borderColor: 'border-blue-500/30', textColor: 'text-blue-300' },
      '月干': { icon: Briefcase, color: 'from-purple-500/20 to-pink-500/20', borderColor: 'border-purple-500/30', textColor: 'text-purple-300' },
      '月支': { icon: Activity, color: 'from-red-500/20 to-pink-500/20', borderColor: 'border-red-500/30', textColor: 'text-red-300' },
      '时干': { icon: Star, color: 'from-indigo-500/20 to-violet-500/20', borderColor: 'border-indigo-500/30', textColor: 'text-indigo-300' },
      '时支': { icon: Heart, color: 'from-rose-500/20 to-pink-500/20', borderColor: 'border-rose-500/30', textColor: 'text-rose-300' },
    }
    
    return configs[position] || { icon: BookOpen, color: 'from-gray-500/20 to-gray-600/20', borderColor: 'border-gray-500/30', textColor: 'text-gray-300' }
  }

  if (!baziData) {
    return null
  }

  if (loading) {
    return (
      <Card className="panel mb-6 bg-gradient-to-br from-amber-900/20 via-orange-900/20 to-red-900/20 border-amber-500/30">
        <div className="p-6">
          <div className="text-center text-gray-400">加载中...</div>
        </div>
      </Card>
    )
  }

  if (interpretations.length === 0) {
    return null
  }

  return (
    <Card className="panel mb-6 bg-gradient-to-br from-amber-900/20 via-orange-900/20 to-red-900/20 border-amber-500/30">
      <div className="p-6">
        <h3 className="text-xl font-bold text-white mb-4 flex items-center space-x-2">
          <BookOpen className="w-5 h-5 text-amber-400" />
          <span>八字信息解读</span>
        </h3>
        <p className="text-sm text-gray-400 mb-6">点击查看各项十神的详细解读</p>

        <div className="space-y-4">
          {interpretations.map((item) => {
            const Icon = item.icon
            return (
              <div
                key={item.id}
                onClick={() => handleItemClick(item)}
                className={`group relative rounded-xl p-4 border-2 ${item.borderColor} bg-gradient-to-r ${item.color} cursor-pointer transition-all duration-300 hover:scale-[1.02] hover:shadow-lg hover:shadow-${item.textColor}/20 tap-highlight active:scale-[0.98]`}
              >
                <div className="flex items-start space-x-4">
                  {/* 图标 */}
                  <div className={`flex-shrink-0 w-12 h-12 rounded-lg bg-gradient-to-br ${item.color} ${item.borderColor} border flex items-center justify-center`}>
                    <Icon className={`w-6 h-6 ${item.textColor}`} />
                  </div>

                  {/* 内容 */}
                  <div className="flex-1 min-w-0">
                    <h4 className={`text-lg font-bold mb-2 ${item.textColor}`}>
                      {item.title}
                    </h4>
                    <p className="text-gray-300 text-sm leading-relaxed line-clamp-2">
                      {item.shortDesc}
                    </p>
                    <div className="mt-3 flex items-center space-x-2 text-xs text-gray-400 group-hover:text-gray-300 transition-colors">
                      <span>查看全文</span>
                      <ArrowRight className="w-4 h-4" />
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </Card>
  )
}

