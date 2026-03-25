import { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import {
  User,
  Lock,
  Mail,
  Eye,
  EyeOff,
  LogIn,
  UserPlus,
  Sparkles,
  ShieldCheck,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, {
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from '../components/Card'
import Button from '../components/Button'
import { userApi, unwrapApiData } from '../api'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import { resolvePageLocale } from '../utils/displayText'

const LOGIN_COPY = {
  'zh-CN': {
    brand: '天机命理',
    tagline: '探索命运的奥秘',
    welcomeBack: '欢迎回来',
    createAccount: '创建账号',
    loginDesc: '登录以保存您的占卜记录',
    registerDesc: '注册以开启您的玄学之旅',
    loginTab: '登录',
    registerTab: '注册',
    username: '用户名',
    email: '邮箱地址',
    password: '密码',
    confirmPassword: '确认密码',
    rememberMe: '记住我',
    forgotPassword: '忘记密码？',
    submitLogin: '登录',
    submitRegister: '注册',
    guestMode: '游客模式体验',
    divider: '或',
    agreementPrefix: '继续即表示您同意我们的',
    terms: '服务条款',
    privacy: '隐私政策',
    loginSuccessNavigate: '登录状态更新完成，准备跳转',
    missingUsername: '请输入用户名',
    missingPassword: '请输入密码',
    passwordMismatch: '两次密码输入不一致',
    incompleteLoginData: '登录响应数据不完整，请重试',
    loginStateUpdateFailed: '登录状态更新失败，请重试',
    loginFailedDefault: '登录失败，请检查用户名和密码',
    registerSuccess: '注册成功，请使用您的账号登录',
    registerFailedDefault: '注册失败，请稍后重试',
    actionFailed: '操作失败，请稍后重试',
    networkError: '网络错误，请检查网络连接',
  },
  'en-US': {
    brand: 'Tianji Destiny',
    tagline: 'Explore the mystery of destiny',
    welcomeBack: 'Welcome back',
    createAccount: 'Create account',
    loginDesc: 'Sign in to save your divination records',
    registerDesc: 'Register to begin your mystic journey',
    loginTab: 'Sign in',
    registerTab: 'Register',
    username: 'Username',
    email: 'Email address',
    password: 'Password',
    confirmPassword: 'Confirm password',
    rememberMe: 'Remember me',
    forgotPassword: 'Forgot password?',
    submitLogin: 'Sign in',
    submitRegister: 'Register',
    guestMode: 'Continue as guest',
    divider: 'or',
    agreementPrefix: 'By continuing, you agree to our',
    terms: 'Terms of Service',
    privacy: 'Privacy Policy',
    loginSuccessNavigate: 'Login state updated, navigating',
    missingUsername: 'Please enter your username',
    missingPassword: 'Please enter your password',
    passwordMismatch: 'The two passwords do not match',
    incompleteLoginData: 'Login response is incomplete. Please try again.',
    loginStateUpdateFailed: 'Failed to update login state. Please try again.',
    loginFailedDefault:
      'Sign in failed. Please check your username and password.',
    registerSuccess:
      'Registration succeeded. Please sign in with your account.',
    registerFailedDefault: 'Registration failed. Please try again later.',
    actionFailed: 'Action failed. Please try again later.',
    networkError: 'Network error. Please check your connection.',
  },
}

const AGENTPIT_OAUTH_EVENT = 'agentpit-oauth-result'
const AGENTPIT_BUTTON_LABEL = 'agentpit 授权登陆'
const AGENTPIT_POPUP_ERROR = '无法打开授权窗口，请允许浏览器弹窗后重试'
const AGENTPIT_DEFAULT_ERROR = 'agentpit 授权失败，请稍后重试'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const auth = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = LOGIN_COPY[locale]
  const [isLogin, setIsLogin] = useState(true)
  const from = location.state?.from?.pathname || '/'
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [justLoggedIn, setJustLoggedIn] = useState(false)
  const [oauthLoading, setOauthLoading] = useState(false)

  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    confirmPassword: '',
  })

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  useEffect(() => {
    if (justLoggedIn && auth.isLoggedIn && auth.user && auth.token) {
      logger.debug(copy.loginSuccessNavigate, from)
      navigate(from, { replace: true })
      setJustLoggedIn(false)
      setOauthLoading(false)
    }
  }, [
    justLoggedIn,
    auth.isLoggedIn,
    auth.user,
    auth.token,
    navigate,
    from,
    copy,
  ])

  useEffect(() => {
    const handleAgentpitResult = (payload) => {
      if (!payload || payload.type !== AGENTPIT_OAUTH_EVENT) {
        return
      }

      setOauthLoading(false)

      if (!payload.success) {
        setError(payload.message || AGENTPIT_DEFAULT_ERROR)
        return
      }

      const loginPayload = payload.data
      const user = loginPayload?.user
      const token = loginPayload?.token

      if (!user || !token) {
        setError(AGENTPIT_DEFAULT_ERROR)
        return
      }

      const loginSuccess = auth.login(user, token)
      if (loginSuccess) {
        setJustLoggedIn(true)
      } else {
        setError(copy.loginStateUpdateFailed)
      }
    }

    const onMessage = (event) => {
      if (event.origin !== window.location.origin) {
        return
      }
      handleAgentpitResult(event.data)
    }

    const onStorage = (event) => {
      if (event.key !== AGENTPIT_OAUTH_EVENT || !event.newValue) {
        return
      }

      try {
        handleAgentpitResult(JSON.parse(event.newValue))
      } catch (storageError) {
        logger.error('Failed to parse AgentPit OAuth storage payload', storageError)
      }
    }

    window.addEventListener('message', onMessage)
    window.addEventListener('storage', onStorage)

    return () => {
      window.removeEventListener('message', onMessage)
      window.removeEventListener('storage', onStorage)
    }
  }, [auth, copy.loginStateUpdateFailed])

  const handleAgentpitOauth = () => {
    setError('')
    setOauthLoading(true)

    const width = 520
    const height = 720
    const left = window.screenX + (window.outerWidth - width) / 2
    const top = window.screenY + (window.outerHeight - height) / 2
    const popup = window.open(
      '/api/auth/agentpit',
      'agentpit-oauth',
      `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
    )

    if (!popup) {
      setOauthLoading(false)
      setError(AGENTPIT_POPUP_ERROR)
      return
    }

    popup.focus()
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (isLogin) {
      if (!formData.username.trim()) {
        setError(copy.missingUsername)
        return
      }
      if (!formData.password) {
        setError(copy.missingPassword)
        return
      }
    } else if (formData.password !== formData.confirmPassword) {
      setError(copy.passwordMismatch)
      return
    }

    setLoading(true)
    setError('')

    try {
      if (isLogin) {
        logger.debug('Attempting login with username:', formData.username)

        const response = await userApi.login(
          formData.username,
          formData.password
        )
        const loginData = unwrapApiData(response)

        logger.debug('Login response:', {
          status: response.status,
          code: response.data?.code,
          hasUser: !!loginData?.user,
          hasToken: !!loginData?.token,
        })

        if (response.data?.code === 200) {
          const { user, token } = loginData

          if (!user || !token) {
            logger.error('Login response missing user or token:', {
              user: !!user,
              token: !!token,
            })
            setError(copy.incompleteLoginData)
            setLoading(false)
            return
          }

          logger.debug('Login successful, updating auth state...', {
            userId: user.id,
            username: user.username,
          })

          const loginSuccess = auth.login(user, token)

          if (loginSuccess) {
            setJustLoggedIn(true)
          } else {
            setError(copy.loginStateUpdateFailed)
          }
        } else {
          const errorMessage = response.data?.message || copy.loginFailedDefault
          logger.error('Login failed:', errorMessage)
          setError(errorMessage)
        }
      } else {
        const response = await userApi.register(
          formData.username,
          formData.password,
          formData.email
        )

        if (response.data?.code === 200) {
          setIsLogin(true)
          setError('')
          setFormData((prev) => ({
            ...prev,
            password: '',
            confirmPassword: '',
          }))
          window.setTimeout(() => {
            window.alert(copy.registerSuccess)
          }, 100)
        } else {
          setError(response.data?.message || copy.registerFailedDefault)
        }
      }
    } catch (err) {
      logger.error('Auth error:', err)

      let errorMessage = copy.actionFailed

      if (err.isNetworkError) {
        errorMessage = copy.networkError
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
    <div className="page-shell flex min-h-screen items-center justify-center">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-1/4 top-1/4 h-96 w-96 rounded-full bg-[#a34224]/12 blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 h-96 w-96 rounded-full bg-[#d0a85b]/10 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="mb-8 text-center">
          <Link to="/" className="inline-flex items-center space-x-3">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]">
              <span className="text-2xl text-white">☯</span>
            </div>
          </Link>
          <h1 className="mt-4 bg-[linear-gradient(135deg,#f6e7cf_0%,#dcb86f_52%,#e19a84_100%)] bg-clip-text text-3xl font-bold text-transparent">
            {copy.brand}
          </h1>
          <p className="mt-2 text-[#8f7b66]">{copy.tagline}</p>
        </div>

        <Card glow>
          <CardHeader className="text-center">
            <CardTitle>
              {isLogin ? copy.welcomeBack : copy.createAccount}
            </CardTitle>
            <CardDescription>
              {isLogin ? copy.loginDesc : copy.registerDesc}
            </CardDescription>
          </CardHeader>

          <CardContent>
            <div className="mb-6 flex rounded-xl border border-white/10 bg-white/[0.04] p-1">
              <button
                onClick={() => setIsLogin(true)}
                className={`flex-1 rounded-lg px-4 py-2 text-sm font-medium transition-all ${
                  isLogin
                    ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-[0_14px_32px_rgba(163,66,36,0.2)]'
                    : 'text-[#8f7b66] hover:text-[#f4ece1]'
                }`}
              >
                <LogIn size={16} className="mr-2 inline" />
                {copy.loginTab}
              </button>
              <button
                onClick={() => setIsLogin(false)}
                className={`flex-1 rounded-lg px-4 py-2 text-sm font-medium transition-all ${
                  !isLogin
                    ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-[0_14px_32px_rgba(163,66,36,0.2)]'
                    : 'text-[#8f7b66] hover:text-[#f4ece1]'
                }`}
              >
                <UserPlus size={16} className="mr-2 inline" />
                {copy.registerTab}
              </button>
            </div>

            {error && (
              <div className="mb-4 rounded-lg border border-[#a34224]/35 bg-[#7a3218]/18 p-3 text-sm text-[#f0b2a2]">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <Field
                icon={User}
                type="text"
                name="username"
                placeholder={copy.username}
                value={formData.username}
                onChange={handleChange}
                required
              />

              {!isLogin && (
                <Field
                  icon={Mail}
                  type="email"
                  name="email"
                  placeholder={copy.email}
                  value={formData.email}
                  onChange={handleChange}
                  required={!isLogin}
                />
              )}

              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-[#8f7b66]" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  placeholder={copy.password}
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="mystic-input w-full py-3 pl-11 pr-11 text-[#f4ece1] placeholder-[#8f7b66]"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#8f7b66] transition-colors hover:text-[#f4ece1]"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {!isLogin && (
                <Field
                  icon={Lock}
                  type={showPassword ? 'text' : 'password'}
                  name="confirmPassword"
                  placeholder={copy.confirmPassword}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required={!isLogin}
                />
              )}

              {isLogin && (
                <div className="flex items-center justify-between text-sm">
                  <label className="flex cursor-pointer items-center space-x-2 text-[#8f7b66]">
                    <input
                      type="checkbox"
                      className="rounded border-white/10 bg-white/[0.04] text-[#d0a85b] focus:ring-[#a34224]"
                    />
                    <span>{copy.rememberMe}</span>
                  </label>
                  <a
                    href="#"
                    className="text-[#dcb86f] transition-colors hover:text-[#f0d9a5]"
                  >
                    {copy.forgotPassword}
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
                <span>{isLogin ? copy.submitLogin : copy.submitRegister}</span>
              </Button>

              {isLogin && (
                <Button
                  type="button"
                  variant="secondary"
                  loading={oauthLoading}
                  className="w-full"
                  size="lg"
                  onClick={handleAgentpitOauth}
                >
                  <ShieldCheck size={18} />
                  <span>{AGENTPIT_BUTTON_LABEL}</span>
                </Button>
              )}
            </form>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-white/10"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="bg-[#100b0a] px-4 text-[#8f7b66]">
                  {copy.divider}
                </span>
              </div>
            </div>

            <Button
              variant="outline"
              className="w-full"
              onClick={() => navigate('/')}
            >
              <User size={18} />
              <span>{copy.guestMode}</span>
            </Button>
          </CardContent>
        </Card>

        <p className="mt-6 text-center text-sm text-[#8f7b66]">
          {copy.agreementPrefix}
          <a href="#" className="mx-1 text-[#dcb86f] hover:underline">
            {copy.terms}
          </a>
          &amp;
          <a href="#" className="mx-1 text-[#dcb86f] hover:underline">
            {copy.privacy}
          </a>
        </p>
      </div>
    </div>
  )
}

function Field({
  icon: Icon,
  type,
  name,
  placeholder,
  value,
  onChange,
  required,
}) {
  return (
    <div className="relative">
      <Icon className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-[#8f7b66]" />
      <input
        type={type}
        name={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
        className="mystic-input w-full py-3 pl-11 pr-4 text-[#f4ece1] placeholder-[#8f7b66]"
      />
    </div>
  )
}
