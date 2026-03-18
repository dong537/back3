/**
 * 成就数据缓存工具
 * 用于缓存成就列表和用户成就，减少API调用
 */

class AchievementCache {
  constructor() {
    this.cache = {
      allAchievements: null,
      userAchievements: null,
      stats: null,
      timestamp: null,
      expireTime: 5 * 60 * 1000 // 5分钟过期
    }
  }

  /**
   * 检查缓存是否有效
   */
  isValid() {
    if (!this.cache.timestamp) {
      return false
    }
    return Date.now() - this.cache.timestamp < this.cache.expireTime
  }

  /**
   * 获取所有成就（带缓存）
   */
  getAllAchievements() {
    if (this.isValid() && this.cache.allAchievements) {
      return this.cache.allAchievements
    }
    return null
  }

  /**
   * 获取用户成就（带缓存）
   */
  getUserAchievements() {
    if (this.isValid() && this.cache.userAchievements) {
      return this.cache.userAchievements
    }
    return null
  }

  /**
   * 获取统计信息（带缓存）
   */
  getStats() {
    if (this.isValid() && this.cache.stats) {
      return this.cache.stats
    }
    return null
  }

  /**
   * 设置缓存
   */
  setCache(allAchievements, userAchievements, stats) {
    this.cache.allAchievements = allAchievements
    this.cache.userAchievements = userAchievements
    this.cache.stats = stats
    this.cache.timestamp = Date.now()
  }

  /**
   * 清除缓存
   */
  clear() {
    this.cache.allAchievements = null
    this.cache.userAchievements = null
    this.cache.stats = null
    this.cache.timestamp = null
  }

  /**
   * 更新用户成就（当解锁新成就时）
   */
  updateUserAchievements(newAchievement) {
    if (this.cache.userAchievements) {
      // 检查是否已存在
      const exists = this.cache.userAchievements.some(
        ua => ua.achievementCode === newAchievement.achievementCode
      )
      if (!exists) {
        this.cache.userAchievements.push(newAchievement)
        // 更新统计
        if (this.cache.stats) {
          this.cache.stats.unlocked = (this.cache.stats.unlocked || 0) + 1
          if (this.cache.stats.total > 0) {
            this.cache.stats.progress = Math.round(
              (this.cache.stats.unlocked * 100) / this.cache.stats.total
            )
          }
        }
      }
    }
  }
}

export default new AchievementCache()
