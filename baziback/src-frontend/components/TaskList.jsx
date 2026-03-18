import { useState, useEffect } from 'react'
import { CheckCircle, Circle, Gift, Target, Lock } from 'lucide-react'
import Card, { CardHeader, CardTitle, CardContent } from './Card'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { checkin, points, achievements } from '../utils/referral'
import { toast } from './Toast'
import { useAuth } from '../context/AuthContext'

/**
 * 任务列表组件
 */
export default function TaskList() {
  const { isLoggedIn } = useAuth()
  const [tasks, setTasks] = useState([])

  useEffect(() => {
    loadTasks()
  }, [])

  const loadTasks = () => {
    const history = historyStorage.getAll()
    const favorites = favoritesStorage.getAll()
    const checkedIn = checkin.getToday()
    const streak = checkin.getStreak()

    const taskList = [
      {
        id: 'daily_checkin',
        title: '每日签到',
        description: '完成每日签到获得积分奖励',
        progress: checkedIn ? 1 : 0,
        target: 1,
        reward: checkin.getStreak() >= 7 ? 30 : checkin.getStreak() >= 3 ? 20 : 10,
        completed: checkedIn,
        disabled: !isLoggedIn,
        action: () => {
          if (!isLoggedIn) {
            toast.warning('请先登录后再进行签到')
            return
          }
          if (!checkedIn) {
            const result = checkin.doCheckin()
            if (result.success) {
              toast.success(`签到成功！获得${result.reward}积分`)
              loadTasks()
            }
          }
        }
      },
      {
        id: 'first_divination',
        title: '完成首次占卜',
        description: '进行第一次占卜',
        progress: history.length > 0 ? 1 : 0,
        target: 1,
        reward: 20,
        completed: history.length > 0,
        action: null
      },
      {
        id: 'divination_5',
        title: '占卜达人',
        description: '完成5次占卜',
        progress: Math.min(history.length, 5),
        target: 5,
        reward: 50,
        completed: history.length >= 5,
        action: null
      },
      {
        id: 'collect_favorite',
        title: '收藏家',
        description: '收藏3个占卜结果',
        progress: Math.min(favorites.length, 3),
        target: 3,
        reward: 30,
        completed: favorites.length >= 3,
        action: null
      },
      {
        id: 'share_result',
        title: '分享达人',
        description: '分享1次占卜结果',
        progress: 0, // 需要跟踪分享次数
        target: 1,
        reward: 10,
        completed: false,
        action: null
      },
      {
        id: 'checkin_streak_7',
        title: '持之以恒',
        description: '连续签到7天',
        progress: Math.min(streak, 7),
        target: 7,
        reward: 50,
        completed: streak >= 7,
        action: null
      }
    ]

    setTasks(taskList)
  }

  const getProgressPercent = (task) => {
    return Math.min((task.progress / task.target) * 100, 100)
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center space-x-2">
          <Target className="text-skin-primary" size={20} />
          <CardTitle>每日任务</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {tasks.map((task) => (
            <div
              key={task.id}
              className={`p-4 rounded-lg border transition ${
                task.completed
                  ? 'bg-green-500/10 border-green-500/30'
                  : 'bg-white/5 border-white/10'
              }`}
            >
              <div className="flex items-start justify-between mb-2">
                <div className="flex-1">
                  <div className="flex items-center space-x-2 mb-1">
                    {task.completed ? (
                      <CheckCircle className="text-green-400" size={18} />
                    ) : (
                      <Circle className="text-gray-400" size={18} />
                    )}
                    <h3 className="font-medium">{task.title}</h3>
                  </div>
                  <p className="text-sm text-gray-400 mb-2">{task.description}</p>
                  
                  {/* 进度条 */}
                  <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden mb-2">
                    <div
                      className="h-full bg-skin-primary transition-all duration-300"
                      style={{ width: `${getProgressPercent(task)}%` }}
                    />
                  </div>
                  
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-400">
                      进度：{task.progress}/{task.target}
                    </span>
                    <div className="flex items-center space-x-1 text-skin-primary">
                      <Gift size={14} />
                      <span>+{task.reward} 积分</span>
                    </div>
                  </div>
                </div>
              </div>

              {task.action && !task.completed && (
                <button
                  onClick={task.action}
                  disabled={task.disabled}
                  className={`mt-2 w-full py-2 text-sm rounded-lg transition ${
                    task.disabled
                      ? 'bg-gray-500/20 text-gray-500 cursor-not-allowed opacity-60'
                      : 'bg-skin-primary/20 hover:bg-skin-primary/30 text-skin-primary'
                  }`}
                >
                  {task.disabled ? (
                    <span className="flex items-center justify-center space-x-2">
                      <Lock size={14} />
                      <span>请先登录</span>
                    </span>
                  ) : (
                    '立即完成'
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
