import { toast } from '../components/Toast'

/**
 * 暂未上线功能提示
 * @param {string} featureName - 功能名称
 */
export const showComingSoonToast = (featureName) => {
  toast.warning(`${featureName}功能暂未上线，敬请期待！`)
}

/**
 * 处理暂未上线功能的点击事件
 * @param {string} featureName - 功能名称
 * @returns {Function} 事件处理函数
 */
export const handleComingSoon = (featureName) => {
  return (e) => {
    e?.preventDefault?.()
    e?.stopPropagation?.()
    showComingSoonToast(featureName)
  }
}

/**
 * 暂未上线的功能列表
 */
export const COMING_SOON_FEATURES = {
  ZODIAC: '星座运势',
  DAILY_TEST: '每日测试',
  RECORDS: '测算记录',
  COMPATIBILITY: '八字合盘',
  MESSAGES: '私信',
  PUBLISH: '发布动态',
  SETTINGS: '设置',
  CREDIT_SHOP: '积分商城',
}
