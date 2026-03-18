/**
 * SSE客户端工具类
 * 用于建立和管理Server-Sent Events连接
 */

import { logger } from './logger'

class SSEClient {
  constructor() {
    this.connections = new Map() // userId -> EventSource
  }

  /**
   * 建立SSE连接
   * @param {string} endpoint - SSE端点路径，如 '/api/achievement/sse'
   * @param {string} token - 用户token
   * @param {Function} onMessage - 消息处理回调
   * @param {Function} onError - 错误处理回调
   * @returns {EventSource} EventSource实例
   */
  connect(endpoint, token, onMessage, onError) {
    // 关闭已存在的连接
    this.disconnect(endpoint)

    try {
      // 注意：EventSource不支持自定义headers，需要通过URL参数传递token
      const url = `${endpoint}?token=${encodeURIComponent(token)}`
      const eventSource = new EventSource(url)

      eventSource.onopen = () => {
        logger.info(`SSE连接已建立: ${endpoint}`)
      }

      eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (onMessage) {
            onMessage(data, event)
          }
        } catch (error) {
          logger.error('解析SSE消息失败:', error)
        }
      }

      eventSource.onerror = (error) => {
        logger.error(`SSE连接错误 (${endpoint}):`, error)
        if (onError) {
          onError(error)
        }
      }

      // 监听自定义事件
      eventSource.addEventListener('achievement', (event) => {
        try {
          const data = JSON.parse(event.data)
          if (onMessage) {
            onMessage(data, event)
          }
        } catch (error) {
          logger.error('解析成就事件失败:', error)
        }
      })

      eventSource.addEventListener('credit', (event) => {
        try {
          const data = JSON.parse(event.data)
          if (onMessage) {
            onMessage(data, event)
          }
        } catch (error) {
          logger.error('解析积分事件失败:', error)
        }
      })

      eventSource.addEventListener('INIT', (event) => {
        logger.info('SSE初始化事件:', event.data)
      })

      this.connections.set(endpoint, eventSource)
      return eventSource
    } catch (error) {
      logger.error(`建立SSE连接失败 (${endpoint}):`, error)
      if (onError) {
        onError(error)
      }
      return null
    }
  }

  /**
   * 断开SSE连接
   * @param {string} endpoint - SSE端点路径
   */
  disconnect(endpoint) {
    const eventSource = this.connections.get(endpoint)
    if (eventSource) {
      eventSource.close()
      this.connections.delete(endpoint)
      logger.info(`SSE连接已关闭: ${endpoint}`)
    }
  }

  /**
   * 断开所有连接
   */
  disconnectAll() {
    this.connections.forEach((eventSource, endpoint) => {
      eventSource.close()
      logger.info(`SSE连接已关闭: ${endpoint}`)
    })
    this.connections.clear()
  }
}

export default new SSEClient()
