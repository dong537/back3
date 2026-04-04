/**
 * SSO 自动登录辅助工具
 *
 * 使用 sessionStorage 标记本次会话是否已尝试过 SSO，
 * 防止在 AgentPit 未登录时陷入无限重定向循环。
 *
 * 逻辑：
 * - 每个浏览器 session 最多尝试一次自动 SSO
 * - SSO 成功登录后清除标记（下次 session 可再次尝试）
 * - SSO 失败后不再重试，用户可手动点击登录
 * - 如果当前路径是 SSO 回调页或登录页，不触发自动 SSO
 */

const SSO_ATTEMPTED_KEY = 'sso_attempted'

/**
 * 判断是否应该自动触发 SSO
 */
export function shouldAutoSso() {
  // SSO 回调页和登录页不触发自动 SSO
  const path = window.location.pathname
  if (path.startsWith('/auth/sso/callback') || path === '/login') {
    return false
  }

  // 如果 URL 中带有 sso_error 参数，说明刚从失败的 SSO 回来
  if (window.location.search.includes('sso_error')) {
    return false
  }

  // 本次 session 已经尝试过 SSO
  if (sessionStorage.getItem(SSO_ATTEMPTED_KEY)) {
    return false
  }

  return true
}

/**
 * 标记本次 session 已尝试过 SSO
 */
export function markSsoAttempted() {
  sessionStorage.setItem(SSO_ATTEMPTED_KEY, '1')
}

/**
 * 清除 SSO 尝试标记（登录成功后调用）
 */
export function clearSsoAttempted() {
  sessionStorage.removeItem(SSO_ATTEMPTED_KEY)
}
