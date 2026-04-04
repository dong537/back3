import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'

/**
 * SSO 回调页：从 URL hash 中提取 token 和 user 信息，完成自动登录后跳转
 * URL 格式: /auth/sso/callback#token=xxx&user=xxx&returnUrl=xxx
 */
export default function SsoCallbackPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const processed = useRef(false)

  useEffect(() => {
    if (processed.current) return
    processed.current = true

    const hash = window.location.hash.substring(1) // 去掉 #
    const params = new URLSearchParams(hash)
    const token = params.get('token')
    const userStr = params.get('user')
    const returnUrl = params.get('returnUrl') || '/'

    if (!token || !userStr) {
      logger.error('SSO callback: missing token or user in URL hash')
      navigate('/login', { replace: true })
      return
    }

    try {
      const user = JSON.parse(decodeURIComponent(userStr))
      const decodedToken = decodeURIComponent(token)

      const loginSuccess = auth.login(user, decodedToken)
      if (loginSuccess) {
        logger.debug('SSO login successful, redirecting to:', returnUrl)
        // 清除 URL 中的敏感信息
        window.history.replaceState(null, '', '/auth/sso/callback')
        navigate(decodeURIComponent(returnUrl), { replace: true })
      } else {
        logger.error('SSO login failed: auth.login returned false')
        navigate('/login', { replace: true })
      }
    } catch (err) {
      logger.error('SSO callback parse error:', err)
      navigate('/login', { replace: true })
    }
  }, [auth, navigate])

  return (
    <div className="page-shell flex min-h-screen items-center justify-center">
      <div className="text-center">
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-[24px] bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] shadow-[0_18px_40px_rgba(163,66,36,0.24)]">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-white/25 border-t-[#fff7eb]" />
        </div>
        <p className="text-sm text-[#bdaa94]">正在完成登录...</p>
      </div>
    </div>
  )
}
