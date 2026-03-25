import { useState, useEffect } from 'react'
import { Trophy, Award, Lock, Unlock } from 'lucide-react'
import Card, { CardContent } from '../components/Card'
import { achievementApi } from '../api'
import { logger } from '../utils/logger'
import achievementCache from '../utils/achievementCache'
import useAppLocale from '../hooks/useAppLocale'
import { useAuth } from '../context/AuthContext'

const ACHIEVEMENT_COPY = {
  'zh-CN': {
    loading: '正在加载...',
    title: '成就系统',
    subtitle: '解锁成就，获得丰厚奖励',
    unlocked: '已解锁',
    total: '总成就',
    progress: '完成度',
    progressLabel: '成就进度',
    allAchievements: '所有成就',
    locked: '未解锁',
    dateError: '日期异常',
    points: '积分',
    types: {
      divination: '占卜',
      favorite: '收藏',
      invite: '邀请',
      checkin: '签到',
      points: '积分',
      other: '其他',
    },
  },
  'en-US': {
    loading: 'Loading...',
    title: 'Achievements',
    subtitle: 'Unlock achievements and earn generous rewards',
    unlocked: 'Unlocked',
    total: 'Total',
    progress: 'Progress',
    progressLabel: 'Achievement Progress',
    allAchievements: 'All Achievements',
    locked: 'Locked',
    dateError: 'Invalid date',
    points: 'credits',
    types: {
      divination: 'Divination',
      favorite: 'Favorites',
      invite: 'Invite',
      checkin: 'Check-in',
      points: 'Credits',
      other: 'Other',
    },
  },
}

export default function AchievementPage() {
  const { user } = useAuth()
  const { locale } = useAppLocale()
  const copy = ACHIEVEMENT_COPY[locale]
  const userId = user?.id ?? null
  const [allAchievements, setAllAchievements] = useState([])
  const [userAchievements, setUserAchievements] = useState([])
  const [stats, setStats] = useState({ unlocked: 0, total: 0, progress: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!userId) {
      achievementCache.clear()
      setAllAchievements([])
      setUserAchievements([])
      setStats({ unlocked: 0, total: 0, progress: 0 })
      setLoading(false)
      return
    }

    loadData()

    const handleAchievementUnlocked = (event) => {
      logger.info(
        'Received achievement-unlocked event, refreshing data',
        event.detail
      )
      if (event.detail) {
        achievementCache.updateUserAchievements(userId, event.detail)
      }
      loadData(true)
    }

    window.addEventListener('achievement-unlocked', handleAchievementUnlocked)

    return () => {
      window.removeEventListener(
        'achievement-unlocked',
        handleAchievementUnlocked
      )
    }
  }, [userId])

  const loadData = async (forceRefresh = false) => {
    try {
      setLoading(true)

      if (!forceRefresh) {
        const cachedAll = achievementCache.getAllAchievements(userId)
        const cachedUser = achievementCache.getUserAchievements(userId)
        const cachedStats = achievementCache.getStats(userId)

        if (cachedAll && cachedUser && cachedStats) {
          logger.info('Using cached achievement data')
          setAllAchievements(cachedAll)
          setUserAchievements(cachedUser)
          setStats(cachedStats)
          setLoading(false)
          return
        }
      }

      const [allRes, userRes, statsRes] = await Promise.all([
        achievementApi
          .getAll()
          .catch(() => ({ data: { code: 200, data: [] } })),
        achievementApi
          .getUserAchievements()
          .catch(() => ({ data: { code: 401, data: [] } })),
        achievementApi.getStats().catch(() => ({
          data: { code: 401, data: { unlocked: 0, total: 0, progress: 0 } },
        })),
      ])

      const handleResponse = (res, defaultValue = []) => {
        if (res.data?.code === 200 || res.data?.code === 0) {
          return res.data?.data || defaultValue
        }
        return defaultValue
      }

      const nextAllAchievements = handleResponse(allRes, [])
      const nextUserAchievements = handleResponse(userRes, [])
      const nextStats = handleResponse(statsRes, {
        unlocked: 0,
        total: 0,
        progress: 0,
      })

      achievementCache.setCache(
        userId,
        nextAllAchievements,
        nextUserAchievements,
        nextStats
      )

      setAllAchievements(nextAllAchievements)
      setUserAchievements(nextUserAchievements)
      setStats(nextStats)
    } catch (error) {
      logger.error('Failed to load achievement data:', error)
      setAllAchievements([])
      setUserAchievements([])
      setStats({ unlocked: 0, total: 0, progress: 0 })
    } finally {
      setLoading(false)
    }
  }

  const isUnlocked = (achievementCode) => {
    return userAchievements.some((ua) => ua.achievementCode === achievementCode)
  }

  const getUserAchievement = (achievementCode) => {
    return userAchievements.find((ua) => ua.achievementCode === achievementCode)
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
        return '🪙'
      default:
        return '🏆'
    }
  }

  const getAchievementTypeName = (type) => {
    return copy.types[type] || copy.types.other
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return copy.locked
    try {
      return new Date(dateStr).toLocaleDateString(locale)
    } catch (error) {
      return copy.dateError
    }
  }

  if (loading) {
    return (
      <div className="page-shell" data-theme="default">
        <div className="flex min-h-screen items-center justify-center">
          <div className="text-center">
            <div className="mb-4 animate-bounce text-6xl">🏆</div>
            <div className="animate-pulse text-lg text-[#bdaa94]">
              {copy.loading}
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Trophy className="text-theme h-4 w-4" />
            <span>{copy.progressLabel}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">{copy.title}</h1>
          <p className="page-subtitle">{copy.subtitle}</p>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 pb-8">

        <Card className="panel mb-8">
          <CardContent className="p-8">
            <div className="grid grid-cols-3 gap-6">
              <Metric
                value={stats.unlocked}
                label={copy.unlocked}
                color="text-[#dcb86f]"
              />
              <Metric
                value={stats.total}
                label={copy.total}
                color="text-[#f0d9a5]"
              />
              <Metric
                value={`${stats.progress}%`}
                label={copy.progress}
                color="text-[#f6e8d0]"
              />
            </div>

            <div className="mt-6">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-sm text-[#bdaa94]">
                  {copy.progressLabel}
                </span>
                <span className="text-sm text-[#bdaa94]">
                  {stats.unlocked} / {stats.total}
                </span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-white/10">
                <div
                  className="h-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] transition-all duration-500"
                  style={{ width: `${stats.progress}%` }}
                ></div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="mb-8">
          <h2 className="mb-6 flex items-center space-x-2 text-2xl font-bold">
            <Award className="text-[#d0a85b]" />
            <span>{copy.allAchievements}</span>
          </h2>

          {['divination', 'favorite', 'invite', 'checkin', 'points'].map(
            (type) => {
              const typeAchievements = allAchievements.filter(
                (achievement) => achievement.achievementType === type
              )
              if (typeAchievements.length === 0) return null

              return (
                <div key={type} className="mb-8">
                  <h3 className="mb-4 flex items-center space-x-2 text-lg font-semibold text-[#f4ece1]">
                    <span className="text-2xl">{getAchievementIcon(type)}</span>
                    <span>{getAchievementTypeName(type)}</span>
                  </h3>

                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {typeAchievements.map((achievement) => {
                      const unlocked = isUnlocked(achievement.achievementCode)
                      const userAchievement = getUserAchievement(
                        achievement.achievementCode
                      )

                      return (
                        <Card
                          key={achievement.id}
                          className={`transition-all hover:scale-105 ${
                            unlocked
                              ? 'border-[#d0a85b]/24 bg-[linear-gradient(180deg,rgba(40,22,18,0.92),rgba(21,15,13,0.8))] shadow-[0_18px_50px_rgba(0,0,0,0.26)]'
                              : 'border-white/8 bg-[linear-gradient(180deg,rgba(18,14,13,0.8),rgba(12,10,9,0.72))] opacity-70'
                          }`}
                        >
                          <CardContent className="p-5">
                            <div className="flex items-start space-x-4">
                              <div
                                className={`text-4xl ${unlocked ? '' : 'opacity-50 grayscale'}`}
                              >
                                {achievement.iconUrl ? (
                                  <img
                                    src={achievement.iconUrl}
                                    alt={achievement.achievementName}
                                    className="h-12 w-12"
                                  />
                                ) : (
                                  <span>
                                    {getAchievementIcon(
                                      achievement.achievementType
                                    )}
                                  </span>
                                )}
                              </div>
                              <div className="flex-1">
                                <div className="mb-2 flex items-center justify-between">
                                  <h4
                                    className={`font-bold ${unlocked ? 'text-white' : 'text-[#8f7b66]'}`}
                                  >
                                    {achievement.achievementName}
                                  </h4>
                                  {unlocked ? (
                                    <Unlock
                                      className="text-[#dcb86f]"
                                      size={20}
                                    />
                                  ) : (
                                    <Lock className="text-[#8f7b66]" size={20} />
                                  )}
                                </div>
                                <p className="mb-3 text-sm text-[#bdaa94]">
                                  {achievement.achievementDescription}
                                </p>
                                <div className="flex items-center justify-between">
                                  <span
                                    className={`rounded px-2 py-1 text-xs ${
                                      unlocked
                                        ? 'bg-[#7a3218]/20 text-[#dcb86f]'
                                        : 'bg-white/5 text-[#8f7b66]'
                                    }`}
                                  >
                                    {achievement.pointsReward} {copy.points}
                                  </span>
                                  {userAchievement?.unlockedTime && (
                                    <span className="text-xs text-[#8f7b66]">
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
            }
          )}
        </div>
      </div>
    </div>
  )
}

function Metric({ value, label, color }) {
  return (
    <div className="text-center">
      <div className={`mb-2 text-4xl font-bold ${color}`}>{value}</div>
      <div className="text-sm text-[#bdaa94]">{label}</div>
    </div>
  )
}
