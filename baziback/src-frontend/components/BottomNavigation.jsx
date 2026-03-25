import { Link, useLocation } from 'react-router-dom'
import { Home, Heart, Zap, Trophy, User } from 'lucide-react'
import useAppLocale from '../hooks/useAppLocale'

const BOTTOM_NAV_COPY = {
  'zh-CN': {
    home: '首页',
    favorites: '收藏',
    ask: '占问',
    achievement: '成就',
    self: '我的',
  },
  'en-US': {
    home: 'Home',
    favorites: 'Favorites',
    ask: 'Ask',
    achievement: 'Achievements',
    self: 'Me',
  },
}

const bottomNavItems = [
  { path: '/', icon: Home, id: 'home' },
  { path: '/favorites', icon: Heart, id: 'favorites' },
  { path: '/ai', icon: Zap, id: 'ask' },
  { path: '/achievement', icon: Trophy, id: 'achievement' },
  { path: '/self', icon: User, id: 'self' },
]

export default function BottomNavigation() {
  const location = useLocation()
  const { locale } = useAppLocale()
  const copy = BOTTOM_NAV_COPY[locale]

  const isActive = (path) => {
    if (path === '/') {
      return location.pathname === '/'
    }
    return location.pathname.startsWith(path)
  }

  return (
    <nav className="safe-area-bottom fixed bottom-0 left-0 right-0 z-[100] border-t border-white/10 bg-[#0f0a09]/92 shadow-lg md:hidden">
      <div className="mx-auto max-w-7xl">
        <div className="flex h-16 items-center justify-around px-1 md:h-14 md:px-2">
          {bottomNavItems.map(({ path, icon: Icon, id }) => {
            const active = isActive(path)
            return (
              <Link
                key={path}
                to={path}
                aria-label={copy[id]}
                className={`flex h-full flex-1 flex-col items-center justify-center transition-all duration-200 ${
                  active ? 'text-[#f0d9a5]' : 'text-[#8f7b66]'
                }`}
              >
                <div
                  className={`mb-0.5 flex h-10 w-10 items-center justify-center rounded-lg transition-all duration-200 md:mb-1 md:h-9 md:w-9 ${
                    active ? 'bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)]' : 'bg-transparent'
                  }`}
                >
                  {active && path === '/' ? (
                    <div className="h-2 w-2 rounded-full bg-white" />
                  ) : active && path === '/ai' ? (
                    <Icon size={18} className="text-white" strokeWidth={2.5} />
                  ) : (
                    <Icon size={20} className={active ? 'text-[#f0d9a5]' : 'text-[#8f7b66]'} strokeWidth={active ? 2.5 : 2} />
                  )}
                </div>
                <span
                  className={`text-xs md:text-[11px] ${
                    active ? 'font-medium text-[#f0d9a5]' : 'font-normal text-[#8f7b66]'
                  }`}
                >
                  {copy[id]}
                </span>
              </Link>
            )
          })}
        </div>
      </div>
    </nav>
  )
}
