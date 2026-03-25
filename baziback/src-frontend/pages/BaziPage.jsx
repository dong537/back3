import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  ArrowLeft,
  Calendar,
  ChevronRight,
  Coins,
  History,
  Sparkles,
  Star,
  Users,
} from 'lucide-react'
import ThinkingChain from '../components/ThinkingChain'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { baziApi, calculationRecordApi, deepseekApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { resolvePageLocale, safeArray, safeText } from '../utils/displayText'

const BAZI_STATE_KEY = 'bazi_page_state'
const PILLARS = [
  { key: 'year', label: 'Year' },
  { key: 'month', label: 'Month' },
  { key: 'day', label: 'Day' },
  { key: 'hour', label: 'Hour' },
]
const ZODIAC_EN = {
  鼠: 'Rat', 牛: 'Ox', 虎: 'Tiger', 兔: 'Rabbit', 龙: 'Dragon', 蛇: 'Snake',
  马: 'Horse', 羊: 'Goat', 猴: 'Monkey', 鸡: 'Rooster', 狗: 'Dog', 猪: 'Pig',
}

const BAZI_PAGE_COPY = {
  'zh-CN': {
    pageTitle: '八字',
    points: '积分',
    userCenter: '用户中心',
    back: '返回',
    records: '记录',
    comingSoonTitle: '该板块即将上线',
    comingSoonDesc: '先完善八字分析与双语切换体验',
    birthInfo: '出生信息',
    date: '日期',
    time: '时间',
    gender: '性别',
    male: '男',
    female: '女',
    analyze: '开始分析',
    fourPillars: '四柱',
    bazi: '八字',
    dayMaster: '日主',
    element: '五行',
    pattern: '格局',
    zodiac: '生肖',
    bodyStrength: '身强弱',
    taiYuan: '胎元',
    mingGong: '命宫',
    interpretation: '十神解读',
    aiReport: 'AI 报告',
    favorite: '收藏',
    save: '保存',
    aiReading: 'AI 解读',
    fillBirthInfo: '请填写出生日期和时间',
    analyzeFailed: '八字分析失败',
    notEnoughCredits: (cost, current) => `积分不足，需要 ${cost} 积分，当前为 ${current}`,
    notEnoughPoints: (cost) => `积分不足，需要 ${cost} 积分`,
    generationFailed: '生成失败',
    aiReportReason: 'AI 八字报告',
    spentCredits: (cost) => `已消耗 ${cost} 积分`,
    aiReportFailed: 'AI 报告生成失败',
    favoriteQuestion: '八字',
    favoriteAdded: '已收藏',
    favoriteRemoved: '已取消收藏',
    favoriteFailed: '收藏失败',
    saved: '记录已保存',
    saveFailed: '保存失败',
    tabs: [
      { key: 'bazi', label: '八字' },
      { key: 'star', label: '星座' },
      { key: 'lodge', label: '星宿' },
      { key: 'ziwei', label: '紫微' },
    ],
  },
  'en-US': {
    pageTitle: 'Bazi',
    points: 'Points',
    userCenter: 'User center',
    back: 'Back',
    records: 'Records',
    comingSoonTitle: 'This section is coming soon',
    comingSoonDesc: 'We are polishing Bazi analysis and bilingual switching first',
    birthInfo: 'Birth Info',
    date: 'Date',
    time: 'Time',
    gender: 'Gender',
    male: 'Male',
    female: 'Female',
    analyze: 'Analyze',
    fourPillars: 'Four Pillars',
    bazi: 'Bazi',
    dayMaster: 'Day Master',
    element: 'Element',
    pattern: 'Pattern',
    zodiac: 'Zodiac',
    bodyStrength: 'Body Strength',
    taiYuan: 'Tai Yuan',
    mingGong: 'Ming Gong',
    interpretation: 'Interpretation',
    aiReport: 'AI Report',
    favorite: 'Favorite',
    save: 'Save',
    aiReading: 'AI Reading',
    fillBirthInfo: 'Please fill in the birth date and time',
    analyzeFailed: 'Bazi analysis failed',
    notEnoughCredits: (cost, current) => `Not enough credits: need ${cost}, current ${current}`,
    notEnoughPoints: (cost) => `Not enough points: need ${cost}`,
    generationFailed: 'Generation failed',
    aiReportReason: 'AI Bazi report',
    spentCredits: (cost) => `Spent ${cost} credits`,
    aiReportFailed: 'AI report failed',
    favoriteQuestion: 'Bazi',
    favoriteAdded: 'Favorited',
    favoriteRemoved: 'Unfavorited',
    favoriteFailed: 'Favorite failed',
    saved: 'Saved',
    saveFailed: 'Save failed',
    tabs: [
      { key: 'bazi', label: 'Bazi' },
      { key: 'star', label: 'Star' },
      { key: 'lodge', label: 'Lodge' },
      { key: 'ziwei', label: 'Ziwei' },
    ],
  },
}

function tValue(isEn, zh, en) {
  return isEn ? en : zh
}

function translateGender(value, isEn) {
  if (!value) return ''
  if (!isEn) return value === 'male' ? '男' : value === 'female' ? '女' : value
  const normalized = String(value).toLowerCase()
  if (normalized === 'male' || value === '男') return 'Male'
  if (normalized === 'female' || value === '女') return 'Female'
  return value
}

function zodiacLabel(value, isEn) {
  if (!value) return ''
  return isEn ? ZODIAC_EN[value] || value : value
}

function normalizeInterpretation(item) {
  if (!item) return null
  return {
    ...item,
    position: item.position || item.type || '',
    mainContent: safeText(item.mainContent || item.content),
    basicDef: safeText(item.basicDef || item.summary),
  }
}

function InfoCard({ label, value }) {
  if (value === undefined || value === null || value === '') return null
  return (
    <div className="mystic-muted-box px-4 py-3">
      <div className="text-xs uppercase tracking-[0.18em] text-[#d0a85b]">{label}</div>
      <div className="mt-1 text-sm font-medium text-[#f4ece1]">{String(value)}</div>
    </div>
  )
}

function PlaceholderBlock({ title, description }) {
  return (
    <div className="panel mt-6 p-6 text-center">
      <div className="text-base font-bold text-[#f4ece1]">{title}</div>
      <p className="mt-2 text-sm text-[#bdaa94]">{description}</p>
    </div>
  )
}

export default function BaziPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const t = (zh, en) => tValue(isEn, zh, en)
  const copy = BAZI_PAGE_COPY[locale] || BAZI_PAGE_COPY['zh-CN']
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()

  const [birthDate, setBirthDate] = useState('')
  const [birthTime, setBirthTime] = useState('')
  const [gender, setGender] = useState('male')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [interpretations, setInterpretations] = useState([])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState('')
  const [activeTab, setActiveTab] = useState('bazi')
  const [userPoints, setUserPoints] = useState(points.get())

  useEffect(() => {
    if (isLoggedIn) refreshCredits()
  }, [isLoggedIn, refreshCredits])

  useEffect(() => {
    const saved = sessionStorage.getItem(BAZI_STATE_KEY)
    if (!saved) return
    try {
      const state = JSON.parse(saved)
      if (state.result) {
        setResult(state.result)
        setBirthDate(state.birthDate || '')
        setBirthTime(state.birthTime || '')
        setGender(state.gender || 'male')
        setInterpretations(safeArray(state.interpretations).map(normalizeInterpretation).filter(Boolean))
        setAiResult(state.aiResult || '')
      }
    } catch (error) {
      logger.error('restore bazi page failed', error)
    } finally {
      sessionStorage.removeItem(BAZI_STATE_KEY)
    }
  }, [location])

  useEffect(() => {
    const state = location?.state
    if (!state?.fromFavorite) return
    if (state.birthDate) setBirthDate(state.birthDate)
    if (state.birthTime) setBirthTime(state.birthTime)
    if (state.gender) setGender(state.gender)
    if (state.result) setResult(state.result)
    if (Array.isArray(state.interpretations)) {
      setInterpretations(state.interpretations.map(normalizeInterpretation).filter(Boolean))
    }
    if (typeof state.aiResult === 'string') setAiResult(state.aiResult)
  }, [location?.state])

  const tabs = copy.tabs

  const currentPoints = isLoggedIn ? credits : userPoints
  const summaryText = [
    result?.birthDateTime,
    translateGender(result?.gender || gender, isEn),
    zodiacLabel(result?.zodiac, isEn) || result?.zodiac,
  ].filter(Boolean).join(' / ')

  const handleAnalyze = async () => {
    if (!birthDate || !birthTime) {
      toast.error(copy.fillBirthInfo)
      return
    }
    setLoading(true)
    setResult(null)
    setInterpretations([])
    setAiResult('')
    sessionStorage.removeItem(BAZI_STATE_KEY)
    try {
      const birthDateTime = `${String(birthDate).replace(/\//g, '-')} ${birthTime}:00`
      const response = await baziApi.generate(birthDateTime, gender === 'male', 4)
      const resultData = response.data
      setResult(resultData)

      const interpRes = await baziApi.getInterpretationsFromBaziData(resultData)
      if (interpRes.data?.code === 200 && Array.isArray(interpRes.data.data)) {
        setInterpretations(interpRes.data.data.map(normalizeInterpretation).filter(Boolean))
      }

      if (resultData?.baZi) {
        historyStorage.add({
          type: 'bazi',
          question: `Bazi - ${birthDate} ${birthTime}`,
          dataId: resultData.baZi.replace(/\s/g, ''),
          summary: `${resultData.baZi} - ${translateGender(resultData.gender || gender, isEn)}`,
          data: resultData,
        })
      }
    } catch (error) {
      logger.error('bazi analyze error', error)
      toast.error(error?.response?.data?.error || error?.message || copy.analyzeFailed)
    } finally {
      setLoading(false)
    }
  }

  const handleAIReport = async () => {
    if (!result) return
    const cost = POINTS_COST.AI_INTERPRET
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(copy.notEnoughCredits(cost, credits))
        return
      }
    } else if (!points.canSpend(cost)) {
      toast.error(copy.notEnoughPoints(cost))
      return
    }

    setAiLoading(true)
    setAiResult('')
    try {
      const prompt = isEn
        ? `Analyze this Bazi in plain text. Bazi: ${result.baZi}; Gender: ${translateGender(result.gender, true)}; Day master: ${result.dayMaster}; Element: ${result.dayMasterElement}; Pattern: ${result.geJu}.`
        : `请用纯文本分析这组八字。八字：${result.baZi}；性别：${translateGender(result.gender, false)}；日主：${result.dayMaster}；五行：${result.dayMasterElement}；格局：${result.geJu}。`
      const response = await deepseekApi.generateReport(prompt)
      const content =
        response.data?.data ||
        response.data?.content ||
        response.data ||
        copy.generationFailed

      if (isLoggedIn) {
        const spendResult = await spendCredits(cost, copy.aiReportReason)
        if (spendResult.success) {
          toast.success(copy.spentCredits(cost))
        }
      } else {
        const spendResult = points.spend(cost, copy.aiReportReason)
        if (spendResult.success) {
          setUserPoints(spendResult.newTotal)
          toast.success(copy.spentCredits(cost))
        }
      }
      setAiResult(String(content))
    } catch (error) {
      logger.error('ai bazi error', error)
      toast.error(copy.aiReportFailed)
      setAiResult(copy.aiReportFailed)
    } finally {
      setAiLoading(false)
    }
  }

  const handleFavorite = async () => {
    if (!result) return
    try {
      const favorited = await favoritesStorage.toggle({
        type: 'bazi',
        question: `${copy.favoriteQuestion} - ${birthDate}`,
        dataId: result.baZi?.replace(/\s/g, '') || '',
        title: `${copy.favoriteQuestion}: ${result.baZi || ''}`,
        summary: `${translateGender(result.gender || gender, isEn)} - ${zodiacLabel(result.zodiac, isEn) || result.zodiac || ''}`,
        data: result,
      })
      toast.success(favorited ? copy.favoriteAdded : copy.favoriteRemoved)
    } catch (error) {
      logger.error('favorite failed', error)
      toast.error(copy.favoriteFailed)
    }
  }

  const handleSaveRecord = async () => {
    if (!result) return
    try {
      await calculationRecordApi.save({
        recordType: 'bazi',
        recordTitle: `${copy.favoriteQuestion} - ${birthDate}`,
        question: `${copy.gender}: ${translateGender(gender, isEn)}`,
        summary: `${result.baZi || ''} - ${zodiacLabel(result.zodiac, isEn) || result.zodiac || ''}`,
        data: JSON.stringify(result),
      })
      toast.success(copy.saved)
    } catch (error) {
      logger.error('save record failed', error)
      toast.error(copy.saveFailed)
    }
  }

  return (
    <div className="page-shell pb-24" data-theme="default">
      <div className="sticky top-0 z-50 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button onClick={() => navigate(-1)} className="rounded-xl p-2 hover:bg-white/10" aria-label={copy.back}>
            <ArrowLeft size={20} className="text-[#f4ece1]" />
          </button>
          <div className="flex items-center space-x-2">
            <span className="font-serif-title text-lg font-bold text-[#f4ece1]">{copy.pageTitle}</span>
            <span className="mystic-chip normal-case tracking-normal">{copy.points} {currentPoints}</span>
          </div>
          <button className="rounded-xl p-2 hover:bg-white/10" aria-label={copy.userCenter}>
            <Users size={20} className="text-[#bdaa94]" />
          </button>
        </div>
        <div className="app-sticky-inner flex items-center justify-between gap-3 pb-3">
          <div className="scrollbar-hide flex flex-1 space-x-6 overflow-x-auto whitespace-nowrap">
            {tabs.map((tab) => (
              <button key={tab.key} onClick={() => setActiveTab(tab.key)} className={`mystic-tab ${activeTab === tab.key ? 'mystic-tab-active' : ''}`}>
                {tab.label}
              </button>
            ))}
          </div>
          {result && <button onClick={() => navigate('/records')} className="flex items-center text-sm text-[#dcb86f] hover:text-[#f0d9a5]">{copy.records}<ChevronRight size={16} /></button>}
        </div>
      </div>

      <div className="app-page-shell-narrow pb-20">
        {activeTab !== 'bazi' && <PlaceholderBlock title={copy.comingSoonTitle} description={copy.comingSoonDesc} />}

        {activeTab === 'bazi' && !result && (
          <>
            <div className="panel mt-6 p-5">
              <h3 className="mb-4 text-base font-bold text-[#f4ece1]">{copy.birthInfo}</h3>
              <div className="space-y-4">
                <div>
                  <label className="mb-2 block text-sm text-[#cdb79a]">{copy.date}</label>
                  <input type="date" value={birthDate} onChange={(e) => setBirthDate(e.target.value)} className="mystic-input" />
                </div>
                <div>
                  <label className="mb-2 block text-sm text-[#cdb79a]">{copy.time}</label>
                  <input type="time" value={birthTime} onChange={(e) => setBirthTime(e.target.value)} className="mystic-input" />
                </div>
                <div>
                  <label className="mb-2 block text-sm text-[#cdb79a]">{copy.gender}</label>
                  <div className="flex gap-3">
                    <button onClick={() => setGender('male')} className={`flex-1 rounded-[18px] border py-3 font-medium transition ${gender === 'male' ? 'border-[#d0a85b]/35 bg-[#7a3218]/20 text-[#f4ece1]' : 'border-white/10 bg-white/[0.03] text-[#bdaa94]'}`}>{copy.male}</button>
                    <button onClick={() => setGender('female')} className={`flex-1 rounded-[18px] border py-3 font-medium transition ${gender === 'female' ? 'border-[#d0a85b]/35 bg-[#7a3218]/20 text-[#f4ece1]' : 'border-white/10 bg-white/[0.03] text-[#bdaa94]'}`}>{copy.female}</button>
                  </div>
                </div>
              </div>
            </div>
            <button onClick={handleAnalyze} disabled={loading} className="btn-primary-theme mt-5 flex w-full items-center justify-center space-x-2 py-4 text-lg font-bold disabled:opacity-50">
              <Calendar size={20} className={loading ? 'animate-spin' : ''} />
              <span>{copy.analyze}</span>
            </button>
          </>
        )}

        {activeTab === 'bazi' && result && (
          <>
            <div className="panel mt-4 p-5">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-base font-bold text-[#f4ece1]">{copy.fourPillars}</h3>
                <span className="text-sm text-[#bdaa94]">{summaryText}</span>
              </div>
              <div className="mb-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
                {PILLARS.map(({ key, label }) => {
                  const pillar = result.pillars?.[key]
                  if (!pillar) return null
                  return (
                    <div key={key} className="text-center">
                      <div className="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-[#d0a85b]">{label}</div>
                      <div className="mystic-muted-box p-3">
                        <div className="mb-1 text-2xl font-bold text-[#f0d9a5]">{pillar.tianGan}</div>
                        <div className="text-2xl font-bold text-[#dcb86f]">{pillar.diZhi}</div>
                        <div className="mt-2 text-xs text-[#bdaa94]">{pillar.naYin}</div>
                      </div>
                    </div>
                  )
                })}
              </div>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <InfoCard label={copy.bazi} value={result.baZi} />
                <InfoCard label={copy.dayMaster} value={result.dayMaster} />
                <InfoCard label={copy.element} value={result.dayMasterElement} />
                <InfoCard label={copy.pattern} value={result.geJu} />
                <InfoCard label={copy.zodiac} value={zodiacLabel(result.zodiac, isEn) || result.zodiac} />
                <InfoCard label={copy.bodyStrength} value={result.bodyStrength} />
                <InfoCard label={copy.taiYuan} value={result.palaces?.taiYuan} />
                <InfoCard label={copy.mingGong} value={result.palaces?.mingGong} />
              </div>
            </div>

            {interpretations.length > 0 && (
              <div className="mt-4 space-y-3">
                {interpretations.map((item, index) => (
                  <div key={item.id || index} className="panel-soft rounded-[24px] p-4">
                    <div className="mb-2 flex items-center justify-between">
                      <h4 className="font-bold text-[#f4ece1]">{item.title || copy.interpretation}</h4>
                      {safeText(item.position) && <span className="text-xs text-[#8f7b66]">{translateGender(item.position, false) || item.position}</span>}
                    </div>
                    {safeText(item.basicDef) && <p className="mb-2 text-sm text-[#bdaa94]">{item.basicDef}</p>}
                    {safeText(item.mainContent) && <p className="text-[15px] leading-relaxed text-[#f4ece1]">{item.mainContent}</p>}
                  </div>
                ))}
              </div>
            )}

            <div className="mt-5 flex flex-wrap gap-3">
              <button onClick={handleAIReport} disabled={aiLoading} className="btn-primary-theme flex flex-1 items-center justify-center space-x-2 py-3 font-bold disabled:opacity-60">
                <Sparkles size={18} className={aiLoading ? 'animate-spin' : ''} />
                <span>{copy.aiReport}</span>
                <span className="rounded-full bg-white/20 px-2 py-0.5 text-xs">{POINTS_COST.AI_INTERPRET}</span>
              </button>
              <button onClick={handleFavorite} className="rounded-[18px] border border-[#d0a85b]/24 bg-[#7a3218]/14 p-3 text-[#dcb86f]" title={copy.favorite}><Star size={18} /></button>
              <button onClick={handleSaveRecord} className="rounded-[18px] border border-white/10 bg-white/[0.04] p-3 text-[#f4ece1]" title={copy.save}><History size={18} /></button>
            </div>
          </>
        )}

        {aiResult && (
          <div className="panel mt-5 p-5">
            <h3 className="mb-3 flex items-center text-base font-bold text-[#f4ece1]"><Sparkles className="mr-2 h-5 w-5 text-[#d0a85b]" />{copy.aiReading}</h3>
            <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/10 p-4">
              <ThinkingChain isThinking={aiLoading} finalContent={aiResult} />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
