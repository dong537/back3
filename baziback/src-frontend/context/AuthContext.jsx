import { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react'
import { userApi, creditApi } from '../api'
import { logger } from '../utils/logger'
import { onLogout } from '../utils/authEvents'
import { favoritesStorage } from '../utils/storage'

const AuthContext = createContext(null)

const FREE_DAILY_LIMIT = 2

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [freeCount, setFreeCount] = useState(0)
  const [lastResetDate, setLastResetDate] = useState(null)
  const [credits, setCredits] = useState(0)
  const [isLoading, setIsLoading] = useState(true)

  const logout = useCallback(() => {
    setUser(null)
    setToken(null)
    setCredits(0)
    // ✅ 改用 sessionStorage
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    // 清理旧的 localStorage（兼容旧版本）
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    favoritesStorage.invalidateCache()
  }, [])

  const refreshCredits = useCallback(async () => {
    if (!token) return 0
    try {
      const res = await creditApi.getBalance()
      if (res.data?.code === 200) {
        const bal = res.data.data?.balance || 0
        setCredits(bal)
        return bal
      }
    } catch (e) {
      logger.warn('刷新积分失败', e)
    }
    return 0
  }, [token])

  // 消费积分（调用后端API）
  const spendCredits = useCallback(async (amount, reason = '功能消费') => {
    if (!token) {
      return { success: false, message: '请先登录' }
    }
    if (credits < amount) {
      return { success: false, message: `积分不足，当前余额：${credits}，需要：${amount}` }
    }
    try {
      const res = await creditApi.spend(amount, reason)
      if (res.data?.code === 200 && res.data?.data?.success) {
        const newBalance = res.data.data.newBalance
        setCredits(newBalance)
        return { success: true, newBalance, amount }
      }
      return { success: false, message: res.data?.message || '积分扣除失败' }
    } catch (e) {
      logger.error('消费积分失败', e)
      return { success: false, message: e?.message || '积分扣除失败' }
    }
  }, [token, credits])

  // 检查是否有足够积分
  const canSpendCredits = useCallback((amount) => {
    return credits >= amount
  }, [credits])

  const login = useCallback(
    (userData, userToken) => {
      if (!userData || !userToken) {
        logger.error('Login failed: Invalid user data or token', {
          hasUserData: !!userData,
          hasToken: !!userToken,
        })
        return false
      }

      try {
        const u = typeof userData === 'object' ? userData : JSON.parse(userData)

        if (!u || !u.id || !u.username) {
          logger.error('Login failed: Invalid user data structure', u)
          return false
        }

        // ✅ 改用 sessionStorage（仅当前标签页有效，更安全）
        sessionStorage.setItem('token', userToken)
        sessionStorage.setItem('user', JSON.stringify(u))

        setToken(() => userToken)
        setUser(() => u)

        // 登录成功后异步刷新积分余额和同步收藏夹
        setTimeout(() => {
          refreshCredits()
          favoritesStorage.syncWithServer()
        }, 0)

        return true
      } catch (error) {
        logger.error('Login failed: Error processing user data', error)
        logout()
        return false
      }
    },
    [logout, refreshCredits]
  )

  useEffect(() => {
    const unsubscribe = onLogout(() => {
      logout()
    })
    return unsubscribe
  }, [logout])

  useEffect(() => {
    const initializeAuth = async () => {
      // ✅ 改用 sessionStorage
      const savedToken = sessionStorage.getItem('token')

      if (savedToken) {
        try {
          // 调用 user/info 获取用户信息
          const response = await userApi.getInfo(savedToken)
          const payload = response.data?.data

          // 获取用户对象（直接从data中获取）
          const userFromApi = payload?.user

          if (userFromApi) {
            login(userFromApi, savedToken)
          } else {
            logout()
          }
        } catch (error) {
          logger.error('Token validation failed:', error)
          logout()
        }
      }

      const savedFreeCount = localStorage.getItem('freeInterpretCount')
      const savedResetDate = localStorage.getItem('freeInterpretResetDate')
      const today = new Date().toDateString()

      if (savedResetDate !== today) {
        setFreeCount(0)
        localStorage.setItem('freeInterpretCount', '0')
        localStorage.setItem('freeInterpretResetDate', today)
      } else {
        setFreeCount(parseInt(savedFreeCount) || 0)
      }
      setLastResetDate(today)

      if (savedToken) {
        try {
          const balRes = await creditApi.getBalance()
          if (balRes.data?.code === 200) {
            setCredits(balRes.data.data?.balance || 0)
          }
        } catch (e) {
          logger.warn('获取积分余额失败', e)
        }
      }

      setIsLoading(false)
    }

    initializeAuth()
  }, [login, logout])

  const canUseAI = useCallback(() => {
    const loggedIn = !!user && !!token
    logger.debug('canUseAI called:', { isLoggedIn: loggedIn, user: !!user, token: !!token, freeCount })
    if (!loggedIn) return { allowed: false, reason: 'not_logged_in' }
    if (freeCount < FREE_DAILY_LIMIT) return { allowed: true, reason: 'free' }
    return { allowed: false, reason: 'limit_reached' }
  }, [user, token, freeCount])

  const useAIInterpretation = useCallback(() => {
    const check = canUseAI()
    if (check.allowed && check.reason === 'free') {
      const newCount = freeCount + 1
      setFreeCount(newCount)
      localStorage.setItem('freeInterpretCount', newCount.toString())
      return true
    }
    return false
  }, [canUseAI, freeCount])

  const getRemainingFreeCount = useCallback(() => {
    if (!user) return 0
    return Math.max(0, FREE_DAILY_LIMIT - freeCount)
  }, [user, freeCount])

  const isLoggedIn = useMemo(() => {
    const loggedIn = !!user && !!token
    logger.debug('isLoggedIn computed:', { user: !!user, token: !!token, isLoggedIn: loggedIn })
    return loggedIn
  }, [user, token])

  const value = useMemo(
    () => ({
      user,
      token,
      credits,
      refreshCredits,
      spendCredits,
      canSpendCredits,
      isLoggedIn,
      isLoading,
      login,
      logout,
      canUseAI,
      useAIInterpretation,
      getRemainingFreeCount,
      FREE_DAILY_LIMIT,
    }),
    [
      user,
      token,
      credits,
      refreshCredits,
      spendCredits,
      canSpendCredits,
      isLoggedIn,
      isLoading,
      login,
      logout,
      canUseAI,
      useAIInterpretation,
      getRemainingFreeCount,
    ]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export default AuthContext
