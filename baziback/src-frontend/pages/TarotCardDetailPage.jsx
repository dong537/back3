import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Activity, ArrowLeft, BookOpen, Briefcase, DollarSign, Heart } from 'lucide-react'
import Card, { CardContent, CardHeader, CardTitle } from '../components/Card'
import Button from '../components/Button'
import { tarotApi } from '../api'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeText } from '../utils/displayText'

function getTarotDetailCopy(locale) {
  const isEn = locale === 'en-US'

  return {
    locale,
    missingParam: isEn ? 'Missing Tarot card parameter' : '缺少塔罗牌参数',
    notFoundToast: isEn ? 'Tarot card not found' : '未找到该塔罗牌',
    loadFailedToast: isEn ? 'Failed to load Tarot card details' : '加载塔罗牌详情失败',
    loading: isEn ? 'Loading Tarot card...' : '正在加载塔罗牌...',
    unavailableTitle: isEn ? 'This Tarot card detail is unavailable right now' : '暂时无法加载这张塔罗牌的详情',
    unavailableDesc: isEn ? 'The card library may be incomplete, or the request did not return usable data.' : '可能是牌库数据不完整，或网络请求没有返回可用数据。',
    back: isEn ? 'Back' : '返回',
    unknownCard: isEn ? 'Unknown Card' : '未知牌名',
    noContent: isEn ? 'No details yet' : '暂无内容',
    description: isEn ? 'Card Description' : '牌面描述',
    uprightKeywords: isEn ? 'Upright Keywords' : '正位关键词',
    reversedKeywords: isEn ? 'Reversed Keywords' : '逆位关键词',
    uprightMeaning: isEn ? 'Upright Meaning' : '正位含义',
    reversedMeaning: isEn ? 'Reversed Meaning' : '逆位含义',
    detailInterpretation: isEn ? 'Detailed Insight' : '详细解读',
    advice: isEn ? 'Advice' : '建议',
    upright: isEn ? 'Upright' : '正位',
    reversed: isEn ? 'Reversed' : '逆位',
    majorArcana: isEn ? 'Major Arcana' : '大阿卡纳',
    minorArcana: isEn ? 'Minor Arcana' : '小阿卡纳',
    tabs: [
      { id: 'general', label: isEn ? 'Overview' : '综合解读', icon: BookOpen },
      { id: 'love', label: isEn ? 'Love' : '爱情', icon: Heart },
      { id: 'career', label: isEn ? 'Career' : '事业', icon: Briefcase },
      { id: 'wealth', label: isEn ? 'Wealth' : '财运', icon: DollarSign },
      { id: 'health', label: isEn ? 'Health' : '健康', icon: Activity },
    ],
    sectionTitles: {
      love: isEn ? 'Love Reading' : '爱情解读',
      career: isEn ? 'Career Reading' : '事业解读',
      wealth: isEn ? 'Wealth Reading' : '财运解读',
      health: isEn ? 'Health Reading' : '健康解读',
    },
    emptyStates: {
      general: isEn ? 'No overall interpretation yet' : '暂无综合解读',
      love: isEn ? 'No love reading yet' : '暂无爱情解读',
      career: isEn ? 'No career reading yet' : '暂无事业解读',
      wealth: isEn ? 'No wealth reading yet' : '暂无财运解读',
      health: isEn ? 'No health reading yet' : '暂无健康解读',
    },
    suits: {
      WANDS: isEn ? 'Wands' : '权杖',
      CUPS: isEn ? 'Cups' : '圣杯',
      SWORDS: isEn ? 'Swords' : '宝剑',
      PENTACLES: isEn ? 'Pentacles' : '星币',
    },
  }
}

function pickText(...values) {
  for (const value of values) {
    const text = safeText(value)
    if (text) return text
  }
  return ''
}

function getCardName(card, locale, fallback) {
  if (locale === 'en-US') {
    return pickText(card?.cardNameEn, card?.card_name_en, card?.cardNameCn, card?.card_name_cn, fallback)
  }

  return pickText(card?.cardNameCn, card?.card_name_cn, card?.cardNameEn, card?.card_name_en, fallback)
}

function getSecondaryCardName(card, locale) {
  if (locale === 'en-US') {
    return pickText(card?.cardNameCn, card?.card_name_cn)
  }

  return pickText(card?.cardNameEn, card?.card_name_en)
}

function getCardTypeLabel(cardType, copy) {
  const type = safeText(cardType).toUpperCase()
  if (!type) return ''
  return type === 'MAJOR_ARCANA' ? copy.majorArcana : copy.minorArcana
}

function getSuitLabel(suit, copy) {
  const key = safeText(suit).toUpperCase()
  if (!key || key === 'NONE') return ''
  return copy.suits[key] || safeText(suit)
}

function normalizeCard(card, locale, copy) {
  return {
    symbol: pickText(card?.symbol, '🎴'),
    primaryName: getCardName(card, locale, copy.unknownCard),
    secondaryName: getSecondaryCardName(card, locale),
    typeLabel: getCardTypeLabel(card?.cardType || card?.card_type, copy),
    suitLabel: getSuitLabel(card?.suit, copy),
    description: pickText(card?.description),
    keywordUp: pickText(card?.keywordUp, card?.keyword_up, copy.noContent),
    keywordRev: pickText(card?.keywordRev, card?.keyword_rev, copy.noContent),
    meaningUp: pickText(card?.meaningUp, card?.meaning_up),
    meaningRev: pickText(card?.meaningRev, card?.meaning_rev),
    interpretationUp: pickText(card?.interpretationUp, card?.interpretation_up),
    interpretationRev: pickText(card?.interpretationRev, card?.interpretation_rev),
    adviceUp: pickText(card?.adviceUp, card?.advice_up),
    adviceRev: pickText(card?.adviceRev, card?.advice_rev),
    loveUp: pickText(card?.loveUp, card?.love_up),
    loveRev: pickText(card?.loveRev, card?.love_rev),
    careerUp: pickText(card?.careerUp, card?.career_up),
    careerRev: pickText(card?.careerRev, card?.career_rev),
    wealthUp: pickText(card?.wealthUp, card?.wealth_up),
    wealthRev: pickText(card?.wealthRev, card?.wealth_rev),
    healthUp: pickText(card?.healthUp, card?.health_up),
    healthRev: pickText(card?.healthRev, card?.health_rev),
  }
}

function renderOrientationPanel(title, content, toneClass) {
  if (!content) return null

  return (
    <div className={`p-5 rounded-xl border-2 backdrop-blur-sm ${toneClass}`}>
      <div className="font-bold mb-3 flex items-center space-x-2">
        <div className="w-2 h-2 rounded-full animate-pulse bg-current"></div>
        <span>{title}</span>
      </div>
      <div className="text-white leading-relaxed text-base">{content}</div>
    </div>
  )
}

function DetailSection({ icon: Icon, title, emptyText, upText, revText, uprightLabel, reversedLabel, accent }) {
  const accents = {
    pink: {
      card: 'panel bg-[linear-gradient(180deg,rgba(41,21,18,0.88),rgba(18,13,12,0.8))] border-[#a34224]/24',
      icon: 'text-[#e19a84]',
      title: 'text-[#f0d9a5]',
    },
    blue: {
      card: 'panel bg-[linear-gradient(180deg,rgba(41,28,18,0.88),rgba(18,13,12,0.8))] border-[#d0a85b]/24',
      icon: 'text-[#dcb86f]',
      title: 'text-[#f0d9a5]',
    },
    amber: {
      card: 'panel bg-[linear-gradient(180deg,rgba(49,34,19,0.88),rgba(18,13,12,0.8))] border-[#d0a85b]/24',
      icon: 'text-[#f0d9a5]',
      title: 'text-[#f0d9a5]',
    },
    emerald: {
      card: 'panel bg-[linear-gradient(180deg,rgba(38,28,19,0.88),rgba(18,13,12,0.8))] border-[#b88a3d]/24',
      icon: 'text-[#dcb86f]',
      title: 'text-[#f0d9a5]',
    },
  }

  const palette = accents[accent] || accents.blue
  const hasContent = Boolean(upText || revText)

  return (
    <Card className={palette.card}>
      <CardHeader>
        <CardTitle className="flex items-center space-x-3 text-xl">
          <Icon size={24} className={palette.icon} />
          <span className={palette.title}>{title}</span>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          {renderOrientationPanel(uprightLabel, upText, 'from-green-500/20 to-emerald-600/20 border-green-400/40 text-green-200 bg-gradient-to-br')}
          {renderOrientationPanel(reversedLabel, revText, 'from-red-500/20 to-rose-600/20 border-red-400/40 text-red-200 bg-gradient-to-br')}
          {!hasContent && (
            <div className="py-12 text-center text-[#8f7b66]">
              <Icon size={48} className="mx-auto mb-4 opacity-30" />
              <div>{emptyText}</div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

export default function TarotCardDetailPage() {
  const { cardName } = useParams()
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = getTarotDetailCopy(locale)

  const [card, setCard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('general')

  useEffect(() => {
    let cancelled = false

    async function loadCardDetail() {
      if (!cardName) {
        toast.error(copy.missingParam)
        navigate('/tarot')
        return
      }

      setLoading(true)
      setActiveTab('general')

      let decodedCardName = cardName
      try {
        decodedCardName = decodeURIComponent(cardName)
      } catch (error) {
        logger.warn('Failed to decode tarot card route param:', cardName, error)
      }

      try {
        logger.debug('Loading card detail for:', decodedCardName)

        const isNumericId = /^\d+$/.test(String(decodedCardName))
        const response = isNumericId
          ? await tarotApi.getCardDetailById(Number(decodedCardName))
          : await tarotApi.getCardDetail(decodedCardName)

        logger.debug('Card detail response:', response)

        const payload = response?.data
        const data = payload?.data
        const ok = payload?.code === 200 || payload?.success === true

        if (!cancelled) {
          if (ok && data) {
            setCard(data)
          } else {
            logger.warn('Card not found:', decodedCardName, payload)
            toast.error(copy.notFoundToast)
            setCard(null)
          }
        }
      } catch (error) {
        logger.error('Load card detail error:', error)
        if (!cancelled) {
          toast.error(copy.loadFailedToast)
          setCard(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadCardDetail()

    return () => {
      cancelled = true
    }
  }, [cardName, copy.loadFailedToast, copy.missingParam, copy.notFoundToast, navigate])

  if (loading) {
    return (
      <div className="page-shell" data-theme="tarot">
        <div className="flex min-h-screen items-center justify-center">
          <div className="text-center">
            <div className="relative">
              <div className="text-8xl mb-6 animate-bounce filter drop-shadow-2xl">🎴</div>
              <div className="absolute inset-0 text-8xl mb-6 animate-ping opacity-20">🎴</div>
            </div>
            <div className="mb-2 text-lg font-medium text-[#dcb86f]">{copy.loading}</div>
            <div className="flex items-center justify-center space-x-2">
              <div className="h-2 w-2 rounded-full bg-[#a34224] animate-pulse"></div>
              <div className="h-2 w-2 rounded-full bg-[#d0a85b] animate-pulse delay-75"></div>
              <div className="h-2 w-2 rounded-full bg-[#e19a84] animate-pulse delay-150"></div>
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
          <div className="mb-4 text-xl text-[#f4ece1]">{copy.unavailableTitle}</div>
          <p className="mb-8 text-sm text-[#bdaa94]">{copy.unavailableDesc}</p>
          <Button
            onClick={() => navigate(-1)}
            variant="secondary"
            className="border-white/10 bg-white/[0.04] backdrop-blur-sm hover:bg-white/[0.08]"
            title={copy.back}
            aria-label={copy.back}
          >
            <ArrowLeft size={16} className="mr-1" />
            {copy.back}
          </Button>
        </div>
      </div>
    )
  }

  const normalizedCard = normalizeCard(card, locale, copy)
  const generalHasContent = Boolean(
    normalizedCard.meaningUp ||
      normalizedCard.meaningRev ||
      normalizedCard.interpretationUp ||
      normalizedCard.interpretationRev ||
      normalizedCard.adviceUp ||
      normalizedCard.adviceRev
  )

  const detailTabs = copy.tabs

  return (
    <div className="page-shell" data-theme="tarot">
      <div className="max-w-5xl mx-auto px-4">
        <div className="mb-6">
          <Button
            onClick={() => navigate(-1)}
            variant="secondary"
            size="sm"
            className="border-white/10 bg-white/[0.04] backdrop-blur-sm hover:bg-white/[0.08]"
            title={copy.back}
            aria-label={copy.back}
          >
            <ArrowLeft size={16} />
            <span>{copy.back}</span>
          </Button>
        </div>

        <Card className="panel gilded-border mb-8 border-[#d0a85b]/24 bg-[linear-gradient(180deg,rgba(41,23,18,0.92),rgba(18,13,12,0.84))] shadow-2xl shadow-[rgba(0,0,0,0.28)]" glow>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-6">
                <div className="relative">
                  <div className="text-8xl filter drop-shadow-2xl animate-pulse-slow">{normalizedCard.symbol}</div>
                  <div className="absolute inset-0 text-8xl opacity-20 blur-xl">{normalizedCard.symbol}</div>
                </div>
                <div>
                  <CardTitle className="mb-2 bg-[linear-gradient(135deg,#f6e7cf_0%,#dcb86f_52%,#e19a84_100%)] bg-clip-text text-4xl font-serif-title text-transparent">
                    {normalizedCard.primaryName}
                  </CardTitle>
                  {normalizedCard.secondaryName && (
                    <div className="mt-1 text-lg font-medium text-[#bdaa94]">{normalizedCard.secondaryName}</div>
                  )}
                  <div className="flex items-center space-x-3 mt-3">
                    {normalizedCard.typeLabel && (
                      <span className="rounded-lg border border-[#a34224]/24 bg-[#7a3218]/18 px-3 py-1.5 text-xs font-semibold text-[#e19a84] backdrop-blur-sm">
                        {normalizedCard.typeLabel}
                      </span>
                    )}
                    {normalizedCard.suitLabel && (
                      <span className="rounded-lg border border-[#d0a85b]/24 bg-[#6a4a1e]/18 px-3 py-1.5 text-xs font-semibold text-[#f0d9a5] backdrop-blur-sm">
                        {normalizedCard.suitLabel}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {normalizedCard.description && (
              <div className="mb-6 rounded-xl border border-white/10 bg-white/[0.04] p-5 backdrop-blur-sm">
                <div className="mb-3 flex items-center space-x-2 text-sm font-semibold text-[#dcb86f]">
                  <BookOpen size={16} />
                  <span>{copy.description}</span>
                </div>
                <div className="text-white leading-relaxed text-base">{normalizedCard.description}</div>
              </div>
            )}

            <div className="grid md:grid-cols-2 gap-4 mb-6">
              <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border-2 border-green-400/40 backdrop-blur-sm hover:border-green-400/60 transition-all">
                <div className="text-sm text-green-200 font-bold mb-3 flex items-center space-x-2">
                  <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                  <span>{copy.uprightKeywords}</span>
                </div>
                <div className="text-white text-lg font-medium">{normalizedCard.keywordUp}</div>
              </div>
              <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border-2 border-red-400/40 backdrop-blur-sm hover:border-red-400/60 transition-all">
                <div className="text-sm text-red-200 font-bold mb-3 flex items-center space-x-2">
                  <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                  <span>{copy.reversedKeywords}</span>
                </div>
                <div className="text-white text-lg font-medium">{normalizedCard.keywordRev}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="mb-8">
          <div className="flex space-x-3 overflow-x-auto pb-2 scrollbar-hide">
            {detailTabs.map((tab) => {
              const Icon = tab.icon

              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  title={tab.label}
                  aria-label={tab.label}
                  aria-pressed={activeTab === tab.id}
                  className={`group relative flex items-center space-x-2 px-5 py-3 rounded-xl transition-all duration-300 whitespace-nowrap font-medium ${
                    activeTab === tab.id
                      ? 'bg-[linear-gradient(135deg,rgba(163,66,36,0.22),rgba(208,168,91,0.18))] border-2 border-[#d0a85b]/35 text-white shadow-lg shadow-[rgba(163,66,36,0.18)] scale-105'
                      : 'bg-white/[0.04] border-2 border-white/10 text-[#bdaa94] hover:bg-white/[0.08] hover:border-[#d0a85b]/24 hover:text-white hover:scale-105'
                  }`}
                >
                  {activeTab === tab.id && (
                    <div className="absolute inset-0 rounded-xl bg-[linear-gradient(135deg,rgba(163,66,36,0.14),rgba(208,168,91,0.12))] animate-pulse"></div>
                  )}
                  <Icon size={18} className={`relative z-10 ${activeTab === tab.id ? 'text-[#f0d9a5]' : 'text-[#8f7b66] group-hover:text-[#dcb86f]'}`} />
                  <span className="relative z-10">{tab.label}</span>
                </button>
              )
            })}
          </div>
        </div>

        <div className="space-y-6">
          {activeTab === 'general' && (
            <>
              <Card className="panel mb-6 bg-gradient-to-br from-green-900/30 to-emerald-900/20 border-green-400/30">
                <CardHeader>
                  <CardTitle className="text-green-200 flex items-center space-x-2 text-xl">
                    <div className="w-3 h-3 bg-green-400 rounded-full animate-pulse"></div>
                    <span>{copy.uprightMeaning}</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-5">
                    {normalizedCard.meaningUp && (
                      <div className="p-5 bg-gradient-to-br from-green-500/20 to-emerald-600/20 rounded-xl border border-green-400/40 backdrop-blur-sm">
                        <div className="text-white leading-relaxed text-base">{normalizedCard.meaningUp}</div>
                      </div>
                    )}
                    {normalizedCard.interpretationUp && (
                      <div className="p-5 bg-white/5 rounded-xl border border-white/10">
                        <div className="text-sm text-green-300 font-semibold mb-3 flex items-center space-x-2">
                          <BookOpen size={16} />
                          <span>{copy.detailInterpretation}</span>
                        </div>
                        <div className="text-white leading-relaxed text-base">{normalizedCard.interpretationUp}</div>
                      </div>
                    )}
                    {normalizedCard.adviceUp && (
                      <div className="p-5 bg-gradient-to-br from-yellow-500/20 to-amber-600/20 rounded-xl border-2 border-yellow-400/40 backdrop-blur-sm">
                        <div className="text-sm text-yellow-200 font-bold mb-2 flex items-center space-x-2">
                          <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                          <span>{copy.advice}</span>
                        </div>
                        <div className="text-yellow-50 text-base leading-relaxed">{normalizedCard.adviceUp}</div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              <Card className="panel mb-6 bg-gradient-to-br from-red-900/30 to-rose-900/20 border-red-400/30">
                <CardHeader>
                  <CardTitle className="text-red-200 flex items-center space-x-2 text-xl">
                    <div className="w-3 h-3 bg-red-400 rounded-full animate-pulse"></div>
                    <span>{copy.reversedMeaning}</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-5">
                    {normalizedCard.meaningRev && (
                      <div className="p-5 bg-gradient-to-br from-red-500/20 to-rose-600/20 rounded-xl border border-red-400/40 backdrop-blur-sm">
                        <div className="text-white leading-relaxed text-base">{normalizedCard.meaningRev}</div>
                      </div>
                    )}
                    {normalizedCard.interpretationRev && (
                      <div className="p-5 bg-white/5 rounded-xl border border-white/10">
                        <div className="text-sm text-red-300 font-semibold mb-3 flex items-center space-x-2">
                          <BookOpen size={16} />
                          <span>{copy.detailInterpretation}</span>
                        </div>
                        <div className="text-white leading-relaxed text-base">{normalizedCard.interpretationRev}</div>
                      </div>
                    )}
                    {normalizedCard.adviceRev && (
                      <div className="p-5 bg-gradient-to-br from-orange-500/20 to-amber-600/20 rounded-xl border-2 border-orange-400/40 backdrop-blur-sm">
                        <div className="text-sm text-orange-200 font-bold mb-2 flex items-center space-x-2">
                          <div className="w-2 h-2 bg-orange-400 rounded-full"></div>
                          <span>{copy.advice}</span>
                        </div>
                        <div className="text-orange-50 text-base leading-relaxed">{normalizedCard.adviceRev}</div>
                      </div>
                    )}
                    {!generalHasContent && (
                      <div className="py-12 text-center text-[#8f7b66]">
                        <BookOpen size={48} className="mx-auto mb-4 opacity-30" />
                        <div>{copy.emptyStates.general}</div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </>
          )}

          {activeTab === 'love' && (
            <DetailSection
              icon={Heart}
              title={copy.sectionTitles.love}
              emptyText={copy.emptyStates.love}
              upText={normalizedCard.loveUp}
              revText={normalizedCard.loveRev}
              uprightLabel={copy.upright}
              reversedLabel={copy.reversed}
              accent="pink"
            />
          )}

          {activeTab === 'career' && (
            <DetailSection
              icon={Briefcase}
              title={copy.sectionTitles.career}
              emptyText={copy.emptyStates.career}
              upText={normalizedCard.careerUp}
              revText={normalizedCard.careerRev}
              uprightLabel={copy.upright}
              reversedLabel={copy.reversed}
              accent="blue"
            />
          )}

          {activeTab === 'wealth' && (
            <DetailSection
              icon={DollarSign}
              title={copy.sectionTitles.wealth}
              emptyText={copy.emptyStates.wealth}
              upText={normalizedCard.wealthUp}
              revText={normalizedCard.wealthRev}
              uprightLabel={copy.upright}
              reversedLabel={copy.reversed}
              accent="amber"
            />
          )}

          {activeTab === 'health' && (
            <DetailSection
              icon={Activity}
              title={copy.sectionTitles.health}
              emptyText={copy.emptyStates.health}
              upText={normalizedCard.healthUp}
              revText={normalizedCard.healthRev}
              uprightLabel={copy.upright}
              reversedLabel={copy.reversed}
              accent="emerald"
            />
          )}
        </div>
      </div>
    </div>
  )
}
