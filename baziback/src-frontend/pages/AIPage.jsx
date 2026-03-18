import React, { useState, useRef, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Brain, Send, Sparkles, Trash2, StopCircle, ArrowLeft, Coins } from 'lucide-react'
import { logger } from '../utils/logger'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'

export default function AIPage() {
  const navigate = useNavigate()
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()
  const [input, setInput] = useState('')
  const [messages, setMessages] = useState([])
  const [isStreaming, setIsStreaming] = useState(false)
  const [currentThinking, setCurrentThinking] = useState('')
  const [currentResponse, setCurrentResponse] = useState('')
  const [userPoints, setUserPoints] = useState(points.get())
  const messagesEndRef = useRef(null)
  const inputRef = useRef(null)
  const abortControllerRef = useRef(null)
  const scrollTimeoutRef = useRef(null)

  // 页面加载时同步一次后端积分余额
  useEffect(() => {
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  // 优化滚动：使用 requestAnimationFrame 而不是频繁调用
  const scrollToBottom = useCallback(() => {
    if (scrollTimeoutRef.current) {
      cancelAnimationFrame(scrollTimeoutRef.current)
    }
    scrollTimeoutRef.current = requestAnimationFrame(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'auto' })
    })
  }, [])

  // 只在消息列表变化时滚动，不在 currentThinking/currentResponse 变化时滚动
  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  const handleStop = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
      abortControllerRef.current = null
    }
  }, [])

  const handleSend = useCallback(async () => {
    if (!input.trim() || isStreaming) return

    // 检查积分
    const cost = POINTS_COST.AI_CHAT
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(`积分不足，AI对话需要 ${cost} 积分，当前余额：${credits}`)
        return
      }
    } else {
      if (!points.canSpend(cost)) {
        toast.error(`积分不足，AI对话需要 ${cost} 积分`)
        return
      }
    }

    const userMessage = input.trim()
    setInput('')
    setMessages(prev => [...prev, { role: 'user', content: userMessage }])
    setIsStreaming(true)
    setCurrentThinking('')
    setCurrentResponse('')

    const formattedMessage = `【用户问题】
${userMessage}

【重要格式要求】
仅输出纯文本，不使用任何 Markdown 或富文本格式。
禁止出现以下符号：井号、星号、反引号、下划线、波浪号、大于号、小于号、方括号、圆括号内的链接格式、竖线，以及以连字符作为项目符号的列表。
使用自然段落和换行来组织内容，用数字序号（如1、2、3）代替符号列表。

请用专业、友好的语气回答用户的问题。`

    try {
      // ✅ 改用普通 HTTP 请求（不使用流式）
      const token = sessionStorage.getItem('token')
      logger.info('开始发送 AI 对话请求...', { hasToken: !!token })
      
      const response = await fetch('/api/deepseek/reasoning', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        body: JSON.stringify({ message: formattedMessage }),
      })

      if (!response.ok) {
        const errorText = await response.text()
        logger.error(`HTTP ${response.status}: ${response.statusText}`, { errorText })
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      logger.info('收到 AI 响应', { success: data.success, contentLength: data.content?.length })

      if (data.success && data.content) {
        // 扣除积分
        if (isLoggedIn) {
          const spendResult = await spendCredits(cost, 'AI智能对话')
          if (spendResult.success) {
            toast.success(`消耗 ${cost} 积分`)
          } else {
            toast.error(spendResult.message || '积分扣除失败')
          }
        } else {
          const spendResult = points.spend(cost, 'AI智能对话')
          if (spendResult.success) {
            setUserPoints(spendResult.newTotal)
            toast.success(`消耗 ${cost} 积分`)
          }
        }
        
        setMessages(prev => [...prev, { 
          role: 'assistant', 
          content: data.content,
          thinking: ''
        }])
      } else {
        throw new Error(data.message || '请求失败')
      }
    } catch (error) {
      logger.error('AI 对话错误:', error)
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: error.message || '请求失败，请稍后重试',
        thinking: ''
      }])
    } finally {
      setIsStreaming(false)
      setCurrentThinking('')
      setCurrentResponse('')
    }
  }, [input, isStreaming, isLoggedIn, credits, canSpendCredits, spendCredits])

  const clearChat = () => {
    setMessages([])
    setCurrentThinking('')
    setCurrentResponse('')
  }

  const quickQuestions = ['帮我分析一下八字', '解读一下今天的运势', '塔罗牌占卜感情']

  return (
    <div className="min-h-screen bg-gradient-to-b from-purple-50 to-white flex flex-col">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-200 safe-area-top">
        <div className="px-4 py-3 flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <h1 className="text-lg font-bold text-gray-800">AI智能对话</h1>
          <div className="flex items-center space-x-2">
            <div className="flex items-center space-x-1 bg-amber-100 px-3 py-1 rounded-full">
              <Coins size={16} className="text-amber-600" />
              <span className="text-sm font-bold text-amber-700">
                {isLoggedIn ? (credits ?? 0) : userPoints}
              </span>
            </div>
            <button 
              onClick={clearChat}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <Trash2 size={20} className="text-gray-500" />
            </button>
          </div>
        </div>
      </div>

      {/* 消息列表区域 - 可滚动 */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {messages.length === 0 && !isStreaming ? (
          <div className="flex flex-col items-center justify-center h-full min-h-[400px]">
            <div className="w-20 h-20 mb-4 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-full flex items-center justify-center shadow-lg">
              <Brain className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">AI智能助手</h2>
            <p className="text-gray-500 text-sm mb-2">可以询问八字、卦象、塔罗等玄学问题</p>
            <p className="text-amber-600 text-xs mb-6">每次对话消耗 {POINTS_COST.AI_CHAT} 积分</p>
            
            {/* 快捷问题 */}
            <div className="w-full max-w-sm space-y-2">
              {quickQuestions.map((q, i) => (
                <button
                  key={i}
                  onClick={() => setInput(q)}
                  className="block w-full px-4 py-3 bg-white rounded-xl border border-gray-200 text-sm text-gray-700 hover:bg-purple-50 hover:border-purple-300 transition-colors text-left"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="space-y-4 pb-4">
            {messages.map((msg, i) => (
              <MessageItem key={i} msg={msg} />
            ))}

            {/* 正在流式输出 */}
            {isStreaming && (
              <StreamingMessage 
                currentThinking={currentThinking} 
                currentResponse={currentResponse} 
              />
            )}

            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* 输入区域 - 固定在底部 */}
      <div className="sticky bottom-0 border-t border-gray-200 bg-white p-4 safe-area-bottom">
        {/* 积分提示 */}
        <div className="flex items-center justify-between mb-2 px-1">
          <span className="text-xs text-gray-500">
            每次对话消耗 <span className="text-amber-600 font-medium">{POINTS_COST.AI_CHAT}</span> 积分
          </span>
          <button 
            onClick={() => navigate('/dashboard')}
            className="text-xs text-purple-600 font-medium"
          >
            获取更多积分 →
          </button>
        </div>
        <div className="flex gap-3 items-end max-w-4xl mx-auto">
          <div className="flex-1 relative">
            <textarea
              ref={inputRef}
              placeholder="输入您的问题..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              rows={1}
              className="w-full px-4 py-3 bg-gray-100 border-0 rounded-2xl text-sm text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-400 resize-none"
              style={{ minHeight: '48px', maxHeight: '120px' }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  handleSend()
                }
              }}
              onInput={(e) => {
                e.target.style.height = 'auto'
                e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px'
              }}
              disabled={isStreaming}
            />
          </div>
          
          {isStreaming ? (
            <button 
              onClick={handleStop}
              className="w-12 h-12 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-full transition-colors shadow-lg"
            >
              <StopCircle size={22} />
            </button>
          ) : (
            <button 
              onClick={handleSend} 
              disabled={!input.trim()}
              className={`w-12 h-12 flex items-center justify-center rounded-full transition-all shadow-lg ${
                input.trim() 
                  ? 'bg-gradient-to-r from-purple-500 to-indigo-500 text-white hover:from-purple-600 hover:to-indigo-600' 
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'
              }`}
            >
              <Send size={20} />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

// 优化的消息项组件 - 使用 React.memo 避免不必要的重新渲染
const MessageItem = React.memo(({ msg }) => (
  <div className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
    <div className="max-w-[85%]">
      {msg.role === 'assistant' && msg.thinking && (
        <details className="mb-2 bg-purple-50 rounded-xl overflow-hidden border border-purple-200">
          <summary className="px-4 py-2 cursor-pointer text-sm text-purple-600 hover:bg-purple-100 transition-colors">
            <Brain className="w-4 h-4 inline mr-2" />
            查看思维过程
          </summary>
          <div className="px-4 py-3 bg-purple-50 text-sm text-gray-600 whitespace-pre-wrap max-h-48 overflow-y-auto border-t border-purple-200">
            {msg.thinking}
          </div>
        </details>
      )}
      <div className={`rounded-2xl px-4 py-3 ${
        msg.role === 'user' 
          ? 'bg-gradient-to-r from-purple-500 to-indigo-500 text-white' 
          : 'bg-white border border-gray-200 text-gray-800 shadow-sm'
      }`}>
        <div className="whitespace-pre-wrap leading-relaxed text-sm">{msg.content}</div>
      </div>
    </div>
  </div>
))

MessageItem.displayName = 'MessageItem'

// 优化的流式消息组件
const StreamingMessage = React.memo(({ currentThinking, currentResponse }) => (
  <div className="flex justify-start">
    <div className="max-w-[85%]">
      {currentThinking && (
        <div className="mb-2 bg-purple-50 rounded-xl p-3 border border-purple-200">
          <div className="flex items-center text-sm text-purple-600 mb-2">
            <Brain className="w-4 h-4 mr-2 animate-pulse" />
            思考中...
          </div>
          <div className="text-sm text-gray-600 whitespace-pre-wrap max-h-32 overflow-y-auto">
            {currentThinking}
          </div>
        </div>
      )}
      {currentResponse && (
        <div className="bg-white rounded-2xl px-4 py-3 border border-gray-200 shadow-sm">
          <div className="whitespace-pre-wrap leading-relaxed text-sm text-gray-800">
            {currentResponse}
            <span className="inline-block w-1 h-4 bg-purple-500 ml-1 animate-pulse" />
          </div>
        </div>
      )}
      {!currentThinking && !currentResponse && (
        <div className="bg-white rounded-2xl px-4 py-3 border border-gray-200 shadow-sm">
          <div className="flex items-center space-x-2 text-gray-500">
            <Sparkles className="w-4 h-4 animate-pulse text-purple-500" />
            <span className="text-sm">正在思考...</span>
          </div>
        </div>
      )}
    </div>
  </div>
))

StreamingMessage.displayName = 'StreamingMessage'
