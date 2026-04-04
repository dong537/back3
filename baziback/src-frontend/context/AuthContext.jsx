import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'
import { userApi, creditApi, unwrapApiData } from '../api'
import { logger } from '../utils/logger'
import { onLogout } from '../utils/authEvents'
import { favoritesStorage } from '../utils/storage'
import achievementCache from '../utils/achievementCache'
import {
  clearStoredAuth,
  getStoredToken,
  migrateLegacyAuthToSession,
  storeSessionAuth,
} from '../utils/authStorage'
import { shouldAutoSso, markSsoAttempted, clearSsoAttempted } from '../utils/ssoHelper'

const AuthContext = createContext(null)
const FREE_DAILY_LIMIT = 2

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [freeCount, setFreeCount] = useState(0)
  const [lastResetDate, setLastResetDate] = useState(null)
  const [credits, setCredits] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const creditRequestRef = useRef(null)

  const logout = useCallback(() => {
    setUser(null)
    setToken(null)
    setCredits(0)
    creditRequestRef.current = null
    clearStoredAuth()
    sessionStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    favoritesStorage.invalidateCache()
    achievementCache.clear()
  }, [])

  const refreshCredits = useCallback(
    async (options = {}) => {
      const { tokenOverride = token, force = false } = options

      if (!tokenOverride) return 0
      if (creditRequestRef.current && !force) {
        return creditRequestRef.current
      }

      const request = creditApi
        .getBalance()
        .then((res) => {
          if (res.data?.code === 200) {
            const balance = res.data.data?.balance || 0
            setCredits(balance)
            return balance
          }
          return 0
        })
        .catch((error) => {
          logger.warn('刷新积分失败', error)
          return 0
        })
        .finally(() => {
          if (creditRequestRef.current === request) {
            creditRequestRef.current = null
          }
        })

      creditRequestRef.current = request
      return request
    },
    [token]
  )

  const spendCredits = useCallback(
    async (amount, reason = '积分消费') => {
      if (!amount || amount <= 0) {
        return { success: true, newBalance: credits, amount: 0, reason }
      }
      if (!token) {
        return { success: true, newBalance: credits, amount, reason }
      }

      try {
        const res = await creditApi.spend(amount, reason)
        if (res.data?.code === 200 && res.data?.data?.success) {
          const newBalance = res.data.data.newBalance
          setCredits(newBalance)
          return { success: true, newBalance, amount }
        }
        return {
          success: false,
          message: res.data?.message || '积分扣除失败',
        }
      } catch (error) {
        logger.error('消费积分失败', error)
        return {
          success: false,
          message: error?.message || '积分扣除失败',
        }
      }
    },
    [token, credits]
  )

  const canSpendCredits = useCallback(
    (amount) => {
      if (!amount || amount <= 0) return true
      return credits >= amount
    },
    [credits]
  )

  const login = useCallback(
    (userData, userToken, options = {}) => {
      if (!userData || !userToken) {
        logger.error('Login failed: Invalid user data or token', {
          hasUserData: !!userData,
          hasToken: !!userToken,
        })
        return false
      }

      try {
        const {
          initialCredits,
          skipCreditRefresh = false,
          skipFavoritesSync = false,
        } = options

        const normalizedUser =
          typeof userData === 'object' ? userData : JSON.parse(userData)

        if (
          !normalizedUser ||
          !normalizedUser.id ||
          !normalizedUser.username
        ) {
          logger.error('Login failed: Invalid user data structure', normalizedUser)
          return false
        }

        storeSessionAuth(userToken, normalizedUser)
        clearSsoAttempted()
        setToken(() => userToken)
        setUser(() => normalizedUser)
        setCredits(
          typeof initialCredits === 'number' ? initialCredits : 0
        )

        setTimeout(() => {
          if (!skipCreditRefresh) {
            refreshCredits({ tokenOverride: userToken, force: true })
          }
          if (!skipFavoritesSync) {
            favoritesStorage.syncWithServer()
          }
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
      migrateLegacyAuthToSession()
      const savedToken = getStoredToken()

      if (savedToken) {
        try {
          const [userResponse, balanceResponse] = await Promise.allSettled([
            userApi.getInfo(savedToken),
            creditApi.getBalance(),
          ])

          if (userResponse.status !== 'fulfilled') {
            throw userResponse.reason
          }

          const payload = unwrapApiData(userResponse.value)
          const userFromApi = payload?.user ?? payload
          const initialCredits =
            balanceResponse.status === 'fulfilled' &&
            balanceResponse.value.data?.code === 200
              ? balanceResponse.value.data.data?.balance || 0
              : undefined

          if (userFromApi) {
            clearSsoAttempted()
            login(userFromApi, savedToken, {
              initialCredits,
              skipCreditRefresh: typeof initialCredits === 'number',
            })
          } else {
            logout()
          }
        } catch (error) {
          logger.error('Token validation failed:', error)
          logout()
        }
      } else if (shouldAutoSso()) {
        // 无本地 token 且未尝试过 SSO，自动触发 SSO 重定向
        markSsoAttempted()
        const returnUrl = window.location.pathname + window.location.search
        window.location.href = '/api/auth/agentpit/sso?returnUrl=' + encodeURIComponent(returnUrl)
        return // 不再继续初始化，页面即将跳转
      }

      const savedFreeCount = localStorage.getItem('freeInterpretCount')
      const savedResetDate = localStorage.getItem('freeInterpretResetDate')
      const today = new Date().toDateString()

      if (savedResetDate !== today) {
        setFreeCount(0)
        localStorage.setItem('freeInterpretCount', '0')
        localStorage.setItem('freeInterpretResetDate', today)
      } else {
        setFreeCount(parseInt(savedFreeCount, 10) || 0)
      }

      setLastResetDate(today)
      setIsLoading(false)
    }

    initializeAuth()
  }, [login, logout])

  const canUseAI = useCallback(() => {
    const loggedIn = !!user && !!token
    logger.debug('canUseAI called:', {
      isLoggedIn: loggedIn,
      user: !!user,
      token: !!token,
      freeCount,
    })
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
    logger.debug('isLoggedIn computed:', {
      user: !!user,
      token: !!token,
      isLoggedIn: loggedIn,
      lastResetDate,
    })
    return loggedIn
  }, [user, token, lastResetDate])

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
