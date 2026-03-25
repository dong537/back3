import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  Activity,
  ArrowRight,
  BookOpen,
  Briefcase,
  Heart,
  Shield,
  Star,
  Users,
  Wallet,
} from 'lucide-react'
import Card from './Card'
import { baziApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from './Toast'
import { resolvePageLocale, safeText } from '../utils/displayText'

const POSITION_ALIASES = {
  day_branch: ['日支'],
  year_stem: ['年干'],
  year_branch: ['年支'],
  month_stem: ['月干'],
  month_branch: ['月支'],
  hour_stem: ['时干'],
  hour_branch: ['时支'],
}

const POSITION_CONFIGS = {
  day_branch: {
    icon: Wallet,
    toneClass: 'border-[#d0a85b]/24 bg-[#6a4a1e]/14',
    iconClass: 'border-[#d0a85b]/24 bg-[#6a4a1e]/18 text-[#f0d9a5]',
    titleClass: 'text-[#f0d9a5]',
  },
  year_stem: {
    icon: Users,
    toneClass: 'border-[#b88a3d]/22 bg-[#5e431d]/12',
    iconClass: 'border-[#b88a3d]/24 bg-[#5e431d]/16 text-[#dcb86f]',
    titleClass: 'text-[#dcb86f]',
  },
  year_branch: {
    icon: Shield,
    toneClass: 'border-[#8f5c1f]/22 bg-[#4d3416]/12',
    iconClass: 'border-[#8f5c1f]/24 bg-[#4d3416]/16 text-[#dcb86f]',
    titleClass: 'text-[#dcb86f]',
  },
  month_stem: {
    icon: Briefcase,
    toneClass: 'border-[#a34224]/22 bg-[#7a3218]/12',
    iconClass: 'border-[#a34224]/24 bg-[#7a3218]/16 text-[#e19a84]',
    titleClass: 'text-[#e19a84]',
  },
  month_branch: {
    icon: Activity,
    toneClass: 'border-[#b5532d]/22 bg-[#6a2a17]/12',
    iconClass: 'border-[#b5532d]/24 bg-[#6a2a17]/16 text-[#e19a84]',
    titleClass: 'text-[#e19a84]',
  },
  hour_stem: {
    icon: Star,
    toneClass: 'border-[#8f6b4c]/22 bg-[#3f2b17]/12',
    iconClass: 'border-[#8f6b4c]/24 bg-[#3f2b17]/16 text-[#bdaa94]',
    titleClass: 'text-[#d9c1aa]',
  },
  hour_branch: {
    icon: Heart,
    toneClass: 'border-[#9a4e34]/22 bg-[#5a2318]/12',
    iconClass: 'border-[#9a4e34]/24 bg-[#5a2318]/16 text-[#e4b3a1]',
    titleClass: 'text-[#e4b3a1]',
  },
}

const POSITION_LABELS = {
  day_branch: { zh: '日支', en: 'Day Branch' },
  year_stem: { zh: '年干', en: 'Year Stem' },
  year_branch: { zh: '年支', en: 'Year Branch' },
  month_stem: { zh: '月干', en: 'Month Stem' },
  month_branch: { zh: '月支', en: 'Month Branch' },
  hour_stem: { zh: '时干', en: 'Hour Stem' },
  hour_branch: { zh: '时支', en: 'Hour Branch' },
}

const SHI_SHEN_ALIASES = {
  indirect_wealth: ['偏财'],
  direct_wealth: ['正财'],
  direct_officer: ['正官'],
  seven_killings: ['七杀'],
  direct_resource: ['正印'],
  indirect_resource: ['偏印'],
  peer: ['比肩'],
  rob_wealth: ['劫财'],
  eating_god: ['食神'],
  hurting_officer: ['伤官'],
}

const SHI_SHEN_LABELS = {
  indirect_wealth: { zh: '偏财', en: 'Indirect Wealth' },
  direct_wealth: { zh: '正财', en: 'Direct Wealth' },
  direct_officer: { zh: '正官', en: 'Direct Officer' },
  seven_killings: { zh: '七杀', en: 'Seven Killings' },
  direct_resource: { zh: '正印', en: 'Direct Resource' },
  indirect_resource: { zh: '偏印', en: 'Indirect Resource' },
  peer: { zh: '比肩', en: 'Peer' },
  rob_wealth: { zh: '劫财', en: 'Rob Wealth' },
  eating_god: { zh: '食神', en: 'Eating God' },
  hurting_officer: { zh: '伤官', en: 'Hurting Officer' },
}

function normalizeAlias(value, aliasMap) {
  if (!value) return ''
  return (
    Object.entries(aliasMap).find(([, aliases]) => aliases.includes(value))?.[0] ||
    value
  )
}

function translateMappedValue(value, map, isEn) {
  if (!value) return ''
  const label = map[value]
  if (!label) return value
  return isEn ? label.en : label.zh
}

function getIconAndColor(position) {
  return (
    POSITION_CONFIGS[position] || {
      icon: BookOpen,
      toneClass: 'border-white/10 bg-white/[0.03]',
      iconClass: 'border-white/10 bg-white/[0.05] text-[#bdaa94]',
      titleClass: 'text-[#f0d9a5]',
    }
  )
}

export default function BaziInterpretationList({ baziData }) {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const [interpretations, setInterpretations] = useState([])
  const [loading, setLoading] = useState(false)
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const text = (zh, en) => (isEn ? en : zh)

  useEffect(() => {
    if (!baziData) {
      setInterpretations([])
      return
    }

    const loadInterpretations = async () => {
      try {
        setLoading(true)
        const response = await baziApi.getInterpretationsFromBaziData(baziData)

        if (response.data?.success && response.data.data) {
          const items = response.data.data.map((item) => {
            const normalizedPosition = normalizeAlias(item.type, POSITION_ALIASES)
            const normalizedShiShen = normalizeAlias(
              item.shiShen,
              SHI_SHEN_ALIASES
            )
            const iconConfig = getIconAndColor(normalizedPosition)
            const shortDesc =
              item.basicDef ||
              text(
                `${item.type}出现${item.shiShen}，会对命盘形成一定影响。`,
                `${translateMappedValue(normalizedPosition, POSITION_LABELS, true)} shows ${translateMappedValue(normalizedShiShen, SHI_SHEN_LABELS, true)}, which may influence the chart.`
              )

            return {
              id: item.id,
              type: item.type,
              position: item.type,
              shiShen: item.shiShen,
              title: item.title,
              icon: iconConfig.icon,
              toneClass: iconConfig.toneClass,
              iconClass: iconConfig.iconClass,
              titleClass: iconConfig.titleClass,
              shortDesc,
              basicDef: item.basicDef,
              mainContent: item.mainContent,
              supportContent: item.supportContent,
              restrictContent: item.restrictContent,
              genderDiff: item.genderDiff,
              tags: Array.isArray(item.tags)
                ? item.tags
                : item.tag
                  ? [item.tag]
                  : [],
              scores: item.scores || {},
              advices: item.advices || {},
            }
          })

          setInterpretations(items)
        } else {
          logger.warn('Failed to load Bazi interpretations:', response.data)
        }
      } catch (error) {
        logger.error('Failed to load Bazi interpretations:', error)
        toast.error(text('加载八字解释失败', 'Failed to load Bazi interpretations'))
      } finally {
        setLoading(false)
      }
    }

    void loadInterpretations()
  }, [baziData, isEn])

  const handleItemClick = (item) => {
    navigate(`/bazi/interpretation/${item.id}`, {
      state: { interpretation: item, baziData },
    })
  }

  if (!baziData) return null

  if (loading) {
    return (
      <Card className="panel mb-6 border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.94),rgba(14,11,10,0.84))]">
        <div className="p-6">
          <div className="text-center text-[#8f7b66]">
            {text('加载中...', 'Loading...')}
          </div>
        </div>
      </Card>
    )
  }

  if (interpretations.length === 0) return null

  return (
    <Card className="panel mb-6 border-white/10 bg-[linear-gradient(180deg,rgba(22,17,16,0.94),rgba(14,11,10,0.84))]">
      <div className="p-6">
        <h3 className="mb-4 flex items-center space-x-2 text-xl font-bold text-[#f4ece1]">
          <BookOpen className="h-5 w-5 text-[#dcb86f]" />
          <span>{text('八字信息解读', 'Bazi Insights')}</span>
        </h3>
        <p className="mb-6 text-sm text-[#8f7b66]">
          {text('点击查看各项十神的详细解释', 'Tap any item to view the full interpretation')}
        </p>

        <div className="space-y-4">
          {interpretations.map((item) => {
            const normalizedPosition = normalizeAlias(item.type, POSITION_ALIASES)
            const normalizedShiShen = normalizeAlias(item.shiShen, SHI_SHEN_ALIASES)
            const Icon = item.icon
            const positionLabel = translateMappedValue(
              normalizedPosition,
              POSITION_LABELS,
              isEn
            )
            const shiShenLabel = translateMappedValue(
              normalizedShiShen,
              SHI_SHEN_LABELS,
              isEn
            )

            return (
              <div
                key={item.id}
                onClick={() => handleItemClick(item)}
                className={`group relative cursor-pointer rounded-[24px] border p-4 transition-all duration-300 hover:-translate-y-0.5 hover:shadow-[0_16px_36px_rgba(0,0,0,0.2)] tap-highlight active:scale-[0.98] ${item.toneClass}`}
              >
                <div className="flex items-start space-x-4">
                  <div
                    className={`flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-[18px] border ${item.iconClass}`}
                  >
                    <Icon className="h-6 w-6" />
                  </div>

                  <div className="min-w-0 flex-1">
                    <div className="mb-2 flex flex-wrap items-center gap-2">
                      <h4 className={`text-lg font-bold ${item.titleClass}`}>
                        {item.title}
                      </h4>
                      <span className="rounded-full border border-white/10 bg-black/10 px-2 py-0.5 text-xs text-[#d9cbbd]">
                        {positionLabel || item.type}
                      </span>
                      <span className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 text-xs text-[#e4d6c8]">
                        {shiShenLabel || item.shiShen}
                      </span>
                    </div>
                    <p className="line-clamp-2 text-sm leading-6 text-[#bdaa94]">
                      {safeText(item.shortDesc)}
                    </p>
                    <div className="mt-3 flex items-center space-x-2 text-xs text-[#8f7b66] transition-colors group-hover:text-[#dcb86f]">
                      <span>{text('查看全文', 'View details')}</span>
                      <ArrowRight className="h-4 w-4" />
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </Card>
  )
}
