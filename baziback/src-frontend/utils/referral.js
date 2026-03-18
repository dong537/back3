/**
 * 推荐/邀请系统工具
 */

const REFERRAL_KEY = 'user_referral_code'
const REFERRED_KEY = 'user_referred_by'
const INVITES_KEY = 'user_invites'
const POINTS_KEY = 'user_points'
const DAILY_CHECKIN_KEY = 'daily_checkin'
const ACHIEVEMENTS_KEY = 'user_achievements'

/**
 * 生成推荐码
 */
export function generateReferralCode() {
  const userId = localStorage.getItem('userId') || Date.now().toString()
  return `REF${userId.slice(-6).toUpperCase()}`
}

/**
 * 获取或创建推荐码
 */
export function getReferralCode() {
  let code = localStorage.getItem(REFERRAL_KEY)
  if (!code) {
    code = generateReferralCode()
    localStorage.setItem(REFERRAL_KEY, code)
  }
  return code
}

/**
 * 设置推荐人
 */
export function setReferrer(code) {
  if (!localStorage.getItem(REFERRED_KEY)) {
    localStorage.setItem(REFERRED_KEY, code)
    return true
  }
  return false
}

/**
 * 获取推荐人
 */
export function getReferrer() {
  return localStorage.getItem(REFERRED_KEY)
}

/**
 * 记录邀请
 */
export function recordInvite(referralCode) {
  const invites = getInvites()
  invites.push({
    code: referralCode,
    timestamp: new Date().toISOString(),
    status: 'pending' // pending, completed
  })
  localStorage.setItem(INVITES_KEY, JSON.stringify(invites))
}

/**
 * 获取邀请列表
 */
export function getInvites() {
  try {
    const data = localStorage.getItem(INVITES_KEY)
    return data ? JSON.parse(data) : []
  } catch {
    return []
  }
}

/**
 * 获取邀请统计
 */
export function getInviteStats() {
  const invites = getInvites()
  return {
    total: invites.length,
    completed: invites.filter(i => i.status === 'completed').length,
    pending: invites.filter(i => i.status === 'pending').length
  }
}

/**
 * 积分系统
 */
export const points = {
  get() {
    return parseInt(localStorage.getItem(POINTS_KEY) || '0')
  },
  
  add(amount, reason = '') {
    const current = this.get()
    const newTotal = current + amount
    localStorage.setItem(POINTS_KEY, newTotal.toString())
    
    // 记录积分变动
    const history = this.getHistory()
    history.unshift({
      type: 'earn',
      amount,
      reason,
      timestamp: new Date().toISOString(),
      total: newTotal
    })
    localStorage.setItem(`${POINTS_KEY}_history`, JSON.stringify(history.slice(0, 100)))
    
    return newTotal
  },
  
  spend(amount, reason = '') {
    const current = this.get()
    if (current < amount) return { success: false, message: '积分不足' }
    
    const newTotal = current - amount
    localStorage.setItem(POINTS_KEY, newTotal.toString())
    
    // 记录积分变动
    const history = this.getHistory()
    history.unshift({
      type: 'spend',
      amount: -amount,
      reason,
      timestamp: new Date().toISOString(),
      total: newTotal
    })
    localStorage.setItem(`${POINTS_KEY}_history`, JSON.stringify(history.slice(0, 100)))
    
    return { success: true, newTotal }
  },
  
  canSpend(amount) {
    return this.get() >= amount
  },
  
  getHistory() {
    try {
      const data = localStorage.getItem(`${POINTS_KEY}_history`)
      return data ? JSON.parse(data) : []
    } catch {
      return []
    }
  }
}

/**
 * 每日签到
 */
export const checkin = {
  getToday() {
    const today = new Date().toDateString()
    const lastCheckin = localStorage.getItem(DAILY_CHECKIN_KEY)
    return lastCheckin === today
  },
  
  canCheckin() {
    return !this.getToday()
  },
  
  doCheckin() {
    if (!this.canCheckin()) return false
    
    const today = new Date().toDateString()
    localStorage.setItem(DAILY_CHECKIN_KEY, today)
    
    // 连续签到天数
    const streak = this.getStreak()
    const newStreak = streak + 1
    
    // 奖励积分（连续签到有额外奖励）
    let reward = 10
    if (newStreak >= 7) reward = 30
    else if (newStreak >= 3) reward = 20
    
    points.add(reward, `每日签到（连续${newStreak}天）`)
    
    // 更新连续签到
    localStorage.setItem(`${DAILY_CHECKIN_KEY}_streak`, newStreak.toString())
    localStorage.setItem(`${DAILY_CHECKIN_KEY}_last_date`, today)
    
    return { success: true, reward, streak: newStreak }
  },
  
  getStreak() {
    const lastDate = localStorage.getItem(`${DAILY_CHECKIN_KEY}_last_date`)
    const today = new Date().toDateString()
    
    if (!lastDate) return 0
    
    const last = new Date(lastDate)
    const now = new Date(today)
    const diffDays = Math.floor((now - last) / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) {
      // 今天已签到
      return parseInt(localStorage.getItem(`${DAILY_CHECKIN_KEY}_streak`) || '0')
    } else if (diffDays === 1) {
      // 连续签到
      return parseInt(localStorage.getItem(`${DAILY_CHECKIN_KEY}_streak`) || '0')
    } else {
      // 中断，重置
      return 0
    }
  }
}

/**
 * 成就系统
 */
export const achievements = {
  getAll() {
    try {
      const data = localStorage.getItem(ACHIEVEMENTS_KEY)
      return data ? JSON.parse(data) : []
    } catch {
      return []
    }
  },
  
  unlock(id, name, description, reward = 0) {
    const unlocked = this.getAll()
    if (unlocked.find(a => a.id === id)) return false
    
    const achievement = {
      id,
      name,
      description,
      reward,
      unlockedAt: new Date().toISOString()
    }
    
    unlocked.push(achievement)
    localStorage.setItem(ACHIEVEMENTS_KEY, JSON.stringify(unlocked))
    
    if (reward > 0) {
      points.add(reward, `成就奖励：${name}`)
    }
    
    return achievement
  },
  
  checkAchievements() {
    // 动态导入避免循环依赖
    return import('./storage').then(({ historyStorage, favoritesStorage }) => {
      // 检查各种成就条件
      const stats = {
        totalDivinations: historyStorage.getAll().length,
        totalFavorites: favoritesStorage.getAll().length,
        inviteCount: getInviteStats().completed,
        checkinStreak: checkin.getStreak(),
        totalPoints: points.get()
      }
      
      const newAchievements = []
      
      // 首次占卜
      if (stats.totalDivinations >= 1 && !this.hasAchievement('first_divination')) {
        newAchievements.push(this.unlock('first_divination', '初窥天机', '完成第一次占卜', 20))
      }
      
      // 占卜达人
      if (stats.totalDivinations >= 10 && !this.hasAchievement('divination_master')) {
        newAchievements.push(this.unlock('divination_master', '占卜达人', '完成10次占卜', 50))
      }
      
      // 收藏家
      if (stats.totalFavorites >= 5 && !this.hasAchievement('collector')) {
        newAchievements.push(this.unlock('collector', '收藏家', '收藏5个结果', 30))
      }
      
      // 邀请达人
      if (stats.inviteCount >= 3 && !this.hasAchievement('inviter')) {
        newAchievements.push(this.unlock('inviter', '邀请达人', '成功邀请3位好友', 100))
      }
      
      // 连续签到
      if (stats.checkinStreak >= 7 && !this.hasAchievement('checkin_week')) {
        newAchievements.push(this.unlock('checkin_week', '持之以恒', '连续签到7天', 50))
      }
      
      return newAchievements
    })
  },
  
  hasAchievement(id) {
    return this.getAll().some(a => a.id === id)
  }
}

// 注意：这里不能直接导入，因为会造成循环依赖
// 在使用时动态导入
