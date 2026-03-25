import React, { useState, useRef, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  Brain,
  Send,
  Sparkles,
  Trash2,
  StopCircle,
  ArrowLeft,
  Coins,
} from 'lucide-react'
import { logger } from '../utils/logger'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { resolvePageLocale, safeText, safeNumber } from '../utils/displayText'

const AI_PAGE_COPY = {
  'zh-CN': {
    back: '返回',
    title: 'AI 智能对话',
    assistantTitle: 'AI 智能助手',
    assistantDesc: '可以询问八字、卦象、塔罗等玄学相关问题。',
    costLabel: '每次对话消耗',
    unit: '积分',
    getMorePoints: '获取更多积分',
    inputPlaceholder: '输入您的问题...',
    thinkingSummary: '查看思维过程',
    thinking: '思考中...',
    thinkingFull: '正在思考...',
    requestFailed: '请求失败，请稍后重试',
    stopped: '已停止生成',
    clear: '清空',
    insufficientPoints: (cost, credits) =>
      `积分不足，AI 对话需要 ${cost} 积分，当前余额：${credits}`,
    insufficientPointsGuest: (cost) => `积分不足，AI 对话需要 ${cost} 积分`,
    spentPoints: (cost) => `消耗 ${cost} 积分`,
    spendFailed: '积分扣除失败',
    sendFailed: '请求失败',
    spendLabel: 'AI 智能对话',
    quickQuestions: [
      '帮我分析一下八字',
      '解读一下今天的运势',
      '做一次塔罗感情占卜',
    ],
    promptIntro: '【用户问题】',
    promptRules: '【重要格式要求】',
    promptRule1: '仅输出纯文本，不使用任何 Markdown 或富文本格式。',
    promptRule2: '禁止出现标题、项目符号、代码块、链接或装饰性符号。',
    promptRule3: '使用自然段落和换行组织内容，必要时可使用数字序号。',
    promptRule4: '请用专业、友好的语气回答用户的问题。',
  },
  'en-US': {
    back: 'Back',
    title: 'AI Chat',
    assistantTitle: 'AI Assistant',
    assistantDesc: 'Ask about Bazi, hexagrams, Tarot, and other mystic topics.',
    costLabel: 'Each chat costs',
    unit: 'credits',
    getMorePoints: 'Get more credits',
    inputPlaceholder: 'Type your question...',
    thinkingSummary: 'View thinking process',
    thinking: 'Thinking...',
    thinkingFull: 'Thinking...',
    requestFailed: 'Request failed. Please try again later.',
    stopped: 'Generation stopped.',
    clear: 'Clear',
    insufficientPoints: (cost, credits) =>
      `Not enough credits. AI chat needs ${cost} credits, current balance: ${credits}`,
    insufficientPointsGuest: (cost) =>
      `Not enough credits. AI chat needs ${cost} credits.`,
    spentPoints: (cost) => `Spent ${cost} credits`,
    spendFailed: 'Failed to deduct credits',
    sendFailed: 'Request failed',
    spendLabel: 'AI chat',
    quickQuestions: [
      'Help me analyze my Bazi chart',
      "Interpret today's fortune for me",
      'Do a Tarot love reading',
    ],
    promptIntro: '[User Question]',
    promptRules: '[Formatting Requirements]',
    promptRule1:
      'Reply in plain text only. Do not use Markdown or rich-text formatting.',
    promptRule2:
      'Do not use headings, bullet markers, code fences, links, or decorative symbols in the answer.',
    promptRule3:
      'Organize the answer with natural paragraphs and line breaks. Use numbered sections when useful.',
    promptRule4: 'Answer in a professional and friendly tone.',
  },
}

function buildPrompt(userMessage, copy) {
  return `${copy.promptIntro}
${userMessage}

${copy.promptRules}
${copy.promptRule1}
${copy.promptRule2}
${copy.promptRule3}

${copy.promptRule4}`
}

export default function AIPage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = AI_PAGE_COPY[locale]
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } =
    useAuth()
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

  useEffect(() => {
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  const scrollToBottom = useCallback(() => {
    if (scrollTimeoutRef.current) {
      cancelAnimationFrame(scrollTimeoutRef.current)
    }
    scrollTimeoutRef.current = requestAnimationFrame(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'auto' })
    })
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  useEffect(
    () => () => {
      if (scrollTimeoutRef.current) {
        cancelAnimationFrame(scrollTimeoutRef.current)
      }
      abortControllerRef.current?.abort()
    },
    []
  )

  const handleStop = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
      abortControllerRef.current = null
    }
  }, [])

  const handleSend = useCallback(async () => {
    const trimmedInput = safeText(input)
    if (!trimmedInput || isStreaming) return

    const cost = POINTS_COST.AI_CHAT
    const currentCredits = safeNumber(credits, 0)
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(copy.insufficientPoints(cost, currentCredits))
        return
      }
    } else if (!points.canSpend(cost)) {
      toast.error(copy.insufficientPointsGuest(cost))
      return
    }

    setInput('')
    setMessages((prev) => [...prev, { role: 'user', content: trimmedInput }])
    setIsStreaming(true)
    setCurrentThinking('')
    setCurrentResponse('')

    const controller = new AbortController()
    abortControllerRef.current = controller

    try {
      const token = sessionStorage.getItem('token')
      logger.info('Starting AI chat request...', { hasToken: !!token, locale })

      const response = await fetch('/api/deepseek/reasoning', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Language': locale,
          'Accept-Language': locale,
          ...(token && { Authorization: `Bearer ${token}` }),
        },
        body: JSON.stringify({ message: buildPrompt(trimmedInput, copy) }),
        signal: controller.signal,
      })

      if (!response.ok) {
        const errorText = safeText(
          await response.text(),
          `${response.status} ${response.statusText}`
        )
        logger.error(`HTTP ${response.status}: ${response.statusText}`, {
          errorText,
        })
        throw new Error(
          errorText || `${response.status} ${response.statusText}`
        )
      }

      const data = await response.json()
      const assistantContent = safeText(data?.content)
      logger.info('Received AI response', {
        success: data?.success,
        contentLength: assistantContent.length,
      })

      if (!data?.success || !assistantContent) {
        throw new Error(safeText(data?.message, copy.sendFailed))
      }

      if (isLoggedIn) {
        const spendResult = await spendCredits(cost, copy.spendLabel)
        if (spendResult.success) {
          toast.success(copy.spentPoints(cost))
        } else {
          toast.error(safeText(spendResult.message, copy.spendFailed))
        }
      } else {
        const spendResult = points.spend(cost, copy.spendLabel)
        if (spendResult.success) {
          setUserPoints(spendResult.newTotal)
          toast.success(copy.spentPoints(cost))
        }
      }

      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: assistantContent, thinking: '' },
      ])
    } catch (error) {
      if (error?.name === 'AbortError') {
        setMessages((prev) => [
          ...prev,
          { role: 'assistant', content: copy.stopped, thinking: '' },
        ])
      } else {
        const errorMessage = safeText(error?.message, copy.requestFailed)
        logger.error('AI chat error:', error)
        setMessages((prev) => [
          ...prev,
          { role: 'assistant', content: errorMessage, thinking: '' },
        ])
      }
    } finally {
      abortControllerRef.current = null
      setIsStreaming(false)
      setCurrentThinking('')
      setCurrentResponse('')
    }
  }, [
    input,
    isStreaming,
    isLoggedIn,
    credits,
    canSpendCredits,
    spendCredits,
    copy,
    locale,
  ])

  const clearChat = () => {
    setMessages([])
    setCurrentThinking('')
    setCurrentResponse('')
  }

  return (
    <div className="page-shell flex min-h-screen flex-col pb-32" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between gap-3 py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 transition-colors hover:bg-white/10"
            aria-label={copy.back}
            title={copy.back}
          >
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <h1 className="text-lg font-bold text-[#f4ece1]">{copy.title}</h1>
          <div className="flex items-center space-x-2">
            <div className="flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1.5">
              <Coins size={16} className="text-[#d0a85b]" />
              <span className="text-sm font-bold text-[#dcb86f]">
                {isLoggedIn
                  ? safeNumber(credits, 0)
                  : safeNumber(userPoints, 0)}
              </span>
            </div>
            <button
              onClick={clearChat}
              className="rounded-xl p-2 transition-colors hover:bg-white/10"
              title={copy.clear}
              aria-label={copy.clear}
            >
              <Trash2 size={20} className="text-[#bdaa94]" />
            </button>
          </div>
        </div>
      </div>

      <div className="app-page-shell-narrow flex-1 py-4">
        {messages.length === 0 && !isStreaming ? (
          <div className="page-hero mt-4">
            <div className="page-hero-inner !max-w-3xl !px-5 !py-8 sm:!px-8">
              <div className="mb-4 flex justify-center">
                <div className="mystic-icon-badge h-20 w-20 rounded-full">
                  <Brain className="h-10 w-10 text-white" />
                </div>
              </div>
              <h2 className="page-title !mt-0 text-center !text-3xl md:!text-4xl">
                {copy.assistantTitle}
              </h2>
              <p className="page-subtitle max-w-2xl">{copy.assistantDesc}</p>
              <div className="mt-5 flex justify-center">
                <span className="mystic-chip normal-case tracking-[0.18em]">
                  {copy.costLabel} {POINTS_COST.AI_CHAT} {copy.unit}
                </span>
              </div>

              <div className="mx-auto mt-8 grid w-full max-w-2xl gap-3 sm:grid-cols-3">
                {copy.quickQuestions.map((question) => (
                  <button
                    key={question}
                    onClick={() => setInput(question)}
                    className="panel-soft block w-full border border-white/10 px-4 py-4 text-left text-sm text-[#e4d6c8] transition-all hover:border-[#d0a85b]/24 hover:bg-white/[0.06]"
                  >
                    {question}
                  </button>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-4 pb-4">
            {messages.map((msg, index) => (
              <MessageItem key={`${msg.role}-${index}`} msg={msg} copy={copy} />
            ))}

            {isStreaming && (
              <StreamingMessage
                currentThinking={currentThinking}
                currentResponse={currentResponse}
                copy={copy}
              />
            )}

            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      <div className="safe-area-bottom sticky bottom-0 z-40 -mx-4 border-t border-white/10 bg-[#0f0a09]/88 px-4 py-4 backdrop-blur-xl">
        <div className="mx-auto max-w-5xl">
          <div className="mb-2 flex items-center justify-between px-1">
            <span className="text-xs text-[#8f7b66]">
            {copy.costLabel}{' '}
              <span className="font-medium text-[#dcb86f]">
                {POINTS_COST.AI_CHAT}
              </span>{' '}
              {copy.unit}
            </span>
            <button
              onClick={() => navigate('/dashboard')}
              className="text-xs font-medium text-[#dcb86f] transition-colors hover:text-[#f0d9a5]"
            >
              {copy.getMorePoints}
            </button>
          </div>
          <div className="mx-auto flex max-w-4xl items-end gap-3">
            <div className="relative flex-1">
              <textarea
                ref={inputRef}
                placeholder={copy.inputPlaceholder}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                rows={1}
                className="mystic-input w-full resize-none rounded-[24px] px-4 py-3 text-sm"
                style={{ minHeight: '48px', maxHeight: '120px' }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault()
                    handleSend()
                  }
                }}
                onInput={(e) => {
                  e.target.style.height = 'auto'
                  e.target.style.height = `${Math.min(e.target.scrollHeight, 120)}px`
                }}
                disabled={isStreaming}
              />
            </div>

            {isStreaming ? (
              <button
                onClick={handleStop}
                className="flex h-12 w-12 items-center justify-center rounded-full border border-[#a34224]/40 bg-[#7a3218]/22 text-[#f4ece1] transition-all hover:bg-[#7a3218]/34"
              >
                <StopCircle size={22} />
              </button>
            ) : (
              <button
                onClick={handleSend}
                disabled={!safeText(input)}
                className={`flex h-12 w-12 items-center justify-center rounded-full transition-all ${
                  safeText(input)
                    ? 'btn-primary-theme px-0 py-0 text-white'
                    : 'cursor-not-allowed border border-white/10 bg-white/[0.05] text-[#8f7b66]'
                }`}
              >
                <Send size={20} />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

const MessageItem = React.memo(function MessageItem({ msg, copy }) {
  const content = safeText(msg?.content)
  const thinking = safeText(msg?.thinking)

  return (
    <div
      className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
    >
      <div className="max-w-[85%]">
        {msg.role === 'assistant' && thinking && (
          <details className="panel-soft mb-2 overflow-hidden border border-white/10">
            <summary className="cursor-pointer px-4 py-2 text-sm text-[#dcb86f] transition-colors hover:bg-white/[0.05]">
              <Brain className="mr-2 inline h-4 w-4" />
              {copy.thinkingSummary}
            </summary>
            <div className="max-h-48 overflow-y-auto whitespace-pre-wrap border-t border-white/10 bg-white/[0.03] px-4 py-3 text-sm text-[#bdaa94]">
              {thinking}
            </div>
          </details>
        )}
        <div
          className={`rounded-2xl px-4 py-3 ${
            msg.role === 'user'
              ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-white shadow-[0_18px_36px_rgba(163,66,36,0.22)]'
              : 'border border-white/10 bg-white/[0.04] text-[#f4ece1]'
          }`}
        >
          <div className="whitespace-pre-wrap text-sm leading-relaxed">
            {content}
          </div>
        </div>
      </div>
    </div>
  )
})

const StreamingMessage = React.memo(function StreamingMessage({
  currentThinking,
  currentResponse,
  copy,
}) {
  const thinking = safeText(currentThinking)
  const response = safeText(currentResponse)

  return (
    <div className="flex justify-start">
      <div className="max-w-[85%]">
        {thinking && (
          <div className="panel-soft mb-2 border border-white/10 p-3">
            <div className="mb-2 flex items-center text-sm text-[#dcb86f]">
              <Brain className="mr-2 h-4 w-4 animate-pulse" />
              {copy.thinking}
            </div>
            <div className="max-h-32 overflow-y-auto whitespace-pre-wrap text-sm text-[#bdaa94]">
              {thinking}
            </div>
          </div>
        )}
        {response && (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-4 py-3">
            <div className="whitespace-pre-wrap text-sm leading-relaxed text-[#f4ece1]">
              {response}
              <span className="ml-1 inline-block h-4 w-1 animate-pulse bg-[#d0a85b]" />
            </div>
          </div>
        )}
        {!thinking && !response && (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-4 py-3">
            <div className="flex items-center space-x-2 text-[#bdaa94]">
              <Sparkles className="h-4 w-4 animate-pulse text-[#d0a85b]" />
              <span className="text-sm">{copy.thinkingFull}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  )
})
