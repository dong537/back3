import { useState, useEffect, useRef } from 'react'
import { Brain, ChevronDown, ChevronUp, Sparkles } from 'lucide-react'
import { stripSymbols } from '../utils/textSanitizer'

export default function ThinkingChain({ 
  isThinking = false,
  thinkingContent = '',
  finalContent = '',
  onStreamMessage,
  streamUrl,
  requestData,
  lightMode = false, // 新增：浅色模式
}) {
  const [expanded, setExpanded] = useState(true)
  const [thinking, setThinking] = useState('')
  const [result, setResult] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const contentRef = useRef(null)

  // 流式请求处理
  useEffect(() => {
    if (!streamUrl || !requestData) return

    const startStream = async () => {
      setIsStreaming(true)
      setThinking('')
      setResult('')

      try {
        // ✅ 改用 sessionStorage（与 AuthContext 保持一致）
        const token = sessionStorage.getItem('token');
        const response = await fetch(streamUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
            ...(token && { 'Authorization': `Bearer ${token}` })
          },
          body: JSON.stringify(requestData),
        })

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''
        let currentThinking = ''
        let currentResult = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.slice(6)
              if (data === '[DONE]') continue

              try {
                const parsed = JSON.parse(data)
                
                // 处理思维链内容
                if (parsed.reasoning_content) {
                  currentThinking += parsed.reasoning_content
                  setThinking(currentThinking)
                }
                
                // 处理最终内容
                if (parsed.content) {
                  currentResult += parsed.content
                  setResult(currentResult)
                }

                onStreamMessage?.(parsed)
              } catch (e) {
                // 非 JSON 数据，可能是纯文本
                if (data && data !== '[DONE]') {
                  currentResult += data
                  setResult(currentResult)
                }
              }
            }
          }
        }
      } catch (error) {
        console.error('Stream error:', error)
        setResult(`请求失败: ${error.message}`)
      } finally {
        setIsStreaming(false)
      }
    }

    startStream()
  }, [streamUrl, requestData])

  // 自动滚动到底部
  useEffect(() => {
    if (contentRef.current) {
      contentRef.current.scrollTop = contentRef.current.scrollHeight
    }
  }, [thinking, result])

  const displayThinking = stripSymbols(thinkingContent || thinking)
  const displayResult = stripSymbols(finalContent || result)
  const showThinking = isThinking || isStreaming || displayThinking

  if (!showThinking && !displayResult) return null

  // 浅色模式样式
  if (lightMode) {
    return (
      <div className="rounded-xl overflow-hidden">
        {/* 思维链区域 - 浅色模式 */}
        {(showThinking || displayThinking) && (
          <div className="border-b border-purple-200 mb-3">
            <button
              onClick={() => setExpanded(!expanded)}
              className="w-full flex items-center justify-between p-3 hover:bg-purple-100/50 transition-colors rounded-lg"
            >
              <div className="flex items-center space-x-2">
                <div className="w-7 h-7 rounded-full bg-purple-100 flex items-center justify-center">
                  <Brain className="w-4 h-4 text-purple-600" />
                </div>
                <span className="font-medium text-purple-700 text-sm">AI 思维过程</span>
                {(isThinking || isStreaming) && (
                  <div className="flex space-x-1">
                    <span className="w-1.5 h-1.5 bg-purple-500 rounded-full animate-pulse"></span>
                    <span className="w-1.5 h-1.5 bg-purple-500 rounded-full animate-pulse delay-75"></span>
                    <span className="w-1.5 h-1.5 bg-purple-500 rounded-full animate-pulse delay-150"></span>
                  </div>
                )}
              </div>
              {expanded ? <ChevronUp size={18} className="text-purple-500" /> : <ChevronDown size={18} className="text-purple-500" />}
            </button>

            {expanded && displayThinking && (
              <div 
                ref={contentRef}
                className="px-3 pb-3 max-h-48 overflow-y-auto"
              >
                <div className="bg-purple-100 rounded-lg p-3 text-sm text-purple-800 whitespace-pre-wrap">
                  {displayThinking}
                </div>
              </div>
            )}
          </div>
        )}

        {/* 最终结果区域 - 浅色模式 */}
        {displayResult && (
          <div>
            <div className="flex items-center space-x-2 mb-3">
              <div className="w-7 h-7 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 flex items-center justify-center">
                <Sparkles className="w-4 h-4 text-white" />
              </div>
              <span className="font-semibold text-purple-800 text-sm">分析结果</span>
            </div>
            <div className="text-gray-800 whitespace-pre-wrap leading-relaxed text-sm font-medium">
              {displayResult}
            </div>
          </div>
        )}
      </div>
    )
  }

  // 深色模式样式（原有样式）
  return (
    <div className="glass rounded-2xl overflow-hidden">
      {/* 思维链区域 */}
      {(showThinking || displayThinking) && (
        <div className="border-b border-white/10">
          <button
            onClick={() => setExpanded(!expanded)}
            className="w-full flex items-center justify-between p-4 hover:bg-white/5 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 rounded-full bg-purple-500/20 flex items-center justify-center">
                <Brain className="w-4 h-4 text-purple-400" />
              </div>
              <span className="font-medium text-purple-300">AI 思维过程</span>
              {(isThinking || isStreaming) && (
                <div className="flex space-x-1">
                  <span className="thinking-dot w-2 h-2 bg-purple-400 rounded-full"></span>
                  <span className="thinking-dot w-2 h-2 bg-purple-400 rounded-full"></span>
                  <span className="thinking-dot w-2 h-2 bg-purple-400 rounded-full"></span>
                </div>
              )}
            </div>
            {expanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
          </button>

          {expanded && displayThinking && (
            <div 
              ref={contentRef}
              className="px-4 pb-4 max-h-64 overflow-y-auto"
            >
              <div className="bg-purple-500/10 rounded-lg p-4 text-sm text-gray-300 whitespace-pre-wrap">
                {displayThinking}
              </div>
            </div>
          )}
        </div>
      )}

      {/* 最终结果区域 */}
      {displayResult && (
        <div className="p-4">
          <div className="flex items-center space-x-3 mb-4">
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-white" />
            </div>
            <span className="font-medium text-white">分析结果</span>
          </div>
          <div className="prose prose-invert max-w-none">
            <div className="text-gray-200 whitespace-pre-wrap leading-relaxed stream-text">
              {displayResult}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
