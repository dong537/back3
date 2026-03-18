/**
 * 输入验证工具
 * 用于前端验证用户输入，防止 XSS 和其他安全问题
 */

/**
 * 验证帖子内容
 */
export const validatePostContent = (content) => {
  if (!content || content.trim().length === 0) {
    return { valid: false, error: '内容不能为空' }
  }
  
  if (content.trim().length < 10) {
    return { valid: false, error: '内容至少10个字' }
  }
  
  if (content.length > 5000) {
    return { valid: false, error: '内容不能超过5000字' }
  }
  
  // 检查是否包含恶意脚本
  if (/<script|javascript:|onerror|onload|onclick|onmouseover/i.test(content)) {
    return { valid: false, error: '内容包含非法字符' }
  }
  
  return { valid: true }
}

/**
 * 验证评论内容
 */
export const validateCommentContent = (content) => {
  if (!content || content.trim().length === 0) {
    return { valid: false, error: '评论不能为空' }
  }
  
  if (content.trim().length < 2) {
    return { valid: false, error: '评论至少2个字' }
  }
  
  if (content.length > 1000) {
    return { valid: false, error: '评论不能超过1000字' }
  }
  
  if (/<script|javascript:|onerror|onload|onclick|onmouseover/i.test(content)) {
    return { valid: false, error: '评论包含非法字符' }
  }
  
  return { valid: true }
}

/**
 * 验证用户名
 */
export const validateUsername = (username) => {
  if (!username || username.trim().length === 0) {
    return { valid: false, error: '用户名不能为空' }
  }
  
  if (username.length < 3) {
    return { valid: false, error: '用户名至少3个字' }
  }
  
  if (username.length > 20) {
    return { valid: false, error: '用户名不能超过20字' }
  }
  
  // 只允许字母、数字、下划线
  if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(username)) {
    return { valid: false, error: '用户名只能包含字母、数字、下划线和中文' }
  }
  
  return { valid: true }
}

/**
 * 验证邮箱
 */
export const validateEmail = (email) => {
  if (!email || email.trim().length === 0) {
    return { valid: false, error: '邮箱不能为空' }
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    return { valid: false, error: '邮箱格式不正确' }
  }
  
  return { valid: true }
}

/**
 * 验证密码
 */
export const validatePassword = (password) => {
  if (!password || password.length === 0) {
    return { valid: false, error: '密码不能为空' }
  }
  
  if (password.length < 6) {
    return { valid: false, error: '密码至少6个字符' }
  }
  
  if (password.length > 50) {
    return { valid: false, error: '密码不能超过50个字符' }
  }
  
  return { valid: true }
}

/**
 * HTML 转义 - 防止 XSS 攻击
 */
export const sanitizeHtml = (html) => {
  const div = document.createElement('div')
  div.textContent = html
  return div.innerHTML
}

/**
 * 清理文本 - 移除危险字符
 */
export const sanitizeText = (text) => {
  if (!text) return ''
  
  // 转义 HTML 特殊字符
  const escaped = sanitizeHtml(text)
  
  // 移除多余空格
  return escaped.trim().replace(/\s+/g, ' ')
}

/**
 * 验证 URL
 */
export const validateUrl = (url) => {
  if (!url || url.trim().length === 0) {
    return { valid: false, error: 'URL 不能为空' }
  }
  
  try {
    new URL(url)
    return { valid: true }
  } catch (e) {
    return { valid: false, error: 'URL 格式不正确' }
  }
}

/**
 * 验证图片 URL
 */
export const validateImageUrl = (url) => {
  const validation = validateUrl(url)
  if (!validation.valid) {
    return validation
  }
  
  // 检查是否是图片格式
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.svg']
  const lowerUrl = url.toLowerCase()
  
  if (!imageExtensions.some(ext => lowerUrl.includes(ext))) {
    return { valid: false, error: '只支持图片格式 (jpg, png, gif, webp, svg)' }
  }
  
  return { valid: true }
}

/**
 * 批量验证
 */
export const validateBatch = (validations) => {
  for (const validation of validations) {
    if (!validation.valid) {
      return validation
    }
  }
  return { valid: true }
}
