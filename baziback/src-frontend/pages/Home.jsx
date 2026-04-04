import { useState, useEffect, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  User,
  ChevronRight,
  Gift,
  Star,
  Sparkles,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { toast } from '../components/Toast'
import CheckinProgress from '../components/CheckinProgress'
import { checkin } from '../utils/referral'
import { historyStorage } from '../utils/storage'
import PangleCarouselAd from '../components/PangleCarouselAd'
import { pangleConfig } from '../config/pangle'
import useAppLocale from '../hooks/useAppLocale'
import LanguageToggleButton from '../components/LanguageToggleButton'
import { safeArray, safeNumber } from '../utils/displayText'
import {
  DISPLAY_FONT_EN,
  DISPLAY_FONT_ZH,
  HOME_COPY,
  PANEL_CLASS,
  ASPECT_META,
  buildFeatureCards,
  buildQuickAccessCards,
} from './homeConfig'
import {
  formatDisplayDate,
  getScoreLevel,
  normalizeFortuneDetailData,
  normalizeLocalizedText,
  pickLocalizedCandidate,
  readDailyFortuneCache,
  writeDailyFortuneCache,
} from './homeFortuneUtils'
import {
  DailyFortuneDeck,
  FeatureCard,
  HeroActionLink,
  QuickAccessCard,
  SectionHeading,
} from '../components/home/HomeCards'

export default function Home() {
  const navigate = useNavigate()
  const { locale } = useAppLocale()
  const copy = HOME_COPY[locale] || HOME_COPY['zh-CN']
  const isEnglish = locale === 'en-US'
  const displayFont = isEnglish ? DISPLAY_FONT_EN : DISPLAY_FONT_ZH
  const heroTitleClass = isEnglish
    ? 'max-w-5xl text-4xl font-semibold leading-[0.94] tracking-[-0.02em] text-stone-50 sm:text-5xl lg:text-[4.75rem]'
    : 'max-w-4xl text-4xl font-semibold leading-tight text-stone-50 sm:text-5xl lg:text-6xl'
  const heroDescriptionClass = isEnglish
    ? 'mt-4 max-w-3xl text-base leading-8 text-stone-300'
    : 'mt-4 max-w-2xl text-sm leading-7 text-stone-300 sm:text-base'
  const { isLoggedIn, user, credits, refreshCredits } = useAuth()

  const [showCheckinProgress, setShowCheckinProgress] = useState(false)
  const [canCheckin, setCanCheckin] = useState(checkin.canCheckin())
  const [fortuneSource, setFortuneSource] = useState(null)
  const [loading, setLoading] = useState(true)
  const [recentHistory, setRecentHistory] = useState([])
  const hasFortuneSource = Boolean(fortuneSource)

  useEffect(() => {
    setCanCheckin(checkin.canCheckin())
    setRecentHistory(historyStorage.getAll().slice(0, 5))
  }, [isLoggedIn])

  useEffect(() => {
    let cancelled = false

    const loadFortuneDetail = async () => {
      const cachedData = readDailyFortuneCache(user?.id, locale)
      if (cachedData) {
        if (!cancelled) {
          setFortuneSource(cachedData)
          setLoading(false)
        }
        return
      }

      try {
        if (!hasFortuneSource && !cancelled) {
          setLoading(true)
        }

        const token = sessionStorage.getItem('token')
        const headers = {
          'Content-Type': 'application/json',
          'X-Language': locale,
          'Accept-Language': locale,
        }

        if (token) {
          headers.Authorization = `Bearer ${token}`
        }

        const response = await fetch('/api/daily-fortune-detail/today', {
          method: 'GET',
          headers,
        })

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }

        const result = await response.json()
        if (result.success && result.data) {
          if (!cancelled) {
            setFortuneSource(result.data)
          }
          writeDailyFortuneCache(user?.id, locale, result.data)
        } else {
          console.warn('获取首页运势详情失败:', result.message || '未知错误')
          if (!cancelled && !hasFortuneSource) {
            setFortuneSource(null)
          }
        }
      } catch (error) {
        console.error('加载首页运势详情失败:', error)
        if (!cancelled && !hasFortuneSource) {
          setFortuneSource(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadFortuneDetail()

    return () => {
      cancelled = true
    }
  }, [hasFortuneSource, locale, user?.id])

  const fortuneDetail = useMemo(
    () => normalizeFortuneDetailData(fortuneSource, locale),
    [fortuneSource, locale]
  )

  const aspectEntries = useMemo(
    () =>
      Object.entries(ASPECT_META).map(([key, meta]) => {
        const aspect = fortuneDetail?.aspects?.[key] || {}
        return {
          key,
          label: copy.aspects[key],
          icon: meta.icon,
          score: safeNumber(aspect.score || 0, 0),
          analysis: normalizeLocalizedText(
            pickLocalizedCandidate(aspect, locale, 'analysis'),
            locale,
            ''
          ),
          barClass: meta.barClass,
          iconWrapClass: meta.iconWrapClass,
          borderClass: meta.borderClass,
        }
      }),
    [copy, fortuneDetail, locale]
  )

  const filledScores = useMemo(
    () => aspectEntries.map((item) => item.score).filter((score) => score > 0),
    [aspectEntries]
  )

  const averageScore = useMemo(() => {
    if (filledScores.length === 0) return null
    return Math.round(
      filledScores.reduce((current, next) => current + next, 0) /
        filledScores.length
    )
  }, [filledScores])

  const scoreTone = useMemo(
    () => getScoreLevel(averageScore ?? 0, copy),
    [averageScore, copy]
  )

  const topAspects = useMemo(() => {
    const scored = aspectEntries.filter((item) => item.score > 0)
    if (scored.length === 0) return aspectEntries.slice(0, 3)
    return [...scored].sort((left, right) => right.score - left.score).slice(0, 3)
  }, [aspectEntries])

  const luckyElements = fortuneDetail?.luckyElements || {}
  const suitableActions = safeArray(fortuneDetail?.suitableActions)
  const unsuitableActions = safeArray(fortuneDetail?.unsuitableActions)
  const keywords =
    safeArray(fortuneDetail?.keywords).length > 0
      ? safeArray(fortuneDetail?.keywords).slice(0, 4)
      : copy.defaultKeywords

  const heroMetrics = useMemo(
    () => [
      {
        label: copy.scoreMetric,
        value: averageScore === null ? '--' : averageScore,
        note: averageScore === null ? copy.awaitingData : scoreTone.label,
      },
      {
        label: copy.timeMetric,
        value: luckyElements.time || '--',
        note: copy.time,
      },
      {
        label: copy.directionMetric,
        value: luckyElements.direction || '--',
        note: copy.direction,
      },
      {
        label: copy.focusMetric,
        value: topAspects[0]?.label || '--',
        note:
          topAspects[0]?.score > 0
            ? `${topAspects[0].score}${copy.points}`
            : copy.awaitingData,
      },
    ],
    [
      averageScore,
      copy,
      luckyElements.direction,
      luckyElements.time,
      scoreTone.label,
      topAspects,
    ]
  )

  const featureCards = useMemo(
    () => buildFeatureCards(copy),
    [copy]
  )

  const quickAccessCards = useMemo(
    () => buildQuickAccessCards(copy),
    [copy]
  )

  const handleCheckin = () => {
    if (!isLoggedIn) {
      toast.warning(copy.loginFirst)
      navigate('/login')
      return
    }

    setShowCheckinProgress(true)
  }

  const onCheckinClose = () => {
    setShowCheckinProgress(false)
    refreshCredits({ force: true })
    setCanCheckin(checkin.canCheckin())
  }

  return (
    <div
      key={locale}
      lang={locale}
      translate="no"
      className="notranslate relative min-h-screen overflow-hidden bg-[#090706] text-stone-100"
    >
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(149,42,25,0.25),transparent_28%),radial-gradient(circle_at_top_right,rgba(214,183,122,0.15),transparent_30%),radial-gradient(circle_at_bottom_left,rgba(43,102,89,0.16),transparent_28%)]" />
        <div
          className="absolute inset-0 opacity-40"
          style={{
            backgroundImage:
              'linear-gradient(rgba(214,183,122,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(214,183,122,0.06) 1px, transparent 1px)',
            backgroundSize: '72px 72px',
            maskImage:
              'radial-gradient(circle at center, black 45%, transparent 85%)',
          }}
        />
      </div>

      <div className="relative mx-auto max-w-7xl px-4 pb-24 pt-4 sm:px-6 lg:px-8">
        <section className={`${PANEL_CLASS} mb-4 p-4 sm:p-6 lg:p-8`}>
          <div className="pointer-events-none absolute -left-12 top-10 h-28 w-28 rounded-full border border-amber-200/10" />
          <div className="pointer-events-none absolute right-8 top-6 h-24 w-24 rounded-full border border-amber-200/10 [animation:spin_18s_linear_infinite]" />
          <div className="pointer-events-none absolute bottom-0 right-0 h-40 w-40 translate-x-1/3 translate-y-1/4 rounded-full bg-amber-200/10 blur-3xl" />

          <div className="mb-8 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
            <Link
              to={isLoggedIn ? '/dashboard' : '/login'}
              className="group flex items-center gap-4 rounded-[24px] border border-white/10 bg-white/[0.04] px-4 py-4 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.06]"
            >
              <div className="flex h-14 w-14 items-center justify-center rounded-[20px] bg-gradient-to-br from-[#7f2416] via-[#b84e2b] to-[#d6b77a] shadow-[0_12px_30px_rgba(130,54,30,0.35)] transition-transform duration-300 group-hover:scale-105">
                <User size={24} className="text-white" />
              </div>

              <div className="min-w-0">
                <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
                  {copy.heroEyebrow}
                </div>
                <div className="mt-1 text-lg font-semibold text-stone-100">
                  {isLoggedIn
                    ? `${copy.greetingBack}${user?.username ? ` · ${user.username}` : ''}`
                    : copy.greetingLogin}
                </div>
              </div>
            </Link>

            <div className="flex flex-wrap items-center gap-2">
              <div className="inline-flex items-center gap-2 rounded-full border border-amber-200/15 bg-amber-500/10 px-4 py-2 text-sm text-amber-100">
                <Star size={16} />
                <span>
                  {copy.creditsLabel}: {credits ?? 0}
                </span>
              </div>

              <LanguageToggleButton className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-4 py-2 text-sm text-stone-200 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.07]">
                {copy.languageLabel}
              </LanguageToggleButton>

              <button
                type="button"
                onClick={handleCheckin}
                className={`inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition-all duration-300 ${
                  canCheckin
                    ? 'bg-gradient-to-r from-[#8d301d] via-[#ba512d] to-[#d2a866] text-white shadow-[0_12px_30px_rgba(133,54,30,0.35)] hover:-translate-y-0.5'
                    : 'border border-white/10 bg-white/[0.05] text-stone-400'
                }`}
              >
                <Gift size={16} />
                <span>{canCheckin ? copy.checkin : copy.checkedIn}</span>
              </button>
            </div>
          </div>

          <div className="grid gap-4 xl:grid-cols-[1.18fr_0.82fr]">
            <div className="space-y-6">
              <div className="inline-flex items-center gap-2 rounded-full border border-amber-200/15 bg-white/[0.04] px-4 py-2 text-[11px] uppercase tracking-[0.28em] text-amber-200/80">
                <span className="h-2 w-2 rounded-full bg-amber-200" />
                {copy.layoutBadge}
              </div>

              <div>
                <h1
                  style={displayFont}
                  className={heroTitleClass}
                >
                  {copy.heroTitle}
                </h1>
                <p className={heroDescriptionClass}>
                  {copy.heroDescription}
                </p>
              </div>

              <div className="flex flex-wrap gap-3">
                <HeroActionLink
                  to="/bazi"
                  title={copy.heroPrimary}
                  meta={copy.heroPrimaryMeta}
                  primary
                  isEnglish={isEnglish}
                />
                <HeroActionLink
                  to="/yijing"
                  title={copy.heroSecondary}
                  meta={copy.heroSecondaryMeta}
                  isEnglish={isEnglish}
                />
              </div>

              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                {heroMetrics.map((metric) => (
                  <div
                    key={metric.label}
                    className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4"
                  >
                    <div className="text-[11px] uppercase tracking-[0.28em] text-stone-400">
                      {metric.label}
                    </div>
                    <div className="mt-3 text-2xl font-semibold text-stone-50">
                      {metric.value}
                    </div>
                    <div className="mt-1 text-sm text-stone-400">{metric.note}</div>
                  </div>
                ))}
              </div>

              <div className="flex flex-wrap gap-2">
                {keywords.map((keyword) => (
                  <span
                    key={keyword}
                    className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1.5 text-xs tracking-[0.16em] text-amber-100"
                  >
                    #{keyword}
                  </span>
                ))}
              </div>
            </div>

            <div>
              <div className="rounded-[28px] border border-white/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.08),rgba(255,255,255,0.02))] p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
                      {copy.overallFortune}
                    </div>
                    <div className="mt-4 flex items-end gap-3">
                      <span
                        className={`text-5xl font-semibold ${averageScore === null ? 'text-stone-300' : scoreTone.textClass}`}
                      >
                        {averageScore === null ? '--' : averageScore}
                      </span>
                      <span className="pb-1 text-sm text-stone-400">
                        {copy.points}
                      </span>
                    </div>
                    <div className="mt-3 inline-flex rounded-full px-3 py-1 text-xs tracking-[0.18em] uppercase">
                      <span className={`rounded-full px-3 py-1 ${scoreTone.badgeClass}`}>
                        {averageScore === null ? copy.awaitingData : scoreTone.label}
                      </span>
                    </div>
                  </div>

                  <div className="relative flex h-24 w-24 items-center justify-center rounded-full border border-amber-200/15 bg-white/[0.04]">
                    <div className="absolute inset-2 rounded-full border border-amber-200/10 [animation:spin_16s_linear_infinite]" />
                    <Sparkles size={28} className="text-amber-100" />
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  {topAspects.map((aspect) => {
                    const Icon = aspect.icon
                    return (
                      <div
                        key={aspect.key}
                        className={`rounded-[20px] border ${aspect.borderClass} bg-black/15 p-3`}
                      >
                        <div className="flex items-center justify-between gap-3">
                          <div className="flex items-center gap-3">
                            <div
                              className={`flex h-10 w-10 items-center justify-center rounded-2xl ${aspect.iconWrapClass}`}
                            >
                              <Icon size={18} />
                            </div>
                            <div>
                              <div className="font-medium text-stone-100">
                                {aspect.label}
                              </div>
                              <div className="text-xs text-stone-400">
                                {aspect.score > 0
                                  ? `${aspect.score}${copy.points}`
                                  : copy.awaitingData}
                              </div>
                            </div>
                          </div>
                          <div className="h-1.5 w-24 overflow-hidden rounded-full bg-white/10">
                            <div
                              className={`h-full rounded-full bg-gradient-to-r ${aspect.barClass}`}
                              style={{ width: `${Math.max(aspect.score, 12)}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className={`${PANEL_CLASS} mb-4 p-5 sm:p-6`}>
          <SectionHeading
            title={copy.quickAccess}
            description={copy.quickAccessMeta}
            eyebrow={copy.sectionEyebrow}
            titleStyle={displayFont}
            isEnglish={isEnglish}
          />

          <div className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            {quickAccessCards.map((item) => (
              <QuickAccessCard key={item.path} item={item} />
            ))}
          </div>
        </section>

        <div className={`${PANEL_CLASS} mb-4 overflow-hidden p-2`}>
          <PangleCarouselAd
            slotId={pangleConfig.carouselSlotId}
            className="w-full"
            autoPlay={true}
            interval={3000}
            showIndicators={true}
            showArrows={true}
            onAdLoad={(ads) => {
              console.log('首页轮播广告加载成功:', ads)
            }}
            onAdError={(error) => {
              console.error('首页轮播广告加载失败:', error)
            }}
          />
        </div>

        <div className="grid gap-4 xl:grid-cols-[1.14fr_0.86fr]">
          <div className="space-y-4">
            <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
              <SectionHeading
                title={copy.featureSectionTitle}
                description={copy.featureSectionMeta}
                eyebrow={copy.sectionEyebrow}
                titleStyle={displayFont}
                isEnglish={isEnglish}
              />

              <div className="mt-5 grid auto-rows-[minmax(190px,auto)] gap-4 lg:grid-cols-4">
                {featureCards.map((feature) => (
                  <FeatureCard
                    key={feature.path}
                    feature={feature}
                    entryLabel={copy.entryLabel}
                    enterLabel={copy.enterLabel}
                    titleStyle={displayFont}
                    isEnglish={isEnglish}
                  />
                ))}
              </div>
            </section>

            <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
              <div className="flex items-start justify-between gap-4">
                <SectionHeading
                  title={copy.recentRecords}
                  description={copy.recentRecordsMeta}
                  eyebrow={copy.sectionEyebrow}
                  titleStyle={displayFont}
                  isEnglish={isEnglish}
                />
                <Link
                  to="/records"
                  className="inline-flex items-center gap-1 text-sm text-amber-100 transition-colors hover:text-amber-50"
                >
                  {copy.viewMore}
                  <ChevronRight size={16} />
                </Link>
              </div>

              <div className="mt-5 space-y-3">
                {recentHistory.length > 0 ? (
                  recentHistory.slice(0, 4).map((item) => (
                    <div
                      key={item.id}
                      className="rounded-[22px] border border-white/10 bg-white/[0.04] p-4 transition-all duration-300 hover:bg-white/[0.06]"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <div className="text-sm font-medium text-stone-100">
                            {item.question || item.title || copy.untitled}
                          </div>
                          <div className="mt-2 text-xs text-stone-400">
                            {formatDisplayDate(item.timestamp, locale)}
                          </div>
                        </div>
                        <span className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1 text-xs text-amber-100">
                          {copy.recordTypes[item.type] || copy.recordTypes.other}
                        </span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="rounded-[22px] border border-dashed border-white/10 bg-white/[0.03] px-4 py-10 text-center text-sm leading-7 text-stone-400">
                    {copy.noRecords}
                  </div>
                )}
              </div>
            </section>
          </div>

          <section className={`${PANEL_CLASS} p-5 sm:p-6`}>
            <SectionHeading
              title={copy.fortuneTitle}
              description={copy.fortuneMeta}
              eyebrow={copy.sectionEyebrow}
              titleStyle={displayFont}
              isEnglish={isEnglish}
            />

            {loading ? (
              <div className="mt-5 space-y-4">
                <div className="h-36 animate-pulse rounded-[28px] bg-white/[0.06]" />
                <div className="grid gap-3 sm:grid-cols-2">
                  {[1, 2, 3, 4].map((value) => (
                    <div
                      key={value}
                      className="h-28 animate-pulse rounded-[24px] bg-white/[0.05]"
                    />
                  ))}
                </div>
              </div>
            ) : (
              <DailyFortuneDeck
                fortuneDetail={fortuneDetail}
                copy={copy}
                averageScore={averageScore}
                scoreTone={scoreTone}
                aspectEntries={aspectEntries}
                luckyElements={luckyElements}
                suitableActions={suitableActions}
                unsuitableActions={unsuitableActions}
                keywords={keywords}
              />
            )}
          </section>
        </div>
      </div>

      <CheckinProgress isOpen={showCheckinProgress} onClose={onCheckinClose} />
    </div>
  )
}
