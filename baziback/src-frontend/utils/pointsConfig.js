/**
 * 积分消费配置
 */

// 功能消费积分配置
export const POINTS_COST = {
  AI_INTERPRET: 0,       // AI解读临时免费
  AI_CHAT: 0,            // AI对话临时免费
  AI_FACE_ANALYZE: 0,    // Gemini 人脸分析临时免费
  TAROT_DRAW: 0,         // 塔罗抽牌免费（入门牌阵）
  TAROT_ADVANCED: 0,     // 塔罗进阶牌阵临时免费
  YIJING_DIVINE: 0,      // 易经起卦免费
  BAZI_ANALYZE: 0,       // 八字排盘免费
}

// 积分获取配置
export const POINTS_EARN = {
  DAILY_CHECKIN: 10,           // 每日签到
  CHECKIN_STREAK_3: 20,        // 连续签到3天
  CHECKIN_STREAK_7: 30,        // 连续签到7天
  FIRST_DIVINATION: 20,        // 首次占卜
  INVITE_FRIEND: 50,           // 邀请好友
  SHARE_RESULT: 5,             // 分享结果
  REGISTER: 100,               // 新用户注册
  WATCH_AD: 10,                // 观看广告
}

// 积分消费记录类型
export const POINTS_TYPE = {
  EARN: 'earn',
  SPEND: 'spend',
}
