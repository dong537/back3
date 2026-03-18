/**
 * 日志工具类
 * 生产环境自动禁用 console.log，保留 console.error
 */

const isDev = import.meta.env.DEV;
const isProduction = import.meta.env.PROD;

/**
 * 日志级别
 */
const LogLevel = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3
};

// 从环境变量读取日志级别
const logLevel = isDev ? LogLevel.DEBUG : LogLevel.ERROR;

/**
 * 日志工具
 */
export const logger = {
  /**
   * 调试日志（仅开发环境）
   */
  debug: (...args) => {
    if (logLevel <= LogLevel.DEBUG) {
      console.debug('[DEBUG]', ...args);
    }
  },

  /**
   * 信息日志（仅开发环境）
   */
  log: (...args) => {
    if (logLevel <= LogLevel.INFO) {
      console.log('[INFO]', ...args);
    }
  },

  /**
   * 信息日志（仅开发环境）
   */
  info: (...args) => {
    if (logLevel <= LogLevel.INFO) {
      console.info('[INFO]', ...args);
    }
  },

  /**
   * 警告日志（仅开发环境）
   */
  warn: (...args) => {
    if (logLevel <= LogLevel.WARN) {
      console.warn('[WARN]', ...args);
    }
  },

  /**
   * 错误日志（始终输出）
   */
  error: (...args) => {
    console.error('[ERROR]', ...args);
    
    // 生产环境可以发送到错误监控服务
    if (isProduction) {
      // TODO: 发送到错误监控服务（如 Sentry）
      // errorTrackingService.captureException(...args);
    }
  }
};

export default logger;
