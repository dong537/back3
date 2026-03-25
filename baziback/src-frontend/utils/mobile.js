/**
 * 移动端工具函数
 */

/**
 * 检测是否为移动设备
 */
export const isMobile = () => {
  if (typeof window === 'undefined') return false
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent
  ) || window.innerWidth < 768
}

/**
 * 检测是否为iOS设备
 */
export const isIOS = () => {
  if (typeof window === 'undefined') return false
  return /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
}

/**
 * 检测是否为Android设备
 */
export const isAndroid = () => {
  if (typeof window === 'undefined') return false
  return /Android/.test(navigator.userAgent)
}

/**
 * 获取安全区域inset值
 */
export const getSafeAreaInsets = () => {
  if (typeof window === 'undefined') return { top: 0, bottom: 0, left: 0, right: 0 }
  
  const style = getComputedStyle(document.documentElement)
  return {
    top: parseInt(style.getPropertyValue('env(safe-area-inset-top)') || '0', 10),
    bottom: parseInt(style.getPropertyValue('env(safe-area-inset-bottom)') || '0', 10),
    left: parseInt(style.getPropertyValue('env(safe-area-inset-left)') || '0', 10),
    right: parseInt(style.getPropertyValue('env(safe-area-inset-right)') || '0', 10),
  }
}

/**
 * 防止iOS双击缩放
 */
export const preventDoubleTapZoom = () => {
  if (typeof window === 'undefined' || !isIOS()) return
  
  let lastTouchEnd = 0
  document.addEventListener('touchend', (event) => {
    const now = Date.now()
    if (now - lastTouchEnd <= 300) {
      event.preventDefault()
    }
    lastTouchEnd = now
  }, false)
}

/**
 * 设置视口高度（解决移动端100vh问题）
 */
export const setViewportHeight = () => {
  if (typeof window === 'undefined') return
  
  const setVH = () => {
    const vh = window.innerHeight * 0.01
    document.documentElement.style.setProperty('--vh', `${vh}px`)
  }
  
  setVH()
  window.addEventListener('resize', setVH)
  window.addEventListener('orientationchange', setVH)
  
  return () => {
    window.removeEventListener('resize', setVH)
    window.removeEventListener('orientationchange', setVH)
  }
}

/**
 * 震动反馈（如果支持）
 */
export const vibrate = (pattern = [10]) => {
  if (typeof navigator === 'undefined' || !navigator.vibrate) return false
  
  try {
    navigator.vibrate(pattern)
    return true
  } catch (e) {
    return false
  }
}

/**
 * 触摸反馈（短震动）
 */
export const hapticFeedback = () => {
  if (isIOS()) {
    // iOS使用触觉反馈
    vibrate([10])
  } else if (isAndroid()) {
    // Android使用震动
    vibrate([10])
  }
}
