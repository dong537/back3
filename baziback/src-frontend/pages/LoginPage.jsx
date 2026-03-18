import { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { User, Lock, Mail, Eye, EyeOff, LogIn, UserPlus, Sparkles } from 'lucide-react'
import Card, { CardHeader, CardTitle, CardDescription, CardContent } from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import { userApi } from '../api'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const auth = useAuth()
  const [isLogin, setIsLogin] = useState(true)
  
  // 获取来源页面，登录成功后返回
  const from = location.state?.from?.pathname || '/'
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    confirmPassword: '',
  })

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
  }

  // 监听登录状态变化，登录成功后自动导航
  // 使用 ref 来跟踪是否刚刚完成登录操作，避免已登录用户访问登录页时立即跳转
  const [justLoggedIn, setJustLoggedIn] = useState(false)
  
  useEffect(() => {
    // 只有在刚刚完成登录操作且状态已更新时才导航
    if (justLoggedIn && auth.isLoggedIn && auth.user && auth.token) {
      logger.debug('Login state updated, navigating to:', from)
      navigate(from, { replace: true })
      setJustLoggedIn(false) // 重置标志
    }
  }, [justLoggedIn, auth.isLoggedIn, auth.user, auth.token, navigate, from])

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // 基本验证
    if (isLogin) {
      if (!formData.username.trim()) {
        setError('请输入用户名')
        return
      }
      if (!formData.password) {
        setError('请输入密码')
        return
      }
    } else {
      if (formData.password !== formData.confirmPassword) {
        setError('两次密码输入不一致')
        return
      }
    }

    setLoading(true)
    setError('')

    try {
      if (isLogin) {
        // 登录逻辑
        logger.debug('Attempting login with username:', formData.username)
        
        const response = await userApi.login(formData.username, formData.password)
        
        logger.debug('Login response:', {
          status: response.status,
          code: response.data?.code,
          hasData: !!response.data?.data,
          hasUser: !!(response.data?.data?.user),
          hasToken: !!(response.data?.data?.token)
        })
        
        if (response.data?.code === 200) {
          const loginData = response.data.data
          const { user, token } = loginData
          
          // 验证返回的数据
          if (!user || !token) {
            logger.error('Login response missing user or token:', { user: !!user, token: !!token })
            setError('登录响应数据不完整，请重试')
            setLoading(false)
            return
          }
          
          logger.debug('Login successful, updating auth state...', { userId: user.id, username: user.username })
          
          // 更新认证状态
          const loginSuccess = auth.login(user, token)
          
          if (loginSuccess) {
            // 设置标志，触发 useEffect 中的导航
            setJustLoggedIn(true)
          } else {
            setError('登录状态更新失败，请重试')
          }
          
        } else {
          // 登录失败
          const errorMessage = response.data?.message || '登录失败，请检查用户名和密码'
          logger.error('Login failed:', errorMessage)
          setError(errorMessage)
        }
      } else {
        // 注册逻辑
        if (formData.password !== formData.confirmPassword) {
          setError('两次密码输入不一致')
          setLoading(false)
          return
        }
        
        const response = await userApi.register(
          formData.username, 
          formData.password, 
          formData.email
        )
        
        if (response.data?.code === 200) {
          // 注册成功，切换到登录模式
          setIsLogin(true)
          setError('')
          setFormData(prev => ({ ...prev, password: '', confirmPassword: '' }))
          // 使用更友好的提示方式
          setTimeout(() => {
            alert('注册成功！请使用您的账号登录')
          }, 100)
        } else {
          setError(response.data?.message || '注册失败，请稍后重试')
        }
      }
    } catch (err) {
      // 统一错误处理
      logger.error('Auth error:', err)
      
      let errorMessage = '操作失败，请稍后重试'
      
      if (err.isNetworkError) {
        errorMessage = '网络错误，请检查网络连接'
      } else if (err.message) {
        errorMessage = err.message
      } else if (err.response?.data?.message) {
        errorMessage = err.response.data.message
      }
      
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4">
      {/* 背景装饰 */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-pink-500/10 rounded-full blur-3xl" />
      </div>

      <div className="w-full max-w-md relative">
        {/* Logo */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center space-x-3">
            <div className="w-14 h-14 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center">
              <span className="text-white text-2xl">☯</span>
            </div>
          </Link>
          <h1 className="text-3xl font-bold mt-4 bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            天机明理
          </h1>
          <p className="text-gray-400 mt-2">探索命运的奥秘</p>
        </div>

        <Card glow>
          <CardHeader className="text-center">
            <CardTitle>{isLogin ? '欢迎回来' : '创建账号'}</CardTitle>
            <CardDescription>
              {isLogin ? '登录以保存您的占卜记录' : '注册以开启您的玄学之旅'}
            </CardDescription>
          </CardHeader>

          <CardContent>
            {/* 切换标签 */}
            <div className="flex rounded-xl bg-white/5 p-1 mb-6">
              <button
                onClick={() => setIsLogin(true)}
                className={`flex-1 py-2 px-4 rounded-lg text-sm font-medium transition-all ${
                  isLogin
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                <LogIn size={16} className="inline mr-2" />
                登录
              </button>
              <button
                onClick={() => setIsLogin(false)}
                className={`flex-1 py-2 px-4 rounded-lg text-sm font-medium transition-all ${
                  !isLogin
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                <UserPlus size={16} className="inline mr-2" />
                注册
              </button>
            </div>

            {/* 错误提示 */}
            {error && (
              <div className="mb-4 p-3 rounded-lg bg-red-500/20 border border-red-500/50 text-red-300 text-sm">
                {error}
              </div>
            )}

            {/* 表单 */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  name="username"
                  placeholder="用户名"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 transition-colors"
                />
              </div>

              {!isLogin && (
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="email"
                    name="email"
                    placeholder="邮箱地址"
                    value={formData.email}
                    onChange={handleChange}
                    required={!isLogin}
                    className="w-full pl-11 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 transition-colors"
                  />
                </div>
              )}

              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  placeholder="密码"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="w-full pl-11 pr-11 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-white transition-colors"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {!isLogin && (
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    placeholder="确认密码"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required={!isLogin}
                    className="w-full pl-11 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 transition-colors"
                  />
                </div>
              )}

              {isLogin && (
                <div className="flex items-center justify-between text-sm">
                  <label className="flex items-center space-x-2 text-gray-400 cursor-pointer">
                    <input type="checkbox" className="rounded border-gray-600 bg-white/5 text-purple-500 focus:ring-purple-500" />
                    <span>记住我</span>
                  </label>
                  <a href="#" className="text-purple-400 hover:text-purple-300 transition-colors">
                    忘记密码？
                  </a>
                </div>
              )}

              <Button
                type="submit"
                loading={loading}
                className="w-full"
                size="lg"
              >
                <Sparkles size={18} />
                <span>{isLogin ? '登录' : '注册'}</span>
              </Button>
            </form>

            {/* 分隔线 */}
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-white/10"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-[#1a1a2e] text-gray-500">或</span>
              </div>
            </div>

            {/* 游客模式 */}
            <Button
              variant="outline"
              className="w-full"
              onClick={() => navigate('/')}
            >
              <User size={18} />
              <span>游客模式体验</span>
            </Button>
          </CardContent>
        </Card>

        {/* 底部提示 */}
        <p className="text-center text-gray-500 text-sm mt-6">
          继续即表示您同意我们的
          <a href="#" className="text-purple-400 hover:underline mx-1">服务条款</a>
          和
          <a href="#" className="text-purple-400 hover:underline mx-1">隐私政策</a>
        </p>
      </div>
    </div>
  )
}
