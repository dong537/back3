import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Compass, Shuffle, Clock, Hash, Coins, Sparkles, Star, Share2, ArrowLeft, History } from 'lucide-react'
import Button from '../components/Button'
import Input, { Textarea } from '../components/Input'
import ThinkingChain from '../components/ThinkingChain'
import HexagramAnimation from '../components/HexagramAnimation'
import WeChatContact from '../components/WeChatContact'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import { yijingApi, deepseekApi, calculationRecordApi, unwrapApiData } from '../api'
import { logger } from '../utils/logger'
import { useAuth } from '../context/AuthContext'

const methods = [
  { value: 'time', icon: Clock, label: '时间起卦', desc: '以当前时间起卦' },
  { value: 'random', icon: Shuffle, label: '随机起卦', desc: '系统随机生成' },
  { value: 'number', icon: Hash, label: '数字起卦', desc: '输入数字起卦' },
  { value: 'coin', icon: Coins, label: '铜钱起卦', desc: '模拟摇卦' },
]

export default function YijingPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()
  const [question, setQuestion] = useState('')
  const [method, setMethod] = useState('time')
  const [seed, setSeed] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState('')
  const [isAnimating, setIsAnimating] = useState(false)
  const [userPoints, setUserPoints] = useState(points.get())

  // 页面加载时同步一次后端积分余额，保证与个人中心/顶部导航一致
  useEffect(() => {
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  // 从收藏页跳转回来时，恢复当次占卜结果
  useEffect(() => {
    const state = location?.state
    if (!state || !state.fromFavorite) return

    if (typeof state.question === 'string') {
      setQuestion(state.question)
    }
    if (state.result) {
      setResult(state.result)
      setIsAnimating(false)
      setLoading(false)
    }
  }, [location?.state])

  const handleDivination = async () => {
    if (!question.trim()) {
      toast.error('请输入占问问题')
      return
    }
    
    setLoading(true)
    setIsAnimating(true)
    setResult(null)
    setAiResult('')

    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      const response = await yijingApi.quickDivination(question, method, seed)
      const resultData = unwrapApiData(response)
      
      setTimeout(() => {
        setResult(resultData)
        setIsAnimating(false)
        
        const original = resultData?.original
        if (original) {
          // 保存到本地历史记录
          historyStorage.add({
            type: 'yijing',
            question: question,
            method: method,
            dataId: original.id?.toString() || '',
            summary: `${original.chinese}卦 - ${original.judgment?.substring(0, 30) || ''}...`,
            data: response.data
          })
          
          // 自动保存到后端（用于统计占卜次数和成就检查）
          if (isLoggedIn) {
            calculationRecordApi.save({
              recordType: 'yijing',
              recordTitle: `${original.chinese}卦占卜`,
              question: question,
              summary: original.judgment?.substring(0, 100) || '',
              data: JSON.stringify(resultData?.data || resultData)
            }).catch(error => {
              // 静默失败，不影响用户体验
              logger.warn('自动保存占卜记录失败:', error)
            })
          }
        }
      }, 1500)
    } catch (error) {
      logger.error('Divination error:', error)
      toast.error(error?.response?.data?.error || '占卜失败，请重试')
      setIsAnimating(false)
    } finally {
      setLoading(false)
    }
  }

  const handleFavorite = async () => {
    const original = result?.original || result?.data?.original
    if (!original) return
    const item = {
      type: 'yijing',
      question: question,
      dataId: original.id?.toString() || '',
      title: `${original.chinese}卦`,
      summary: original.judgment?.substring(0, 50),
      data: result?.data || result
    }
    try {
      const favorited = await favoritesStorage.toggle(item)
      toast.success(favorited ? '已收藏' : '已取消收藏')
    } catch (error) {
      toast.error('收藏操作失败')
    }
  }

  const handleShare = () => {
    if (!result) return
    const original = result?.original || result?.data?.original
    if (!original) return
    const shareText = `我刚刚占卜了：${question} - 得到${original.chinese}卦\n${window.location.href}`
    navigator.clipboard.writeText(shareText).then(() => {
      toast.success('链接已复制')
    }).catch(() => {
      toast.error('复制失败')
    })
  }

  const handleSaveRecord = async () => {
    if (!result) return
    const original = result?.original || result?.data?.original
    if (!original) return
    try {
      const record = {
        recordType: 'yijing',
        recordTitle: `${original.chinese}卦占卜`,
        question: question,
        summary: original.judgment?.substring(0, 100) || '',
        data: JSON.stringify(result?.data || result)
      }
      await calculationRecordApi.save(record)
      toast.success('记录已保存')
    } catch (error) {
      toast.error('保存记录失败')
    }
  }

  const handleAIInterpret = async () => {
    if (!result) return
    const cost = POINTS_COST.AI_INTERPRET
    
    // 已登录用户使用后端积分系统
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分，当前余额：${credits}`)
        return
      }
    } else {
      // 未登录用户使用本地积分系统
      if (!points.canSpend(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分`)
        return
      }
    }
    
    setAiLoading(true)
    setAiResult('')
    try {
      const originalHexagram = result?.original || result?.data?.original
      const changedHexagram = result?.changed || result?.data?.changed
      
      // 构建提示词 - 要求纯文本输出
      let prompt = `你是一位精通易经的命理大师，请为用户解读以下卦象。

【重要格式要求】
仅输出纯文本，不使用任何 Markdown 或富文本格式。
禁止出现以下符号：井号、星号、反引号、下划线、波浪号、大于号、小于号、方括号、圆括号内的链接格式、竖线，以及以连字符作为项目符号的列表。
使用自然段落和换行来组织内容，用数字序号（如1、2、3）代替符号列表。

【占卜信息】
占问问题：${question}
本卦：${originalHexagram?.chinese || ''}卦
卦辞：${originalHexagram?.judgment || ''}`
      
      if (changedHexagram) {
        prompt += `
变卦：${changedHexagram.chinese}卦`
      }
      
      prompt += `

【解读要求】
请从以下几个方面进行解读：
1、卦象总论：解释本卦的核心含义
2、针对问题的分析：结合用户的具体问题进行解读
3、吉凶判断：给出明确的吉凶趋势
4、行动建议：给出具体可行的建议
5、注意事项：需要特别留意的地方

请用温和、专业的语气，让用户感受到传统文化的智慧。`

      const response = await deepseekApi.interpretHexagram(prompt)
      
      // 解析AI响应 - 后端返回 Result { code, message, data }
      let aiContent = ''
      if (response.data?.code === 200 && response.data?.data) {
        aiContent = response.data.data
      } else if (typeof response.data === 'string') {
        aiContent = response.data
      } else if (response.data?.content) {
        aiContent = response.data.content
      } else {
        aiContent = '解读失败'
      }
      
      // 扣除积分
      if (isLoggedIn) {
        const spendResult = await spendCredits(cost, 'AI易经解读')
        if (spendResult.success) {
          toast.success(`消耗 ${cost} 积分`)
        } else {
          toast.error(spendResult.message || '积分扣除失败')
        }
      } else {
        const spendResult = points.spend(cost, 'AI易经解读')
        if (spendResult.success) {
          setUserPoints(spendResult.newTotal)
          toast.success(`消耗 ${cost} 积分`)
        }
      }
      
      setAiResult(aiContent)
    } catch (error) {
      logger.error('AI interpret error:', error)
      toast.error('AI解读失败，请稍后重试')
      setAiResult('AI解读失败，请稍后重试')
    } finally {
      setAiLoading(false)
    }
  }

  const needsSeed = method === 'number'

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-950 to-slate-900">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-xl border-b border-white/10">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-white/10 rounded-xl transition-all">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div className="flex items-center space-x-2">
            <Compass className="w-5 h-5 text-purple-400" />
            <h1 className="text-lg font-bold text-white">天机明理</h1>
          </div>
          <div className="flex items-center space-x-1 bg-amber-500/20 px-3 py-1.5 rounded-full border border-amber-500/30">
            <Coins size={14} className="text-amber-400" />
            <span className="text-sm font-bold text-amber-400">
              {isLoggedIn ? (credits ?? 0) : userPoints}
            </span>
          </div>
        </div>
      </div>

      <div className="px-4 pb-20 pt-6">
        {/* 页面标题 */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-purple-500 to-pink-500 mb-4 shadow-lg shadow-purple-500/30">
            <Compass className="w-8 h-8 text-white" />
          </div>
          <h2 className="text-2xl font-bold text-white mb-2">天机明理</h2>
          <p className="text-gray-400 text-sm">64卦象 · 5种起卦方法 · AI智能解读</p>
        </div>

        {/* 起卦方法选择 */}
        <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-5 mb-5">
          <h3 className="text-base font-bold text-white mb-2">选择起卦方法</h3>
          <p className="text-gray-400 text-xs mb-4">不同方法适用于不同场景</p>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {methods.map(({ value, icon: Icon, label, desc }) => (
              <button
                key={value}
                onClick={() => setMethod(value)}
                className={`p-4 rounded-xl border transition-all text-left group hover:scale-[1.02] ${
                  method === value 
                    ? 'bg-gradient-to-br from-purple-500/20 to-pink-500/20 border-purple-500/50 shadow-lg shadow-purple-500/20' 
                    : 'bg-white/5 border-white/10 hover:border-white/20 hover:bg-white/10'
                }`}
              >
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 ${
                  method === value 
                    ? 'bg-gradient-to-br from-purple-500 to-pink-500' 
                    : 'bg-white/10 group-hover:bg-white/20'
                }`}>
                  <Icon className={`w-5 h-5 ${method === value ? 'text-white' : 'text-gray-400'}`} />
                </div>
                <div className={`font-bold text-sm mb-1 ${method === value ? 'text-white' : 'text-gray-300'}`}>
                  {label}
                </div>
                <div className="text-xs text-gray-500">{desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* 输入问题 */}
        <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-5 mb-5">
          <h3 className="text-base font-bold text-white mb-2">输入您的问题</h3>
          <p className="text-gray-400 text-xs mb-4">心诚则灵，专注于您想问的事情</p>
          <Textarea
            placeholder="请输入您想占卜的问题，例如：今年事业运势如何？"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            rows={3}
            className="bg-white/5 border-white/10 text-white placeholder-gray-500 focus:border-purple-500/50 focus:ring-purple-500/30 rounded-xl"
          />
          {needsSeed && (
            <Input
              label="输入数字"
              placeholder="请输入一个数字"
              value={seed}
              onChange={(e) => setSeed(e.target.value)}
              type="number"
              className="mt-3 bg-white/5 border-white/10 text-white"
            />
          )}
        </div>

        {/* 起卦按钮 */}
        <button
          onClick={handleDivination}
          disabled={loading || !question.trim()}
          className="w-full py-4 rounded-xl font-bold text-white text-lg bg-gradient-to-r from-purple-500 via-pink-500 to-purple-500 bg-[length:200%_100%] hover:bg-[position:100%_0] transition-all duration-500 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-purple-500/30 flex items-center justify-center space-x-2 mb-5"
        >
          <Compass size={20} className={loading ? 'animate-spin' : ''} />
          <span>开始占卜</span>
        </button>

        {/* 卦象生成动画 */}
        {(isAnimating || result) && (
          <HexagramAnimation isGenerating={isAnimating} onComplete={() => {}}>
            {result && !isAnimating && (
              <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-5 mb-5">
                <h3 className="text-base font-bold text-white mb-4">卦象结果</h3>
                
                <div className="grid grid-cols-2 gap-4 mb-5">
                  {/* 本卦 */}
                  <div className="bg-gradient-to-br from-amber-500/10 to-orange-500/10 rounded-xl p-4 text-center border border-amber-500/20">
                    <div className="text-5xl mb-3">
                      {(result?.original || result?.data?.original)?.symbol || '☰'}
                    </div>
                    <div className="text-xl font-bold text-white mb-1">
                      {(result?.original || result?.data?.original)?.chinese || '乾'}卦
                    </div>
                    <div className="text-xs text-amber-400 font-medium">本卦</div>
                  </div>

                  {/* 变卦 */}
                  {(result?.changed || result?.data?.changed) && (
                    <div className="bg-gradient-to-br from-blue-500/10 to-indigo-500/10 rounded-xl p-4 text-center border border-blue-500/20">
                      <div className="text-5xl mb-3">
                        {(result?.changed || result?.data?.changed)?.symbol || '☱'}
                      </div>
                      <div className="text-xl font-bold text-white mb-1">
                        {(result?.changed || result?.data?.changed)?.chinese}卦
                      </div>
                      <div className="text-xs text-blue-400 font-medium">变卦</div>
                    </div>
                  )}
                </div>

                {/* 卦辞 */}
                <div className="bg-amber-500/10 rounded-xl p-4 mb-4 border border-amber-500/20">
                  <h4 className="text-sm font-bold text-amber-400 mb-2">卦辞</h4>
                  <p className="text-white text-sm leading-relaxed">
                    {(result?.original || result?.data?.original)?.judgment || '暂无卦辞'}
                  </p>
                  {((result?.original || result?.data?.original)?.judgmentExplanation) && (
                    <p className="text-gray-300 text-sm leading-relaxed mt-2">
                      {(result?.original || result?.data?.original)?.judgmentExplanation}
                    </p>
                  )}
                </div>

                {/* 象辞 */}
                <div className="bg-blue-500/10 rounded-xl p-4 mb-4 border border-blue-500/20">
                  <h4 className="text-sm font-bold text-blue-400 mb-2">象辞</h4>
                  <p className="text-white text-sm leading-relaxed">
                    {(result?.original || result?.data?.original)?.image || '暂无象辞'}
                  </p>
                  {((result?.original || result?.data?.original)?.imageExplanation) && (
                    <p className="text-gray-300 text-sm leading-relaxed mt-2">
                      {(result?.original || result?.data?.original)?.imageExplanation}
                    </p>
                  )}
                </div>

                {/* 六爻装卦（来自后端 original_yaos / changed_yaos） */}
                {(() => {
                  const originalYaos = result?.original_yaos || result?.data?.original_yaos || []
                  const changedYaos = result?.changed_yaos || result?.data?.changed_yaos || []
                  if ((!originalYaos || originalYaos.length === 0) && (!changedYaos || changedYaos.length === 0)) return null

                  const changingLines = result?.changing_lines || result?.data?.changing_lines || []

                  const renderYaoRow = (yao, idx, isChanged = false) => {
                    const pos = yao?.yao_position ?? (idx + 1)
                    const isChanging = Array.isArray(changingLines) && changingLines.includes(pos)
                    const yaoType = yao?.yao_type || ''
                    const stem = yao?.stem || ''
                    const branch = yao?.branch || ''
                    const liuQin = yao?.liu_qin || ''
                    const isShi = yao?.is_shi === 1 || yao?.is_shi === true
                    const isYing = yao?.is_ying === 1 || yao?.is_ying === true

                    return (
                      <div key={`${isChanged ? 'c' : 'o'}-${pos}`} className={`flex items-center justify-between gap-3 px-3 py-2 rounded-lg border ${isChanged ? 'bg-blue-500/10 border-blue-500/20' : 'bg-white/5 border-white/10'}`}>
                        <div className="text-xs text-gray-300 w-12">{pos}爻</div>
                        <div className="flex-1 text-sm text-white">
                          <span className="font-semibold mr-2">{yaoType}</span>
                          <span className="text-purple-200 mr-2">{stem}{branch}</span>
                          <span className="text-gray-300">{liuQin}</span>
                          {isShi && <span className="ml-2 text-[10px] px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">世</span>}
                          {isYing && <span className="ml-2 text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">应</span>}
                          {isChanging && <span className="ml-2 text-[10px] px-2 py-0.5 rounded-full bg-pink-500/20 text-pink-300 border border-pink-500/30">动</span>}
                        </div>
                      </div>
                    )
                  }

                  return (
                    <div className="mb-4">
                      <h4 className="text-sm font-bold text-purple-300 mb-2">六爻装卦</h4>
                      {originalYaos && originalYaos.length > 0 && (
                        <div className="mb-3">
                          <div className="text-xs text-gray-400 mb-2">本卦六爻</div>
                          <div className="space-y-2">
                            {originalYaos.map((yao, idx) => renderYaoRow(yao, idx, false))}
                          </div>
                        </div>
                      )}
                      {changedYaos && changedYaos.length > 0 && (
                        <div>
                          <div className="text-xs text-gray-400 mb-2">变卦六爻</div>
                          <div className="space-y-2">
                            {changedYaos.map((yao, idx) => renderYaoRow(yao, idx, true))}
                          </div>
                        </div>
                      )}
                    </div>
                  )
                })()}

                {/* 六爻爻辞（按SQL：爻辞 + 白话 explanation） */}
                {(() => {
                  const orig = result?.original || result?.data?.original
                  const lines = orig?.lines || []
                  if (!Array.isArray(lines) || lines.length === 0) return null

                  // lines 在后端按 yao_position 升序返回（1-6，从下往上）
                  return (
                    <div className="mb-5">
                      <h4 className="text-sm font-bold text-indigo-300 mb-2">六爻爻辞</h4>
                      <div className="space-y-3">
                        {lines.map((ln) => (
                          <div key={ln.position} className="bg-white/5 rounded-xl p-4 border border-white/10">
                            <div className="text-xs text-gray-400 mb-2">第{ln.position}爻</div>
                            <div className="text-white text-sm leading-relaxed">{ln.text || '暂无爻辞'}</div>
                            {ln.textExplanation && (
                              <div className="text-gray-300 text-sm leading-relaxed mt-2">{ln.textExplanation}</div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )
                })()}

                {/* 六爻分析（来自后端 liu_yao_analysis） */}
                {(() => {
                  const analysis = result?.liu_yao_analysis || result?.data?.liu_yao_analysis
                  if (!analysis) return null
                  const yongshen = analysis?.用神信息
                  const dongbian = analysis?.动变分析

                  return (
                    <div className="mb-5">
                      <h4 className="text-sm font-bold text-pink-300 mb-2">六爻分析</h4>
                      {yongshen && (
                        <div className="bg-purple-500/10 rounded-xl p-4 mb-3 border border-purple-500/20">
                          <div className="text-xs text-purple-300 font-semibold mb-2">用神信息</div>
                          <div className="text-sm text-white space-y-1">
                            {yongshen.首选用神 && <div>首选用神：{yongshen.首选用神}</div>}
                            {yongshen.辅助参考 && <div>辅助参考：{Array.isArray(yongshen.辅助参考) ? yongshen.辅助参考.join('、') : String(yongshen.辅助参考)}</div>}
                            {yongshen.核心判断要点 && <div>核心判断要点：{Array.isArray(yongshen.核心判断要点) ? yongshen.核心判断要点.join('；') : String(yongshen.核心判断要点)}</div>}
                          </div>
                        </div>
                      )}
                      {dongbian && (
                        <div className="bg-pink-500/10 rounded-xl p-4 border border-pink-500/20">
                          <div className="text-xs text-pink-300 font-semibold mb-2">动变分析</div>
                          <pre className="text-xs text-white whitespace-pre-wrap break-words">{JSON.stringify(dongbian, null, 2)}</pre>
                        </div>
                      )}
                    </div>
                  )
                })()}

                {/* 操作按钮 */}
                <div className="flex gap-3">
                  <button
                    onClick={handleAIInterpret}
                    disabled={aiLoading}
                    className="flex-1 py-3 rounded-xl font-bold text-white bg-gradient-to-r from-purple-500 to-pink-500 hover:opacity-90 transition-all flex items-center justify-center space-x-2 shadow-lg shadow-purple-500/30"
                  >
                    <Sparkles size={18} className={aiLoading ? 'animate-spin' : ''} />
                    <span>AI解读</span>
                    <span className="px-2 py-0.5 bg-white/20 rounded-full text-xs">{POINTS_COST.AI_INTERPRET}积分</span>
                  </button>
                  <button onClick={handleFavorite} className="p-3 rounded-xl bg-yellow-500/20 text-yellow-400 hover:bg-yellow-500/30 transition-all border border-yellow-500/30" title="收藏">
                    <Star size={18} />
                  </button>
                  <button onClick={handleShare} className="p-3 rounded-xl bg-green-500/20 text-green-400 hover:bg-green-500/30 transition-all border border-green-500/30" title="分享">
                    <Share2 size={18} />
                  </button>
                  <button onClick={handleSaveRecord} className="p-3 rounded-xl bg-blue-500/20 text-blue-400 hover:bg-blue-500/30 transition-all border border-blue-500/30" title="保存记录">
                    <History size={18} />
                  </button>
                </div>
              </div>
            )}
          </HexagramAnimation>
        )}

        {/* AI 解读结果 */}
        {(aiLoading || aiResult) && (
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-5 mb-5">
            <h3 className="text-base font-bold text-white mb-3 flex items-center">
              <Sparkles className="w-5 h-5 text-purple-400 mr-2" />
              AI解读
            </h3>
            <div className="bg-gradient-to-r from-purple-500/10 to-pink-500/10 rounded-xl p-4 border border-purple-500/20">
              <ThinkingChain isThinking={aiLoading} finalContent={aiResult} />
            </div>
          </div>
        )}

        {/* 微信联系方式 - 占卜完成后显示 */}
        {result && (
          <WeChatContact 
            className="mb-5" 
            relatedRecordId={result?.original?.id || result?.data?.original?.id || null}
          />
        )}

        {/* 积分提示（统一展示后端积分余额） */}
        <div className="bg-amber-500/10 rounded-xl p-4 border border-amber-500/20">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Coins size={18} className="text-amber-400" />
              <span className="text-sm text-amber-300">
                当前积分：{isLoggedIn ? (credits ?? 0) : userPoints}
              </span>
            </div>
            <button
              onClick={() => navigate('/dashboard')}
              className="text-sm text-amber-400 font-medium hover:text-amber-300 transition-colors"
            >
              获取更多积分 →
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
