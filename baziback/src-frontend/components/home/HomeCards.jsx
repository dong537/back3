import { Link } from 'react-router-dom'
import {
  ChevronRight,
  Sparkles,
  Star,
  Clock,
  MapPin,
  CheckCircle2,
  XCircle,
  ArrowRight,
} from 'lucide-react'
import { DISPLAY_FONT_ZH } from '../../pages/homeConfig'

export function SectionHeading({
  title,
  description,
  eyebrow = 'Section',
  titleStyle = DISPLAY_FONT_ZH,
  isEnglish = false,
}) {
  return (
    <div>
      <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
        {eyebrow}
      </div>
      <h2
        style={titleStyle}
        className={`mt-2 font-semibold text-stone-50 ${
          isEnglish ? 'text-[2rem] leading-[1.05]' : 'text-2xl'
        }`}
      >
        {title}
      </h2>
      <p className="mt-2 max-w-2xl text-sm leading-6 text-stone-400">
        {description}
      </p>
    </div>
  )
}

export function HeroActionLink({
  to,
  title,
  meta,
  primary = false,
  isEnglish = false,
}) {
  return (
    <Link
      to={to}
      className={`group inline-flex items-center gap-3 rounded-full px-5 py-3 text-sm transition-all duration-300 ${
        primary
          ? 'bg-gradient-to-r from-[#8f331f] via-[#b9552f] to-[#d6b77a] text-white shadow-[0_14px_34px_rgba(138,59,32,0.35)] hover:-translate-y-0.5'
          : 'border border-white/10 bg-white/[0.05] text-stone-100 hover:border-amber-200/20 hover:bg-white/[0.08]'
      } ${isEnglish ? 'min-w-[220px] justify-between' : ''}`}
    >
      <div className={isEnglish ? 'text-left' : ''}>
        <div className="font-medium">{title}</div>
        <div className="text-xs text-white/70">{meta}</div>
      </div>
      <ArrowRight
        size={18}
        className="transition-transform duration-300 group-hover:translate-x-1"
      />
    </Link>
  )
}

function InfoLine({ icon: Icon, label, value }) {
  return (
    <div className="flex items-center gap-3 rounded-[20px] border border-white/10 bg-black/15 px-4 py-3">
      <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-white/[0.06] text-amber-100">
        <Icon size={18} />
      </div>
      <div>
        <div className="text-xs uppercase tracking-[0.24em] text-stone-500">
          {label}
        </div>
        <div className="mt-1 text-sm font-medium text-stone-100">{value}</div>
      </div>
    </div>
  )
}

function ActionListCard({ icon: Icon, title, items, emptyLabel, tone }) {
  const toneClass =
    tone === 'good'
      ? 'border-emerald-300/15 bg-emerald-500/10 text-emerald-100'
      : 'border-orange-300/15 bg-orange-500/10 text-orange-100'

  const chipsClass =
    tone === 'good'
      ? 'border-emerald-300/15 bg-emerald-500/10 text-emerald-100'
      : 'border-orange-300/15 bg-orange-500/10 text-orange-100'

  return (
    <div className={`rounded-[22px] border p-4 ${toneClass}`}>
      <div className="flex items-center gap-2 text-sm font-medium">
        <Icon size={16} />
        <span>{title}</span>
      </div>
      <div className="mt-3 flex flex-wrap gap-2">
        {items.length > 0 ? (
          items.slice(0, 4).map((item) => (
            <span
              key={`${title}-${item}`}
              className={`rounded-full border px-3 py-1.5 text-xs ${chipsClass}`}
            >
              {item}
            </span>
          ))
        ) : (
          <span className="text-xs opacity-80">{emptyLabel}</span>
        )}
      </div>
    </div>
  )
}

export function FeatureCard({
  feature,
  entryLabel = 'Entry',
  enterLabel = 'Enter',
  titleStyle = DISPLAY_FONT_ZH,
  isEnglish = false,
}) {
  const Icon = feature.icon

  return (
    <Link
      to={feature.path}
      className={`group relative overflow-hidden rounded-[28px] border border-white/10 bg-white/[0.04] p-5 transition-all duration-300 hover:-translate-y-1 hover:border-amber-200/20 hover:bg-white/[0.06] ${feature.spanClass}`}
    >
      <div className={`pointer-events-none absolute inset-0 ${feature.accentClass}`} />
      <div className="relative flex h-full flex-col justify-between">
        <div className="flex items-start justify-between gap-3">
          <div className={`inline-flex rounded-full border px-3 py-1 text-xs ${feature.badgeClass}`}>
            {entryLabel}
          </div>
          <div
            className={`flex h-12 w-12 items-center justify-center rounded-[18px] ${feature.iconClass}`}
          >
            <Icon size={22} />
          </div>
        </div>

        <div className="mt-8">
          <h3
            style={titleStyle}
            className={`font-semibold text-stone-50 ${
              isEnglish ? 'text-[1.9rem] leading-[1.05]' : 'text-2xl'
            }`}
          >
            {feature.label}
          </h3>
          <p className="mt-3 text-sm leading-7 text-stone-300">{feature.description}</p>
        </div>

        <div className="mt-6 flex items-center justify-between gap-3">
          <span className="text-sm text-stone-300">{feature.signal}</span>
          <span className="inline-flex items-center gap-1 text-sm text-amber-100 transition-transform duration-300 group-hover:translate-x-1">
            <span>{enterLabel}</span>
            <ChevronRight size={16} />
          </span>
        </div>
      </div>
    </Link>
  )
}

export function QuickAccessCard({ item }) {
  const Icon = item.icon

  return (
    <Link
      to={item.path}
      className="group flex items-center justify-between gap-3 rounded-[22px] border border-white/10 bg-white/[0.04] px-4 py-4 transition-all duration-300 hover:border-amber-200/20 hover:bg-white/[0.06]"
    >
      <div className="flex items-center gap-3">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-[18px] ${item.iconClass}`}
        >
          <Icon size={20} />
        </div>
        <div>
          <div className="font-medium text-stone-100">{item.label}</div>
          <div className="mt-1 text-sm text-stone-400">{item.description}</div>
        </div>
      </div>

      <ChevronRight
        size={18}
        className="text-stone-500 transition-transform duration-300 group-hover:translate-x-1"
      />
    </Link>
  )
}

export function DailyFortuneDeck({
  fortuneDetail,
  copy,
  averageScore,
  scoreTone,
  aspectEntries,
  luckyElements,
  suitableActions,
  unsuitableActions,
  keywords,
}) {
  if (!fortuneDetail) {
    return (
      <div className="mt-5 rounded-[28px] border border-dashed border-white/10 bg-white/[0.03] p-6">
        <div className="text-lg font-semibold text-stone-100">
          {copy.fortuneEmptyTitle}
        </div>
        <p className="mt-3 text-sm leading-7 text-stone-400">{copy.fortuneEmptyDesc}</p>
      </div>
    )
  }

  return (
    <div className="mt-5 space-y-4">
      <div className="overflow-hidden rounded-[28px] border border-white/10 bg-[radial-gradient(circle_at_top_right,rgba(214,183,122,0.18),transparent_34%),linear-gradient(180deg,rgba(255,255,255,0.08),rgba(255,255,255,0.03))] p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
              {copy.overallFortune}
            </div>
            <div className="mt-3 flex items-end gap-3">
              <span className={`text-5xl font-semibold ${scoreTone.textClass}`}>
                {averageScore ?? '--'}
              </span>
              <span className="pb-1 text-sm text-stone-400">{copy.points}</span>
            </div>
            <div className="mt-3 inline-flex rounded-full px-3 py-1 text-xs tracking-[0.18em] uppercase">
              <span className={`rounded-full px-3 py-1 ${scoreTone.badgeClass}`}>
                {averageScore === null ? copy.awaitingData : scoreTone.label}
              </span>
            </div>
          </div>

          <div className="rounded-full border border-amber-200/15 bg-white/[0.05] px-3 py-1 text-xs tracking-[0.18em] text-amber-100">
            {fortuneDetail.date || copy.today}
          </div>
        </div>

        {fortuneDetail.overallAdvice && (
          <p className="mt-5 text-sm leading-7 text-stone-300">
            {fortuneDetail.overallAdvice}
          </p>
        )}
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        {aspectEntries.map((aspect) => {
          const Icon = aspect.icon
          return (
            <div
              key={aspect.key}
              className={`rounded-[24px] border ${aspect.borderClass} bg-white/[0.04] p-4`}
            >
              <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-2xl ${aspect.iconWrapClass}`}
                  >
                    <Icon size={18} />
                  </div>
                  <div>
                    <div className="font-medium text-stone-100">{aspect.label}</div>
                    <div className="text-xs text-stone-400">
                      {aspect.score > 0
                        ? `${aspect.score}${copy.points}`
                        : copy.awaitingData}
                    </div>
                  </div>
                </div>
                <div className="h-1.5 w-20 overflow-hidden rounded-full bg-white/10">
                  <div
                    className={`h-full rounded-full bg-gradient-to-r ${aspect.barClass}`}
                    style={{ width: `${Math.max(aspect.score, 12)}%` }}
                  />
                </div>
              </div>

              {aspect.analysis && (
                <p className="mt-3 text-sm leading-6 text-stone-400">
                  {aspect.analysis}
                </p>
              )}
            </div>
          )
        })}
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
        <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
          {copy.luckyElements}
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <InfoLine
            icon={Sparkles}
            label={copy.color}
            value={luckyElements.color || copy.awaitingData}
          />
          <InfoLine
            icon={Star}
            label={copy.number}
            value={luckyElements.number || copy.awaitingData}
          />
          <InfoLine
            icon={MapPin}
            label={copy.direction}
            value={luckyElements.direction || copy.awaitingData}
          />
          <InfoLine
            icon={Clock}
            label={copy.time}
            value={luckyElements.time || copy.awaitingData}
          />
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <ActionListCard
          icon={CheckCircle2}
          title={copy.doToday}
          items={suitableActions}
          emptyLabel={copy.awaitingData}
          tone="good"
        />
        <ActionListCard
          icon={XCircle}
          title={copy.avoidToday}
          items={unsuitableActions}
          emptyLabel={copy.awaitingData}
          tone="warn"
        />
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/[0.04] p-4">
        <div className="text-[11px] uppercase tracking-[0.32em] text-amber-200/70">
          {copy.keywordTitle}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {keywords.map((keyword) => (
            <span
              key={keyword}
              className="rounded-full border border-amber-200/15 bg-amber-500/10 px-3 py-1.5 text-xs text-amber-100"
            >
              #{keyword}
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}
