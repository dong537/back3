import axios from 'axios'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'
import { emitLogout } from '../utils/authEvents'

const API_BASE = '/api'

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器，自动添加token
api.interceptors.request.use(
  (config) => {
    // ✅ 改用 sessionStorage（更安全）
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

function getBackendMessage(data) {
  // 统一后端 Result { code,message,data }
  if (data && typeof data === 'object') {
    if (typeof data.message === 'string' && data.message.trim()) return data.message
    if (typeof data.msg === 'string' && data.msg.trim()) return data.msg
  }
  return ''
}

export function unwrapApiData(response) {
  const payload = response?.data
  if (payload === undefined || payload === null) {
    throw new Error('无效的响应数据')
  }
  if (typeof payload === 'object' && payload !== null && 'data' in payload && payload.data !== undefined && payload.data !== null) {
    return payload.data
  }
  return payload
}

// 响应拦截器，统一处理错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      const backendMessage = getBackendMessage(data)

      // 401 未授权 - token过期或无效
      if (status === 401) {
        // ✅ 同时清除 sessionStorage 和 localStorage（兼容旧版本）
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        emitLogout('unauthorized')

        // 自动跳转登录（避免状态不同步）
        if (typeof window !== 'undefined' && window.location?.pathname !== '/login') {
          window.location.assign('/login')
        }
      }

      if (status === 403) {
        logger.warn('没有权限访问该资源')
      }

      if (status >= 500) {
        logger.error('服务器错误:', backendMessage || '服务器内部错误')
      }

      const message = backendMessage || error.message || '请求失败'
      console.log('Toast Error Message (backend):', message, typeof message)
      toast.error(typeof message === 'object' ? JSON.stringify(message) : message)

      return Promise.reject({
        ...error,
        message,
        status,
      })
    }

    if (error.request) {
      logger.error('网络错误: 无法连接到服务器')
      const message = '网络错误，请检查网络连接'
      console.log('Toast Error Message (network):', message, typeof message)
      toast.error(typeof message === 'object' ? JSON.stringify(message) : message)
      return Promise.reject({
        ...error,
        message,
        isNetworkError: true,
      })
    }

    logger.error('请求错误:', error.message)
    const message = error.message || '请求失败'
    console.log('Toast Error Message (request):', message, typeof message)
    toast.error(typeof message === 'object' ? JSON.stringify(message) : message)
    return Promise.reject({
      ...error,
      message,
    })
  }
)

// 易经相关 API
export const yijingApi = {
  quickDivination: (question, method = 'time', seed = '') =>
    api.post('/standalone/yijing/quick-divination', { question, method, seed }),

  generateHexagram: (question, method, seed) =>
    api.post('/standalone/yijing/hexagram/generate', { question, method, seed }),

  interpretHexagram: (hexagramId, changingLines, question) =>
    api.post('/standalone/yijing/hexagram/interpret', { hexagramId, changingLines, question }),

  getAllHexagrams: () => api.get('/standalone/yijing/hexagrams'),

  getHexagram: (id) => api.get(`/standalone/yijing/hexagram/${id}`),

  getMethods: () => api.get('/standalone/yijing/methods'),

  aiInterpret: (data) => api.post('/standalone/yijing/hexagram/interpret', data),

  liuYaoDivination: (data) => api.post('/liuyao/divination', data),
}

// 星座相关 API
export const zodiacApi = {
  getInfo: (zodiac) => api.post('/zodiac/info', { zodiac }),

  getDailyHoroscope: (zodiac, date, category = 'overall') =>
    api.post('/zodiac/daily-horoscope', { zodiac, date, category }),

  getCompatibility: (zodiac1, zodiac2, aspect = 'love') =>
    api.post('/zodiac/compatibility', { zodiac1, zodiac2, aspect }),

  getByDate: (month, day) => api.post('/zodiac/by-date', { month, day }),

  getAll: () => api.post('/zodiac/all', {}),
}

// 八字相关 API
export const baziApi = {
  generate: (birthDateTime, isMale = true, qiYunAge = 4) =>
    api.post('/bazi/generate', { birthDateTime, isMale, qiYunAge }),

  convert: (birthDateTime, isMale = true) =>
    api.post('/bazi/convert', { birthDateTime, isMale }),

  analyze: (baZi, birthYear, isMale = true, qiYunAge = 4) =>
    api.post('/bazi/analyze', { baZi, birthYear, isMale, qiYunAge }),

  analyzeSimple: (baZi, isMale = true) =>
    api.post('/bazi/analyze/simple', { baZi, isMale }),

  getInterpretation: (godType, ganzhiPosition) =>
    api.get('/bazi/interpretation/by-type-position', { params: { godType, ganzhiPosition } }),

  getInterpretationsFromBaziData: (baziData) =>
    api.post('/bazi/interpretation/from-bazi-data', baziData),

  getDayun: (baZi, birthYear, isMale = true) =>
    api.post('/bazi/dayun', { baZi, birthYear, isMale }),

  getXingchonghe: (baZi) => api.post('/bazi/xingchonghe', { baZi }),

  getShensha: (baZi) => api.post('/bazi/shensha', { baZi }),

  getXiyongshen: (baZi) => api.post('/bazi/xiyongshen', { baZi }),

  generateReport: (baZi, birthYear, isMale = true, qiYunAge = 4) =>
    api.post('/bazi/generate-report', { baZi, birthYear, isMale, qiYunAge }),

  interpretAspects: (baZi, birthYear, isMale, qiYunAge, aspects) =>
    api.post('/bazi/interpret-aspects', { baZi, birthYear, isMale, qiYunAge, aspects }),
}

// 塔罗牌相关 API
export const tarotApi = {
  getAllCards: () => api.get('/tarot/cards'),

  getCard: (id) => api.get(`/tarot/card/${id}`),

  getCardDetailById: (cardId) =>
    api.get(`/tarot/card/${cardId}`),

  getCardDetail: (cardName) =>
    api.get(`/tarot/card/name/${encodeURIComponent(cardName)}`),

  getSpreads: () => api.get('/tarot/spreads'),

  drawCards: (spreadType, question) =>
    api.post('/tarot/draw', { spreadType, question }),

  interpretSpread: (cards, spreadType, question) =>
    api.post('/tarot/interpret', { cards, spreadType, question }),

  quickDraw: (question) =>
    api.post('/tarot/quick-draw', { question }),

  aiInterpret: (data) => api.post('/tarot/ai-interpret', data),

  getDailyFortune: (cardId) => api.get(`/tarot/daily-fortune/${cardId}`),

  getTodayDraw: () => api.get('/tarot/daily-draw'),

  drawDailyCard: () => api.post('/tarot/daily-draw'),
}

// DeepSeek AI API
export const deepseekApi = {
  generateReport: (prompt) => api.post('/deepseek/generate-report', prompt),

  interpretHexagram: (prompt) => api.post('/deepseek/interpret-hexagram', prompt),

  interpretChart: (prompt) => api.post('/deepseek/chart/deepseek-interpret', prompt),

  getStreamUrl: () => `${API_BASE}/deepseek/reasoning-stream`,
}

export const geminiApi = {
  analyzeFace: (imageBase64, mimeType, prompt = '') =>
    api.post('/gemini/face-analyze', { imageBase64, mimeType, prompt }),
}

// 用户相关 API
export const userApi = {
  register: (username, password, email) =>
    api.post('/user/register', { username, password, email }),

  login: (username, password) =>
    api.post('/user/login', { username, password }),

  getInfo: (token) => api.get('/user/info', {
    headers: { Authorization: `Bearer ${token}` }
  }),
}

// 邀请/推荐相关 API
export const referralApi = {
  getReferralCode: () => api.get('/referral/code'),

  useReferralCode: (referralCode) =>
    api.post('/referral/use', { referralCode }),

  getStats: () => api.get('/referral/stats'),

  getRecords: () => api.get('/referral/records'),
}

// 积分相关 API
export const creditApi = {
  getBalance: () => api.get('/credit/balance'),

  getProducts: () => api.get('/credit/products'),

  exchange: (productCode) =>
    api.post('/credit/exchange', { productCode }),

  getExchangeRecords: () => api.get('/credit/exchange/records'),

  // 消费积分（用于AI解读、进阶牌阵等功能）
  spend: (amount, reason = '功能消费') =>
    api.post('/credit/spend', { amount, reason }),

  // 观看广告获得积分
  earnByWatchingAd: () =>
    api.post('/credit/earn/watch-ad'),
}

// 成就相关 API
export const achievementApi = {
  getAll: () => api.get('/achievement/list'),

  getUserAchievements: () => api.get('/achievement/user'),

  getStats: () => api.get('/achievement/stats'),
}

// 打卡签到相关 API
export const checkinApi = {
  doCheckin: () => api.post('/checkin'),

  getTodayStatus: () => api.get('/checkin/today'),

  getWeeklyProgress: () => api.get('/checkin/weekly'),

  getStreakInfo: () => api.get('/checkin/streak'),
}

// 每日幸运相关 API
export const dailyLuckyApi = {
  getToday: () => api.get('/daily-lucky/today'),

  getByDate: (date) => api.get('/daily-lucky/date', { params: { date } }),

  getFuture: (days = 7) => api.get('/daily-lucky/future', { params: { days } }),

  getByRange: (startDate, endDate) => api.get('/daily-lucky/range', {
    params: { startDate, endDate }
  }),
}

// 社区相关 API
export const communityApi = {
  getPosts: (page = 1, size = 10, category = null, tab = null) =>
    api.get('/community/posts', { params: { page, size, category, tab } }),

  getPostDetail: (id) => api.get(`/community/posts/${id}`),

  createPost: (data) => api.post('/community/posts', data),

  deletePost: (id) => api.delete(`/community/posts/${id}`),

  toggleLike: (targetType, targetId) =>
    api.post('/community/like', { targetType, targetId }),

  toggleFavorite: (postId) =>
    api.post('/community/favorite', { postId }),

  getComments: (postId, page = 1, size = 20) =>
    api.get(`/community/posts/${postId}/comments`, { params: { page, size } }),

  createComment: (data) => api.post('/community/comments', data),

  getTopics: () => api.get('/community/topics'),

  getHotTopics: (limit = 8) => api.get('/community/topics/hot', { params: { limit } }),

  getNotifications: (page = 1, size = 20, type = null) =>
    api.get('/community/notifications', { params: { page, size, type } }),

  markAllNotificationsAsRead: () => api.post('/community/notifications/read-all'),

  markNotificationAsRead: (id) => api.post(`/community/notifications/${id}/read`),
}

// 收藏夹 API
export const favoriteApi = {
  getAll: () => api.get('/favorites'),
  add: (favorite) => api.post('/favorites', favorite),
  remove: (id) => api.delete(`/favorites/${id}`),
  removeBatch: (ids) => api.post('/favorites/delete-batch', { ids }),
  clearAll: () => api.delete('/favorites/clear-all'),
}

// 联系方式记录 API
export const contactRecordApi = {
  // 记录联系方式查看/点击
  record: (data) => api.post('/contact-record', data),
  
  // 获取用户联系记录
  getUserRecords: (page = 1, size = 20) => 
    api.get('/contact-record', { params: { page, size } }),
  
  // 获取联系记录统计
  getStats: () => api.get('/contact-record/stats'),
}

// 测算记录 API
export const calculationRecordApi = {
  save: (record) => api.post('/calculation-records', record),

  getById: (id) => api.get(`/calculation-records/${id}`),

  getAll: (recordType = null, page = 1, size = 20) => {
    const params = { page, size }
    if (recordType) params.recordType = recordType
    return api.get('/calculation-records', { params })
  },

  getByType: (recordType, page = 1, size = 20) =>
    api.get('/calculation-records', { params: { recordType, page, size } }),

  update: (id, record) => api.put(`/calculation-records/${id}`, record),

  delete: (id) => api.delete(`/calculation-records/${id}`),

  getStats: () => api.get('/calculation-records/stats'),
}

export default api
