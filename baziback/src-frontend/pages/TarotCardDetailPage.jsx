import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Heart, Briefcase, DollarSign, Activity, BookOpen } from 'lucide-react'
import Card, { CardHeader, CardTitle, CardContent } from '../components/Card'
import Button from '../components/Button'
import { tarotApi } from '../api'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'

export default function TarotCardDetailPage() {
  const { cardName } = useParams()
  const navigate = useNavigate()
  const [card, setCard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('general') // general, love, career, wealth, health

  useEffect(() => {
    loadCardDetail()
  }, [cardName])

  const loadCardDetail = async () => {
    if (!cardName) {
      toast.error('牌名参数缺失')
      navigate('/tarot')
      return
    }
    
    try {
      setLoading(true)
      // 解码URL参数
      const decodedCardName = decodeURIComponent(cardName)
      logger.debug('Loading card detail for:', decodedCardName)
      
      const isNumericId = /^\d+$/.test(String(decodedCardName))
      const response = isNumericId
        ? await tarotApi.getCardDetailById(Number(decodedCardName))
        : await tarotApi.getCardDetail(decodedCardName)
      logger.debug('Card detail response:', response)
      
      const payload = response?.data
      const ok = payload?.code === 200 || payload?.success === true
      if (ok && payload?.data) {
        setCard(payload.data)
      } else {
        // 尝试使用错误信息
        const errorMsg = payload?.message || '未找到该塔罗牌'
        logger.warn('Card not found:', decodedCardName, errorMsg)
        toast.error(errorMsg)
        setCard(null)
      }
    } catch (error) {
      logger.error('Load card detail error:', error)
      const errorMessage = error.response?.data?.message || error.message || '加载塔罗牌详情失败'
      toast.error(errorMessage)
      setCard(null)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="page-shell" data-theme="tarot">
        <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-purple-900 via-indigo-900 to-blue-900">
          <div className="text-center">
            <div className="relative">
              <div className="text-8xl mb-6 animate-bounce filter drop-shadow-2xl">🎴</div>
              <div className="absolute inset-0 text-8xl mb-6 animate-ping opacity-20">🎴</div>
            </div>
            <div className="text-purple-300 text-lg font-medium mb-2">正在加载塔罗牌...</div>
            <div className="flex items-center justify-center space-x-2">
              <div className="w-2 h-2 bg-purple-400 rounded-full animate-pulse"></div>
              <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse delay-75"></div>
              <div className="w-2 h-2 bg-pink-400 rounded-full animate-pulse delay-150"></div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!card) {
    return (
      <div className="page-shell" data-theme="tarot">
        <div className="max-w-3xl mx-auto px-4 py-16 text-center">
          <div className="text-7xl mb-4">🎴</div>
          <div className="text-xl text-purple-100 mb-4">暂时无法加载这张塔罗牌的详情</div>
          <p className="text-sm text-purple-200/80 mb-8">可能是牌库数据不完整，或网络请求出现了问题。</p>
          <Button
            onClick={() => navigate(-1)}
            variant="secondary"
            className="bg-white/10 hover:bg-white/20 border-white/20 backdrop-blur-sm"
          >
            <ArrowLeft size={16} className="mr-1" />
            返回
          </Button>
        </div>
      </div>
    )
  }

  const tabs = [
    { id: 'general', label: '综合解读', icon: BookOpen },
    { id: 'love', label: '爱情', icon: Heart },
    { id: 'career', label: '事业', icon: Briefcase },
    { id: 'wealth', label: '财运', icon: DollarSign },
    { id: 'health', label: '健康', icon: Activity },
  ]

  return (
    <div className="page-shell" data-theme="tarot">
      <div className="max-w-5xl mx-auto px-4">
        {/* 返回按钮 */}
        <div className="mb-6">
          <Button
            onClick={() => navigate(-1)}
            variant="secondary"
            size="sm"
            className="bg-white/10 hover:bg-white/20 border-white/20 backdrop-blur-sm"
          >
            <ArrowLeft size={16} />
            <span>返回</span>
          </Button>
        </div>

        {/* 牌面信息 */}
        <Card className="panel mb-8 gilded-border bg-gradient-to-br from-purple-900/50 via-indigo-900/40 to-blue-900/50 border-purple-400/40 shadow-2xl shadow-purple-900/30" glow>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-6">
                <div className="relative">
                  <div className="text-8xl filter drop-shadow-2xl animate-pulse-slow">{card.symbol || '🎴'}</div>
                  <div className="absolute inset-0 text-8xl opacity-20 blur-xl">{card.symbol || '🎴'}</div>
                </div>
                <div>
                  <CardTitle className="text-4xl font-serif-title mb-2 bg-gradient-to-r from-purple-200 via-pink-200 to-blue-200 bg-clip-text text-transparent">
                    {card.cardNameCn || card.card_name_cn || '未知牌名'}
                  </CardTitle>
                  <div className="text-gray-300 mt-1 text-lg font-medium">{card.cardNameEn || card.card_name_en || ''}</div>
                  <div className="flex items-center space-x-3 mt-3">
                    <span className="px-3 py-1.5 rounded-lg bg-gradient-to-r from-purple-500/30 to-purple-600/30 text-purple-200 text-xs font-semibold border border-purple-400/50 backdrop-blur-sm">
                      {card.cardType === 'MAJOR_ARCANA' ? '大阿卡纳' : '小阿卡纳'}
                    </span>
                    {card.suit && card.suit !== 'NONE' && (
                      <span className="px-3 py-1.5 rounded-lg bg-gradient-to-r from-blue-500/30 to-blue-600/30 text-blue-200 text-xs font-semibold border border-blue-400/50 backdrop-blur-sm">
                        {card.suit === 'WANDS' ? '权杖' : 
                         card.suit === 'CUPS' ? '圣杯' : 
                         card.suit === 'SWORDS' ? '宝剑' : '星币'}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {card.description && (
              <div className="mb-6 p-5 bg-gradient-to-br from-white/10 to-white/5 rounded-xl border border-white/20 backdrop-blur-sm">
                <div className="text-sm text-purple-300 font-semibold mb-3 flex items-center space-x-2">
                  <BookOpen size={16} />
                  <span>牌面描述</span>
                </div>
                <div className="text-white leading-relaxed text-base">{card.description}</div>
              </div>
            )}

            {/* 关键词 */}
            <div className="grid md:grid-cols-2 gap-4 mb-6">
              <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border-2 border-green-400/40 backdrop-blur-sm hover:border-green-400/60 transition-all">
                <div className="text-sm text-green-200 font-bold mb-3 flex items-center space-x-2">
                  <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                  <span>正位关键词</span>
                </div>
                <div className="text-white text-lg font-medium">{card.keywordUp || '暂无'}</div>
              </div>
              <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border-2 border-red-400/40 backdrop-blur-sm hover:border-red-400/60 transition-all">
                <div className="text-sm text-red-200 font-bold mb-3 flex items-center space-x-2">
                  <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                  <span>逆位关键词</span>
                </div>
                <div className="text-white text-lg font-medium">{card.keywordRev || '暂无'}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 标签页 */}
        <div className="mb-8">
          <div className="flex space-x-3 overflow-x-auto pb-2 scrollbar-hide">
            {tabs.map(tab => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`group relative flex items-center space-x-2 px-5 py-3 rounded-xl transition-all duration-300 whitespace-nowrap font-medium ${
                    activeTab === tab.id
                      ? 'bg-gradient-to-r from-purple-500/30 to-blue-500/30 border-2 border-purple-400/60 text-white shadow-lg shadow-purple-500/30 scale-105'
                      : 'bg-white/5 border-2 border-white/10 text-gray-300 hover:bg-white/10 hover:border-purple-400/30 hover:text-white hover:scale-105'
                  }`}
                >
                  {activeTab === tab.id && (
                    <div className="absolute inset-0 bg-gradient-to-r from-purple-500/20 to-blue-500/20 rounded-xl animate-pulse"></div>
                  )}
                  <Icon size={18} className={`relative z-10 ${activeTab === tab.id ? 'text-purple-200' : 'text-gray-400 group-hover:text-purple-300'}`} />
                  <span className="relative z-10">{tab.label}</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* 内容区域 */}
        <div className="space-y-6">
          {/* 综合解读 */}
          {activeTab === 'general' && (
            <>
              <Card className="panel mb-6 bg-gradient-to-br from-green-900/30 to-emerald-900/20 border-green-400/30">
                <CardHeader>
                  <CardTitle className="text-green-200 flex items-center space-x-2 text-xl">
                    <div className="w-3 h-3 bg-green-400 rounded-full animate-pulse"></div>
                    <span>正位含义</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-5">
                    {card.meaningUp && (
                      <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border border-green-400/40 backdrop-blur-sm">
                        <div className="text-white leading-relaxed text-base">{card.meaningUp}</div>
                      </div>
                    )}
                    {card.interpretationUp && (
                      <div className="p-5 bg-white/5 rounded-xl border border-white/10">
                        <div className="text-sm text-green-300 font-semibold mb-3 flex items-center space-x-2">
                          <BookOpen size={16} />
                          <span>详细解读</span>
                        </div>
                        <div className="text-white leading-relaxed text-base">{card.interpretationUp}</div>
                      </div>
                    )}
                    {card.adviceUp && (
                      <div className="p-5 bg-gradient-to-br from-yellow-500/20 to-amber-600/20 rounded-xl border-2 border-yellow-400/40 backdrop-blur-sm">
                        <div className="text-sm text-yellow-200 font-bold mb-2 flex items-center space-x-2">
                          <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                          <span>建议</span>
                        </div>
                        <div className="text-yellow-50 text-base leading-relaxed">{card.adviceUp}</div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              <Card className="panel mb-6 bg-gradient-to-br from-red-900/30 to-rose-900/20 border-red-400/30">
                <CardHeader>
                  <CardTitle className="text-red-200 flex items-center space-x-2 text-xl">
                    <div className="w-3 h-3 bg-red-400 rounded-full animate-pulse"></div>
                    <span>逆位含义</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-5">
                    {card.meaningRev && (
                      <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border border-red-400/40 backdrop-blur-sm">
                        <div className="text-white leading-relaxed text-base">{card.meaningRev}</div>
                      </div>
                    )}
                    {card.interpretationRev && (
                      <div className="p-5 bg-white/5 rounded-xl border border-white/10">
                        <div className="text-sm text-red-300 font-semibold mb-3 flex items-center space-x-2">
                          <BookOpen size={16} />
                          <span>详细解读</span>
                        </div>
                        <div className="text-white leading-relaxed text-base">{card.interpretationRev}</div>
                      </div>
                    )}
                    {card.adviceRev && (
                      <div className="p-5 bg-gradient-to-br from-orange-500/20 to-amber-600/20 rounded-xl border-2 border-orange-400/40 backdrop-blur-sm">
                        <div className="text-sm text-orange-200 font-bold mb-2 flex items-center space-x-2">
                          <div className="w-2 h-2 bg-orange-400 rounded-full"></div>
                          <span>建议</span>
                        </div>
                        <div className="text-orange-50 text-base leading-relaxed">{card.adviceRev}</div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </>
          )}

          {/* 爱情 */}
          {activeTab === 'love' && (
            <Card className="panel">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Heart size={20} />
                  <span>爱情解读</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {card.loveUp && (
                    <div>
                      <div className="text-green-300 font-medium mb-2">正位</div>
                      <div className="p-4 bg-green-500/10 rounded-lg">
                        <div className="text-white leading-relaxed">{card.loveUp}</div>
                      </div>
                    </div>
                  )}
                  {card.loveRev && (
                    <div>
                      <div className="text-red-300 font-medium mb-2">逆位</div>
                      <div className="p-4 bg-red-500/10 rounded-lg">
                        <div className="text-white leading-relaxed">{card.loveRev}</div>
                      </div>
                    </div>
                  )}
                  {!card.loveUp && !card.loveRev && (
                    <div className="text-center text-gray-400 py-8">暂无爱情解读</div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {/* 事业 */}
          {activeTab === 'career' && (
            <Card className="panel bg-gradient-to-br from-blue-900/30 to-indigo-900/20 border-blue-400/30">
              <CardHeader>
                <CardTitle className="flex items-center space-x-3 text-xl">
                  <Briefcase size={24} className="text-blue-400" />
                  <span className="text-blue-200">事业解读</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {card.careerUp && (
                    <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border-2 border-green-400/40 backdrop-blur-sm">
                      <div className="text-green-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                        <span>正位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.careerUp}</div>
                    </div>
                  )}
                  {card.careerRev && (
                    <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border-2 border-red-400/40 backdrop-blur-sm">
                      <div className="text-red-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                        <span>逆位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.careerRev}</div>
                    </div>
                  )}
                  {!card.careerUp && !card.careerRev && (
                    <div className="text-center text-gray-400 py-12">
                      <Briefcase size={48} className="mx-auto mb-4 opacity-30" />
                      <div>暂无事业解读</div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {/* 财运 */}
          {activeTab === 'wealth' && (
            <Card className="panel bg-gradient-to-br from-yellow-900/30 to-amber-900/20 border-yellow-400/30">
              <CardHeader>
                <CardTitle className="flex items-center space-x-3 text-xl">
                  <DollarSign size={24} className="text-yellow-400" />
                  <span className="text-yellow-200">财运解读</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {card.wealthUp && (
                    <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border-2 border-green-400/40 backdrop-blur-sm">
                      <div className="text-green-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                        <span>正位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.wealthUp}</div>
                    </div>
                  )}
                  {card.wealthRev && (
                    <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border-2 border-red-400/40 backdrop-blur-sm">
                      <div className="text-red-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                        <span>逆位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.wealthRev}</div>
                    </div>
                  )}
                  {!card.wealthUp && !card.wealthRev && (
                    <div className="text-center text-gray-400 py-12">
                      <DollarSign size={48} className="mx-auto mb-4 opacity-30" />
                      <div>暂无财运解读</div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {/* 健康 */}
          {activeTab === 'health' && (
            <Card className="panel bg-gradient-to-br from-emerald-900/30 to-teal-900/20 border-emerald-400/30">
              <CardHeader>
                <CardTitle className="flex items-center space-x-3 text-xl">
                  <Activity size={24} className="text-emerald-400" />
                  <span className="text-emerald-200">健康解读</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {card.healthUp && (
                    <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border-2 border-green-400/40 backdrop-blur-sm">
                      <div className="text-green-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                        <span>正位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.healthUp}</div>
                    </div>
                  )}
                  {card.healthRev && (
                    <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border-2 border-red-400/40 backdrop-blur-sm">
                      <div className="text-red-200 font-bold mb-3 flex items-center space-x-2">
                        <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                        <span>逆位</span>
                      </div>
                      <div className="text-white leading-relaxed text-base">{card.healthRev}</div>
                    </div>
                  )}
                  {!card.healthUp && !card.healthRev && (
                    <div className="text-center text-gray-400 py-12">
                      <Activity size={48} className="mx-auto mb-4 opacity-30" />
                      <div>暂无健康解读</div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}
