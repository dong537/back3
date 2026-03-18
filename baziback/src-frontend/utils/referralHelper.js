/**
 * 推荐系统辅助函数
 * 用于在应用启动时检测推荐码
 */

import { setReferrer, recordInvite, points } from './referral'

/**
 * 检测URL中的推荐码
 */
export function checkReferralCode() {
  const urlParams = new URLSearchParams(window.location.search)
  const refCode = urlParams.get('ref')
  
  if (refCode) {
    // 设置推荐人
    const isNew = setReferrer(refCode)
    if (isNew) {
      // 记录邀请
      recordInvite(refCode)
      
      // 给新用户奖励（首次通过推荐码访问）
      const hasRewarded = localStorage.getItem('referral_rewarded')
      if (!hasRewarded) {
        points.add(20, '通过推荐码注册')
        localStorage.setItem('referral_rewarded', 'true')
      }
      
      // 清理URL参数
      const newUrl = window.location.pathname + window.location.search.replace(/[?&]ref=[^&]*/, '').replace(/^\?$/, '')
      window.history.replaceState({}, '', newUrl)
      
      return { success: true, code: refCode }
    }
  }
  
  return { success: false }
}

/**
 * 检测推荐人是否完成首次占卜
 */
export function checkReferrerFirstDivination() {
  const referrerCode = localStorage.getItem('user_referred_by')
  if (referrerCode) {
    // 这里应该通知后端，给推荐人奖励
    // 暂时在本地标记
    const hasNotified = localStorage.getItem(`referrer_notified_${referrerCode}`)
    if (!hasNotified) {
      localStorage.setItem(`referrer_notified_${referrerCode}`, 'true')
      // 实际应该调用后端API通知推荐人获得奖励
      return true
    }
  }
  return false
}
