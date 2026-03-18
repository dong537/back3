import { logger } from './logger'

/**
 * 缓存管理器
 * 用于前端缓存 API 响应，减少网络请求
 */
class CacheManager {
  constructor() {
    this.cache = new Map()
    this.ttl = new Map() // 存储过期时间
  }

  /**
   * 设置缓存
   * @param {string} key - 缓存键
   * @param {*} value - 缓存值
   * @param {number} ttlSeconds - 过期时间（秒），默认 5 分钟
   */
  set(key, value, ttlSeconds = 5 * 60) {
    this.cache.set(key, value)
    this.ttl.set(key, Date.now() + ttlSeconds * 1000)
    logger.debug(`缓存设置: ${key}, TTL: ${ttlSeconds}s`)
  }

  /**
   * 获取缓存
   * @param {string} key - 缓存键
   * @returns {*} 缓存值，如果过期或不存在则返回 null
   */
  get(key) {
    const expireTime = this.ttl.get(key)
    
    // 检查是否过期
    if (expireTime && Date.now() > expireTime) {
      this.cache.delete(key)
      this.ttl.delete(key)
      logger.debug(`缓存过期: ${key}`)
      return null
    }
    
    const value = this.cache.get(key)
    if (value) {
      logger.debug(`缓存命中: ${key}`)
    }
    return value
  }

  /**
   * 清除指定缓存
   * @param {string} key - 缓存键
   */
  clear(key) {
    this.cache.delete(key)
    this.ttl.delete(key)
    logger.debug(`缓存清除: ${key}`)
  }

  /**
   * 清除所有缓存
   */
  clearAll() {
    this.cache.clear()
    this.ttl.clear()
    logger.debug('所有缓存已清除')
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      size: this.cache.size,
      keys: Array.from(this.cache.keys())
    }
  }
}

export const cacheManager = new CacheManager()
