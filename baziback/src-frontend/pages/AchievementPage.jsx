import { useState, useEffect } from 'react'
import { Trophy, Award, Star, Lock, Unlock, TrendingUp } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardHeader, CardTitle, CardContent } from '../components/Card'
import { achievementApi } from '../api'
import { logger } from '../utils/logger'
import achievementCache from '../utils/achievementCache'

export default function AchievementPage() {
  const { t } = useTranslation()
  const [allAchievements, setAllAchievements] = useState([])
  const [userAchievements, setUserAchievements] = useState([])
  const [stats, setStats] = useState({ unlocked: 0, total: 0, progress: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
    
    // 监听全局成就解锁事件（由App.jsx中的useSSE触发）
    const handleAchievementUnlocked = (event) => {
      logger.info('收到成就解锁事件，刷新数据:', event.detail)
      // 更新缓存
      if (event.detail) {
        achievementCache.updateUserAchievements(event.detail)
      }
      // 强制刷新数据
      loadData(true)
    }
    
    window.addEventListener('achievement-unlocked', handleAchievementUnlocked)
    
    return () => {
      window.removeEventListener('achievement-unlocked', handleAchievementUnlocked)
    }
  }, [])

  const loadData = async (forceRefresh = false) => {
    try {
      setLoading(true)
      
      // 检查缓存（如果不强制刷新）
      if (!forceRefresh) {
        const cachedAll = achievementCache.getAllAchievements()
        const cachedUser = achievementCache.getUserAchievements()
        const cachedStats = achievementCache.getStats()
        
        if (cachedAll && cachedUser && cachedStats) {
          logger.info('使用缓存的成就数据')
          setAllAchievements(cachedAll)
          setUserAchievements(cachedUser)
          setStats(cachedStats)
          setLoading(false)
          return
        }
      }
      
      // 从API加载数据
      const [allRes, userRes, statsRes] = await Promise.all([
        achievementApi.getAll().catch(e => ({ data: { code: 200, data: [] } })),
        achievementApi.getUserAchievements().catch(e => ({ data: { code: 401, data: [] } })),
        achievementApi.getStats().catch(e => ({ data: { code: 401, data: { unlocked: 0, total: 0, progress: 0 } } }))
      ])
      
      // 处理多种响应格式
      const handleResponse = (res, defaultValue = []) => {
        if (res.data?.code === 200 || res.data?.code === 0) {
          return res.data?.data || defaultValue
        }
        return defaultValue
      }
      
      const allAchievements = handleResponse(allRes, [])
      const userAchievements = handleResponse(userRes, [])
      const stats = handleResponse(statsRes, { unlocked: 0, total: 0, progress: 0 })
      
      // 更新缓存
      achievementCache.setCache(allAchievements, userAchievements, stats)
      
      setAllAchievements(allAchievements)
      setUserAchievements(userAchievements)
      setStats(stats)
    } catch (error) {
      logger.error('加载数据失败:', error)
      // 设置默认值，避免页面崩溃
      setAllAchievements([])
      setUserAchievements([])
      setStats({ unlocked: 0, total: 0, progress: 0 })
    } finally {
      setLoading(false)
    }
  }


  const isUnlocked = (achievementCode) => {
    return userAchievements.some(ua => ua.achievementCode === achievementCode)
  }

  const getUserAchievement = (achievementCode) => {
    return userAchievements.find(ua => ua.achievementCode === achievementCode)
  }

  const getAchievementIcon = (type) => {
    switch (type) {
      case 'divination':
        return '🔮'
      case 'favorite':
        return '⭐'
      case 'invite':
        return '👥'
      case 'checkin':
        return '📅'
      case 'points':
        return '💎'
      default:
        return '🏆'
    }
  }

  const getAchievementTypeName = (type) => {
    switch (type) {
      case 'divination':
        return '占卜'
      case 'favorite':
        return '收藏'
      case 'invite':
        return '邀请'
      case 'checkin':
        return '签到'
      case 'points':
        return '积分'
      default:
        return '其他'
    }
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return '未解锁'
    try {
      return new Date(dateStr).toLocaleDateString('zh-CN')
    } catch (e) {
      return '日期错误'
    }
  }

  if (loading) {
    return (
      <div className="page-shell">
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="text-6xl mb-4 animate-bounce">🏆</div>
            <div className="text-gray-400 text-lg animate-pulse">正在加载...</div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* 页面标题 */}
        <div className="text-center mb-8">
          <div className="relative inline-block mb-4">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gradient-to-br from-purple-600 to-pink-600 shadow-lg shadow-purple-500/50">
              <Trophy className="w-10 h-10 text-white" />
            </div>
            <div className="absolute inset-0 w-20 h-20 rounded-full bg-gradient-to-br from-purple-600 to-pink-600 opacity-30 blur-xl animate-pulse"></div>
          </div>
          <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-purple-200 via-pink-200 to-rose-200 bg-clip-text text-transparent">
            成就系统
          </h1>
          <p className="text-gray-300">解锁成就，获得丰厚奖励</p>
        </div>

        {/* 成就统计 */}
        <Card className="mb-8 bg-gradient-to-br from-purple-900/30 via-pink-900/20 to-rose-900/30 border-purple-400/30 shadow-2xl">
          <CardContent className="p-8">
            <div className="grid grid-cols-3 gap-6">
              <div className="text-center">
                <div className="text-4xl font-bold text-purple-300 mb-2">{stats.unlocked}</div>
                <div className="text-sm text-gray-400">已解锁</div>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-pink-300 mb-2">{stats.total}</div>
                <div className="text-sm text-gray-400">总成就</div>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-rose-300 mb-2">{stats.progress}%</div>
                <div className="text-sm text-gray-400">完成度</div>
              </div>
            </div>
            
            {/* 进度条 */}
            <div className="mt-6">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-gray-400">成就进度</span>
                <span className="text-sm text-gray-400">{stats.unlocked} / {stats.total}</span>
              </div>
              <div className="w-full bg-gray-700 rounded-full h-3 overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-purple-500 via-pink-500 to-rose-500 transition-all duration-500"
                  style={{ width: `${stats.progress}%` }}
                ></div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 成就列表 */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-6 flex items-center space-x-2">
            <Award className="text-purple-400" />
            <span>所有成就</span>
          </h2>
          
          {/* 按类型分组显示 */}
          {['divination', 'favorite', 'invite', 'checkin', 'points'].map((type) => {
            const typeAchievements = allAchievements.filter(a => a.achievementType === type)
            if (typeAchievements.length === 0) return null
            
            return (
              <div key={type} className="mb-8">
                <h3 className="text-lg font-semibold mb-4 text-gray-300 flex items-center space-x-2">
                  <span className="text-2xl">{getAchievementIcon(type)}</span>
                  <span>{getAchievementTypeName(type)}成就</span>
                </h3>
                
                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {typeAchievements.map((achievement) => {
                    const unlocked = isUnlocked(achievement.achievementCode)
                    const userAchievement = getUserAchievement(achievement.achievementCode)
                    
                    return (
                      <Card
                        key={achievement.id}
                        className={`transition-all hover:scale-105 ${
                          unlocked
                            ? 'bg-gradient-to-br from-purple-900/30 via-pink-900/20 to-rose-900/30 border-purple-400/40 shadow-lg shadow-purple-500/20'
                            : 'bg-gradient-to-br from-gray-900/30 via-gray-800/20 to-gray-900/30 border-gray-600/20 opacity-60'
                        }`}
                      >
                        <CardContent className="p-5">
                          <div className="flex items-start space-x-4">
                            <div className={`text-4xl ${unlocked ? '' : 'grayscale opacity-50'}`}>
                              {achievement.iconUrl ? (
                                <img src={achievement.iconUrl} alt={achievement.achievementName} className="w-12 h-12" />
                              ) : (
                                <span>{getAchievementIcon(achievement.achievementType)}</span>
                              )}
                            </div>
                            <div className="flex-1">
                              <div className="flex items-center justify-between mb-2">
                                <h4 className={`font-bold ${unlocked ? 'text-white' : 'text-gray-500'}`}>
                                  {achievement.achievementName}
                                </h4>
                                {unlocked ? (
                                  <Unlock className="text-green-400" size={20} />
                                ) : (
                                  <Lock className="text-gray-500" size={20} />
                                )}
                              </div>
                              <p className="text-sm text-gray-400 mb-3">{achievement.achievementDescription}</p>
                              <div className="flex items-center justify-between">
                                <span className={`text-xs px-2 py-1 rounded ${
                                  unlocked
                                    ? 'bg-purple-500/20 text-purple-300'
                                    : 'bg-gray-700/50 text-gray-500'
                                }`}>
                                  {achievement.pointsReward} 积分
                                </span>
                                {userAchievement?.unlockedTime && (
                                  <span className="text-xs text-gray-500">
                                    {formatDate(userAchievement.unlockedTime)}
                                  </span>
                                )}
                              </div>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    )
                  })}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
