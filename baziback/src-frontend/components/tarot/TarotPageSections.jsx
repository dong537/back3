import {
  ArrowLeft,
  ChevronRight,
  Coins,
  Search,
} from 'lucide-react'
import { getBadgeClass, formatCredits } from '../../pages/tarotPageUtils'
import { safeNumber, safeText } from '../../utils/displayText'

export function TarotTopBar({
  copy,
  activeTab,
  onTabChange,
  pointsDisplay,
  onBack,
}) {
  return (
    <div className="sticky top-0 z-40 -mx-4 mb-4 border-b border-white/10 bg-[#0f0a09]/80 backdrop-blur-xl">
      <div className="app-sticky-inner flex items-center justify-between gap-3 py-3">
        <button
          onClick={onBack}
          className="rounded-xl p-2 transition-all hover:bg-white/10"
          title={copy.back}
          aria-label={copy.back}
        >
          <ArrowLeft size={20} className="text-[#f4ece1]" />
        </button>
        <div className="scrollbar-hide mx-2 flex flex-1 items-center justify-center space-x-5 overflow-x-auto whitespace-nowrap">
          {copy.tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => onTabChange(tab.key)}
              className={`mystic-tab ${activeTab === tab.key ? 'mystic-tab-active' : ''}`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <div className="flex items-center space-x-2">
          <button
            className="rounded-xl p-2 transition-all hover:bg-white/10"
            title={copy.search}
            aria-label={copy.search}
          >
            <Search size={20} className="text-[#bdaa94]" />
          </button>
          <div className="flex items-center space-x-1 rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-3 py-1.5">
            <Coins size={14} className="text-[#d0a85b]" />
            <span className="text-sm font-bold text-[#dcb86f]">{pointsDisplay}</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export function TarotHeroCarousel({
  banners,
  bannerIndex,
  dailyDrawLoading,
  hasDrawnToday,
  copy,
  onBannerClick,
  onBannerSelect,
}) {
  return (
    <div className="page-hero mb-5">
      <div className="page-hero-inner !max-w-none !px-5 !py-5 sm:!p-6">
        <div
          className="flex transition-transform duration-500 ease-out"
          style={{ transform: `translateX(-${bannerIndex * 100}%)` }}
        >
          {banners.map((banner) => (
            <div
              key={banner.id}
              className={`flex min-h-[140px] w-full flex-shrink-0 cursor-pointer flex-col items-start justify-between gap-4 bg-gradient-to-r p-5 sm:min-h-[140px] sm:flex-row sm:items-center sm:p-6 ${banner.gradient}`}
              onClick={onBannerClick}
            >
              <div>
                <h3 className="mb-1 text-xl font-bold text-white">
                  {banner.title}
                </h3>
                <p className="text-sm text-white/80">{banner.subtitle}</p>
                <button
                  disabled={dailyDrawLoading}
                  className="mt-3 rounded-full bg-white/20 px-4 py-1.5 text-sm font-medium text-white backdrop-blur transition-all hover:bg-white/30 disabled:opacity-70"
                >
                  {hasDrawnToday ? copy.todayButton : copy.drawNow}
                </button>
              </div>
              <span className="text-6xl opacity-80">{banner.icon}</span>
            </div>
          ))}
        </div>
        <div className="absolute bottom-3 left-1/2 flex -translate-x-1/2 space-x-1.5">
          {banners.map((banner, idx) => (
            <button
              key={banner.id}
              onClick={() => onBannerSelect(idx)}
              className={`h-2 w-2 rounded-full transition-all ${idx === bannerIndex ? 'w-4 bg-white' : 'bg-white/50'}`}
            />
          ))}
        </div>
      </div>
    </div>
  )
}

export function TarotQuickConsultGrid({ items, onConsult }) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
      {items.map((item) => (
        <button
          key={item.title}
          onClick={() => onConsult(item)}
          className="group rounded-[24px] border border-white/10 bg-white/[0.04] p-4 text-left transition-all hover:border-[#d0a85b]/24 hover:bg-white/[0.08]"
        >
          <span className="mb-2 block text-3xl transition-transform group-hover:scale-110">
            {item.icon}
          </span>
          <h4 className="mb-0.5 text-sm font-bold text-[#f4ece1]">
            {item.title}
          </h4>
          <p className="text-xs leading-tight text-[#bdaa94]">{item.desc}</p>
        </button>
      ))}
    </div>
  )
}

export function TarotSpreadSection({
  title,
  badge,
  badgeWithCoins = false,
  accentClass,
  items,
  isAdvanced = false,
  copy,
  onSpreadClick,
}) {
  return (
    <div className="mb-6">
      <div className="mb-3 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div className={`h-5 w-1 rounded-full ${accentClass}`}></div>
          <h3 className="font-bold text-[#f4ece1]">{title}</h3>
          {badgeWithCoins ? (
            <span className="flex items-center rounded-full border border-[#d0a85b]/25 bg-[#7a3218]/16 px-2 py-0.5 text-xs text-[#f0d9a5]">
              <Coins size={10} className="mr-1" />
              {badge}
            </span>
          ) : (
            <span className="mystic-chip normal-case tracking-normal">{badge}</span>
          )}
        </div>
        <button className="flex items-center text-sm text-[#bdaa94] transition-colors hover:text-[#dcb86f]">
          {copy.more} <ChevronRight size={16} />
        </button>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        {items.map((spread) => (
          <button
            key={spread.title}
            onClick={() => onSpreadClick(spread, isAdvanced)}
            className={`group relative overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.04] p-3 text-left transition-all ${!spread.code ? 'opacity-60' : 'hover:border-[#d0a85b]/24 hover:bg-white/[0.08]'}`}
          >
            {safeText(spread.badge) && (
              <span
                className={`absolute right-2 top-2 rounded-md px-1.5 py-0.5 text-xs font-medium ${getBadgeClass(spread.badgeTone)}`}
              >
                {spread.badge}
              </span>
            )}
            <span className="mb-2 block text-2xl transition-transform group-hover:scale-110">
              {spread.icon}
            </span>
            <h4 className="text-sm font-bold text-[#f4ece1]">{spread.title}</h4>
            <p className="mt-0.5 text-xs text-[#bdaa94]">{spread.desc}</p>
            {isAdvanced && safeNumber(spread.cost, 0) > 0 && (
              <div className="mt-1 flex items-center text-xs text-[#dcb86f]">
                <Coins size={10} className="mr-1" />
                <span>{formatCredits(spread.cost, copy)}</span>
              </div>
            )}
          </button>
        ))}
      </div>
    </div>
  )
}

export function TarotPointsPanel({ copy, pointsDisplay, onGoDashboard }) {
  return (
    <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#7a3218]/12 p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Coins size={18} className="text-[#d0a85b]" />
          <span className="text-sm text-[#f0d9a5]">
            {copy.currentPoints}
            {pointsDisplay}
          </span>
        </div>
        <button
          onClick={onGoDashboard}
          className="text-sm font-medium text-[#dcb86f] transition-colors hover:text-[#f0d9a5]"
        >
          {copy.getMorePoints}
        </button>
      </div>
    </div>
  )
}
