/**
 * SSE连接Hook
 * 用于在应用级别建立和管理SSE连接
 */

import { useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import sseClient from '../utils/sseClient'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'

/**
 * 使用SSE连接Hook
 * 在应用级别建立SSE连接，监听成就解锁和积分变化事件
 */
export function useSSE() {
  const { isLoggedIn, token, refreshCredits } = useAuth()
  const reconnectTimeoutRef = useRef(null)

  useEffect(() => {
    if (!isLoggedIn || !token) {
      // 未登录时，断开所有SSE连接
      sseClient.disconnectAll()
      return
    }

    const currentToken = sessionStorage.getItem('token')
    if (!currentToken) {
      logger.warn('SSE连接失败: 未找到token')
      return
    }

    // 建立成就SSE连接
    const achievementEndpoint = '/api/achievement/sse'
    sseClient.connect(
      achievementEndpoint,
      currentToken,
      (data, event) => {
        logger.info('收到SSE事件:', { data, eventType: event.type })
        
        if (data.type === 'ACHIEVEMENT_UNLOCKED') {
          // 显示成就解锁提示
          toast.success(`🎉 解锁成就：${data.achievementName}！获得 ${data.pointsReward} 积分`)
          
          // 刷新积分余额
          refreshCredits()
          
          // 触发自定义事件，通知其他组件刷新数据
          window.dispatchEvent(new CustomEvent('achievement-unlocked', { detail: data }))
        }
      },
      (error) => {
        logger.error('成就SSE连接错误:', error)
        // 连接断开后，尝试重新连接（延迟5秒）
        if (reconnectTimeoutRef.current) {
          clearTimeout(reconnectTimeoutRef.current)
        }
        reconnectTimeoutRef.current = setTimeout(() => {
          if (isLoggedIn && sessionStorage.getItem('token')) {
            logger.info('尝试重新建立成就SSE连接...')
            // 重新建立连接（通过重新调用connect）
            const currentToken = sessionStorage.getItem('token')
            if (currentToken) {
              sseClient.connect(
                achievementEndpoint,
                currentToken,
                (data, event) => {
                  logger.info('收到SSE事件:', { data, eventType: event.type })
                  if (data.type === 'ACHIEVEMENT_UNLOCKED') {
                    toast.success(`🎉 解锁成就：${data.achievementName}！获得 ${data.pointsReward} 积分`)
                    refreshCredits()
                    window.dispatchEvent(new CustomEvent('achievement-unlocked', { detail: data }))
                  }
                },
                (error) => {
                  logger.error('成就SSE重连失败:', error)
                }
              )
            }
          }
        }, 5000)
      }
    )

    // 建立积分SSE连接（如果需要）
    const creditEndpoint = '/api/credit/sse'
    sseClient.connect(
      creditEndpoint,
      currentToken,
      (data, event) => {
        logger.info('收到积分SSE事件:', { data, eventType: event.type })
        
        if (data.type === 'CREDIT_CHANGED' || data.type === 'CREDIT_UPDATED') {
          // 刷新积分余额
          refreshCredits()
        }
      },
      (error) => {
        logger.error('积分SSE连接错误:', error)
      }
    )

    // 清理函数
    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current)
      }
      sseClient.disconnect(achievementEndpoint)
      sseClient.disconnect(creditEndpoint)
    }
  }, [isLoggedIn, token, refreshCredits])
}
