import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Brain, ChevronDown, ChevronUp, Sparkles } from 'lucide-react'
import { stripSymbols } from '../utils/textSanitizer'
import { resolvePageLocale } from '../utils/displayText'

export default function ThinkingChain({
  isThinking = false,
  thinkingContent = '',
  finalContent = '',
  onStreamMessage,
  streamUrl,
  requestData,
  lightMode = false,
}) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const t = (zh, en) => (isEn ? en : zh)
  const [expanded, setExpanded] = useState(true)
  const [thinking, setThinking] = useState('')
  const [result, setResult] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const contentRef = useRef(null)

  useEffect(() => {
    if (!streamUrl || !requestData) return

    const startStream = async () => {
      setIsStreaming(true)
      setThinking('')
      setResult('')

      try {
        const token = sessionStorage.getItem('token')
        const response = await fetch(streamUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'text/event-stream',
            ...(token && { Authorization: `Bearer ${token}` }),
          },
          body: JSON.stringify(requestData),
        })

        const reader = response.body?.getReader()
        if (!reader) {
          throw new Error(
            t('流式响应不可用', 'Streaming response is unavailable')
          )
        }

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
            if (!line.startsWith('data: ')) continue
            const data = line.slice(6)
            if (data === '[DONE]') continue

            try {
              const parsed = JSON.parse(data)
              if (parsed.reasoning_content) {
                currentThinking += parsed.reasoning_content
                setThinking(currentThinking)
              }
              if (parsed.content) {
                currentResult += parsed.content
                setResult(currentResult)
              }
              onStreamMessage?.(parsed)
            } catch {
              if (data && data !== '[DONE]') {
                currentResult += data
                setResult(currentResult)
              }
            }
          }
        }
      } catch (error) {
        console.error('Stream error:', error)
        setResult(
          t('请求失败：', 'Request failed: ') + (error?.message || '')
        )
      } finally {
        setIsStreaming(false)
      }
    }

    void startStream()
  }, [streamUrl, requestData, isEn, onStreamMessage])

  useEffect(() => {
    if (contentRef.current) {
      contentRef.current.scrollTop = contentRef.current.scrollHeight
    }
  }, [thinking, result])

  const displayThinking = stripSymbols(thinkingContent || thinking)
  const displayResult = stripSymbols(finalContent || result)
  const showThinking = Boolean(isThinking || isStreaming || displayThinking)

  if (!showThinking && !displayResult) return null

  if (lightMode) {
    return (
      <div className="overflow-hidden rounded-2xl border border-[#d0a85b]/16 bg-[#f8efe0] text-[#3b2a22] shadow-[0_12px_30px_rgba(81,48,31,0.08)]">
        {showThinking && (
          <div className="mb-3 border-b border-[#d0a85b]/18">
            <button
              onClick={() => setExpanded(!expanded)}
              className="flex w-full items-center justify-between rounded-2xl p-3 transition-colors hover:bg-[#f3e6d4]"
            >
              <div className="flex items-center space-x-2">
                <div className="flex h-7 w-7 items-center justify-center rounded-full bg-[#efe0c2]">
                  <Brain className="h-4 w-4 text-[#8f5c1f]" />
                </div>
                <span className="text-sm font-medium text-[#6a4a1e]">
                  {t('AI 思维过程', 'AI Thinking Process')}
                </span>
                {(isThinking || isStreaming) && (
                  <div className="flex space-x-1">
                    <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-[#a34224]" />
                    <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-[#a34224] delay-75" />
                    <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-[#a34224] delay-150" />
                  </div>
                )}
              </div>
              {expanded ? (
                <ChevronUp size={18} className="text-[#8f5c1f]" />
              ) : (
                <ChevronDown size={18} className="text-[#8f5c1f]" />
              )}
            </button>

            {expanded && displayThinking && (
              <div ref={contentRef} className="max-h-48 overflow-y-auto px-3 pb-3">
                <div className="whitespace-pre-wrap rounded-2xl bg-[#efe6d8] p-3 text-sm leading-6 text-[#5a4538]">
                  {displayThinking}
                </div>
              </div>
            )}
          </div>
        )}

        {displayResult && (
          <div className="p-4">
            <div className="mb-3 flex items-center space-x-2">
              <div className="flex h-7 w-7 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]">
                <Sparkles className="h-4 w-4 text-white" />
              </div>
              <span className="text-sm font-semibold text-[#6a4a1e]">
                {t('分析结果', 'Analysis Result')}
              </span>
            </div>
            <div className="whitespace-pre-wrap text-sm font-medium leading-7 text-[#3b2a22]">
              {displayResult}
            </div>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="glass overflow-hidden rounded-[28px] border border-white/10">
      {showThinking && (
        <div className="border-b border-white/10">
          <button
            onClick={() => setExpanded(!expanded)}
            className="flex w-full items-center justify-between p-4 transition-colors hover:bg-white/[0.04]"
          >
            <div className="flex items-center space-x-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#6a4a1e]/18">
                <Brain className="h-4 w-4 text-[#dcb86f]" />
              </div>
              <span className="font-medium text-[#f0d9a5]">
                {t('AI 思维过程', 'AI Thinking Process')}
              </span>
              {(isThinking || isStreaming) && (
                <div className="flex space-x-1">
                  <span className="thinking-dot h-2 w-2 rounded-full bg-[#dcb86f]" />
                  <span className="thinking-dot h-2 w-2 rounded-full bg-[#dcb86f]" />
                  <span className="thinking-dot h-2 w-2 rounded-full bg-[#dcb86f]" />
                </div>
              )}
            </div>
            {expanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
          </button>

          {expanded && displayThinking && (
            <div ref={contentRef} className="max-h-64 overflow-y-auto px-4 pb-4">
              <div className="whitespace-pre-wrap rounded-[22px] border border-white/10 bg-white/[0.03] p-4 text-sm leading-7 text-[#e4d6c8]">
                {displayThinking}
              </div>
            </div>
          )}
        </div>
      )}

      {displayResult && (
        <div className="p-4">
          <div className="mb-4 flex items-center space-x-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]">
              <Sparkles className="h-4 w-4 text-white" />
            </div>
            <span className="font-medium text-[#f4ece1]">
              {t('分析结果', 'Analysis Result')}
            </span>
          </div>
          <div className="prose prose-invert max-w-none">
            <div className="stream-text whitespace-pre-wrap leading-7 text-[#e4d6c8]">
              {displayResult}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
