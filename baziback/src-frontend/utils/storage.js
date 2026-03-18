import { logger } from './logger'
import { favoriteApi } from '../api'

const STORAGE_KEYS = {
  HISTORY: 'divination_history',
  FAVORITES: 'divination_favorites_local', // Rename to avoid conflict
  SETTINGS: 'app_settings',
}

// Helper to check login status without context dependency
const isLoggedIn = () => !!localStorage.getItem('token')

// --- historyStorage remains unchanged ---
export const historyStorage = {
  getAll() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.HISTORY)
      return data ? JSON.parse(data) : []
    } catch (error) {
      logger.error('读取历史记录失败:', error)
      return []
    }
  },
  add(record) {
    try {
      const history = this.getAll()
      const newRecord = {
        id: Date.now().toString(),
        timestamp: new Date().toISOString(),
        ...record
      }
      history.unshift(newRecord)
      const limited = history.slice(0, 100)
      localStorage.setItem(STORAGE_KEYS.HISTORY, JSON.stringify(limited))
      return newRecord
    } catch (error) {
      logger.error('保存历史记录失败:', error)
      return null
    }
  },
  remove(id) {
    try {
      const history = this.getAll()
      const filtered = history.filter(item => item.id !== id)
      localStorage.setItem(STORAGE_KEYS.HISTORY, JSON.stringify(filtered))
      return true
    } catch (error) {
      logger.error('删除历史记录失败:', error)
      return false
    }
  },
  clear() {
    try {
      localStorage.removeItem(STORAGE_KEYS.HISTORY)
      return true
    } catch (error) {
      logger.error('清空历史记录失败:', error)
      return false
    }
  },
  getByType(type) {
    const history = this.getAll()
    return history.filter(item => item.type === type)
  }
}

// --- Refactored favoritesStorage ---
let _favoritesCache = null
let _isSyncing = false

const favoritesStorage = {
  // Sync local favorites with server on login
  async syncWithServer() {
    if (!isLoggedIn() || _isSyncing) return
    _isSyncing = true

    try {
      const localFavorites = this._getLocalFavorites()
      if (localFavorites.length > 0) {
        // Upload local favorites to the server
        for (const fav of localFavorites) {
          // Convert local structure to server structure
          const serverFav = {
            favoriteType: fav.type,
            dataId: fav.dataId,
            title: fav.title,
            summary: fav.summary,
            data: JSON.stringify(fav.data),
          }
          await favoriteApi.add(serverFav)
        }
        // Clear local storage after successful upload
        localStorage.removeItem(STORAGE_KEYS.FAVORITES)
      }

      // Fetch all favorites from server and update cache
      await this.getAll(true) // force refresh
    } catch (error) {
      logger.error('同步收藏失败:', error)
    } finally {
      _isSyncing = false
    }
  },

  // Get all favorites (from cache, server, or local)
  async getAll(forceRefresh = false) {
    if (!isLoggedIn()) {
      return this._getLocalFavorites()
    }

    if (_favoritesCache && !forceRefresh) {
      return _favoritesCache
    }

    try {
      const response = await favoriteApi.getAll()
      _favoritesCache = response.data.data || []
      return _favoritesCache
    } catch (error) {
      logger.error('从服务器获取收藏失败:', error)
      return []
    }
  },

  // Add a favorite
  async add(item) {
    const newItem = {
      id: Date.now(), // Temporary ID for local state
      timestamp: new Date().toISOString(),
      favoriteType: item.type || item.favoriteType,  // 确保有 favoriteType 字段
      ...item
    }

    if (!isLoggedIn()) {
      const local = this._getLocalFavorites()
      local.unshift(newItem)
      this._setLocalFavorites(local)
      return newItem
    }

    try {
      const serverFav = {
        favoriteType: item.type || item.favoriteType,
        dataId: item.dataId,
        title: item.title,
        summary: item.summary,
        data: JSON.stringify(item.data),
      }
      const response = await favoriteApi.add(serverFav)
      const added = response.data.data
      if (_favoritesCache) {
        _favoritesCache.unshift(added)
      }
      return added
    } catch (error) {
      logger.error('添加收藏到服务器失败:', error)
      return null
    }
  },

  // Remove a favorite
  async remove(id) {
    if (!isLoggedIn()) {
      const local = this._getLocalFavorites()
      const filtered = local.filter(fav => String(fav.id) !== String(id))
      this._setLocalFavorites(filtered)
      return true
    }

    try {
      await favoriteApi.remove(id)
      if (_favoritesCache) {
        _favoritesCache = _favoritesCache.filter(fav => fav.id !== id)
      }
      return true
    } catch (error) {
      logger.error('从服务器删除收藏失败:', error)
      return false
    }
  },

  // Toggle favorite status
  async toggle(item) {
    const all = await this.getAll()
    // 兼容 favoriteType 和 type 两种字段
    const itemType = item.favoriteType || item.type
    const existing = all.find(fav => {
      const favType = fav.favoriteType || fav.type
      return fav.dataId === item.dataId && favType === itemType
    })

    if (existing) {
      await this.remove(existing.id)
      return false  // 返回 false 表示取消收藏
    } else {
      await this.add(item)
      return true  // 返回 true 表示添加收藏
    }
  },

  // Invalidate cache (e.g., on logout)
  invalidateCache() {
    _favoritesCache = null
  },

  // --- Internal local storage helpers ---
  _getLocalFavorites() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.FAVORITES)
      return data ? JSON.parse(data) : []
    } catch (error) {
      logger.error('读取本地收藏失败:', error)
      return []
    }
  },

  _setLocalFavorites(favorites) {
    try {
      localStorage.setItem(STORAGE_KEYS.FAVORITES, JSON.stringify(favorites))
    } catch (error) {
      logger.error('保存本地收藏失败:', error)
    }
  },
}

// --- settingsStorage remains unchanged ---
export const settingsStorage = {
  get() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SETTINGS)
      return data ? JSON.parse(data) : {
        theme: 'auto',
        language: 'zh-CN',
        animations: true,
        notifications: true
      }
    } catch (error) {
      logger.error('读取设置失败:', error)
      return {}
    }
  },
  save(settings) {
    try {
      const current = this.get()
      const updated = { ...current, ...settings }
      localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(updated))
      return true
    } catch (error) {
      logger.error('保存设置失败:', error)
      return false
    }
  }
}

export { favoritesStorage }
