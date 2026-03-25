import { useEffect, useMemo, useState } from 'react'
import { CheckCircle, Circle, Gift, Lock, Target } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardContent, CardHeader, CardTitle } from './Card'
import { favoritesStorage, historyStorage } from '../utils/storage'
import { checkin } from '../utils/referral'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'
import { POINTS_EARN } from '../utils/pointsConfig'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const TASK_COPY = {
  'zh-CN': {
    title: '每日任务',
    progress: '进度',
    points: '积分',
    loginFirst: '请先登录后再进行签到',
    loginAction: '请先登录',
    completeNow: '立即完成',
    checkinSuccess: (reward) => `签到成功！获得 ${reward} 积分`,
    tasks: {
      daily_checkin: {
        title: '每日签到',
        description: '完成每日签到获得积分奖励',
      },
      first_divination: {
        title: '完成首次占卜',
        description: '进行第一次占卜',
      },
      divination_5: {
        title: '占卜达人',
        description: '完成 5 次占卜',
      },
      collect_favorite: {
        title: '收藏家',
        description: '收藏 3 个占卜结果',
      },
      share_result: {
        title: '分享达人',
        description: '分享 1 次占卜结果',
      },
      checkin_streak_7: {
        title: '持之以恒',
        description: '连续签到 7 天',
      },
    },
  },
  'en-US': {
    title: 'Daily Tasks',
    progress: 'Progress',
    points: 'credits',
    loginFirst: 'Please sign in before checking in',
    loginAction: 'Sign in first',
    completeNow: 'Complete now',
    checkinSuccess: (reward) => `Check-in complete. Earned ${reward} credits`,
    tasks: {
      daily_checkin: {
        title: 'Daily Check-in',
        description: 'Complete today’s check-in to earn credits',
      },
      first_divination: {
        title: 'First Reading',
        description: 'Complete your first divination',
      },
      divination_5: {
        title: 'Reading Expert',
        description: 'Complete 5 readings',
      },
      collect_favorite: {
        title: 'Collector',
        description: 'Save 3 reading results',
      },
      share_result: {
        title: 'Sharing Pro',
        description: 'Share 1 reading result',
      },
      checkin_streak_7: {
        title: 'Keep It Going',
        description: 'Maintain a 7-day check-in streak',
      },
    },
  },
}

export default function TaskList() {
  const { isLoggedIn } = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = TASK_COPY[locale]
  const [tasks, setTasks] = useState([])

  useEffect(() => {
    loadTasks()
  }, [isLoggedIn, locale])

  const loadTasks = async () => {
    try {
      const history = historyStorage.getAll()
      const favorites = await favoritesStorage.getAll()
      const checkedIn = checkin.getToday()
      const streak = checkin.getStreak()
      const shareCount = Number.parseInt(
        localStorage.getItem('share_count') || '0',
        10
      )

      const taskList = [
        {
          id: 'daily_checkin',
          progress: checkedIn ? 1 : 0,
          target: 1,
          reward:
            streak >= 7
              ? POINTS_EARN.CHECKIN_STREAK_7
              : streak >= 3
                ? POINTS_EARN.CHECKIN_STREAK_3
                : POINTS_EARN.DAILY_CHECKIN,
          completed: checkedIn,
          disabled: !isLoggedIn,
          action: () => {
            if (!isLoggedIn) {
              toast.warning(copy.loginFirst)
              return
            }
            if (!checkedIn) {
              const result = checkin.doCheckin()
              if (result?.success) {
                toast.success(copy.checkinSuccess(result.reward))
                loadTasks()
              }
            }
          },
        },
        {
          id: 'first_divination',
          progress: history.length > 0 ? 1 : 0,
          target: 1,
          reward: POINTS_EARN.FIRST_DIVINATION,
          completed: history.length > 0,
        },
        {
          id: 'divination_5',
          progress: Math.min(history.length, 5),
          target: 5,
          reward: 50,
          completed: history.length >= 5,
        },
        {
          id: 'collect_favorite',
          progress: Math.min(favorites.length, 3),
          target: 3,
          reward: 30,
          completed: favorites.length >= 3,
        },
        {
          id: 'share_result',
          progress: Math.min(shareCount, 1),
          target: 1,
          reward: POINTS_EARN.SHARE_RESULT,
          completed: shareCount >= 1,
        },
        {
          id: 'checkin_streak_7',
          progress: Math.min(streak, 7),
          target: 7,
          reward: 50,
          completed: streak >= 7,
        },
      ].map((task) => ({
        ...task,
        title: copy.tasks[task.id].title,
        description: copy.tasks[task.id].description,
      }))

      setTasks(taskList)
    } catch (error) {
      logger.error('Load task list failed:', error)
      setTasks([])
    }
  }

  const getProgressPercent = (task) =>
    Math.min((task.progress / task.target) * 100, 100)

  const taskItems = useMemo(() => tasks, [tasks])

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center space-x-2">
          <Target className="text-skin-primary" size={20} />
          <CardTitle>{copy.title}</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {taskItems.map((task) => (
            <div
              key={task.id}
              className={`rounded-lg border p-4 transition ${
                task.completed
                  ? 'border-green-500/30 bg-green-500/10'
                  : 'border-white/10 bg-white/5'
              }`}
            >
              <div className="mb-2 flex items-start justify-between">
                <div className="flex-1">
                  <div className="mb-1 flex items-center space-x-2">
                    {task.completed ? (
                      <CheckCircle className="text-green-400" size={18} />
                    ) : (
                      <Circle className="text-gray-400" size={18} />
                    )}
                    <h3 className="font-medium">{task.title}</h3>
                  </div>
                  <p className="mb-2 text-sm text-gray-400">
                    {task.description}
                  </p>

                  <div className="mb-2 h-2 w-full overflow-hidden rounded-full bg-white/10">
                    <div
                      className="h-full bg-skin-primary transition-all duration-300"
                      style={{ width: `${getProgressPercent(task)}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-400">
                      {copy.progress}: {task.progress}/{task.target}
                    </span>
                    <div className="flex items-center space-x-1 text-skin-primary">
                      <Gift size={14} />
                      <span>
                        +{task.reward} {copy.points}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {task.action && !task.completed && (
                <button
                  onClick={task.action}
                  disabled={task.disabled}
                  className={`mt-2 w-full rounded-lg py-2 text-sm transition ${
                    task.disabled
                      ? 'cursor-not-allowed bg-gray-500/20 text-gray-500 opacity-60'
                      : 'bg-skin-primary/20 hover:bg-skin-primary/30 text-skin-primary'
                  }`}
                >
                  {task.disabled ? (
                    <span className="flex items-center justify-center space-x-2">
                      <Lock size={14} />
                      <span>{copy.loginAction}</span>
                    </span>
                  ) : (
                    copy.completeNow
                  )}
                </button>
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
